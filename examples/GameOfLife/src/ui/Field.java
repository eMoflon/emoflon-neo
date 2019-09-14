package ui;

public class Field {
	private int row;
	private int col;
	private boolean alive;
	
	public Field(int row, int col, boolean alive) {
		this.col = col;
		this.row = row;
		this.alive = alive;
	}

	public int getRow() {
		return row;
	}

	public int getCol() {
		return col;
	}

	public boolean isAlive() {
		return alive;
	}

	@Override
	public String toString() {
		return "[" + row + ", " +  col + "]";
	}

	public void setIsAlive(boolean alive) {
		this.alive = alive;
	}
}
