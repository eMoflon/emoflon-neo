package org.moflon.tutorial.sokobangamegui.view;

public class Field {
	private int row;
	private int col;
	private boolean endPos;
	private String figName;
	
	public Field(int row, int col, boolean endPos, String figName) {
		this.col = col;
		this.row = row;
		this.figName = figName;
		this.endPos = endPos;
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	public boolean isEndPos() {
		return endPos;
	}

	public String getFigureName() {
		return figName;
	}

	@Override
	public String toString() {
		return "[" + row + ", " +  col + "]";
	}
}
