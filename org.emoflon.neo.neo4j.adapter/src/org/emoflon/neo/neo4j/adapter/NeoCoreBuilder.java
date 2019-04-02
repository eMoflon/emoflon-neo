package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emoflon.neo.emsl.eMSL.EMSLPackage;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.emsl.eMSL.NodeBlock;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.StatementResult;

public class NeoCoreBuilder implements AutoCloseable {
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	public static final String META_TYPE = "eType";
	public static final String META_EL_OF = "elementOf";

	// EClasses
	private static final String ECLASSIFIER = "EClassifier";
	private static final String ECLASS = "EClass";
	private static final String EATTRIBUTE = "EAttribute";
	private static final String EREFERENCE = "EReference";
	private static final String EDATA_TYPE = "EDataType";
	private static final String ESTRUCTURAL_FEATURE = "EStructuralFeature";
	private static final String ENAMED_ELEMENT = "ENamedElement";
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
					List.of(new NeoStrProp(URI_PROP, ORG_EMOFLON_NEO_CORE)), List.of(METAMODEL));
			var eclass = cb.createNodeWithCont(//
					List.of(new NeoStrProp(NAME_PROP, ECLASS)), List.of(ECLASS), neocore);
			var mmodel = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, METAMODEL)), List.of(ECLASS), eclass, neocore);
			var model1 = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, MODEL)), List.of(ECLASS), eclass, neocore);
			var eobject = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, EOBJECT)), List.of(ECLASS), eclass, neocore);
			var eref = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, EREFERENCE)), List.of(ECLASS), eclass, neocore);
			var eleof = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, META_EL_OF)), List.of(EREFERENCE), eref, neocore);
			var conformto = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, CONFORMS_TO_PROP)), List.of(EREFERENCE), eref, neocore);
			var erefs = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, EREFERENCES)), List.of(EREFERENCE), eref, neocore);
			var eRefType = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, EREFERENCE_TYPE)), List.of(EREFERENCE), eref, neocore);
			var eattr = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, EATTRIBUTE)), List.of(ECLASS), eclass, neocore);
			var name = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, NAME_PROP)), List.of(EATTRIBUTE), eattr, neocore);
			var eDataType = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, EDATA_TYPE)), List.of(ECLASS), eclass, neocore);
			var eAttrEle = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, EATTRIBUTED_ELEMENTS)), List.of(ECLASS), eclass, neocore);
			var eString = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, ESTRING)), List.of(EDATA_TYPE), eDataType, neocore);
			cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, EINT)), List.of(EDATA_TYPE), eDataType, neocore);
			var eAttrType = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, EATTRIBUTE_TYPE)), List.of(EREFERENCE), eref, neocore);
			var eSupType = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, ESUPER_TYPE)), List.of(EREFERENCE), eref, neocore);
			var eclassifier = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, ECLASSIFIER)), List.of(ECLASS), eclass, neocore);
			var eTypedele = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, ETYPED_ELEMENT)), List.of(ECLASS), eclass, neocore);
			var enamedele = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, ENAMED_ELEMENT)), List.of(ECLASS), eclass, neocore);
			var eType = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, META_TYPE)), List.of(EREFERENCE), eref, neocore);
			var eAttributes = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, EATTRIBUTES)), List.of(EREFERENCE), eref, neocore);
			var eStruct = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, ESTRUCTURAL_FEATURE)), List.of(ECLASS), eclass, neocore);
			var abstractattr = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, ABSTRACT_PROP)), List.of(EATTRIBUTE), eattr, neocore);
			var eBoolean = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, EBOOLEAN)), List.of(EDATA_TYPE), eDataType, neocore);

			cb.createEdge(List.of(), CONFORMS_TO_PROP, neocore, neocore);
			cb.createEdge(List.of(), META_TYPE, neocore, mmodel);
			cb.createEdge(List.of(), EREFERENCES, neocore, mmodel);
			cb.createEdge(List.of(), META_TYPE, eclass, eclass);
			cb.createEdge(List.of(), EREFERENCES, eclass, erefs);
			cb.createEdge(List.of(), EREFERENCE_TYPE, erefs, eref);
			cb.createEdge(List.of(), EREFERENCES, eref, eRefType);
			cb.createEdge(List.of(), EREFERENCES, eclass, eSupType);
			cb.createEdge(List.of(), EREFERENCE_TYPE, eSupType, eclass);
			cb.createEdge(List.of(), EATTRIBUTES, enamedele, name);
			cb.createEdge(List.of(), EATTRIBUTE_TYPE, name, eString);
			cb.createEdge(List.of(), EREFERENCES, eattr, eAttrType);
			cb.createEdge(List.of(), EREFERENCE_TYPE, eAttrType, eDataType);
			cb.createEdge(List.of(), EREFERENCES, eTypedele, eType);
			cb.createEdge(List.of(), EREFERENCE_TYPE, eType, eclassifier);
			cb.createEdge(List.of(), EREFERENCES, eAttrEle, eAttributes);
			cb.createEdge(List.of(), EREFERENCE_TYPE, eAttributes, eattr);
			cb.createEdge(List.of(), ESUPER_TYPE, eclass, eAttrEle);
			cb.createEdge(List.of(), ESUPER_TYPE, eref, eAttrEle);
			cb.createEdge(List.of(), ESUPER_TYPE, eref, eStruct);
			cb.createEdge(List.of(), ESUPER_TYPE, eattr, eStruct);
			cb.createEdge(List.of(), ESUPER_TYPE, eTypedele, enamedele);
			cb.createEdge(List.of(), ESUPER_TYPE, eclassifier, enamedele);
			cb.createEdge(List.of(), EATTRIBUTES, eclass, abstractattr);
			cb.createEdge(List.of(), EATTRIBUTE_TYPE, abstractattr, eBoolean);
			cb.createEdge(List.of(), ESUPER_TYPE, mmodel, model1);
			cb.createEdge(List.of(), ESUPER_TYPE, model1, eobject);
			cb.createEdge(List.of(), ESUPER_TYPE, eAttrEle, eobject);
			cb.createEdge(List.of(), ESUPER_TYPE, enamedele, eobject);
			cb.createEdge(List.of(), ESUPER_TYPE, eobject, eTypedele);
			cb.createEdge(List.of(), EREFERENCES, model1, conformto);
			cb.createEdge(List.of(), EREFERENCE_TYPE, conformto, eref);
			cb.createEdge(List.of(), EREFERENCES, eobject, eleof);
			cb.createEdge(List.of(), EREFERENCE_TYPE, eleof, model1);
			cb.createEdge(List.of(), EREFERENCES, conformto, mmodel);
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
			cb.returnWith(cb.matchNode(List.of(new NeoStrProp(URI_PROP, ORG_EMOFLON_NEO_CORE)), List.of(METAMODEL)));
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
					List.of(new NeoStrProp(URI_PROP, ORG_EMOFLON_NEO_CORE)), //
					List.of(METAMODEL));
			var eclass = cb.matchNodeWithContainer(//
					List.of(new NeoStrProp(NAME_PROP, ECLASS)), //
					List.of(ECLASS), neocore);
			var eref = cb.matchNodeWithContainer(//
					List.of(new NeoStrProp(NAME_PROP, EREFERENCE)), //
					List.of(ECLASS), neocore);
			var edatatype = cb.matchNodeWithContainer(//
					List.of(new NeoStrProp(NAME_PROP, EDATA_TYPE)), //
					List.of(ECLASS), neocore);
			var eattribute = cb.matchNodeWithContainer(//
					List.of(new NeoStrProp(NAME_PROP, EATTRIBUTE)), //
					List.of(ECLASS), neocore);
			var model1 = cb.matchNodeWithContainer(//
					List.of(new NeoStrProp(NAME_PROP, MODEL)), //
					List.of(ECLASS), neocore);

			// Create nodes and edges in models
			var mNodes = new HashMap<Model, NodeCommand>();
			var blockToCommand = new HashMap<NodeBlock, NodeCommand>();
			for (var model : newModels) {
				handleNodeBlocksInModel(cb, neocore, eclass, blockToCommand, mNodes, model, model1);
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
					List.of(new NeoStrProp(URI_PROP, ORG_EMOFLON_NEO_CORE)), //
					List.of(METAMODEL));
			var eclass = cb.matchNodeWithContainer(//
					List.of(new NeoStrProp(NAME_PROP, ECLASS)), //
					List.of(ECLASS), neocore);
			var eref = cb.matchNodeWithContainer(//
					List.of(new NeoStrProp(NAME_PROP, EREFERENCE)), //
					List.of(ECLASS), neocore);
			var edatatype = cb.matchNodeWithContainer(//
					List.of(new NeoStrProp(NAME_PROP, EDATA_TYPE)), //
					List.of(ECLASS), neocore);
			var eattribute = cb.matchNodeWithContainer(//
					List.of(new NeoStrProp(NAME_PROP, EATTRIBUTE)), //
					List.of(ECLASS), neocore);
			var mmodel = cb.matchNodeWithContainer(//
					List.of(new NeoStrProp(NAME_PROP, METAMODEL)), //
					List.of(ECLASS), neocore);

			// Create metamodel nodes and handle node blocks for all metamodels
			var mmNodes = new HashMap<Metamodel, NodeCommand>();
			var blockToCommand = new HashMap<NodeBlock, NodeCommand>();
			for (var metamodel : newMetamodels) {
				handleNodeBlocks(cb, neocore, eclass, blockToCommand, mmNodes, metamodel, mmodel);
			}

			// Handle all other features of node blocks
			for (var metamodel : newMetamodels) {
				var mmNode = mmNodes.get(metamodel);
				for (var nb : metamodel.getNodeBlocks()) {
					handleRelationStatement(cb, neocore, eref, edatatype, eattribute, blockToCommand, mmNode, nb);
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
			HashMap<NodeBlock, NodeCommand> blockToCommand, NodeCommand mmNode, NodeBlock nb) {
		for (var ps : nb.getPropertyStatements()) {
			var attr = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, ps.getName())), //
					List.of(EATTRIBUTE), eattribute, mmNode);
			var attrOwner = blockToCommand.get(nb);
			var nameOfTypeofAttr = ps.getValue();
			var typeofattr = cb.matchNodeWithContainer(//
					List.of(new NeoStrProp(NAME_PROP, nameOfTypeofAttr)), //
					List.of(EDATA_TYPE), neocore);
			cb.createEdge(List.of(), EATTRIBUTES, attrOwner, attr);
			cb.createEdge(List.of(), EATTRIBUTE_TYPE, attr, typeofattr);
		}
	}

	private void handleInheritance(CypherCreator cb, HashMap<NodeBlock, NodeCommand> blockToCommand, NodeBlock nb) {
		for (var st : nb.getSuperTypes()) {
			var nodeblock = blockToCommand.get(nb);
			var sType = blockToCommand.get(st);
			cb.createEdge(List.of(), ESUPER_TYPE, nodeblock, sType);
		}
	}

	private void handleRelationStatement(CypherCreator cb, NodeCommand neocore, NodeCommand eref, NodeCommand edatatype,
			NodeCommand eattribute, HashMap<NodeBlock, NodeCommand> blockToCommand, NodeCommand mmNode, NodeBlock nb) {
		for (var rs : nb.getRelationStatements()) {
			var ref = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, rs.getName())), //
					List.of(EREFERENCE), eref, mmNode);

			var refOwner = blockToCommand.get(nb);
			var typeOfRef = blockToCommand.get(rs.getValue());

			cb.createEdge(List.of(), EREFERENCES, refOwner, ref);
			cb.createEdge(List.of(), EREFERENCE_TYPE, ref, typeOfRef);

			// Handle attributes of the relation
			rs.getPropertyStatements().forEach(ps -> {
				var attr = cb.createNodeWithContAndType(//
						List.of(new NeoStrProp(NAME_PROP, ps.getName())), //
						List.of(EATTRIBUTE), eattribute, mmNode);

				var nameOfTypeofAttr = ps.getValue();

				var typeofattr = cb.matchNodeWithContainer(//
						List.of(new NeoStrProp(NAME_PROP, nameOfTypeofAttr)), //
						List.of(EDATA_TYPE), neocore);

				cb.createEdge(List.of(), EATTRIBUTES, ref, attr);
				cb.createEdge(List.of(), EATTRIBUTE_TYPE, attr, typeofattr);
			});
		}
	}

	private void handleRelationStatementInModel(CypherCreator cb, NodeCommand neocore, NodeCommand eref,
			NodeCommand edatatype, NodeCommand eattribute, HashMap<NodeBlock, NodeCommand> blockToCommand,
			NodeCommand mNode, NodeBlock nb) {

		for (var rs : nb.getRelationStatements()) {

			var refOwner = blockToCommand.get(nb);
			var typeOfRef = blockToCommand.get(rs.getValue());

			// Handle attributes of relation in model
			List<NeoProp> props = new ArrayList<>();
			rs.getPropertyStatements().forEach(ps -> {
				props.add(new NeoStrProp(ps.getName(), ps.getValue()));
			});

			cb.createEdge(props, rs.getName(), refOwner, typeOfRef);
		}
	}

	private void handleNodeBlocks(CypherCreator cb, NodeCommand neocore, NodeCommand eclass,
			HashMap<NodeBlock, NodeCommand> blockToCommand, HashMap<Metamodel, NodeCommand> mmNodes,
			Metamodel metamodel, NodeCommand mmodel) {

		var mmNode = cb.createNode(List.of(new NeoStrProp(URI_PROP, metamodel.getName())), List.of(METAMODEL));

		mmNodes.put(metamodel, mmNode);

		cb.createEdge(List.of(), CONFORMS_TO_PROP, mmNode, neocore);
		cb.createEdge(List.of(), META_TYPE, mmNode, mmodel);

		metamodel.getNodeBlocks().forEach(nb -> {
			var nbNode = cb.createNodeWithContAndType(//
					List.of(new NeoStrProp(NAME_PROP, nb.getName()),
							new NeoProp(ABSTRACT_PROP, String.valueOf(nb.isAbstract()))),
					List.of(ECLASS), eclass, mmNode);

			blockToCommand.put(nb, nbNode);
		});
	}

	private void handleNodeBlocksInModel(CypherCreator cb, NodeCommand neocore, NodeCommand eclass,
			HashMap<NodeBlock, NodeCommand> blockToCommand, HashMap<Model, NodeCommand> mNodes, Model model,
			NodeCommand model1) {

		var mNode = cb.createNode(List.of(new NeoStrProp(URI_PROP, model.getName())), List.of(MODEL));

		mNodes.put(model, mNode);

		model.getNodeBlocks().forEach(nb -> {
			Metamodel mm = (Metamodel) nb.getType().eContainer();

			var mmNode = cb.matchNode(List.of(new NeoStrProp(URI_PROP, mm.getName())), List.of(METAMODEL));

			var typeOfNode = cb.matchNodeWithContainer(//
					List.of(new NeoStrProp(NAME_PROP, nb.getType().getName())), //
					List.of(ECLASS), mmNode);

			cb.createEdge(List.of(), CONFORMS_TO_PROP, mNode, mmNode);
			cb.createEdge(List.of(), META_TYPE, mNode, model1);

			// Handle attributes of model
			List<NeoProp> props = new ArrayList<>();
			nb.getPropertyStatements().forEach(ps -> {
				props.add(new NeoStrProp(ps.getName(), ps.getValue()));
			});

			var nbNode = cb.createNodeWithContAndType(//
					props, List.of(nb.getName(), nb.getType().getName()), typeOfNode, mNode);
			blockToCommand.put(nb, nbNode);
		});
	}
}