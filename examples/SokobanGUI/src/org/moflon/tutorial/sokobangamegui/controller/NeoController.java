
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
import org.emoflon.neo.neo4j.adapter.NeoMatch;
import org.emoflon.neo.neo4j.adapter.NeoPattern;
import org.moflon.tutorial.sokobangamegui.view.Field;
import org.moflon.tutorial.sokobangamegui.view.View;
import org.neo4j.driver.v1.types.Node;

public class NeoController implements IController {

	private API_SokobanGUIPatterns api;
	private NeoCoreBuilder builder;

	private int width = 0;
	private int height = 0;
	private List<Field> fields;

	public NeoController() {
		builder = API_Common.createBuilder();
		api = new API_SokobanGUIPatterns(builder, //
				"/Users/anthonyanjorin/git/emoflon-neo/examples/", //
				"/Users/anthonyanjorin/git/emoflon-neo/");
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
					NeoMatch nm = (NeoMatch) m;
					var figType = nm.getData().get("eclass");
					return figType.get("name").asString();
				}).collect(Collectors.toList());
	}

	@Override
	public Optional<Field> getSelectedField() {
		return api.getPattern_SelectedFigure().determineOneMatch().flatMap(m -> {
			var neoMatch = (NeoMatch) m;
			var p = (NeoPattern) m.getPattern();
			var r = p.getNodes()//
					.stream()//
					.flatMap(n -> n.getRelations().stream())//
					.filter(rl -> rl.getRelType().equals("fields")).findAny();
			var rName = r.get().getVarName();
			var rNode = neoMatch.getData().get(rName);
			var row = rNode.get("row").asInt();
			var col = rNode.get("col").asInt();
			return fields.stream()//
					.filter(f -> f.getRow() == row && f.getCol() == col)//
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
		var exampleBoard = new API_SokobanSimpleTestField(builder);
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
						fData.board_fields_field.row, //
						fData.board_fields_field.col, //
						fData.field.endPos, //
						null));
			});

			api.getPattern_OcupiedFields().determineMatches().forEach(f -> {
				var fm = (NeoMatch) f;
				var data = fm.getData();
				var fNode = data.get("field");
				var p = (NeoPattern) fm.getPattern();
				var r = p.getNodes()//
						.stream()//
						.flatMap(n -> n.getRelations().stream())//
						.filter(rl -> rl.getRelType().equals("fields")).findAny();
				var rName = r.get().getVarName();
				var rNode = data.get(rName);
				var figNode = data.get("fig").asNode();
				fields.add(new Field(//
						rNode.get("row").asInt(), //
						rNode.get("col").asInt(), //
						fNode.get("endPos").asBoolean(), //
						determineTypeOfFigure(figNode)));
			});
		});
	}

	private String determineTypeOfFigure(Node figNode) {
		if (figNode.hasLabel("Block"))
			return "Block";
		else if (figNode.hasLabel("Boulder"))
			return "Boulder";
		else
			return "Sokoban";
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