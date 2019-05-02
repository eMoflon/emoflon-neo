package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emoflon.neo.emsl.eMSL.BuiltInDataTypes;
import org.emoflon.neo.emsl.eMSL.BuiltInType;
import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.emsl.eMSL.EMSLPackage;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock;
import org.emoflon.neo.emsl.eMSL.MetamodelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.StatementResult;

import com.google.common.collect.Lists;

public class NeoCoreBuilder implements AutoCloseable {
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	public static final String META_TYPE = "metaType";
	public static final String META_EL_OF = "elementOf";

	// EClasses
	private static final String ECLASSIFIER = "EClassifier";
	private static final String ECLASS = "EClass";
	private static final String EATTRIBUTE = "EAttribute";
	private static final String EREFERENCE = "EReference";
	private static final String EDATA_TYPE = "EDataType";
	private static final String ESTRUCTURAL_FEATURE = "EStructuralFeature";
	private static final String ETYPED_ELEMENT = "ETypedElement";
	private static final String EATTRIBUTED_ELEMENTS = "EAttributedElement";
	private static final String METAMODEL = "MetaModel";
	private static final String MODEL = "Model";
	private static final String EOBJECT = "EObject";

	// EReferences
	private static final String EREFERENCE_TYPE = "eReferenceType";
	private static final String EREFERENCES = "eReferences";
	private static final String ESUPER_TYPE = "eSuperType";
	private static final String EATTRIBUTE_TYPE = "eAttributeType";
	private static final String EATTRIBUTES = "eAttributes";

	// EDataTypes
	private static final String ESTRING = "EString";
	private static final String EINT = "EInt";
	private static final String EBOOLEAN = "EBoolean";

	// Attributes
	private static final String NAME_PROP = "name";
	private static final String ABSTRACT_PROP = "abstract";

	// Meta attributes and relations
	private static final String ORG_EMOFLON_NEO_CORE = "org.emoflon.neo.NeoCore";
	private static final String CONFORMS_TO_PROP = "conformsTo";
	private static final String URI_PROP = "_uri_";

	private int maxTransactionSizeEdges = 10000;
	private int maxTransactionSizeNodes = 10000;

	public void setMaxTransactionSize(int nodes, int edges) {
		maxTransactionSizeNodes = nodes;
		maxTransactionSizeEdges = edges;
	}

	private final Driver driver;

	public NeoCoreBuilder(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}

	public Driver getDriver() {
		return driver;
	}

	@Override
	public void close() throws Exception {
		driver.close();
	}

	public void exportEMSLEntityToNeo4j(EObject entity) {
		bootstrapNeoCoreIfNecessary();

		ResourceSet rs = entity.eResource().getResourceSet();
		EcoreUtil.resolveAll(rs);

		var metamodels = new ArrayList<Metamodel>();
		var models = new ArrayList<Model>();
		rs.getAllContents().forEachRemaining(c -> {
			if (c instanceof Metamodel)
				metamodels.add((Metamodel) c);
			if (c instanceof Model)
				models.add((Model) c);
		});

		var metamodelNames = metamodels.stream().map(Metamodel::getName).collect(Collectors.joining(","));
		logger.info("Trying to export metamodels: " + metamodelNames);
		var newMetamodels = removeExistingMetamodels(metamodels);

		for (Metamodel mm : metamodels) {
			if (!newMetamodels.contains(mm))
				logger.info("Skipping metamodel " + mm.getName() + " as it is already present.");
		}

		if (!newMetamodels.isEmpty())
			exportMetamodelsToNeo4j(newMetamodels);
		logger.info("Exported metamodels.");

		var modelNames = models.stream().map(Model::getName).collect(Collectors.joining(","));
		logger.info("Trying to export models: " + modelNames);
		var newModels = removeExistingModels(models);

		for (Model m : models) {
			if (!newModels.contains(m))
				logger.info("Skipping model " + m.getName() + " as it is already present.");
		}

		if (!newModels.isEmpty())
			exportModelsToNeo4j(newModels);
		logger.info("Exported models.");
	}

	public static boolean canBeExported(EClass eclass) {
		return eclass.equals(EMSLPackage.eINSTANCE.getMetamodel()) || eclass.equals(EMSLPackage.eINSTANCE.getModel());
	}

	private void bootstrapNeoCore() {
		executeActionAsCreateTransaction((cb) -> {
			var neocore = cb.createNode(//
					List.of(new NeoProp(URI_PROP, ORG_EMOFLON_NEO_CORE)), List.of(METAMODEL));
			var eclass = cb.createNodeWithCont(//
					List.of(new NeoProp(NAME_PROP, ECLASS)),
					List.of(ECLASS, EOBJECT, ECLASSIFIER, EATTRIBUTED_ELEMENTS), neocore);
			var mmodel = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, METAMODEL)), List.of(ECLASS, EOBJECT), eclass, neocore);
			var model = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, MODEL)), List.of(ECLASS, EOBJECT), eclass, neocore);
			var eobject = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, EOBJECT)), List.of(ECLASS, ECLASSIFIER, EATTRIBUTED_ELEMENTS),
					eclass, neocore);
			var eref = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, EREFERENCE)),
					List.of(ECLASS, EATTRIBUTED_ELEMENTS, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT, EOBJECT), eclass,
					neocore);
			var eleof = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, META_EL_OF)),
					List.of(EREFERENCE, EATTRIBUTED_ELEMENTS, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT, EOBJECT), eref,
					neocore);
			var conformto = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, CONFORMS_TO_PROP)),
					List.of(EREFERENCE, EATTRIBUTED_ELEMENTS, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT, EOBJECT), eref,
					neocore);
			var erefs = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, EREFERENCES)),
					List.of(EREFERENCE, EATTRIBUTED_ELEMENTS, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT, EOBJECT), eref,
					neocore);
			var eRefType = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, EREFERENCE_TYPE)),
					List.of(EREFERENCE, EATTRIBUTED_ELEMENTS, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT, EOBJECT), eref,
					neocore);
			var eattr = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, EATTRIBUTE)),
					List.of(ECLASS, EOBJECT, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT), eclass, neocore);
			var name = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, NAME_PROP)),
					List.of(EATTRIBUTE, EOBJECT, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT), eattr, neocore);
			var eDataType = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, EDATA_TYPE)), List.of(EDATA_TYPE, EOBJECT, ECLASSIFIER), eclass,
					neocore);
			var eAttrEle = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, EATTRIBUTED_ELEMENTS)), List.of(ECLASS, EOBJECT), eclass, neocore);
			var eString = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, ESTRING)), List.of(EDATA_TYPE, ECLASSIFIER, EOBJECT), eDataType,
					neocore);
			cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, EINT)), List.of(EDATA_TYPE, ECLASSIFIER, EOBJECT), eDataType,
					neocore);
			var eAttrType = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, EATTRIBUTE_TYPE)),
					List.of(EREFERENCE, EATTRIBUTED_ELEMENTS, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT, EOBJECT), eref,
					neocore);
			var eSupType = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, ESUPER_TYPE)),
					List.of(EREFERENCE, EATTRIBUTED_ELEMENTS, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT, EOBJECT), eref,
					neocore);
			var eclassifier = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, ECLASSIFIER)), List.of(ECLASS, EOBJECT), eclass, neocore);
			var eTypedele = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, ETYPED_ELEMENT)), List.of(ECLASS, EOBJECT), eclass, neocore);
			var metaType = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, META_TYPE)),
					List.of(EREFERENCE, EATTRIBUTED_ELEMENTS, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT, EOBJECT), eref,
					neocore);
			var eAttributes = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, EATTRIBUTES)),
					List.of(EREFERENCE, EATTRIBUTED_ELEMENTS, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT, EOBJECT), eref,
					neocore);
			var eStruct = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, ESTRUCTURAL_FEATURE)), List.of(ECLASS, ETYPED_ELEMENT), eclass,
					neocore);
			var abstractattr = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, ABSTRACT_PROP)),
					List.of(EATTRIBUTE, EOBJECT, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT), eattr, neocore);
			var eBoolean = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, EBOOLEAN)), List.of(EDATA_TYPE, ECLASSIFIER, EOBJECT), eDataType,
					neocore);

			cb.createEdge(CONFORMS_TO_PROP, neocore, neocore);
			cb.createEdge(META_TYPE, neocore, mmodel);
			cb.createEdge(META_TYPE, eclass, eclass);
			cb.createEdge(EREFERENCES, eclass, erefs);
			cb.createEdge(EREFERENCE_TYPE, erefs, eref);
			cb.createEdge(EREFERENCES, eref, eRefType);
			cb.createEdge(EREFERENCES, eclass, eSupType);
			cb.createEdge(EREFERENCE_TYPE, eSupType, eclass);
			cb.createEdge(EATTRIBUTES, eobject, name);
			cb.createEdge(EATTRIBUTE_TYPE, name, eString);
			cb.createEdge(EREFERENCES, eattr, eAttrType);
			cb.createEdge(EREFERENCE_TYPE, eAttrType, eDataType);
			cb.createEdge(EREFERENCES, eobject, metaType);
			cb.createEdge(EREFERENCE_TYPE, metaType, eobject);
			cb.createEdge(EREFERENCES, eAttrEle, eAttributes);
			cb.createEdge(EREFERENCE_TYPE, eAttributes, eattr);
			cb.createEdge(ESUPER_TYPE, eclass, eAttrEle);
			cb.createEdge(ESUPER_TYPE, eref, eAttrEle);
			cb.createEdge(ESUPER_TYPE, eref, eStruct);
			cb.createEdge(ESUPER_TYPE, eattr, eStruct);
			cb.createEdge(ESUPER_TYPE, eTypedele, eobject);
			cb.createEdge(ESUPER_TYPE, eclassifier, eobject);
			cb.createEdge(EATTRIBUTES, eclass, abstractattr);
			cb.createEdge(EATTRIBUTE_TYPE, abstractattr, eBoolean);
			cb.createEdge(ESUPER_TYPE, mmodel, model);
			cb.createEdge(ESUPER_TYPE, model, eobject);
			cb.createEdge(ESUPER_TYPE, eAttrEle, eobject);
			cb.createEdge(ESUPER_TYPE, eDataType, eobject);
			cb.createEdge(ESUPER_TYPE, eStruct, eobject);
			cb.createEdge(EREFERENCES, model, conformto);
			cb.createEdge(EREFERENCE_TYPE, conformto, mmodel);
			cb.createEdge(EREFERENCES, eobject, eleof);
			cb.createEdge(EREFERENCE_TYPE, eleof, model);
		});
	}

	private void executeActionAsCreateTransaction(Consumer<CypherCreator> action) {
		var creator = new CypherCreator(maxTransactionSizeNodes, maxTransactionSizeEdges);
		action.accept(creator);
		creator.run(driver);
	}

	private StatementResult executeActionAsMatchTransaction(Consumer<CypherNodeMatcher> action) {
		var matcher = new CypherNodeMatcher();
		action.accept(matcher);
		return matcher.run(driver);
	}

	private boolean ecoreIsNotPresent() {
		var result = executeActionAsMatchTransaction(cb -> {
			cb.returnWith(cb.matchNode(List.of(new NeoProp(URI_PROP, ORG_EMOFLON_NEO_CORE)), List.of(METAMODEL)));
		});

		return result.stream().count() == 0;
	}

	private void bootstrapNeoCoreIfNecessary() {
		if (ecoreIsNotPresent()) {
			logger.info("Trying to bootstrap NeoCore...");
			bootstrapNeoCore();
			logger.info("Done.");
		} else {
			logger.info("NeoCore is already present.");
		}
	}

	private void exportModelsToNeo4j(List<Model> newModels) {
		executeActionAsCreateTransaction((cb) -> {
			// Match required classes from NeoCore
			var neocore = cb.matchNode(//
					List.of(new NeoProp(URI_PROP, ORG_EMOFLON_NEO_CORE)), //
					List.of(METAMODEL));
			var eclass = cb.matchNodeWithContainer(//
					List.of(new NeoProp(NAME_PROP, ECLASS)), //
					List.of(ECLASS, ECLASSIFIER, EOBJECT, EATTRIBUTED_ELEMENTS), neocore);
			var eref = cb.matchNodeWithContainer(//
					List.of(new NeoProp(NAME_PROP, EREFERENCE)), //
					List.of(ECLASS, EATTRIBUTED_ELEMENTS, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT), neocore);
			var edatatype = cb.matchNodeWithContainer(//
					List.of(new NeoProp(NAME_PROP, EDATA_TYPE)), //
					List.of(ECLASSIFIER, EOBJECT), neocore);
			var eattribute = cb.matchNodeWithContainer(//
					List.of(new NeoProp(NAME_PROP, EATTRIBUTE)), //
					List.of(ECLASS, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT), neocore);
			var model1 = cb.matchNodeWithContainer(//
					List.of(new NeoProp(NAME_PROP, MODEL)), //
					List.of(ECLASS, EOBJECT), neocore);
			var eobject = cb.matchNodeWithContainer(//
					List.of(new NeoProp(NAME_PROP, EOBJECT)), //
					List.of(ECLASS, ECLASSIFIER, EATTRIBUTED_ELEMENTS), neocore);

			// Create nodes and edges in models
			var mNodes = new HashMap<Model, NodeCommand>();
			var blockToCommand = new HashMap<ModelNodeBlock, NodeCommand>();
			for (var model : newModels) {
				handleNodeBlocksInModel(cb, neocore, eclass, blockToCommand, mNodes, model, model1, eobject);
			}
			for (var model : newModels) {
				var mNode = mNodes.get(model);
				for (var nb : model.getNodeBlocks()) {
					handleRelationStatementInModel(cb, neocore, eref, edatatype, eattribute, blockToCommand, mNode, nb);
				}
			}
		});
	}

	private void exportMetamodelsToNeo4j(List<Metamodel> newMetamodels) {
		executeActionAsCreateTransaction((cb) -> {
			// Match required classes from NeoCore
			var neocore = cb.matchNode(//
					List.of(new NeoProp(URI_PROP, ORG_EMOFLON_NEO_CORE)), //
					List.of(METAMODEL));
			var eclass = cb.matchNodeWithContainer(//
					List.of(new NeoProp(NAME_PROP, ECLASS)), //
					List.of(ECLASS, ECLASSIFIER, EOBJECT, EATTRIBUTED_ELEMENTS), neocore);
			var eref = cb.matchNodeWithContainer(//
					List.of(new NeoProp(NAME_PROP, EREFERENCE)), //
					List.of(ECLASS, EATTRIBUTED_ELEMENTS, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT), neocore);
			var edatatype = cb.matchNodeWithContainer(//
					List.of(new NeoProp(NAME_PROP, EDATA_TYPE)), //
					List.of(ECLASSIFIER, EOBJECT), neocore);
			var eattribute = cb.matchNodeWithContainer(//
					List.of(new NeoProp(NAME_PROP, EATTRIBUTE)), //
					List.of(ECLASS, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT), neocore);
			var mmodel = cb.matchNodeWithContainer(//
					List.of(new NeoProp(NAME_PROP, METAMODEL)), //
					List.of(ECLASS, EOBJECT), neocore);
			var eobject = cb.matchNodeWithContainer(//
					List.of(new NeoProp(NAME_PROP, EOBJECT)), //
					List.of(ECLASS, ECLASSIFIER, EATTRIBUTED_ELEMENTS), neocore);

			// Create metamodel nodes and handle node blocks for all metamodels
			var mmNodes = new HashMap<Metamodel, NodeCommand>();
			var blockToCommand = new HashMap<Object, NodeCommand>();
			for (var metamodel : newMetamodels) {
				handleNodeBlocksInMetaModel(cb, neocore, eclass, blockToCommand, mmNodes, metamodel, mmodel, eobject);
			}

			// Handle all other features of node blocks
			for (var metamodel : newMetamodels) {
				var mmNode = mmNodes.get(metamodel);
				for (var nb : metamodel.getNodeBlocks()) {
					handleRelationStatementInMetaModel(cb, neocore, eref, edatatype, eattribute, blockToCommand, mmNode,
							nb);
					handleInheritance(cb, blockToCommand, nb);
					handleAttributes(cb, neocore, edatatype, eattribute, blockToCommand, mmNode, nb);
				}
			}
		});
	}

	private List<Metamodel> removeExistingMetamodels(List<Metamodel> metamodels) {
		var newMetamodels = new ArrayList<Metamodel>();
		newMetamodels.addAll(metamodels);
		StatementResult result = executeActionAsMatchTransaction(cb -> {
			var nc = cb.matchNode(List.of(), List.of(METAMODEL));
			cb.returnWith(nc);
		});
		result.forEachRemaining(
				mmNode -> newMetamodels.removeIf(mm -> mm.getName().equals(mmNode.get(0).get(URI_PROP).asString())));
		return newMetamodels;
	}

	private List<Model> removeExistingModels(List<Model> models) {
		var newModels = new ArrayList<Model>();
		newModels.addAll(models);
		StatementResult result = executeActionAsMatchTransaction(cb -> {
			var nc = cb.matchNode(List.of(), List.of(MODEL));
			cb.returnWith(nc);
		});
		result.forEachRemaining(
				mmNode -> newModels.removeIf(mm -> mm.getName().equals(mmNode.get(0).get(URI_PROP).asString())));
		return newModels;
	}

	private void handleAttributes(CypherCreator cb, NodeCommand neocore, NodeCommand edatatype, NodeCommand eattribute,
			HashMap<Object, NodeCommand> blockToCommand, NodeCommand mmNode, MetamodelNodeBlock nb) {
		for (var ps : nb.getProperties()) {
			var attr = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, ps.getName())), //
					List.of(EATTRIBUTE, EOBJECT, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT), eattribute, mmNode);
			var attrOwner = blockToCommand.get(nb);
			var nameOfTypeofAttr = inferType(ps);
			var typeofattr = cb.matchNodeWithContainer(//
					List.of(new NeoProp(NAME_PROP, nameOfTypeofAttr)), //
					List.of(EDATA_TYPE, ECLASSIFIER, EOBJECT), neocore);
			cb.createEdge(EATTRIBUTES, attrOwner, attr);
			cb.createEdge(EATTRIBUTE_TYPE, attr, typeofattr);
		}
	}

	private String inferType(MetamodelPropertyStatement ps) {
		return ((BuiltInType) ps.getType()).getReference().getLiteral();
	}

	private void handleInheritance(CypherCreator cb, HashMap<Object, NodeCommand> blockToCommand,
			MetamodelNodeBlock nb) {
		for (var st : nb.getSuperTypes()) {
			var nodeblock = blockToCommand.get(nb);
			var sType = blockToCommand.get(st);
			cb.createEdge(ESUPER_TYPE, nodeblock, sType);
		}
	}

	private void handleRelationStatementInMetaModel(CypherCreator cb, NodeCommand neocore, NodeCommand eref,
			NodeCommand edatatype, NodeCommand eattribute, HashMap<Object, NodeCommand> blockToCommand,
			NodeCommand mmNode, MetamodelNodeBlock nb) {
		for (var rs : nb.getRelations()) {
			var ref = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, rs.getName())), //
					List.of(EREFERENCE, EOBJECT, EATTRIBUTED_ELEMENTS, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT), eref,
					mmNode);

			var refOwner = blockToCommand.get(nb);
			var typeOfRef = blockToCommand.get(rs.getTarget());

			cb.createEdge(EREFERENCES, refOwner, ref);
			cb.createEdge(EREFERENCE_TYPE, ref, typeOfRef);

			// Handle attributes of the relation
			rs.getProperties().forEach(ps -> {
				var attr = cb.createNodeWithContAndType(//
						List.of(new NeoProp(NAME_PROP, ps.getName())), //
						List.of(EATTRIBUTE, EOBJECT, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT), eattribute, mmNode);

				var nameOfTypeofAttr = inferType(ps);

				var typeofattr = cb.matchNodeWithContainer(//
						List.of(new NeoProp(NAME_PROP, nameOfTypeofAttr)), //
						List.of(EDATA_TYPE, ECLASSIFIER, EOBJECT), neocore);

				cb.createEdge(EATTRIBUTES, ref, attr);
				cb.createEdge(EATTRIBUTE_TYPE, attr, typeofattr);
			});
		}
	}

	private void handleRelationStatementInModel(CypherCreator cb, NodeCommand neocore, NodeCommand eref,
			NodeCommand edatatype, NodeCommand eattribute, HashMap<ModelNodeBlock, NodeCommand> blockToCommand,
			NodeCommand mNode, ModelNodeBlock nb) {

		for (var rs : nb.getRelations()) {

			var refOwner = blockToCommand.get(nb);
			var typeOfRef = blockToCommand.get(rs.getTarget());

			// Handle attributes of relation in model
			List<NeoProp> props = new ArrayList<>();
			rs.getProperties().forEach(ps -> {
				props.add(new NeoProp(ps.getType().getName(), inferType(ps, nb)));
			});

			cb.createEdgeWithProps(props, rs.getType().getName(), refOwner, typeOfRef);
		}
	}

	private void handleNodeBlocksInMetaModel(CypherCreator cb, NodeCommand neocore, NodeCommand eclass,
			HashMap<Object, NodeCommand> blockToCommand, HashMap<Metamodel, NodeCommand> mmNodes, Metamodel metamodel,
			NodeCommand mmodel, NodeCommand eobject) {

		var mmNode = cb.createNode(List.of(new NeoProp(URI_PROP, metamodel.getName())), List.of(METAMODEL));

		mmNodes.put(metamodel, mmNode);

		cb.createEdge(CONFORMS_TO_PROP, mmNode, neocore);
		cb.createEdge(META_TYPE, mmNode, mmodel);

		metamodel.getNodeBlocks().forEach(nb -> {

			var nbNode = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, nb.getName()), //
							new NeoProp(ABSTRACT_PROP, nb.isAbstract())),
					List.of(ECLASS, EOBJECT), eclass, mmNode);

			if (nb.getSuperTypes().isEmpty()) {
				cb.createEdge(ESUPER_TYPE, nbNode, eobject);
			}

			blockToCommand.put(nb, nbNode);
		});
	}

	private void handleNodeBlocksInModel(CypherCreator cb, NodeCommand neocore, NodeCommand eclass,
			HashMap<ModelNodeBlock, NodeCommand> blockToCommand, HashMap<Model, NodeCommand> mNodes, Model model,
			NodeCommand nodeCommandForModel, NodeCommand eobject) {

		var mNode = cb.createNode(List.of(new NeoProp(URI_PROP, model.getName())), List.of(MODEL));

		mNodes.put(model, mNode);

		model.getNodeBlocks().forEach(nb -> {
			Metamodel mm = (Metamodel) nb.getType().eContainer();

			var mmNode = cb.matchNode(List.of(new NeoProp(URI_PROP, mm.getName())), List.of(METAMODEL));

			var typeOfNode = cb.matchNodeWithContainer(//
					List.of(new NeoProp(NAME_PROP, nb.getType().getName())), //
					List.of(ECLASS), mmNode);

			cb.createEdge(CONFORMS_TO_PROP, mNode, mmNode);
			cb.createEdge(META_TYPE, mNode, nodeCommandForModel);

			// Handle attributes of model
			List<NeoProp> props = new ArrayList<>();
			nb.getProperties().forEach(ps -> {
				props.add(new NeoProp(ps.getType().getName(), inferType(ps, nb)));
			});

			props.add(new NeoProp(NAME_PROP, nb.getName()));

			var allLabels = new ArrayList<String>();
			allLabels.addAll(computeLabelsFromType(nb.getType()));
			allLabels.add(EOBJECT);

			var nbNode = cb.createNodeWithContAndType(//
					props, allLabels, typeOfNode, mNode);

			blockToCommand.put(nb, nbNode);
		});

	}

	private List<String> computeLabelsFromType(MetamodelNodeBlock type) {
		var labels = new LinkedHashSet<String>();
		labels.add(type.getName());
		for (MetamodelNodeBlock st : type.getSuperTypes()) {
			labels.addAll(computeLabelsFromType(st));
		}

		logger.debug("Computed labels: " + labels);

		return Lists.newArrayList(labels);
	}

	private Object inferType(ModelPropertyStatement ps, ModelNodeBlock nb) {
		String stringVal = ps.getValue();
		ModelRelationStatement rs = (ModelRelationStatement) ps.eContainer();
		String relName = rs.getType().getName();
		String propName = ps.getType().getName();
		MetamodelNodeBlock nodeType = nb.getType();

		if (ps.eContainer().equals(nb))
			return inferTypeForNodeAttribute(stringVal, propName, nodeType);
		else
			return inferTypeForEdgeAttribute(stringVal, relName, propName, nodeType);
	}

	private Object inferTypeForEdgeAttribute(String stringVal, String relName, String propName,
			MetamodelNodeBlock nodeType) {
		var typedValue = nodeType.getRelations().stream()//
				.filter(et -> et.getName().equals(relName))//
				.flatMap(et -> et.getProperties().stream())//
				.filter(etPs -> etPs.getName().equals(propName))//
				.map(etPs -> etPs.getType())//
				.map(t -> parseStringWithType(stringVal, t))//
				.findAny();

		return typedValue.orElse(stringVal);
	}

	private Object inferTypeForNodeAttribute(String stringVal, String propName, MetamodelNodeBlock nodeType) {
		var typedValue = nodeType.getProperties().stream()//
				.filter(t -> t.getName().equals(propName))//
				.map(psType -> psType.getType())//
				.map(t -> parseStringWithType(stringVal, t))//
				.findAny();

		return typedValue.orElse(stringVal);
	}

	private Object parseStringWithType(String stringVal, DataType type) {
		// TODO[Tony]: How about enums?

		BuiltInDataTypes builtInType = ((BuiltInType) type).getReference();

		switch (builtInType) {
		case EINT:
			return Integer.parseInt(stringVal);
		case EBOOLEAN:
			return Boolean.parseBoolean(stringVal);
		// TODO[Tony]: Handle other literals!
		default:
			return null;
		}
	}
}
