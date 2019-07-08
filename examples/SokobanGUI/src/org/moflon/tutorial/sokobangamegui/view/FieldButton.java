package org.moflon.tutorial.sokobangamegui.view;

import javax.swing.JButton;


/**
 * Custom button class (inherited from JButton).
 * Each button represents an own field.
 * @author Matthias Senker (Comments by Lukas Hermanns)
 */
public class FieldButton extends JButton {
	private static final long serialVersionUID = 1L;
	
	/* Reference to the field which is represented by this button */
	private Field field;
	
	public FieldButton() {
		setOpaque(true);
	}
	
	/**
	 * @param field Sets the field object.
	 */
	public void setField(Field field) {
		this.field = field;
	}
	
	/**
	 * @return The field object.
	 */
	public Field getField() {
		return field;
	}
	
	/**
	 * @return The field row on the board.
	 */
	public int getRow() {
		return field.getRow();
	}
	
	/**
	 * @return The field column on the board.
	 */
	public int getColumn() {
		return field.getCol();
	}
}
