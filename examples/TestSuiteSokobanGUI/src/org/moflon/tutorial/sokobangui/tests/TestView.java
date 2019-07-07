package org.moflon.tutorial.sokobangui.tests;


import org.moflon.tutorial.sokobangamegui.controller.IController;
import org.moflon.tutorial.sokobangamegui.view.Field;
import org.moflon.tutorial.sokobangamegui.view.View;

public class TestView extends View {
	private static final String BOULDER = "Boulder";
	private static final String BLOCK = "Block";
	private static final String SOKOBAN = "Sokoban";
	private static final long serialVersionUID = 1L;

	public TestView(IController controller) {
		super(controller);
	}

	public void createSokoban(int x, int y) {
		createFigure(SOKOBAN, buttons[x][y]);
	}

	public void createBlock(int x, int y) {
		createFigure(BLOCK, buttons[x][y]);
	}

	public void createBoulder(int x, int y) {
		createFigure(BOULDER, buttons[x][y]);
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
			if (field.getFigureName() == null) {
				return "[.]";
			} else {
				switch (field.getFigureName()) {
				case SOKOBAN:
					return "[+]";
				case BLOCK:
					return "[*]";
				default:
					return "[?]";
				}
			}
		} else {
			if (field.getFigureName() == null) {
				return "[ ]";
			} else {
				switch (field.getFigureName()) {
				case SOKOBAN:
					return "[@]";
				case BLOCK:
					return "[$]";
				case BOULDER:
					return "[#]";
				default:
					return "[?]";
				}
			}
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
