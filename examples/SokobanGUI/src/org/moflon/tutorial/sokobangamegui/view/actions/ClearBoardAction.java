package org.moflon.tutorial.sokobangamegui.view.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.moflon.tutorial.sokobangamegui.view.View;

/**
 * Custom action listener for clear-board action.
 * @author Matthias Senker (Comments by Lukas Hermanns)
 */
public class ClearBoardAction implements ActionListener {

	private View view;
	
	public ClearBoardAction(View view) {
		this.view = view;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		/* Ask user before clearing the board */
		int answer = JOptionPane.showConfirmDialog(view, "This will remove all figures from the board. Do you wish to continue?", "Are you sure?", JOptionPane.YES_NO_OPTION);
		if (answer == JOptionPane.YES_OPTION) {
			view.getController().clearBoard();
		}
	}

}
