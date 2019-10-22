package org.emoflon.neo.neo4j.adapter.rules;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emoflon.neo.emsl.eMSL.ActionOperator;
import org.emoflon.neo.emsl.eMSL.BuiltInDataTypes;
import org.emoflon.neo.emsl.eMSL.ConditionOperator;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock;
import org.emoflon.neo.emsl.eMSL.MetamodelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.MetamodelRelationStatement;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBootstrapper;

public class RulePreProcessor {
	Map<String, ModelNodeBlock> nodeBlocks;
	private MetamodelNodeBlock eClass;
	private MetamodelNodeBlock metamodelClass;
	private MetamodelPropertyStatement eName;
	private MetamodelRelationStatement elOfRel;
	private MetamodelRelationStatement mtRel;

	public RulePreProcessor(List<ModelNodeBlock> nodeBlocks) {
		this.nodeBlocks = new HashMap<>();
		nodeBlocks.forEach(nb -> this.nodeBlocks.putIfAbsent(nb.getName(), nb));
		eClass = EMSLFactory.eINSTANCE.createMetamodelNodeBlock();
		eClass.setName(NeoCoreBootstrapper.ECLASS);

		eName = EMSLFactory.eINSTANCE.createMetamodelPropertyStatement();
		eName.setName(NeoCoreBootstrapper.NAME_PROP);
		var estring = EMSLFactory.eINSTANCE.createBuiltInType();
		estring.setReference(BuiltInDataTypes.ESTRING);
		eName.setType(estring);
		eClass.getProperties().add(eName);

		metamodelClass = EMSLFactory.eINSTANCE.createMetamodelNodeBlock();
		metamodelClass.setName(NeoCoreBootstrapper.METAMODEL);

		elOfRel = EMSLFactory.eINSTANCE.createMetamodelRelationStatement();
		elOfRel.setName(NeoCoreBootstrapper.META_EL_OF);

		mtRel = EMSLFactory.eINSTANCE.createMetamodelRelationStatement();
		mtRel.setName(NeoCoreBootstrapper.META_TYPE);
	}

	public List<ModelNodeBlock> preprocess() {
		addMetaTypeEdges();
		return new ArrayList<>(nodeBlocks.values());
	}

	private void addMetaTypeEdges() {
		var originalBlocks = List.copyOf(nodeBlocks.values());
		for (var nodeBlock : originalBlocks) {
			var mmNode = addMetamodelNodeIfNecessary(nodeBlock);
			var typeNode = addTypeNodeIfNecessary(nodeBlock, mmNode);

			if (nodeBlock.getAction() != null) {
				switch (nodeBlock.getAction().getOp()) {
				case CREATE:
					addGreenMetaTypeEdge(nodeBlock, typeNode);
					break;
				case DELETE:
					addRedMetaTypeEdge(nodeBlock, typeNode);
					break;
				default:
					throw new IllegalArgumentException();
				}
			}
		}
	}

	private ModelRelationStatement addBlackMetaTypeEdge(ModelNodeBlock nodeBlock, ModelNodeBlock typeNode) {
		var mt = EMSLFactory.eINSTANCE.createModelRelationStatement();
		mt.setTarget(typeNode);

		var mtRelType = EMSLFactory.eINSTANCE.createModelRelationStatementType();
		mtRelType.setType(mtRel);

		mt.getTypes().add(mtRelType);
		nodeBlock.getRelations().add(mt);

		return mt;
	}

	private void addRedMetaTypeEdge(ModelNodeBlock nodeBlock, ModelNodeBlock typeNode) {
		var mt = addBlackMetaTypeEdge(nodeBlock, typeNode);
		var action = EMSLFactory.eINSTANCE.createAction();
		action.setOp(ActionOperator.DELETE);
		mt.setAction(action);
	}

	private void addGreenMetaTypeEdge(ModelNodeBlock nodeBlock, ModelNodeBlock typeNode) {
		var mt = addBlackMetaTypeEdge(nodeBlock, typeNode);
		var action = EMSLFactory.eINSTANCE.createAction();
		action.setOp(ActionOperator.CREATE);
		mt.setAction(action);
	}

	private void addElementOfRelation(ModelNodeBlock typeNode, ModelNodeBlock mmNode) {
		var elOf = EMSLFactory.eINSTANCE.createModelRelationStatement();
		elOf.setTarget(mmNode);

		var elOfRelType = EMSLFactory.eINSTANCE.createModelRelationStatementType();
		elOfRelType.setType(elOfRel);

		elOf.getTypes().add(elOfRelType);
		typeNode.getRelations().add(elOf);
	}

	private ModelNodeBlock addMetamodelNodeIfNecessary(ModelNodeBlock nodeBlock) {
		var mm = (Metamodel) nodeBlock.getType().eContainer();
		var mmName = mm.getName();

		if (!nodeBlocks.containsKey(mmName)) {
			var mmNode = EMSLFactory.eINSTANCE.createModelNodeBlock();
			mmNode.setType(metamodelClass);
			mmNode.setName(mmName);
			var nameProp = EMSLFactory.eINSTANCE.createModelPropertyStatement();
			nameProp.setType(eName);
			var value = EMSLFactory.eINSTANCE.createPrimitiveString();
			value.setLiteral(mm.getName());
			nameProp.setValue(value);
			nameProp.setOp(ConditionOperator.EQ);
			mmNode.getProperties().add(nameProp);
			nodeBlocks.put(mmNode.getName(), mmNode);
		}

		return nodeBlocks.get(mmName);
	}

	private ModelNodeBlock addTypeNodeIfNecessary(ModelNodeBlock nodeBlock, ModelNodeBlock mmNodeBlock) {
		var typeName = nodeBlock.getType().getName() + "_" + mmNodeBlock.getName();

		if (!nodeBlocks.containsKey(typeName)) {
			var typeNode = EMSLFactory.eINSTANCE.createModelNodeBlock();
			typeNode.setType(eClass);
			typeNode.setName(typeName);
			var nameProp = EMSLFactory.eINSTANCE.createModelPropertyStatement();
			nameProp.setType(eName);
			var value = EMSLFactory.eINSTANCE.createPrimitiveString();
			value.setLiteral(nodeBlock.getType().getName());
			nameProp.setValue(value);
			nameProp.setOp(ConditionOperator.EQ);
			typeNode.getProperties().add(nameProp);

			addElementOfRelation(typeNode, mmNodeBlock);
			nodeBlocks.put(typeNode.getName(), typeNode);
		}

		return nodeBlocks.get(typeName);
	}

}
