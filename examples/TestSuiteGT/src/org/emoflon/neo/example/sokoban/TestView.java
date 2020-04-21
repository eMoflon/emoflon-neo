package org.emoflon.neo.example.sokoban;

import org.moflon.tutorial.sokobangamegui.controller.IController;
import org.moflon.tutorial.sokobangamegui.view.Field;
import org.moflon.tutorial.sokobangamegui.view.View;

public class TestView extends View {
	private static final long serialVersionUID = 1L;

	public TestView(IController controller) {
		super(controller);
	}

	public void createSokoban(int x, int y) {
		createFigure(IController.SOKOBAN, buttons[x][y]);
	}

	public void createBlock(int x, int y) {
		createFigure(IController.BLOCK, buttons[x][y]);
	}

	public void createBoulder(int x, int y) {
		createFigure(IController.BOULDER, buttons[x][y]);
	}

	public void createEndPos(int x, int y) {
		createEndFigure(buttons[x][y]);
	}

	/**
	 * Prints the board as a kind of ASCII art to the console.
	 */
	public String printBoard() {
		String rep = "";

		/* Check parameter validity */
		if (controller == null)
			return rep;

		/* Allocate temporary field array */
		int w = controller.getWidth();
		int h = controller.getHeight();

		Field[][] fields = new Field[h][w];

		/* Fill temporary field array with the board fields */
		for (Field f : controller.getFields()) {
			fields[f.getRow()][f.getCol()] = f;
		}

		/* Print each row */
		for (int r = 0; r < h; r++) {
			/* Print each column */
			for (int c = 0; c < w; c++) {
				rep += printField(fields[r][c]);
			}
			rep += String.format("%n");
		}

		return rep;
	}

	/**
	 * Prints the given field object.
	 * 
	 * @param field Specifies the field object which is to be printed.
	 */
	private String printField(Field field) {
		if (field.isEndPos()) {
			return field.getFigureName().map(figureName -> {
				switch (figureName) {
				case IController.SOKOBAN:
					return "[+]";
				case IController.BLOCK:
					return "[*]";
				default:
					return "[?]";
				}
			}).orElse("[.]");
		} else {
			return field.getFigureName().map(figureName -> {
				switch (figureName) {
				case IController.SOKOBAN:
					return "[@]";
				case IController.BLOCK:
					return "[$]";
				case IController.BOULDER:
					return "[#]";
				default:
					return "[?]";
				}
			}).orElse("[ ]");

		}
	}

	@Override
	protected String imageFolder() {
		return "../SokobanGUI/images/";
	}

	public void setPlayModus(boolean b) {
		playAction.setPlayModus(b);
	}

	public Field getField(int i, int j) {
		return buttons[i][j].getField();
	}

	public void moveFigure(Field from, Field to) {
		selectField(from);
		selectField(to);
	}
}
