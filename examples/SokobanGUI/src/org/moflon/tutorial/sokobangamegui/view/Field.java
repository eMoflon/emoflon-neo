package org.moflon.tutorial.sokobangamegui.view;

import java.util.Optional;

public class Field {
	private int row;
	private int col;
	private boolean endPos;
	private Optional<String> figName;
	
	public Field(int row, int col, boolean endPos, Optional<String> figName) {
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

	public Optional<String> getFigureName() {
		return figName;
	}

	@Override
	public String toString() {
		return "[" + row + ", " +  col + "]";
	}

	public void setIsEndPos(boolean endPos) {
		this.endPos = endPos;
	}

	public void setFigureName(Optional<String> figName) {
		this.figName = figName;
	}
}
