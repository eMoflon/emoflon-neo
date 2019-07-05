
package org.moflon.tutorial.sokobangamegui.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.models.API_SokobanSimpleTestField;
import org.emoflon.neo.api.org.moflon.tutorial.sokobangamegui.patterns.API_SokobanGUIPatterns;
import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;
import org.moflon.tutorial.sokobangamegui.view.Field;
import org.moflon.tutorial.sokobangamegui.view.View;

public class NeoController implements IController {
	private API_SokobanGUIPatterns api;
	private NeoCoreBuilder builder;

	private int width = 0;
	private int height = 0;
	private List<Field> fields;

	public NeoController() {
		builder = API_Common.createBuilder();
		api = new API_SokobanGUIPatterns(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
		defaultBoard();
	}

	public static void main(String[] args) {
		Logger.getRootLogger().setLevel(Level.DEBUG);
		var controller = new NeoController();
		new View(controller);
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
		return api.getPattern_FigureTypes().determineMatches()//
				.stream().map(m -> {
					var data = api.getData_FigureTypes(m);
					return data.eclass.name;
				}).collect(Collectors.toList());
	}

	@Override
	public Optional<Field> getSelectedField() {
		return api.getPattern_SelectedFigure().determineOneMatch().flatMap(m -> {
			var data = api.getData_SelectedFigure(m);
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
		// TODO: Check all kind of constraints
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
		// TODO: Use width and height to create an empty board?
	}

	private void defaultBoard() {
		var exampleBoard = new API_SokobanSimpleTestField(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
		var board = exampleBoard.getModel_SokobanSimpleTestField();
		builder.exportEMSLEntityToNeo4j(board);

		api.getPattern_Board().determineOneMatch().ifPresent(m -> {
			var mData = api.getData_Board(m);
			this.width = mData.board.width;
			this.height = mData.board.height;

			fields = new ArrayList<Field>();
			api.getPattern_EmptyFields().determineMatches().forEach(f -> {
				var fData = api.getData_EmptyFields(f);
				fields.add(new Field(//
						fData.board_fields_0_field.row, //
						fData.board_fields_0_field.col, //
						fData.field.endPos, //
						null));
			});

			api.getPattern_OccupiedFields().determineMatches().forEach(f -> {
				var data = api.getData_OccupiedFields(f);
				fields.add(new Field(//
						data.board_fields_0_field.row, //
						data.board_fields_0_field.col, //
						data.field.endPos, //
						data.type.name));
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