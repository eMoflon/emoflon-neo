package org.moflon.tutorial.sokobangamegui.view.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.moflon.tutorial.sokobangamegui.view.View;

/**
 * Custom action listener for new-board action.
 * @author Matthias Senker (Comments by Lukas Hermanns)
 */
public class NewBoardAction implements ActionListener {

	private View view;
	
	public NewBoardAction(View view) {
		this.view = view;
	}
	
	/**
	 * @param e Specifies the action event
	 * @throw NumberFormatException when the board width or height is smaller than 1 or greater than 30.
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		/* Temporary memory */
		int width = 0;
		int height = 0;
		
		/* Setup board width */
		String widthInput = JOptionPane.showInputDialog(view, "Enter width of the new boad:", "Input", JOptionPane.PLAIN_MESSAGE);
		
		if (widthInput == null)
			return;
		try {
			/* Parse entered width */
			width = Integer.parseInt(widthInput);
			if (width < 1 || width > 30) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(view, "Please enter an integer number from 1 to 30");
			return;
		}
		
		/* Setup board height */
		String heightInput = JOptionPane.showInputDialog(view, "Enter height of the new boad:", "Input", JOptionPane.PLAIN_MESSAGE);
		
		if (heightInput == null)
			return;
		try {
			/* Parse entered height */
			height = Integer.parseInt(heightInput);
			if (height < 1 || height> 30) {
				throw new NumberFormatException();
			}
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(view, "Please enter an integer number from 1 to 30");
			return;
		}
		
		// /* Create new board with entered size */
		view.getController().newBoard(width, height);	
	}

}
