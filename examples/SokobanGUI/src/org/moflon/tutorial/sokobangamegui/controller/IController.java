package org.moflon.tutorial.sokobangamegui.controller;

import java.util.List;
import java.util.Optional;

import org.moflon.tutorial.sokobangamegui.view.Field;

public interface IController {
	public String BOULDER = "Boulder";
	public String BLOCK = "Block";
	public String SOKOBAN = "Sokoban";
	int getWidth();
	int getHeight();

	List<String> getFigureTypes();
	Optional<Field> getSelectedField();
	List<Field> getFields();
	
	boolean boardIsValid();

	void setFigure(Field field, String figureType);
	void selectField(Field field);
	void setEndPos(Field field, boolean b);

	void newBoard(int width, int height);
	void clearBoard();

	void loadSOKFile(String filePath);
	void saveSOKFile(String filePath);
	
	void update();
}
