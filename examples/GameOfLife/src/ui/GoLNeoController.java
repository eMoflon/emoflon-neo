package ui;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.gameoflife.API_Common;
import org.emoflon.neo.api.gameoflife.spec.API_GameOfLifeBuilder;
import org.emoflon.neo.api.gameoflife.spec.API_GameOfLifeRules;
import org.emoflon.neo.emsl.util.FlattenerException;

public class GoLNeoController implements IController {

	private Field[][] fields;
	private int width;
	private int height;
	private API_GameOfLifeBuilder creator;
	private API_GameOfLifeRules rules;
	private static final Logger logger = Logger.getLogger(GoLNeoController.class);

	public GoLNeoController(int width, int height, int[][] configuration) throws FlattenerException {
		this.width = width;
		this.height = height;

		var builder = API_Common.createBuilder();
		builder.clearDataBase();

		creator = new API_GameOfLifeBuilder(builder);
		rules = new API_GameOfLifeRules(builder);

		fields = new Field[height][width];
		for (int i = 0; i < width; i++)
			for (int j = 0; j < height; j++)
				fields[j][i] = new Field(j, i, false);

		builder.exportEMSLEntityToNeo4j(rules.getMetamodel_GameOfLife());

		logger.info("Done initialising...  Now building the board.");
		
		creator.getRule_CreateTopLeftCorner().rule().apply();
		for (int col = 1; col < width; col++)
			creator.getRule_CreateCellsInFirstRow().rule().apply();
		
		for (int row = 1; row < height; row++)
			creator.getRule_CreateCellsInFirstCol().rule().apply();

		logger.info("Created first row and column.");
		
		{
			var rule = creator.getRule_CreateAllOtherCells().rule();
			var matches = rule.determineMatches();
			while (matches.size() > 0) {
				rule.applyAll(matches);
				matches = rule.determineMatches();
			}
		}
		
		logger.info("Created all other cells.");

		{
			var rule = creator.getRule_CreateDiagonals().rule();
			var matches = rule.determineMatches();
			rule.applyAll(matches);
		}
		
		logger.info("Created all missing diagonal edges.");

		for (int i = 0; i < configuration.length; i++) {
			var mask = creator.getRule_MakeCellAlive().mask();
			mask.setCellRow(configuration[i][0]);
			mask.setCellCol(configuration[i][1]);
			creator.getRule_MakeCellAlive().apply(mask, creator.getRule_MakeCellAlive().mask());
		}
		
		logger.info("Set configuration in board.");

		updateFields();
	}

	private void updateFields() {
		for (int col = 0; col < width; col++)
			for (int row = 0; row < height; row++)
				fields[row][col].setIsAlive(false);

		var liveCells = creator.getPattern_ALiveCell().pattern().determineMatches();
		var data = creator.getPattern_ALiveCell().data(liveCells);
		
		data.forEach(d -> {
			fields[d._cell._row][d._cell._col].setIsAlive(true);
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
		long tic = System.currentTimeMillis();
		
		var dieOverPop = rules.getRule_DieDueToOverpopulation().rule().determineMatches();
		var dieUnderPop = rules.getRule_DieDueToUnderpopulation().rule().determineMatches();
		var reproduce = rules.getRule_Reproduce().rule().determineMatches();

		rules.getRule_DieDueToOverpopulation().rule().applyAll(dieOverPop);
		rules.getRule_DieDueToUnderpopulation().rule().applyAll(dieUnderPop);
		rules.getRule_Reproduce().rule().applyAll(reproduce);

		updateFields();
		
		long toc = System.currentTimeMillis();
		
		var sleep = 3000 - (toc - tic);
		if(sleep > 0)
			Thread.sleep(sleep);
		
		view.update();

		startSimulation(view);
	}

	public static void main(String[] args) throws FlattenerException, Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var controller = new GoLNeoController(11, 21, pentadecathlon());
		var view = new View(controller);
		controller.startSimulation(view);
	}

	/* https://en.wikipedia.org/wiki/Conway's_Game_of_Life */
	@SuppressWarnings("unused")
	private static int[][] glider() {
		return new int[][] { { 0, 1 }, { 1, 2 }, { 2, 0 }, { 2, 1 }, { 2, 2 } };
	}
	
	@SuppressWarnings("unused")
	private static int[][] blinker() {
		return new int[][] { { 3, 2 }, { 3, 3 }, { 3, 4 } };
	}
	
	private static int[][] pentadecathlon() {
		return new int[][] { 
			{ 5, 5 }, 
			{ 6, 5 }, 
			{ 7, 4 }, {7, 6},
			{ 8, 5 },
			{ 9, 5 },
			{ 10, 5 },
			{ 11, 5 },
			{ 12, 4 }, {12, 6},
			{ 13, 5 },
			{ 14, 5 }
		}
		;
	}
}
