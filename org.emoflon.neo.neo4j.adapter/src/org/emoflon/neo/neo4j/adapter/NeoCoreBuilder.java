package org.emoflon.neo.neo4j.adapter;

import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.ABSTRACT_PROP;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.CONFORMS_TO_PROP;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.EATTRIBUTED_ELEMENT;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.EATTRIBUTES;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.EATTRIBUTE_TYPE;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.ECLASS;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.ECLASSIFIER;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.EDATA_TYPE;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.EENUM;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.EENUM_LITERAL;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.ELITERALS;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.EOBJECT;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.EREFERENCES;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.EREFERENCE_TYPE;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.ESUPER_TYPE;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.METAMODEL;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.META_TYPE;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.MODEL;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.NAME_PROP;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.eDataTypeLabels;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.eDataTypeProps;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.eattrLabels;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.eattrProps;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.eclassLabels;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.eclassProps;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.eenumLabels;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.eenumLiteralLabels;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.eenumLiteralProps;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.eenumProps;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.eobjectLabels;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.eobjectProps;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.erefLabels;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.erefProps;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.mmodelLabels;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.mmodelProps;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.modelLabels;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.modelProps;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.neoCoreLabels;
import static org.emoflon.neo.neo4j.adapter.NeoCoreBootstrapper.neoCoreProps;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emoflon.neo.emsl.eMSL.BuiltInType;
import org.emoflon.neo.emsl.eMSL.EMSLPackage;
import org.emoflon.neo.emsl.eMSL.Entity;
import org.emoflon.neo.emsl.eMSL.EnumLiteral;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock;
import org.emoflon.neo.emsl.eMSL.MetamodelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.RelationKind;
import org.emoflon.neo.emsl.eMSL.UserDefinedType;
import org.emoflon.neo.emsl.eMSL.Value;
import org.emoflon.neo.emsl.refinement.EMSLFlattener;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.StatementResult;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;

public class NeoCoreBuilder implements AutoCloseable, IBuilder {
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	// Defaults for export
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
	public StatementResult executeQuery(String cypherStatement) {
		return driver.session().run(cypherStatement.trim());
	}

	public void executeQueryForSideEffect(String cypherStatement) {
		var st = driver.session().run(cypherStatement.trim());
		st.consume();
	}

	public void clearDataBase() {
		executeQueryForSideEffect("MATCH (n) DETACH DELETE n");
	}

	@Override
	public void close() throws Exception {
		driver.close();
	}

	private void exportModelToNeo4j(Model model) throws FlattenerException {
		bootstrapNeoCoreIfNecessary();

		ResourceSet rs = model.eResource().getResourceSet();
		EcoreUtil.resolveAll(rs);

		var m = (Model) EMSLFlattener.flatten(model);

		var models = collectReferencedModels(m);
		models.add(m);

		var metamodels = models.stream()//
				.flatMap(_m -> collectDependentMetamodels(_m).stream())//
				.collect(Collectors.toSet());

		var metamodelNames = metamodels.stream().map(Metamodel::getName).collect(Collectors.joining(","));
		logger.info("Trying to export metamodels: " + metamodelNames);
		var newMetamodels = removeExistingMetamodels(metamodels);

		for (Metamodel mm : metamodels) {
			if (!newMetamodels.contains(mm))
				logger.info("Skipping metamodel " + mm.getName() + " as it is already present.");
		}

		if (!newMetamodels.isEmpty())
			exportMetamodelsToNeo4j(newMetamodels);
		logger.info("Exported metamodels: " + newMetamodels);

		var modelNames = models.stream().map(Model::getName).collect(Collectors.joining(","));
		logger.info("Trying to export models: " + modelNames);
		var newModels = removeExistingModels(models);

		// Remove abstract models
		newModels = newModels.stream().filter(mod -> !mod.isAbstract()).collect(Collectors.toList());

		for (Model mod : models) {
			if (!newModels.contains(mod))
				logger.info("Skipping model " + mod.getName() + " as it is already present or is abstract.");
		}

		if (!newModels.isEmpty())
			exportModelsToNeo4j(newModels);

		logger.info("Exported models: " + newModels);
	}

	private Collection<Model> collectReferencedModels(Model m) {
		var allRefs = new HashSet<Model>();
		collectReferencedModels(m, allRefs);
		return allRefs;
	}

	private void collectReferencedModels(Model m, Set<Model> allRefs) {
		var directRefs = m.getNodeBlocks().stream()//
				.flatMap(nb -> nb.getRelations().stream())//
				.map(rel -> (Model) (rel.getTarget().eContainer()))//
				.collect(Collectors.toSet());

		directRefs.forEach(ref -> {
			if (!allRefs.contains(ref)) {
				allRefs.add(ref);
				collectReferencedModels(ref, allRefs);
			}
		});
	}

	private Collection<Metamodel> collectDependentMetamodels(Model m) {
		return m.getNodeBlocks().stream()//
				.map(nb -> (Metamodel) (nb.getType().eContainer()))//
				.flatMap(mm -> collectReferencedMetamodels(mm).stream())//
				.collect(Collectors.toSet());
	}

	private Collection<Metamodel> collectReferencedMetamodels(Metamodel m) {
		var allRefs = new HashSet<Metamodel>();
		collectReferencedMetamodels(m, allRefs);
		return allRefs;
	}

	private void collectReferencedMetamodels(Metamodel m, HashSet<Metamodel> allRefs) {
		var superTypes = m.getNodeBlocks().stream()//
				.flatMap(nb -> nb.getSuperTypes().stream());

		var referencedTypes = m.getNodeBlocks().stream()//
				.flatMap(nb -> nb.getRelations().stream())//
				.map(rel -> rel.getTarget());

		var directRefs = Streams.concat(superTypes, referencedTypes)//
				.map(nb -> (Metamodel) (nb.eContainer()))//
				.collect(Collectors.toSet());

		directRefs.forEach(ref -> {
			if (!allRefs.contains(ref)) {
				allRefs.add(ref);
				collectReferencedMetamodels(ref, allRefs);
			}
		});
	}

	private void exportMetamodelToNeo4j(Metamodel m) {
		bootstrapNeoCoreIfNecessary();
		ResourceSet rs = m.eResource().getResourceSet();
		EcoreUtil.resolveAll(rs);

		var metamodels = collectReferencedMetamodels(m);

		var metamodelNames = metamodels.stream().map(Metamodel::getName).collect(Collectors.joining(","));
		logger.info("Trying to export metamodels: " + metamodelNames);
		var newMetamodels = removeExistingMetamodels(metamodels);

		for (Metamodel mm : metamodels) {
			if (!newMetamodels.contains(mm))
				logger.info("Skipping metamodel " + mm.getName() + " as it is already present.");
		}

		if (!newMetamodels.isEmpty())
			exportMetamodelsToNeo4j(newMetamodels);

		logger.info("Exported metamodels: " + newMetamodels);
	}

	public static boolean canBeExported(EClass eclass) {
		return eclass.equals(EMSLPackage.eINSTANCE.getMetamodel()) || eclass.equals(EMSLPackage.eINSTANCE.getModel());
	}

	public static boolean canBeCopiedToClipboard(EClass eclass) {
		return eclass.equals(EMSLPackage.eINSTANCE.getAtomicPattern())
				|| eclass.equals(EMSLPackage.eINSTANCE.getPattern())
				|| eclass.equals(EMSLPackage.eINSTANCE.getConstraint())
				|| eclass.equals(EMSLPackage.eINSTANCE.getCondition());
	}

	void executeActionAsCreateTransaction(Consumer<CypherCreator> action) {
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
			cb.returnWith(cb.matchNode(neoCoreProps, neoCoreLabels));
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

	private void bootstrapNeoCore() {
		var bootstrapper = new NeoCoreBootstrapper();
		bootstrapper.bootstrapNeoCore(this);
	}

	private void exportModelsToNeo4j(List<Model> newModels) {
		var flattenedModels = newModels.stream().map(m -> {
			try {
				return (Model) EMSLFlattener.flatten(m);
			} catch (FlattenerException e) {
				e.printStackTrace();
				return m;
			}
		}).collect(Collectors.toList());

		executeActionAsCreateTransaction((cb) -> {
			// Match required classes from NeoCore
			var neocore = cb.matchNode(neoCoreProps, neoCoreLabels);
			var model = cb.matchNodeWithContainer(modelProps, modelLabels, neocore);

			// Create nodes and edges in models
			var mNodes = new HashMap<Model, NodeCommand>();
			var blockToCommand = new HashMap<ModelNodeBlock, NodeCommand>();
			for (var newModel : flattenedModels) {
				handleNodeBlocksInModel(cb, blockToCommand, mNodes, newModel, model);
			}
			for (var newModel : flattenedModels) {
				for (var nb : newModel.getNodeBlocks()) {
					handleRelationStatementInModel(cb, blockToCommand, nb);
				}
			}
		});
	}

	private void exportMetamodelsToNeo4j(List<Metamodel> newMetamodels) {
		executeActionAsCreateTransaction((cb) -> {
			// Match required classes from NeoCore
			var neocore = cb.matchNode(neoCoreProps, neoCoreLabels);
			var eclass = cb.matchNodeWithContainer(eclassProps, eclassLabels, neocore);
			var eref = cb.matchNodeWithContainer(erefProps, erefLabels, neocore);
			var edatatype = cb.matchNodeWithContainer(eDataTypeProps, eDataTypeLabels, neocore);
			var eattribute = cb.matchNodeWithContainer(eattrProps, eattrLabels, neocore);
			var eobject = cb.matchNodeWithContainer(eobjectProps, eobjectLabels, neocore);
			var mmodel = cb.matchNodeWithContainer(mmodelProps, mmodelLabels, neocore);
			var eenum = cb.matchNodeWithContainer(eenumProps, eenumLabels, neocore);
			var eenumLiteral = cb.matchNodeWithContainer(eenumLiteralProps, eenumLiteralLabels, neocore);

			// Create metamodel nodes and handle node blocks for all metamodels
			var mmNodes = new HashMap<Metamodel, NodeCommand>();
			var blockToCommand = new HashMap<Object, NodeCommand>();
			for (var metamodel : newMetamodels) {
				handleNodeBlocksInMetaModel(cb, neocore, eclass, blockToCommand, mmNodes, metamodel, mmodel, eobject);
				var mmNode = mmNodes.get(metamodel);
				handleEnumsInMetamodel(cb, metamodel, mmNode, eenum, eenumLiteral, blockToCommand);
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

	private void handleEnumsInMetamodel(CypherCreator cb, Metamodel metamodel, NodeCommand mmNode, NodeCommand eenum,
			NodeCommand eenumLiteral, HashMap<Object, NodeCommand> blockToCommand) {
		metamodel.getEnums().forEach(eenumNode -> {
			var eenumCommand = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, eenumNode.getName())), //
					List.of(EENUM, EDATA_TYPE, EOBJECT, ECLASSIFIER), eenum, mmNode);

			blockToCommand.put(eenumNode, eenumCommand);

			for (EnumLiteral literal : eenumNode.getLiterals()) {
				var literalCommand = cb.createNodeWithContAndType(//
						List.of(new NeoProp(NAME_PROP, literal.getName())), //
						List.of(EENUM_LITERAL, EOBJECT, EDATA_TYPE, ECLASSIFIER), eenumLiteral, mmNode);

				cb.createEdge(ELITERALS, eenumCommand, literalCommand);
			}
		});
	}

	private List<Metamodel> removeExistingMetamodels(Collection<Metamodel> metamodels) {
		var newMetamodels = new ArrayList<Metamodel>();
		newMetamodels.addAll(metamodels);
		StatementResult result = executeActionAsMatchTransaction(cb -> {
			var nc = cb.matchNode(List.of(), List.of(METAMODEL));
			cb.returnWith(nc);
		});
		result.forEachRemaining(
				mmNode -> newMetamodels.removeIf(mm -> mm.getName().equals(mmNode.get(0).get(NAME_PROP).asString())));
		return newMetamodels;
	}

	private List<Model> removeExistingModels(Collection<Model> models) {
		var newModels = new ArrayList<Model>();
		newModels.addAll(models);
		StatementResult result = executeActionAsMatchTransaction(cb -> {
			var nc = cb.matchNode(List.of(), List.of(MODEL));
			cb.returnWith(nc);
		});
		result.forEachRemaining(
				mmNode -> newModels.removeIf(mm -> mm.getName().equals(mmNode.get(0).get(NAME_PROP).asString())));
		return newModels;
	}

	private void handleAttributes(CypherCreator cb, NodeCommand neocore, NodeCommand edatatype, NodeCommand eattribute,
			HashMap<Object, NodeCommand> blockToCommand, NodeCommand mmNode, MetamodelNodeBlock nb) {
		for (var ps : nb.getProperties()) {
			var attr = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, ps.getName())), //
					NeoCoreBootstrapper.LABELS_FOR_AN_EATTRIBUTE, eattribute, mmNode);
			var attrOwner = blockToCommand.get(nb);
			var nameOfTypeofAttr = inferType(ps);

			var dataType = ps.getType();
			NodeCommand typeofattr = null;
			if (dataType instanceof BuiltInType) {
				typeofattr = cb.matchNodeWithContainer(//
						List.of(new NeoProp(NAME_PROP, nameOfTypeofAttr)), //
						NeoCoreBootstrapper.LABELS_FOR_AN_EDATATYPE, neocore);
			} else if (dataType instanceof UserDefinedType) {
				typeofattr = blockToCommand.get(((UserDefinedType) dataType).getReference());
			}

			cb.createEdge(EATTRIBUTES, attrOwner, attr);
			cb.createEdge(EATTRIBUTE_TYPE, attr, typeofattr);
		}
	}

	private String inferType(MetamodelPropertyStatement ps) {
		var dataType = ps.getType();

		if (dataType instanceof BuiltInType)
			return ((BuiltInType) dataType).getReference().getLiteral();
		else if (dataType instanceof UserDefinedType)
			return ((UserDefinedType) dataType).getReference().getName();
		else
			throw new IllegalArgumentException("Unknown type of property statement: " + ps.getType());
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
			var isCompProp = new NeoProp(NeoCoreBootstrapper.ISCOMPOSITION_PROP,
					rs.getKind().equals(RelationKind.COMPOSITION));

			var isContainmentProp = new NeoProp(NeoCoreBootstrapper.ISCONTAINMENT_PROP,
					rs.getKind().equals(RelationKind.COMPOSITION) || rs.getKind().equals(RelationKind.AGGREGATION));

			var ref = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, rs.getName()), isCompProp, isContainmentProp), //
					NeoCoreBootstrapper.LABELS_FOR_AN_EREFERENCE, eref, mmNode);

			var refOwner = blockToCommand.get(nb);
			var typeOfRef = blockToCommand.get(rs.getTarget());

			cb.createEdge(EREFERENCES, refOwner, ref);
			cb.createEdge(EREFERENCE_TYPE, ref, typeOfRef);

			// Handle attributes of the relation
			rs.getProperties().forEach(ps -> {
				var attr = cb.createNodeWithContAndType(//
						List.of(new NeoProp(NAME_PROP, ps.getName())), //
						NeoCoreBootstrapper.LABELS_FOR_AN_EATTRIBUTE, eattribute, mmNode);

				var nameOfTypeofAttr = inferType(ps);

				var typeofattr = cb.matchNodeWithContainer(//
						List.of(new NeoProp(NAME_PROP, nameOfTypeofAttr)), //
						NeoCoreBootstrapper.LABELS_FOR_AN_EDATATYPE, neocore);

				cb.createEdge(EATTRIBUTES, ref, attr);
				cb.createEdge(EATTRIBUTE_TYPE, attr, typeofattr);
			});
		}
	}

	private void handleRelationStatementInModel(CypherCreator cb, HashMap<ModelNodeBlock, NodeCommand> blockToCommand,
			ModelNodeBlock nb) {
		for (var rs : nb.getRelations()) {
			var refOwner = blockToCommand.get(nb);
			var typeOfRef = blockToCommand.get(rs.getTarget());

			// Handle attributes of relation in model
			List<NeoProp> props = new ArrayList<>();
			rs.getProperties().forEach(ps -> {
				props.add(new NeoProp(ps.getType().getName(), inferType(ps, nb)));
			});

			cb.createEdgeWithProps(props, EMSLUtil.getOnlyType(rs).getName(), refOwner, typeOfRef);

			if (isContainment(rs)) {
				createContainerEdge(cb, rs, typeOfRef, refOwner);
			}
		}
	}

	private void createContainerEdge(CypherCreator cb, ModelRelationStatement rs, NodeCommand container,
			NodeCommand containee) {
		var prop = new NeoProp(NeoCoreBootstrapper.ISCOMPOSITION_PROP,
				EMSLUtil.getOnlyType(rs).getKind().equals(RelationKind.COMPOSITION));
		cb.createEdgeWithProps(List.of(prop), NeoCoreBootstrapper.ECONTAINER, container, containee);
	}

	private boolean isContainment(ModelRelationStatement rs) {
		return EMSLUtil.getOnlyType(rs).getKind().equals(RelationKind.AGGREGATION)
				|| EMSLUtil.getOnlyType(rs).getKind().equals(RelationKind.COMPOSITION);
	}

	private void handleNodeBlocksInMetaModel(CypherCreator cb, NodeCommand neocore, NodeCommand eclass,
			HashMap<Object, NodeCommand> blockToCommand, HashMap<Metamodel, NodeCommand> mmNodes, Metamodel metamodel,
			NodeCommand mmodel, NodeCommand eobject) {

		var mmNode = cb.createNode(List.of(new NeoProp(NAME_PROP, metamodel.getName())),
				List.of(METAMODEL, MODEL, EOBJECT));

		mmNodes.put(metamodel, mmNode);

		cb.createEdge(CONFORMS_TO_PROP, mmNode, neocore);
		cb.createEdge(META_TYPE, mmNode, mmodel);

		metamodel.getNodeBlocks().forEach(nb -> {

			var nbNode = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, nb.getName()), //
							new NeoProp(ABSTRACT_PROP, nb.isAbstract())),
					NeoCoreBootstrapper.LABELS_FOR_AN_ECLASS, eclass, mmNode);

			if (nb.getSuperTypes().isEmpty()) {
				cb.createEdge(ESUPER_TYPE, nbNode, eobject);
			}

			blockToCommand.put(nb, nbNode);
		});
	}

	private void handleNodeBlocksInModel(CypherCreator cb, HashMap<ModelNodeBlock, NodeCommand> blockToCommand,
			HashMap<Model, NodeCommand> mNodes, Model model, NodeCommand nodeCommandForModel) {

		var mNode = cb.createNode(List.of(new NeoProp(NAME_PROP, model.getName())), List.of(MODEL, EOBJECT));

		mNodes.put(model, mNode);

		model.getNodeBlocks().forEach(nb -> {
			Metamodel mm = (Metamodel) nb.getType().eContainer();

			var mmNode = cb.matchNode(List.of(new NeoProp(NAME_PROP, mm.getName())), List.of(METAMODEL));

			var typeOfNode = cb.matchNodeWithContainer(//
					List.of(new NeoProp(NAME_PROP, nb.getType().getName())), //
					List.of(ECLASS, ECLASSIFIER, EATTRIBUTED_ELEMENT, EOBJECT), mmNode);

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

		return Lists.newArrayList(labels);
	}

	private Object inferType(ModelPropertyStatement ps, ModelNodeBlock nb) {
		String propName = ps.getType().getName();
		MetamodelNodeBlock nodeType = nb.getType();

		if (ps.eContainer().equals(nb)) {
			return inferTypeForNodeAttribute(ps.getValue(), propName, nodeType);
		} else if (ps.eContainer() instanceof ModelRelationStatement) {
			ModelRelationStatement rs = (ModelRelationStatement) ps.eContainer();
			return inferTypeForEdgeAttribute(ps.getValue(), EMSLUtil.getOnlyType(rs).getName(), propName, nodeType);
		} else {
			throw new IllegalArgumentException("Unable to handle: " + ps);
		}
	}

	private Object inferTypeForEdgeAttribute(Value value, String relName, String propName,
			MetamodelNodeBlock nodeType) {
		var typedValue = nodeType.getRelations().stream()//
				.filter(et -> et.getName().equals(relName))//
				.flatMap(et -> et.getProperties().stream())//
				.filter(etPs -> etPs.getName().equals(propName))//
				.map(etPs -> etPs.getType())//
				.map(t -> EMSLUtil.parseStringWithType(value, t))//
				.findAny();

		return typedValue.orElseThrow(() -> new IllegalStateException("Unable to infer type of " + value));
	}

	private Object inferTypeForNodeAttribute(Value value, String propName, MetamodelNodeBlock nodeType) {
		var typedValue = EMSLUtil.allPropertiesOf(nodeType).stream()//
				.filter(t -> t.getName().equals(propName))//
				.map(psType -> psType.getType())//
				.map(t -> EMSLUtil.parseStringWithType(value, t))//
				.findAny();

		return typedValue.orElseThrow(() -> new IllegalStateException("Unable to infer type of " + value));
	}

	public void exportEMSLEntityToNeo4j(Entity entity) throws FlattenerException {
		if (entity instanceof Model) {
			exportModelToNeo4j((Model) entity);
		} else if (entity instanceof Metamodel)
			exportMetamodelToNeo4j((Metamodel) entity);
		else
			throw new IllegalArgumentException("This type of entity cannot be exported: " + entity);
	}
}