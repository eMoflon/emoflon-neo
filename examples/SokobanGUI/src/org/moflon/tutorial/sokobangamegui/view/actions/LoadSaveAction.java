package org.moflon.tutorial.sokobangamegui.view.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import org.moflon.tutorial.sokobangamegui.view.View;

/**
 * Custom action listener for load- and save-action.
 * 
 * @author Matthias Senker (Comments by Lukas Hermanns)
 */
public class LoadSaveAction implements ActionListener {
	private static final String SOK = "sok";

	private JFileChooser fileChooser;
	private View view;

	private JComponent saveSource;

	public LoadSaveAction(View view, JComponent saveSource, JComponent loadSource) {
		/* Setup internal memory */
		this.view = view;
		this.saveSource = saveSource;

		/* Show file selector */
		fileChooser = new JFileChooser("boards/");
		fileChooser.setAcceptAllFileFilterUsed(false);
		FileFilter filterSOK = new FileNameExtensionFilter(SOK, SOK);
		fileChooser.addChoosableFileFilter(filterSOK);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getSource().equals(saveSource)) {
			if (fileChooser.showSaveDialog(view) == JFileChooser.APPROVE_OPTION) {
				/* Get full filename from file-selector */
				String filePath = fileChooser.getSelectedFile().getPath();

				if (fileChooser.getFileFilter().getDescription().contentEquals(SOK)) {
					filePath = addExtensionIfMissing(filePath, SOK);
					view.getController().saveSOKFile(filePath);
				}
			}
		} else {
			if (fileChooser.showOpenDialog(view) == JFileChooser.APPROVE_OPTION) {
				/* Get full filename from file-selector */
				String filePath = fileChooser.getSelectedFile().getPath();

				/* Read the model from file */
				if (fileChooser.getFileFilter().getDescription().equals(SOK))
					view.getController().loadSOKFile(filePath);
			}
		}
	}

	private String addExtensionIfMissing(String filePath, String extension) {
		if (fileChooser.getFileFilter().getDescription().equals(extension) && !filePath.endsWith("." + extension))
			return filePath + "." + extension;
		else
			return filePath;
	}

}
