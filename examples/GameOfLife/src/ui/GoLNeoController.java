package ui;

import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.spec.API_GameOfLifeBuilder;
import org.emoflon.neo.api.spec.API_GameOfLifeRules;
import org.emoflon.neo.emsl.util.FlattenerException;

public class GoLNeoController implements IController {

	private Field[][] fields;
	private int width;
	private int height;
	private API_GameOfLifeBuilder creator;

	public GoLNeoController(int width, int height, int[][] configuration) throws FlattenerException {
		this.width = width;
		this.height = height;
		
		var builder = API_Common.createBuilder();
		
		creator = new API_GameOfLifeBuilder(builder);
		
		fields = new Field[height][width];
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				fields[j][i] = new Field(j, i, false);
			}
		}
				
		builder.exportEMSLEntityToNeo4j(new API_GameOfLifeRules(builder).getMetamodel_GameOfLife());
		
		creator.getRule_CreateTopLeftCell().rule().apply();
		for(int col = 1; col < width; col++)
			creator.getRule_CreateCellsInFirstRow().rule().apply();
		
		for(int row = 1; row < height; row++)
			creator.getRule_CreateCellsInFirstCol().rule().apply();
		
		var rule = creator.getRule_CreateAllOtherCells().rule();
		while(rule.apply().isPresent());
		
		updateFields();
	}

	private void updateFields() {
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				fields[j][i].setIsAlive(false);
		
		var liveCells = creator.getPattern_ALiveCell().matcher().determineMatches();
		liveCells.forEach(m -> {
			var data = creator.getPattern_ALiveCell().data(m);
			fields[data.cell.x][data.cell.y].setIsAlive(true);
		});
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}

	@Override
	public Field[][] getFields() {
		return fields;
	}

	private void startSimulation(View view) throws InterruptedException {
		// TODO: Perform tick
		
		updateFields();
		view.update();
		
		Thread.sleep(5000);
		
		startSimulation(view);
	}
	
	public static void main(String[] args) throws FlattenerException, Exception {
		var controller = new GoLNeoController(10,10, new int[][]{{1,2},{2,3}});
		var view = new View(controller);
		controller.startSimulation(view);
	}
}
