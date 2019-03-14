package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

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
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

public class NeoCoreBuilder implements AutoCloseable {
	public static final String META_TYPE = "_type_";
	public static final String META_EL_OF = "_elementOf_";

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

	// EReferences
	private static final String EREFERENCE_TYPE = "eReferenceType";
	private static final String EREFERENCES = "eReferences";
	private static final String ESUPER_TYPE = "eSuperType";
	private static final String EATTRIBUTE_TYPE = "eAttributeType";
	private static final String ETYPE = "eType";
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
	private static final String CONFORMS_TO_PROP = "_conformsTo_";
	private static final String URI_PROP = "_uri_";
	private static final String METAMODEL = "_Metamodel_";
	private static final String MODEL = "_Model_";

	private final Driver driver;

	public NeoCoreBuilder(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}

	@Override
	public void close() throws Exception {
		driver.close();
	}

	public void exportEMSLEntityToNeo4j(EObject entity, Consumer<String> logger) {
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
		logger.accept("Trying to export metamodels: " + metamodelNames);
		var newMetamodels = removeExistingMetamodels(metamodels);

		for (Metamodel mm : metamodels) {
			if (!newMetamodels.contains(mm))
				logger.accept("Skipping metamodel " + mm.getName() + " as it is already present.");
		}

		if (!newMetamodels.isEmpty())
			exportMetamodelsToNeo4j(newMetamodels);
		logger.accept("Exported metamodels.");

		var modelNames = models.stream().map(Model::getName).collect(Collectors.joining(","));
		logger.accept("Trying to export models: " + modelNames);
		var newModels = removeExistingModels(models);

		for (Model m : models) {
			if (!newModels.contains(m))
				logger.accept("Skipping model " + m.getName() + " as it is already present.");
		}

		if (!newModels.isEmpty())
			exportModelsToNeo4j(newModels);
		logger.accept("Exported models.");
	}

	public void bootstrapNeoCore() {
		executeActionAsTransaction((cb) -> {
			var neocore = cb.createNode(List.of(new NeoStringProperty(URI_PROP, ORG_EMOFLON_NEO_CORE)),
					List.of(METAMODEL), null, null);

			var eclass = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, ECLASS)), List.of(ECLASS), null,
					neocore);

			var eref = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, EREFERENCE)), List.of(ECLASS), eclass,
					neocore);

			var erefs = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, EREFERENCES)), List.of(EREFERENCE), eref,
					neocore);

			var eRefType = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, EREFERENCE_TYPE)),
					List.of(EREFERENCE), eref, neocore);

			var eattr = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, EATTRIBUTE)), List.of(ECLASS), eclass,
					neocore);

			var name = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, NAME_PROP)), List.of(EATTRIBUTE), eattr,
					neocore);

			var eDataType = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, EDATA_TYPE)), List.of(ECLASS),
					eclass, neocore);

			var eAttrEle = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, EATTRIBUTED_ELEMENTS)),
					List.of(ECLASS), eclass, neocore);

			var eString = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, ESTRING)), List.of(EDATA_TYPE),
					eDataType, neocore);

			cb.createNode(List.of(new NeoStringProperty(NAME_PROP, EINT)), List.of(EDATA_TYPE), eDataType, neocore);

			var eAttrType = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, EATTRIBUTE_TYPE)),
					List.of(EREFERENCE), eref, neocore);

			var eSupType = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, ESUPER_TYPE)), List.of(EREFERENCE),
					eref, neocore);

			var eclassifier = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, ECLASSIFIER)), List.of(ECLASS),
					eclass, neocore);

			var eTypedele = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, ETYPED_ELEMENT)), List.of(ECLASS),
					eclass, neocore);

			var enamedele = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, ENAMED_ELEMENT)), List.of(ECLASS),
					eclass, neocore);

			var eType = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, ETYPE)), List.of(EREFERENCE), eref,
					neocore);

			var eAttributes = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, EATTRIBUTES)), List.of(EREFERENCE),
					eref, neocore);

			var eStruct = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, ESTRUCTURAL_FEATURE)), List.of(ECLASS),
					eclass, neocore);

			var abstractattr = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, ABSTRACT_PROP)),
					List.of(EATTRIBUTE), eattr, neocore);

			var eBoolean = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, EBOOLEAN)), List.of(EDATA_TYPE),
					eDataType, neocore);

			cb.createEdge(List.of(), CONFORMS_TO_PROP, neocore, neocore);
			cb.createEdge(List.of(), NAME_PROP, eclass, eclass);
			cb.createEdge(List.of(), EREFERENCES, eclass, erefs);
			cb.createEdge(List.of(), EREFERENCE_TYPE, erefs, eref);
			cb.createEdge(List.of(), EREFERENCES, eref, eRefType);
			cb.createEdge(List.of(), EREFERENCES, eclass, eSupType);
			cb.createEdge(List.of(), EREFERENCE_TYPE, eSupType, eclass);
			cb.createEdge(List.of(), EATTRIBUTE, enamedele, name);
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
			cb.createEdge(List.of(), ESUPER_TYPE, eclass, eclassifier);
			cb.createEdge(List.of(), ESUPER_TYPE, eDataType, eclassifier);
			cb.createEdge(List.of(), ESUPER_TYPE, eStruct, eTypedele);
			cb.createEdge(List.of(), ESUPER_TYPE, eTypedele, enamedele);
			cb.createEdge(List.of(), ESUPER_TYPE, eclassifier, enamedele);
			cb.createEdge(List.of(), EATTRIBUTE, eclass, abstractattr);
			cb.createEdge(List.of(), EATTRIBUTE_TYPE, abstractattr, eBoolean);

		});
	}

	public StatementResult executeActionAsTransaction(Consumer<CypherBuilder> action) {
		final var resultContainer = new ArrayList<StatementResult>();
		try (Session session = driver.session()) {
			session.writeTransaction(new TransactionWork<StatementResult>() {
				@Override
				public StatementResult execute(Transaction tx) {
					CypherBuilder cb = new CypherBuilder();
					action.accept(cb);
					String cypherCommand = cb.buildCommand();

					System.out.println(cypherCommand);

					StatementResult result = tx.run(cypherCommand);
					resultContainer.add(result);
					return result;
				}
			});
		}

		return resultContainer.get(0);
	}

	public boolean ecoreIsNotPresent() {
		var result = executeActionAsTransaction(cb -> {

			cb.returnWith(cb.matchNode(List.of(new NeoStringProperty(URI_PROP, ORG_EMOFLON_NEO_CORE)),
					List.of(METAMODEL), null, null));

		});

		return result.stream().count() == 0;
	}

	public static boolean canBeExported(EClass eclass) {
		return eclass.equals(EMSLPackage.eINSTANCE.getMetamodel()) || eclass.equals(EMSLPackage.eINSTANCE.getModel());
	}

	public void bootstrapNeoCoreIfNecessary(Consumer<String> logger) {
		if (ecoreIsNotPresent()) {
			logger.accept("Trying to bootstrap NeoCore...");
			bootstrapNeoCore();
			logger.accept("Done.");
		} else {
			logger.accept("NeoCore is already present.");
		}
	}

	public void exportModelsToNeo4j(List<Model> newModels) {
		executeActionAsTransaction((cb) -> {
			// Match required classes from NeoCore

			var neocore = cb.matchNode(List.of(new NeoStringProperty(URI_PROP, ORG_EMOFLON_NEO_CORE)),
					List.of(METAMODEL), null, null);

			var eclass = cb.matchNode(List.of(new NeoStringProperty(NAME_PROP, ECLASS)), List.of(ECLASS), null,
					neocore);

			var eref = cb.matchNode(List.of(new NeoStringProperty(NAME_PROP, EREFERENCE)), List.of(ECLASS), eclass,
					neocore);

			var edatatype = cb.matchNode(List.of(new NeoStringProperty(NAME_PROP, EDATA_TYPE)), List.of(ECLASS), null,
					neocore);

			var eattribute = cb.matchNode(List.of(new NeoStringProperty(NAME_PROP, EATTRIBUTE)), List.of(ECLASS), null,
					neocore);

			var mNodes = new HashMap<Model, NodeCommand>();
			var blockToCommand = new HashMap<NodeBlock, NodeCommand>();
			for (var model : newModels) {
				handleNodeBlocksInModel(cb, neocore, eclass, blockToCommand, mNodes, model);
			}
			for (var model : newModels) {
				var mNode = mNodes.get(model);
				for (var nb : model.getNodeBlocks()) {
					handleRelationStatementInModel(cb, neocore, eref, edatatype, eattribute, blockToCommand, mNode, nb);
				}
			}
		});
	}

	public void exportMetamodelsToNeo4j(List<Metamodel> newMetamodels) {
		executeActionAsTransaction((cb) -> {
			// Match required classes from NeoCore
			var neocore = cb.matchNode(List.of(new NeoStringProperty(URI_PROP, ORG_EMOFLON_NEO_CORE)),
					List.of(METAMODEL), null, null);

			var eclass = cb.matchNode(List.of(new NeoStringProperty(NAME_PROP, ECLASS)), List.of(ECLASS), null,
					neocore);

			var eref = cb.matchNode(List.of(new NeoStringProperty(NAME_PROP, EREFERENCE)), List.of(ECLASS), eclass,
					neocore);

			var edatatype = cb.matchNode(List.of(new NeoStringProperty(NAME_PROP, EDATA_TYPE)), List.of(ECLASS), null,
					neocore);

			var eattribute = cb.matchNode(List.of(new NeoStringProperty(NAME_PROP, EATTRIBUTE)), List.of(ECLASS), null,
					neocore);

			// Create metamodel nodes and handle node blocks for all metamodels
			var mmNodes = new HashMap<Metamodel, NodeCommand>();
			var blockToCommand = new HashMap<NodeBlock, NodeCommand>();
			for (var metamodel : newMetamodels) {
				handleNodeBlocks(cb, neocore, eclass, blockToCommand, mmNodes, metamodel);
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

	public List<Metamodel> removeExistingMetamodels(List<Metamodel> metamodels) {
		var newMetamodels = new ArrayList<Metamodel>();
		newMetamodels.addAll(metamodels);
		StatementResult result = executeActionAsTransaction(cb -> {
			var nc = cb.matchNode(List.of(), List.of(METAMODEL), null, null);
			cb.returnWith(nc);
		});
		result.forEachRemaining(
				mmNode -> newMetamodels.removeIf(mm -> mm.getName().equals(mmNode.get(0).get(URI_PROP).asString())));
		return newMetamodels;
	}

	public List<Model> removeExistingModels(List<Model> models) {
		var newModels = new ArrayList<Model>();
		newModels.addAll(models);
		StatementResult result = executeActionAsTransaction(cb -> {
			var nc = cb.matchNode(List.of(), List.of(MODEL), null, null);

			cb.returnWith(nc);
		});
		result.forEachRemaining(
				mmNode -> newModels.removeIf(mm -> mm.getName().equals(mmNode.get(0).get(URI_PROP).asString())));
		return newModels;
	}

	private void handleAttributes(CypherBuilder cb, NodeCommand neocore, NodeCommand edatatype, NodeCommand eattribute,
			HashMap<NodeBlock, NodeCommand> blockToCommand, NodeCommand mmNode, NodeBlock nb) {
		for (var ps : nb.getPropertyStatements()) {
			var attr = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, ps.getName())), List.of(EATTRIBUTE),
					eattribute, mmNode);

			var attrOwner = blockToCommand.get(nb);
			var nameOfTypeofAttr = ps.getValue();

			var typeofattr = cb.matchNode(List.of(new NeoStringProperty(NAME_PROP, nameOfTypeofAttr)),
					List.of(EDATA_TYPE), edatatype, neocore);

			cb.createEdge(List.of(), EATTRIBUTES, attrOwner, attr);
			cb.createEdge(List.of(), EATTRIBUTE_TYPE, attr, typeofattr);

		}
	}

	private void handleInheritance(CypherBuilder cb, HashMap<NodeBlock, NodeCommand> blockToCommand, NodeBlock nb) {
		for (var st : nb.getSuperTypes()) {
			var nodeblock = blockToCommand.get(nb);
			var sType = blockToCommand.get(st);

			cb.createEdge(List.of(), ESUPER_TYPE, nodeblock, sType);

		}
	}

	private void handleRelationStatement(CypherBuilder cb, NodeCommand neocore, NodeCommand eref, NodeCommand edatatype,
			NodeCommand eattribute, HashMap<NodeBlock, NodeCommand> blockToCommand, NodeCommand mmNode, NodeBlock nb) {
		for (var rs : nb.getRelationStatements()) {
			var ref = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, rs.getName())), List.of(EREFERENCE), eref,
					mmNode);

			var refOwner = blockToCommand.get(nb);
			var typeOfRef = blockToCommand.get(rs.getValue());

			cb.createEdge(List.of(), EREFERENCES, refOwner, ref);
			cb.createEdge(List.of(), EREFERENCE_TYPE, ref, typeOfRef);

			// Handle attributes of the relation
			rs.getPropertyStatements().forEach(ps -> {
				var attr = cb.createNode(List.of(new NeoStringProperty(NAME_PROP, ps.getName())), List.of(EATTRIBUTE),
						eattribute, mmNode);

				var nameOfTypeofAttr = ps.getValue();

				var typeofattr = cb.matchNode(List.of(new NeoStringProperty(NAME_PROP, nameOfTypeofAttr)),
						List.of(EDATA_TYPE), edatatype, neocore);

				cb.createEdge(List.of(), EATTRIBUTES, ref, attr);
				cb.createEdge(List.of(), EATTRIBUTE_TYPE, attr, typeofattr);

			});
		}
	}

	private void handleRelationStatementInModel(CypherBuilder cb, NodeCommand neocore, NodeCommand eref,
			NodeCommand edatatype, NodeCommand eattribute, HashMap<NodeBlock, NodeCommand> blockToCommand,
			NodeCommand mNode, NodeBlock nb) {

		for (var rs : nb.getRelationStatements()) {

			var refOwner = blockToCommand.get(nb);
			var typeOfRef = blockToCommand.get(rs.getValue());

			// Handle attributes of relation in model
			List<NeoProperty> props = new ArrayList<>();
			rs.getPropertyStatements().forEach(ps -> {
				props.add(new NeoStringProperty(ps.getName(), ps.getValue()));
			});

			cb.createEdge(props, rs.getName(), refOwner, typeOfRef);
		}
	}

	private void handleNodeBlocks(CypherBuilder cb, NodeCommand neocore, NodeCommand eclass,
			HashMap<NodeBlock, NodeCommand> blockToCommand, HashMap<Metamodel, NodeCommand> mmNodes,
			Metamodel metamodel) {

		var mmNode = cb.createNode(List.of(new NeoStringProperty(URI_PROP, metamodel.getName())), List.of(METAMODEL),
				null, null);

		mmNodes.put(metamodel, mmNode);

		cb.createEdge(List.of(), CONFORMS_TO_PROP, mmNode, neocore);

		metamodel.getNodeBlocks().forEach(nb -> {
			var nbNode = cb.createNode(
					List.of(new NeoStringProperty(NAME_PROP, nb.getName()),
							new NeoProperty(ABSTRACT_PROP, String.valueOf(nb.isAbstract()))),
					List.of(ECLASS), eclass, mmNode);

			blockToCommand.put(nb, nbNode);
		});
	}

	private void handleNodeBlocksInModel(CypherBuilder cb, NodeCommand neocore, NodeCommand eclass,
			HashMap<NodeBlock, NodeCommand> blockToCommand, HashMap<Model, NodeCommand> mNodes, Model model) {

		var mNode = cb.createNode(List.of(new NeoStringProperty(URI_PROP, model.getName())), List.of(MODEL), null,
				null);

		mNodes.put(model, mNode);

		model.getNodeBlocks().forEach(nb -> {
			Metamodel mm = (Metamodel) nb.getType().eContainer();

			var mmNode = cb.matchNode(List.of(new NeoStringProperty(URI_PROP, mm.getName())), List.of(METAMODEL), null,
					null);

			var typeOfNode = cb.matchNode(List.of(new NeoStringProperty(NAME_PROP, nb.getType().getName())),
					List.of(ECLASS), null, mmNode);

			cb.mergeEdge(List.of(), CONFORMS_TO_PROP, mNode, mmNode);

			// Handle attributes of model
			List<NeoProperty> props = new ArrayList<>();
			nb.getPropertyStatements().forEach(ps -> {
				props.add(new NeoStringProperty(ps.getName(), ps.getValue()));
			});
			
			var nbNode = cb.createNode(props, List.of(nb.getName(), nb.getType().getName()), typeOfNode, mNode);
			blockToCommand.put(nb, nbNode);
		});
	}
}