package org.moflon.tutorial.sokobangamegui.view.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JMenuItem;

import org.moflon.tutorial.sokobangamegui.controller.IController;
import org.moflon.tutorial.sokobangamegui.view.View;

public class PlayAction implements ActionListener {
	public static final String PLAY  = "Play Game!";
	public static final String EDIT = "Edit Game";
	
	private JMenuItem playToggle;
	private boolean playModus;
	private View view;
	private IController controller;
	
	public PlayAction(JMenuItem playToggle, View view, IController controller) {
		this.playToggle = playToggle;
		playModus = false;
		playToggle.setText(PLAY);
		this.view = view;
		this.controller = controller;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(isEditModus())
			if(!controller.boardIsValid()) 
				return;
		
		playModus = !playModus;
		playToggle.setText(playModus? EDIT : PLAY);
		view.updateView();
	}
	
	private boolean isEditModus() {
		return !playModus;
	}

	public boolean isPlayModus() {
		return playModus;
	}

	public void setPlayModus(boolean b) {
		playModus = b;
	}
}
