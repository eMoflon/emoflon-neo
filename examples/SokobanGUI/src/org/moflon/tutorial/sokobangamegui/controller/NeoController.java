
package org.moflon.tutorial.sokobangamegui.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.models.API_SokobanSimpleTestField;
import org.emoflon.neo.api.org.moflon.tutorial.sokobangamegui.patterns.API_SokobanGUIPatterns;
import org.emoflon.neo.api.rules.API_SokobanPatternsRulesConstraints;
import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;
import org.moflon.tutorial.sokobangamegui.view.Field;
import org.moflon.tutorial.sokobangamegui.view.View;

public class NeoController implements IController {
	private View view;
	private API_SokobanGUIPatterns api1;
	private API_SokobanPatternsRulesConstraints api2;
	private NeoCoreBuilder builder;

	private int width = 0;
	private int height = 0;
	private List<Field> fields;

	public NeoController() {
		this(c -> new View(c));
	}

	public NeoController(Function<IController, View> createView) {
		builder = API_Common.createBuilder();
		api1 = new API_SokobanGUIPatterns(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
		api2 = new API_SokobanPatternsRulesConstraints(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
		defaultBoard();
		view = createView.apply(this);
	}

	public static void main(String[] args) {
		Logger.getRootLogger().setLevel(Level.DEBUG);
		new NeoController();
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
		return access.matcher().determineMatches()//
				.stream().map(m -> {
					var data = access.data(m);
					return data.eclass.ename;
				}).collect(Collectors.toList());
	}

	@Override
	public Optional<Field> getSelectedField() {
		var access = api1.getPattern_SelectedFigure(); 
		return access.matcher().determineOneMatch().flatMap(m -> {
			var data = access.data(m);
			return fields.stream()//
					.filter(f -> f.getRow() == data.b_fields_1_f.row && f.getCol() == data.b_fields_1_f.col)//
					.findFirst();
		});
	}

	@Override
	public List<Field> getFields() {
		return fields;
	}

	@Override
	public boolean boardIsValid() {
		if(api1.getConstraint_ExactlyOneSokoban().isViolated()) {
			view.updateStatus("You must have exactly one Sokoban!");
			return false;
		}
					
		if(api1.getConstraint_OneEndField().isViolated()) {
			view.updateStatus("You must have exactly one end field!");
			return false;
		}
		
		if(api1.getPattern_Block().matcher().countMatches() != api1.getPattern_EndField().matcher().countMatches()) {
			view.updateStatus("You must have the same number of blocks as end fields!");
			return false;
		}
			
		if(api1.getConstraint_NoBlockedEndField().isViolated()) {
			view.updateStatus("One of your end fields is blocked by a boulder!");
			return false;
		}
		
		if(api2.getPattern_BlockNotOnEndFieldInCorner().matcher().determineOneMatch().isPresent()) {
			view.updateStatus("One of your blocks is in a corner (which is not an end field)!");
			return false;
		}
		
		view.updateStatus("Everything seems to be ok...");
		return true;
	}

	@Override
	public void setFigure(Field field, String figureType) {
		// TODO: Use a rule to set the figure
	}

	@Override
	public void selectField(Field field) {
		// TODO: Use a rule to select the field
	}

	@Override
	public void setEndPos(Field field, boolean b) {
		// TODO: Use a rule to set the field as an end position
	}

	@Override
	public void newBoard(int width, int height) {
		// TODO: Use a grammar to generate a new board
	}

	private void defaultBoard() {
		var exampleBoard = new API_SokobanSimpleTestField(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
		var board = exampleBoard.getModel_SokobanSimpleTestField();
		builder.exportModelToNeo4j(board);
		extractFields();
	}

	private void extractFields() {	
		var accessBoard = api1.getPattern_Board();
		accessBoard.matcher().determineOneMatch().ifPresent(m -> {
			var mData = accessBoard.data(m);
			this.width = mData.board.width;
			this.height = mData.board.height;

			fields = new ArrayList<Field>();
			var accessEmptyFields = api1.getPattern_EmptyFields();
			accessEmptyFields.matcher().determineMatches().forEach(f -> {
				var fData = accessEmptyFields.data(f);
				fields.add(new Field(//
						fData.board_fields_0_field.row, //
						fData.board_fields_0_field.col, //
						fData.field.endPos, //
						null));
			});

			var accessOccupiedFields = api1.getPattern_OccupiedFields();
			accessOccupiedFields.matcher().determineMatches().forEach(f -> {
				var data = accessOccupiedFields.data(f);
				fields.add(new Field(//
						data.board_fields_0_field.row, //
						data.board_fields_0_field.col, //
						data.field.endPos, //
						data.type.ename));
			});
		});
	}

	@Override
	public void loadSOKFile(String filePath) {
		// TODO: Populate database from sok file
	}

	@Override
	public void clearBoard() {
		// TODO: Remove all figures on the board
	}

	@Override
	public void saveSOKFile(String filePath) {
		// TODO: Write out board to a sok file
	}
}