package models;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.models.API_SokobanSimple;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.eMSL.MetamodelRelationStatement;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.impl.EMSLPackageImpl;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;

public class BoardGenerator {

	private static final Logger logger = Logger.getLogger(BoardGenerator.class);

	public static void main(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.DEBUG);

		BoardGenerator t = new BoardGenerator();
		EMSLPackageImpl.init();

		int start = 0;
		int step = 50;
		int stop = 5;

		String log = "";
		for (int size = start; size <= stop; size += step) {
			log += t.generateBoard("sokobanBoard_" + size, size, 10000, 10000);
		}

		logger.info(log);
	}

	/**
	 * Generate an empty sokoban board
	 * 
	 * @param modelName
	 * @param numberOfFields number of fields on the board
	 * @throws Exception
	 */
	public void generateBoard(String modelName, int numberOfFields) throws Exception {
		generateBoard(modelName, numberOfFields, 1000, 1000);
	}

	private String generateBoard(String modelName, int modelSize, int nodes, int edges) throws Exception {
		String time = "";
		NeoCoreBuilder builder = API_Common.createBuilder();

		try {
			Model model = new API_SokobanSimple(builder, API_Common.PLATFORM_RESOURCE_URI,
					API_Common.PLATFORM_PLUGIN_URI).getModel_SokobanSimple();

			model.setName(modelName);
			generateContents(model, modelSize);

			long tic = System.currentTimeMillis();
			builder.setMaxTransactionSize(nodes, edges);
			builder.exportEMSLEntityToNeo4j(model);
			long toc = System.currentTimeMillis();
			logger.info("Export took: " + (toc - tic) / 1000.0 + "s");
			time += (toc - tic) / 1000.0 + "\n";
		} finally {
			builder.close();
		}

		return time;
	}

	private void generateContents(Model model, int size) {
		ModelNodeBlock boardNb = model.getNodeBlocks().get(0);
		ModelNodeBlock field0 = model.getNodeBlocks().get(1);

		List<ModelNodeBlock> lastCol = new ArrayList<>();
		lastCol.add(field0);
		List<ModelNodeBlock> lastRow = new ArrayList<>();
		lastRow.add(field0);
		extendBoard(lastCol, lastRow, size, boardNb, model, field0);
	}

	private void extendBoard(List<ModelNodeBlock> lastCol, List<ModelNodeBlock> lastRow, int size, ModelNodeBlock board,
			Model model, ModelNodeBlock firstField) {

		if (size == 0)
			return;

		List<ModelNodeBlock> newLastCol = new ArrayList<>();
		List<ModelNodeBlock> newLastRow = new ArrayList<>();
		Metamodel mm = (Metamodel) firstField.getType().eContainer();

		for (ModelNodeBlock nb : lastCol) {
			ModelNodeBlock rightField = EMSLFactory.eINSTANCE.createModelNodeBlock();
			rightField.setName("rf");
			rightField.setType(firstField.getType());
			model.getNodeBlocks().add(rightField);

			newLastCol.add(rightField);

			{
				ModelRelationStatement rs = EMSLFactory.eINSTANCE.createModelRelationStatement();
				var type = EMSLFactory.eINSTANCE.createModelRelationStatementType();
				type.setType(retrieveRelType(mm, "fields"));
				rs.getTypes().add(type);
				rs.setTarget(rightField);
				board.getRelations().add(rs);
			}

			{
				ModelRelationStatement rs = EMSLFactory.eINSTANCE.createModelRelationStatement();
				var type = EMSLFactory.eINSTANCE.createModelRelationStatementType();
				type.setType(retrieveRelType(mm, "right"));
				rs.getTypes().add(type);
				rs.setTarget(rightField);
				nb.getRelations().add(rs);
			}

			int index = lastCol.indexOf(nb);

			if (index > 0) {
				var lCol = newLastCol.get(index - 1);

				ModelRelationStatement rs = EMSLFactory.eINSTANCE.createModelRelationStatement();
				var type = EMSLFactory.eINSTANCE.createModelRelationStatementType();
				type.setType(retrieveRelType(mm, "bottom"));
				rs.getTypes().add(type);
				rs.setTarget(rightField);
				lCol.getRelations().add(rs);
			}

		}

		for (ModelNodeBlock nb1 : lastRow) {
			ModelNodeBlock bottomField = EMSLFactory.eINSTANCE.createModelNodeBlock();
			bottomField.setName("bf");
			bottomField.setType(firstField.getType());
			model.getNodeBlocks().add(bottomField);

			newLastRow.add(bottomField);

			{
				ModelRelationStatement rs = EMSLFactory.eINSTANCE.createModelRelationStatement();
				var type = EMSLFactory.eINSTANCE.createModelRelationStatementType();
				type.setType(retrieveRelType(mm, "fields"));
				rs.getTypes().add(type);
				rs.setTarget(bottomField);
				board.getRelations().add(rs);
			}

			{
				ModelRelationStatement rs = EMSLFactory.eINSTANCE.createModelRelationStatement();
				var type = EMSLFactory.eINSTANCE.createModelRelationStatementType();
				type.setType(retrieveRelType(mm, "bottom"));
				rs.getTypes().add(type);
				rs.setTarget(bottomField);
				nb1.getRelations().add(rs);
			}

			int index = lastRow.indexOf(nb1);

			if (index > 0) {
				var lRow = newLastRow.get(index - 1);

				ModelRelationStatement rs = EMSLFactory.eINSTANCE.createModelRelationStatement();
				var type = EMSLFactory.eINSTANCE.createModelRelationStatementType();
				type.setType(retrieveRelType(mm, "right"));
				rs.getTypes().add(type);
				rs.setTarget(bottomField);
				lRow.getRelations().add(rs);
			}

		}

		var lc = newLastCol.get(newLastCol.size() - 1);
		var lr = newLastRow.get(newLastRow.size() - 1);

		ModelNodeBlock cornerField = EMSLFactory.eINSTANCE.createModelNodeBlock();
		cornerField.setName("cf");
		cornerField.setType(firstField.getType());
		model.getNodeBlocks().add(cornerField);

		{
			ModelRelationStatement rs = EMSLFactory.eINSTANCE.createModelRelationStatement();
			var type = EMSLFactory.eINSTANCE.createModelRelationStatementType();
			type.setType(retrieveRelType(mm, "fields"));
			rs.getTypes().add(type);
			rs.setTarget(cornerField);
			board.getRelations().add(rs);
		}

		{
			ModelRelationStatement rs = EMSLFactory.eINSTANCE.createModelRelationStatement();
			var type = EMSLFactory.eINSTANCE.createModelRelationStatementType();
			type.setType(retrieveRelType(mm, "bottom"));
			rs.getTypes().add(type);
			rs.setTarget(cornerField);
			lc.getRelations().add(rs);

		}

		{
			ModelRelationStatement rs = EMSLFactory.eINSTANCE.createModelRelationStatement();
			var type = EMSLFactory.eINSTANCE.createModelRelationStatementType();
			type.setType(retrieveRelType(mm, "right"));
			rs.getTypes().add(type);
			rs.setTarget(cornerField);
			lr.getRelations().add(rs);
		}

		newLastCol.add(cornerField);
		newLastRow.add(cornerField);

		extendBoard(newLastCol, newLastRow, size - 1, board, model, firstField);
	}

	private MetamodelRelationStatement retrieveRelType(Metamodel mm, String name) {
		return mm.getNodeBlocks().stream()//
				.flatMap(bl -> bl.getRelations().stream())//
				.filter(r -> r.getName().equals(name))//
				.findAny()//
				.get();
	}
}
