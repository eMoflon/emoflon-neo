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
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBootstrapper;

public class RulePreProcessor {
	Map<String, ModelNodeBlock> nodeBlocks;
	private MetamodelNodeBlock eClass;
	private MetamodelPropertyStatement eName;
	private MetamodelPropertyStatement eNamespace;
	private MetamodelRelationStatement mtRel;

	public RulePreProcessor(List<ModelNodeBlock> nodeBlocks) {
		this.nodeBlocks = new HashMap<>();
		nodeBlocks.forEach(nb -> this.nodeBlocks.putIfAbsent(nb.getName(), nb));

		var neocore = EMSLFactory.eINSTANCE.createMetamodel();
		neocore.setName(EMSLUtil.ORG_EMOFLON_NEO_CORE);

		eClass = EMSLFactory.eINSTANCE.createMetamodelNodeBlock();
		eClass.setName(NeoCoreBootstrapper.ECLASS);
		neocore.getNodeBlocks().add(eClass);

		{
			eName = EMSLFactory.eINSTANCE.createMetamodelPropertyStatement();
			eName.setName(NeoCoreBootstrapper.NAME_PROP);
			var estring = EMSLFactory.eINSTANCE.createBuiltInType();
			estring.setReference(BuiltInDataTypes.ESTRING);
			eName.setType(estring);
			eClass.getProperties().add(eName);
		}

		{
			eNamespace = EMSLFactory.eINSTANCE.createMetamodelPropertyStatement();
			eNamespace.setName(NeoCoreBootstrapper.NAMESPACE_PROP);
			var estring = EMSLFactory.eINSTANCE.createBuiltInType();
			eName.setType(estring);
			eClass.getProperties().add(eNamespace);
		}

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
			if (nodeBlock.getAction() != null && nodeBlock.getAction().getOp().equals(ActionOperator.CREATE)) {
				var typeNode = addTypeNodeIfNecessary(nodeBlock);
				addGreenMetaTypeEdge(nodeBlock, typeNode);
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

	private void addGreenMetaTypeEdge(ModelNodeBlock nodeBlock, ModelNodeBlock typeNode) {
		var mt = addBlackMetaTypeEdge(nodeBlock, typeNode);
		var action = EMSLFactory.eINSTANCE.createAction();
		action.setOp(ActionOperator.CREATE);
		mt.setAction(action);
	}

	private ModelNodeBlock addTypeNodeIfNecessary(ModelNodeBlock nodeBlock) {
		var mm = (Metamodel) nodeBlock.getType().eContainer();
		var mmName = mm.getName();
		var typeName = "__" + mmName + "__" + nodeBlock.getType().getName();

		if (!nodeBlocks.containsKey(typeName)) {
			var typeNode = EMSLFactory.eINSTANCE.createModelNodeBlock();
			typeNode.setType(eClass);
			typeNode.setName(typeName);

			{
				var nameProp = EMSLFactory.eINSTANCE.createModelPropertyStatement();
				nameProp.setType(eName);
				var value = EMSLFactory.eINSTANCE.createPrimitiveString();
				value.setLiteral(nodeBlock.getType().getName());
				nameProp.setValue(value);
				nameProp.setOp(ConditionOperator.EQ);
				typeNode.getProperties().add(nameProp);
			}

			{
				var namespaceProp = EMSLFactory.eINSTANCE.createModelPropertyStatement();
				namespaceProp.setType(eNamespace);
				var value = EMSLFactory.eINSTANCE.createPrimitiveString();
				value.setLiteral(mmName);
				namespaceProp.setValue(value);
				namespaceProp.setOp(ConditionOperator.EQ);
				typeNode.getProperties().add(namespaceProp);
			}

			nodeBlocks.put(typeNode.getName(), typeNode);
		}

		return nodeBlocks.get(typeName);
	}

}
