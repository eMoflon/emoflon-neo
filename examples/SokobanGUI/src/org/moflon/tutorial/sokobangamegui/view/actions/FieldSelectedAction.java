package org.moflon.tutorial.sokobangamegui.view.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.moflon.tutorial.sokobangamegui.view.Field;
import org.moflon.tutorial.sokobangamegui.view.View;


/**
 * Custom action listener for field-selected action.
 * @author Matthias Senker (Comments by Lukas Hermanns)
 */
public class FieldSelectedAction implements ActionListener {

	private View view;
	private Field field;
	
	public FieldSelectedAction(View view, Field field) {
		this.view = view;
		this.field = field;
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		/* Select field with controller */
		view.selectField(field);
	}
}
