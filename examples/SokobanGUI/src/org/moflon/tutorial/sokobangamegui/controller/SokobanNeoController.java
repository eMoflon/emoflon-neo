package org.moflon.tutorial.sokobangamegui.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.sokobangui.API_Common;
import org.emoflon.neo.api.sokobangui.org.moflon.tutorial.sokobangamegui.patterns.API_SokobanGUIPatterns;
import org.emoflon.neo.api.sokobanlanguage.metamodels.API_SokobanLanguage;
import org.emoflon.neo.api.sokobanlanguage.models.API_SokobanSimpleTestField;
import org.emoflon.neo.api.sokobanlanguage.rules.API_SokobanPatternsRulesConstraints;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.moflon.tutorial.sokobangamegui.view.Field;
import org.moflon.tutorial.sokobangamegui.view.View;

public class SokobanNeoController implements IController {
	private View view;
	private API_SokobanGUIPatterns api1;
	private API_SokobanPatternsRulesConstraints api2;
	private NeoCoreBuilder builder;

	private int width = 0;
	private int height = 0;
	private List<Field> fields;

	private static final Logger logger = Logger.getLogger(SokobanNeoController.class);

	public SokobanNeoController() {
		this(c -> new View(c), (c) -> c.defaultBoard());
	}

	public SokobanNeoController(Function<IController, View> createView, int width, int height) {
		this(createView, (c) -> c.newBoard(width, height));
	}

	public SokobanNeoController(int width, int height) {
		this(c -> new View(c), (c) -> c.newBoard(width, height));
	}

	public SokobanNeoController(Function<IController, View> createView, Consumer<SokobanNeoController> createBoard) {
		builder = API_Common.createBuilder();
		api1 = new API_SokobanGUIPatterns(builder);
		api2 = new API_SokobanPatternsRulesConstraints(builder);

		logger.info("Starting to build board...");
		long tic = System.currentTimeMillis();
		createBoard.accept(this);
		update();
		view = createView.apply(this);
		long toc = System.currentTimeMillis();
		logger.info("Took: " + (toc - tic) / 1000.0 + "s");
	}

	public static void main(String[] args) {
		Logger.getRootLogger().setLevel(Level.INFO);
		new SokobanNeoController(10, 10);
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public List<String> getFigureTypes() {
		var access = api1.getPattern_FigureTypes();
		var matches = access.pattern().determineMatches();
		var data = access.data(matches);
		return data.map(d -> d._eclass._ename).collect(Collectors.toList());
	}

	@Override
	public Optional<Field> getSelectedField() {
		var access = api1.getPattern_SelectedFigure();
		return access.pattern().determineOneMatch().flatMap(m -> {
			var data = access.data(List.of(m)).findAny().get();
			return fields.stream()//
					.filter(f -> f.getRow() == data._b_fields_1_f._row && f.getCol() == data._b_fields_1_f._col)//
					.findFirst();
		});
	}

	@Override
	public List<Field> getFields() {
		return fields;
	}

	@Override
	public boolean boardIsValid() {
		if (api1.getConstraint_ExactlyOneSokoban().isViolated()) {
			view.updateStatus("You must have exactly one Sokoban!");
			return false;
		}

		if (api1.getConstraint_OneEndField().isViolated()) {
			view.updateStatus("You must have exactly one end field!");
			return false;
		}

		if (api1.getPattern_Block().pattern().countMatches() != api1.getPattern_EndField().pattern().countMatches()) {
			view.updateStatus("You must have the same number of blocks as end fields!");
			return false;
		}

		if (api1.getPattern_EndFieldBlockedByBoulder().pattern().countMatches() != 0) {
			view.updateStatus("One of your end fields is blocked by a boulder!");
			return false;
		}

		if (api2.getPattern_BlockNotOnEndFieldInCorner().pattern().determineOneMatch().isPresent()) {
			view.updateStatus("One of your blocks is in a corner (which is not an end field)!");
			return false;
		}

		view.updateStatus("Everything seems to be ok...");
		return true;
	}

	@Override
	public void setFigure(Field field, String figureType) {
		Optional<?> result;
		switch (figureType) {
		case SOKOBAN: {
			var access = api1.getRule_CreateSokoban();
			var mask = access.mask();
			mask.setB_fields_0_fCol(field.getCol());
			mask.setB_fields_0_fRow(field.getRow());
			result = access.apply(mask, access.mask());
			break;
		}
		case BLOCK: {
			var access = api1.getRule_CreateBlock();
			var mask = access.mask();
			mask.setB_fields_0_fCol(field.getCol());
			mask.setB_fields_0_fRow(field.getRow());
			result = access.apply(mask, access.mask());
			break;
		}
		case BOULDER: {
			var access = api1.getRule_CreateBoulder();
			var mask = access.mask();
			mask.setB_fields_0_fCol(field.getCol());
			mask.setB_fields_0_fRow(field.getRow());
			result = access.apply(mask, access.mask());
			break;
		}
		default:
			var access = api1.getRule_DeleteFigure();
			var mask = access.mask();
			mask.setB_fields_0_fCol(field.getCol());
			mask.setB_fields_0_fRow(field.getRow());
			result = access.apply(mask, access.mask());
			break;
		}

		if (!result.isPresent() && !figureType.equals(NONE))
			throw new UnsupportedOperationException("Unable to create " + figureType + " on " + field);
	}

	@Override
	public void selectField(Field field) {
		Optional<NeoCoMatch> comatch = Optional.empty();

		// If the board has a selected figure, try to move figure to this field
		{
			var mask = api2.getRule_MoveSokobanDownWithCondition().mask();
			mask.setB_fields_1_toCol(field.getCol());
			mask.setB_fields_1_toRow(field.getRow());
			comatch = api2.getRule_MoveSokobanDownWithCondition().apply(mask,
					api2.getRule_MoveSokobanDownWithCondition().mask());

			if (comatch.isPresent())
				return;
		}

		{
			var mask = api2.getRule_PushBlockDown().mask();
			mask.setB_fields_1_toCol(field.getCol());
			mask.setB_fields_1_toRow(field.getRow());
			comatch = api2.getRule_PushBlockDown().apply(mask, api2.getRule_PushBlockDown().mask());

			if (comatch.isPresent())
				return;
		}

		{
			var mask = api2.getRule_MoveSokobanUpWithCondition().mask();
			mask.setB_fields_1_toCol(field.getCol());
			mask.setB_fields_1_toRow(field.getRow());
			comatch = api2.getRule_MoveSokobanUpWithCondition().apply(mask,
					api2.getRule_MoveSokobanUpWithCondition().mask());

			if (comatch.isPresent())
				return;
		}

		{
			var mask = api2.getRule_PushBlockUp().mask();
			mask.setB_fields_1_toCol(field.getCol());
			mask.setB_fields_1_toRow(field.getRow());
			comatch = api2.getRule_PushBlockUp().apply(mask, api2.getRule_PushBlockUp().mask());

			if (comatch.isPresent())
				return;
		}

		{
			var mask = api2.getRule_MoveSokobanRightWithCondition().mask();
			mask.setB_fields_1_toCol(field.getCol());
			mask.setB_fields_1_toRow(field.getRow());
			comatch = api2.getRule_MoveSokobanRightWithCondition().apply(mask,
					api2.getRule_MoveSokobanRightWithCondition().mask());

			if (comatch.isPresent())
				return;
		}

		{
			var mask = api2.getRule_PushBlockRight().mask();
			mask.setB_fields_1_toCol(field.getCol());
			mask.setB_fields_1_toRow(field.getRow());
			comatch = api2.getRule_PushBlockRight().apply(mask, api2.getRule_PushBlockRight().mask());

			if (comatch.isPresent())
				return;
		}

		{
			var mask = api2.getRule_MoveSokobanLeftWithCondition().mask();
			mask.setB_fields_1_toCol(field.getCol());
			mask.setB_fields_1_toRow(field.getRow());
			comatch = api2.getRule_MoveSokobanLeftWithCondition().apply(mask,
					api2.getRule_MoveSokobanLeftWithCondition().mask());

			if (comatch.isPresent())
				return;
		}

		{
			var mask = api2.getRule_PushBlockLeft().mask();
			mask.setB_fields_1_toCol(field.getCol());
			mask.setB_fields_1_toRow(field.getRow());
			comatch = api2.getRule_PushBlockLeft().apply(mask, api2.getRule_PushBlockLeft().mask());

			if (comatch.isPresent())
				return;
		}

		// All movement failed, so try to select the figure on the field
		api1.getRule_SelectFigure().apply(//
				api1.getRule_SelectFigure().mask()//
						.setB_fields_0_fCol(field.getCol())//
						.setB_fields_0_fRow(field.getRow()), //
				api1.getRule_SelectFigure().mask());
	}

	@Override
	public void setEndPos(Field field, boolean b) {
		if (b) {
			var mask = api1.getRule_SetEndField().mask();
			mask.setB_fields_0_fCol(field.getCol());
			mask.setB_fields_0_fRow(field.getRow());
			api1.getRule_SetEndField().apply(mask, api1.getRule_SetEndField().mask());
		} else {
			var mask = api1.getRule_SetNotEndField().mask();
			mask.setB_fields_0_fCol(field.getCol());
			mask.setB_fields_0_fRow(field.getRow());
			api1.getRule_SetNotEndField().apply(mask, api1.getRule_SetNotEndField().mask());
		}
	}

	@Override
	public void newBoard(int width, int height) {
		try {
			var language = new API_SokobanLanguage(builder);
			var metamodel = language.getMetamodel_SokobanLanguage();
			builder.clearDataBase();
			builder.exportEMSLEntityToNeo4j(metamodel);
		} catch (FlattenerException e) {
			e.printStackTrace();
		}

		var maskTopLeft = api1.getRule_CreateTopLeft().mask();
		maskTopLeft.setBWidth(width);
		maskTopLeft.setBHeight(height);

		// Top-left corner
		api1.getRule_CreateTopLeft().apply(api1.getRule_CreateTopLeft().mask(), maskTopLeft);

		// First row
		for (int col = 0; col < width - 1; col++) {
			api1.getRule_CreateFirstRow().rule().apply();
		}

		// First column
		for (int row = 0; row < height - 1; row++) {
			api1.getRule_CreateFirstCol().rule().apply();
		}

		// Apply as long as possible
		var rest = api1.getRule_CreateRestOfFields().rule();
		while (rest.apply().isPresent())
			;

		logger.info("Finished board creation.");

		extractFields();

		logger.info("Finished extracting fields.");
	}

	private void defaultBoard() {
		try {
			var exampleBoard = new API_SokobanSimpleTestField(builder);
			var board = exampleBoard.getModel_SokobanSimpleTestField();
			builder.exportEMSLEntityToNeo4j(board);
			extractFields();
		} catch (FlattenerException e) {
			e.printStackTrace();
		}
	}

	private void extractFields() {
		var accessBoard = api1.getPattern_Board();
		accessBoard.pattern().determineOneMatch().ifPresent(m -> {
			var mData = accessBoard.data(List.of(m)).findAny().get();
			this.width = mData._board._width;
			this.height = mData._board._height;

			fields = new ArrayList<Field>();

			var accessEmptyFields = api1.getPattern_EmptyFields();
			var emptyFields = accessEmptyFields.pattern().determineMatches();
			var emptyField = accessEmptyFields.data(emptyFields);
			emptyField.forEach(f -> fields.add(new Field(//
					f._board_fields_0_field._row, //
					f._board_fields_0_field._col, //
					f._field._endPos, //
					Optional.empty())));

			var accessOccupiedFields = api1.getPattern_OccupiedFields();
			var occupiedFields = accessOccupiedFields.pattern().determineMatches();
			var occupiedField = accessOccupiedFields.data(occupiedFields);
			occupiedField.forEach(d -> fields.add(new Field(//
					d._board_fields_0_field._row, //
					d._board_fields_0_field._col, //
					d._field._endPos, //
					Optional.of(d._fig._ename)
					)));
		});
	}

	@Override
	public void clearBoard() {
		var access = api1.getRule_DeleteFigure();

		access.rule().determineMatches().forEach(m -> {
			access.rule().apply(m);
		});
	}

	@Override
	public void loadSOKFile(String filePath) {
		// Populate database from sok file
	}

	@Override
	public void saveSOKFile(String filePath) {
		// Write out board to a sok file
	}

	@Override
	public void update() {
		var accessEmptyFields = api1.getPattern_EmptyFields();
		var emptyFields = accessEmptyFields.pattern().determineMatches();
		var emptyFieldData = accessEmptyFields.data(emptyFields);
		emptyFieldData.forEach(fData -> {
			fields.stream().filter(fld -> fld.getCol() == fData._board_fields_0_field._col
					&& fld.getRow() == fData._board_fields_0_field._row).forEach(fld -> {
						fld.setIsEndPos(fData._field._endPos);
						fld.setFigureName(Optional.empty());
					});
		});

		var accessOccupiedFields = api1.getPattern_OccupiedFields();
		var occupiedFields = accessOccupiedFields.pattern().determineMatches();
		var occupiedFieldData = accessOccupiedFields.data(occupiedFields);
		occupiedFieldData.forEach(fData -> {
			fields.stream().filter(fld -> fld.getCol() == fData._board_fields_0_field._col
					&& fld.getRow() == fData._board_fields_0_field._row).forEach(fld -> {
						fld.setIsEndPos(fData._field._endPos);
						fld.setFigureName(Optional.of(fData._fig._ename));
					});
		});
	}
}