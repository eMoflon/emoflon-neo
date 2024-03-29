package org.emoflon.neo.cypher.models;

import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.eDataTypeLabels;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.eDataTypeProps;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.eattrLabels;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.eattrProps;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.eclassLabels;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.eclassProps;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.eenumLabels;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.eenumLiteralLabels;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.eenumLiteralProps;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.eenumProps;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.eobjectLabels;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.eobjectProps;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.erefLabels;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.erefProps;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.mmodelLabels;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.mmodelProps;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.modelLabels;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.modelProps;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.neoCoreLabels;
import static org.emoflon.neo.cypher.models.NeoCoreBootstrapper.neoCoreProps;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.ABSTRACT_PROP;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.CONFORMS_TO_PROP;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EATTRIBUTES;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EATTRIBUTE_TYPE;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.ELITERALS;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EOBJECT;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EREFERENCES;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EREFERENCE_TYPE;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.ESUPER_TYPE;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.NAMESPACE_PROP;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.NAME_PROP;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emoflon.neo.cypher.models.templates.CypherBuilder;
import org.emoflon.neo.cypher.models.templates.CypherCreator;
import org.emoflon.neo.cypher.models.templates.CypherNodeMatcher;
import org.emoflon.neo.cypher.models.templates.NeoProp;
import org.emoflon.neo.cypher.models.templates.NodeCommand;
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
import org.emoflon.neo.emsl.eMSL.PrimitiveBoolean;
import org.emoflon.neo.emsl.eMSL.PrimitiveString;
import org.emoflon.neo.emsl.eMSL.RelationKind;
import org.emoflon.neo.emsl.eMSL.UserDefinedType;
import org.emoflon.neo.emsl.eMSL.ValueExpression;
import org.emoflon.neo.emsl.refinement.EMSLFlattener;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.neocore.util.NeoCoreConstants;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.driver.Record;
import org.neo4j.driver.Result;

import com.google.common.collect.Streams;

public class NeoCoreBuilder implements AutoCloseable, IBuilder {
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	private static final List<String> MARKERS = List.of(NeoCoreConstants._TR_PROP, //
			NeoCoreConstants._DE_PROP, //
			NeoCoreConstants._CR_PROP, //
			NeoCoreConstants._EX_PROP);

	private static final Object TYPE_AS_ATTRIBUTE = "_type_";

	private static final Object CORR = "corr";

	// Defaults for export
	private int maxTransactionSizeEdges = 10000;
	private int maxTransactionSizeNodes = 10000;

	public void setMaxTransactionSize(int nodes, int edges) {
		maxTransactionSizeNodes = nodes;
		maxTransactionSizeEdges = edges;
	}

	private final Driver driver;
	
	// Weightings for concurrent synchronisation
	private double alpha;
	private double beta;
	private double gamma;
	
	public NeoCoreBuilder(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}
	
	public NeoCoreBuilder(String uri, String user, String password, double alpha, double beta, double gamma) {
		this(uri, user, password);
		this.alpha = alpha;
		this.beta = beta;
		this.gamma = gamma;
	}

	public Driver getDriver() {
		return driver;
	}
	
	public double getAlpha() {
		return alpha;
	}

	public double getBeta() {
		return beta;
	}

	public double getGamma() {
		return gamma;
	}
	
	@Override
	public List<Record> executeQuery(String cypherStatement) {
		return executeQuery(cypherStatement, Collections.emptyMap());
	}

	@Override
	public List<Record> executeQuery(String cypherStatement, Map<String, Object> parameters) {
		try (var session = driver.session()) {

			return session.writeTransaction(transaction -> {
				if (parameters.isEmpty())
					return transaction.run(cypherStatement.trim()).list();
				else
					return transaction.run(cypherStatement.trim(), parameters).list();
			});
		} catch (Exception e) {
			logger.error(e);
			e.printStackTrace();
			return null;
		}
	}

	public void executeQueryForSideEffect(String cypherStatement) {
		executeQueryForSideEffect(cypherStatement, Collections.emptyMap());
	}

	public void executeQueryForSideEffect(String cypherStatement, Map<String, Object> parameters) {
		executeQuery(cypherStatement, parameters);
	}

	public void clearDataBase() {
		executeQueryForSideEffect("MATCH (n) DETACH DELETE n");
	}
	public void clearModel(String modelName) {
		executeQueryForSideEffect("match(n)-[r]->(m:NeoCore__Model) Where m.ename = '" + modelName + "' detach delete n,m");
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
		// Make sure the model to be exported is part of the set
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

		// For the actual export, replace with original model so it can be flattened
		// together with all its referenced models
		models.remove(m);
		models.add(model);

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

	public Collection<Metamodel> collectDependentMetamodels(Model m) {
		return m.getNodeBlocks().stream()//
				.map(nb -> (Metamodel) (nb.getType().eContainer()))//
				.flatMap(mm -> collectReferencedMetamodels(mm).stream())//
				.collect(Collectors.toSet());
	}

	private Set<Metamodel> collectReferencedMetamodels(Metamodel m) {
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
		metamodels.add(m);

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
				|| eclass.equals(EMSLPackage.eINSTANCE.getCondition())
				|| eclass.equals(EMSLPackage.eINSTANCE.getRule());
	}

	public void executeActionAsCreateTransaction(Consumer<CypherCreator> action) {
		var creator = new CypherCreator(maxTransactionSizeNodes, maxTransactionSizeEdges);
		action.accept(creator);
		creator.run(driver);
	}

	public Result executeActionAsMatchTransaction(Consumer<CypherNodeMatcher> action) {
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

	public void bootstrapNeoCoreIfNecessary() {
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

		executeQueryForSideEffect("CREATE CONSTRAINT IF NOT EXISTS ON (mm:" //
				+ NeoCoreBootstrapper.addNeoCoreNamespace(NeoCoreConstants.MODEL) + ") ASSERT mm.ename IS UNIQUE");

		executeQueryForSideEffect(//
				"CREATE INDEX IF NOT EXISTS FOR (n:" + //
						NeoCoreBootstrapper.addNeoCoreNamespace(NeoCoreConstants.ECLASS) + //
						") ON (n." + NeoCoreConstants.NAME_PROP + ")");
	}

	private void exportModelsToNeo4j(List<Model> newModels) throws FlattenerException {
		var flattenedModels = EMSLFlattener.flattenAllModels(newModels);

		executeActionAsCreateTransaction((cb) -> {
			// Match required classes from NeoCore
			var model = cb.matchNode(modelProps, modelLabels);

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
			var eclass = cb.matchNodeWithContainer(eclassProps, eclassLabels, neocore.getName());
			var eref = cb.matchNodeWithContainer(erefProps, erefLabels, neocore.getName());
			var edatatype = cb.matchNodeWithContainer(eDataTypeProps, eDataTypeLabels, neocore.getName());
			var eattribute = cb.matchNodeWithContainer(eattrProps, eattrLabels, neocore.getName());
			var eobject = cb.matchNodeWithContainer(eobjectProps, eobjectLabels, neocore.getName());
			var mmodel = cb.matchNodeWithContainer(mmodelProps, mmodelLabels, neocore.getName());
			var eenum = cb.matchNodeWithContainer(eenumProps, eenumLabels, neocore.getName());
			var eenumLiteral = cb.matchNodeWithContainer(eenumLiteralProps, eenumLiteralLabels, neocore.getName());

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
			var eenumCommand = cb.createNodeWithType(//
					List.of(new NeoProp(NAME_PROP, eenumNode.getName()), //
							new NeoProp(NAMESPACE_PROP, metamodel.getName())), //
					NeoCoreBootstrapper.LABELS_FOR_AN_ENUM, eenum);

			blockToCommand.put(eenumNode, eenumCommand);

			for (EnumLiteral literal : eenumNode.getLiterals()) {
				var literalCommand = cb.createNodeWithType(//
						List.of(new NeoProp(NAME_PROP, literal.getName())), //
						NeoCoreBootstrapper.LABELS_FOR_AN_ENUMLITERAL, eenumLiteral);

				cb.createEdge(ELITERALS, eenumCommand, literalCommand);
			}
		});
	}

	private List<Metamodel> removeExistingMetamodels(Collection<Metamodel> metamodels) {
		var newMetamodels = new ArrayList<Metamodel>();
		newMetamodels.addAll(metamodels);
		Result result = executeActionAsMatchTransaction(cb -> {
			var nc = cb.matchNode(List.of(), NeoCoreBootstrapper.LABELS_FOR_A_METAMODEL);
			cb.returnWith(nc);
		});
		result.forEachRemaining(
				mmNode -> newMetamodels.removeIf(mm -> mm.getName().equals(mmNode.get(0).get(NAME_PROP).asString())));
		return newMetamodels;
	}

	private List<Model> removeExistingModels(Collection<Model> models) {
		var newModels = new ArrayList<Model>();
		newModels.addAll(models);
		Result result = executeActionAsMatchTransaction(cb -> {
			var nc = cb.matchNode(List.of(), NeoCoreBootstrapper.LABELS_FOR_A_MODEL);
			cb.returnWith(nc);
		});
		result.forEachRemaining(
				mmNode -> newModels.removeIf(mm -> mm.getName().equals(mmNode.get(0).get(NAME_PROP).asString())));
		return newModels;
	}

	private void handleAttributes(CypherCreator cb, NodeCommand neocore, NodeCommand edatatype, NodeCommand eattribute,
			HashMap<Object, NodeCommand> blockToCommand, NodeCommand mmNode, MetamodelNodeBlock nb) {
		for (var ps : nb.getProperties()) {
			var attr = cb.createNodeWithType(//
					List.of(new NeoProp(NAME_PROP, ps.getName())), //
					NeoCoreBootstrapper.LABELS_FOR_AN_EATTRIBUTE, eattribute);
			var attrOwner = blockToCommand.get(nb);
			var nameOfTypeofAttr = inferType(ps);

			var dataType = ps.getType();
			NodeCommand typeofattr = null;
			if (dataType instanceof BuiltInType) {
				typeofattr = cb.matchNodeWithContainer(//
						List.of(new NeoProp(NAME_PROP, nameOfTypeofAttr)), //
						NeoCoreBootstrapper.LABELS_FOR_AN_EDATATYPE, neocore.getName());
			} else if (dataType instanceof UserDefinedType) {
				var reference = ((UserDefinedType) dataType).getReference();
				if (!blockToCommand.containsKey(reference)) {
					blockToCommand.put(reference, cb.matchNode(//
							List.of(new NeoProp(NAME_PROP, nameOfTypeofAttr), //
									new NeoProp(NAMESPACE_PROP, ((Metamodel) reference.eContainer()).getName())), //
							NeoCoreBootstrapper.LABELS_FOR_AN_ENUM));//
				}

				typeofattr = blockToCommand.get(reference);
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
			var isCompProp = new NeoProp(NeoCoreConstants.ISCOMPOSITION_PROP,
					rs.getKind().equals(RelationKind.COMPOSITION));

			var isContainmentProp = new NeoProp(NeoCoreConstants.ISCONTAINMENT_PROP,
					rs.getKind().equals(RelationKind.COMPOSITION) || rs.getKind().equals(RelationKind.AGGREGATION));

			var ref = cb.createNodeWithType(//
					List.of(new NeoProp(NAME_PROP, rs.getName()), isCompProp, isContainmentProp), //
					NeoCoreBootstrapper.LABELS_FOR_AN_EREFERENCE, eref);

			var refOwner = blockToCommand.get(nb);

			var target = rs.getTarget();
			var targetMM = (Metamodel) target.eContainer();
			if (!blockToCommand.containsKey(target)) {
				var existingNode = cb.matchNode(//
						List.of(new NeoProp(NAME_PROP, target.getName()), //
								new NeoProp(NAMESPACE_PROP, targetMM.getName())), //
						NeoCoreBootstrapper.LABELS_FOR_AN_ECLASS);
				blockToCommand.put(target, existingNode);
			}

			var typeOfRef = blockToCommand.get(target);
			if (typeOfRef == null) {
				throw new IllegalStateException("Unable to resolve type of relation: '" + rs.getName()
						+ "' in the context of class '" + nb.getName() + "' in metamodel: " + nb.eContainer());
			}

			cb.createEdge(EREFERENCES, refOwner, ref);
			cb.createEdge(EREFERENCE_TYPE, ref, typeOfRef);

			// Handle attributes of the relation
			rs.getProperties().forEach(ps -> {
				var attr = cb.createNodeWithType(//
						List.of(new NeoProp(NAME_PROP, ps.getName())), //
						NeoCoreBootstrapper.LABELS_FOR_AN_EATTRIBUTE, eattribute);

				var nameOfTypeofAttr = inferType(ps);

				var typeofattr = cb.matchNodeWithContainer(//
						List.of(new NeoProp(NAME_PROP, nameOfTypeofAttr)), //
						NeoCoreBootstrapper.LABELS_FOR_AN_EDATATYPE, neocore.getName());

				cb.createEdge(EATTRIBUTES, ref, attr);
				cb.createEdge(EATTRIBUTE_TYPE, attr, typeofattr);
			});

			for (String mrk : MARKERS)
				addAttributeForReference(cb, ref, neocore, mrk);

		}
	}

	private void addAttributeForReference(CypherCreator cb, NodeCommand ref, NodeCommand neocore, String mrk) {
		var attr = cb.matchNodeWithContainer(//
				List.of(new NeoProp(NAME_PROP, mrk)), //
				NeoCoreBootstrapper.LABELS_FOR_AN_EATTRIBUTE, neocore.getName());
		cb.createEdge(EATTRIBUTES, ref, attr);
	}

	private void handleRelationStatementInModel(CypherCreator cb, HashMap<ModelNodeBlock, NodeCommand> blockToCommand,
			ModelNodeBlock nb) {
		for (var rs : nb.getRelations()) {
			var refOwner = blockToCommand.get(nb);

			var target = rs.getTarget();
			if (!blockToCommand.containsKey(target)) {
				var nodeBlockOfContainerOfTarget = (Model) target.eContainer();
				var typeOfRef = cb.matchNodeWithContainer(//
						List.of(new NeoProp(NAME_PROP, rs.getTarget().getName()), new NeoProp(NAMESPACE_PROP, nodeBlockOfContainerOfTarget.getName())), //
						computeLabelsFromType(rs.getTarget().getType()), //
						nodeBlockOfContainerOfTarget.getName());
				blockToCommand.put(target, typeOfRef);
			}

			var typeOfRef = blockToCommand.get(target);

			// Handle attributes of relation in model
			List<NeoProp> props = new ArrayList<>();
			rs.getProperties().forEach(ps -> {
				props.add(new NeoProp(EMSLUtil.getNameOfType(ps), inferType(ps, nb)));
			});

			cb.createEdgeWithProps(props, EMSLUtil.getOnlyType(rs).getName(), refOwner, typeOfRef);
		}
	}

	@SuppressWarnings("unused")
	private void createContainerEdge(CypherCreator cb, ModelRelationStatement rs, NodeCommand container,
			NodeCommand containee) {
		var prop = new NeoProp(NeoCoreConstants.ISCOMPOSITION_PROP,
				EMSLUtil.getOnlyType(rs).getKind().equals(RelationKind.COMPOSITION));
		cb.createEdgeWithProps(List.of(prop), NeoCoreConstants.ECONTAINER, container, containee);
	}

	@SuppressWarnings("unused")
	private boolean isContainment(ModelRelationStatement rs) {
		return EMSLUtil.getOnlyType(rs).getKind().equals(RelationKind.AGGREGATION)
				|| EMSLUtil.getOnlyType(rs).getKind().equals(RelationKind.COMPOSITION);
	}

	private void handleNodeBlocksInMetaModel(CypherCreator cb, NodeCommand neocore, NodeCommand eclass,
			HashMap<Object, NodeCommand> blockToCommand, HashMap<Metamodel, NodeCommand> mmNodes, Metamodel metamodel,
			NodeCommand mmodel, NodeCommand eobject) {

		var mmNode = cb.createNode(List.of(new NeoProp(NAME_PROP, metamodel.getName())),
				NeoCoreBootstrapper.LABELS_FOR_A_METAMODEL);

		mmNodes.put(metamodel, mmNode);

		cb.createEdge(CONFORMS_TO_PROP, mmNode, neocore);
//		cb.createEdge(META_TYPE, mmNode, mmodel);

		metamodel.getNodeBlocks().forEach(nb -> {
			var nbNode = cb.createNodeWithType(//
					List.of(new NeoProp(NAME_PROP, nb.getName()), //
							new NeoProp(ABSTRACT_PROP, nb.isAbstract()),
							new NeoProp(NAMESPACE_PROP, metamodel.getName())),
					NeoCoreBootstrapper.LABELS_FOR_AN_ECLASS, eclass);

			if (nb.getSuperTypes().isEmpty()) {
				cb.createEdge(ESUPER_TYPE, nbNode, eobject);
			}

			blockToCommand.put(nb, nbNode);
		});
	}

	private void handleNodeBlocksInModel(CypherCreator cb, HashMap<ModelNodeBlock, NodeCommand> blockToCommand,
			HashMap<Model, NodeCommand> mNodes, Model model, NodeCommand nodeCommandForModel) {

		var mNode = cb.createNode(List.of(new NeoProp(NAME_PROP, model.getName())),
				NeoCoreBootstrapper.LABELS_FOR_A_MODEL);

		mNodes.put(model, mNode);

		model.getNodeBlocks().forEach(nb -> {
			Metamodel mm = (Metamodel) nb.getType().eContainer();

			var mmNode = cb.matchNode(List.of(new NeoProp(NAME_PROP, mm.getName())),
					NeoCoreBootstrapper.LABELS_FOR_A_METAMODEL);

//			var typeOfNode = cb.matchNodeWithContainer(//
//					List.of(new NeoProp(NAME_PROP, nb.getType().getName()), new NeoProp(NAMESPACE_PROP, mm.getName())), //
//					NeoCoreBootstrapper.LABELS_FOR_AN_ECLASS, mm.getName());

			cb.createEdge(CONFORMS_TO_PROP, mNode, mmNode);
//			cb.createEdge(META_TYPE, mNode, nodeCommandForModel);

			// Handle attributes of model
			List<NeoProp> props = new ArrayList<>();
			nb.getProperties().forEach(ps -> {
				props.add(new NeoProp(ps.getType().getName(), inferType(ps, nb)));
			});

			props.add(new NeoProp(NAME_PROP, nb.getName()));
			props.add(new NeoProp(NAMESPACE_PROP, model.getName()));

			var allLabels = new ArrayList<String>();
			allLabels.addAll(computeLabelsFromType(nb.getType()));

			var nbNode = cb.createNode(props, allLabels);

			blockToCommand.put(nb, nbNode);
		});

	}

	public static List<String> computeLabelsFromType(MetamodelNodeBlock type) {
		var labels = new LinkedHashSet<String>();
		var namespace = ((Metamodel) type.eContainer()).getName();
		labels.add(NeoCoreBootstrapper.addNameSpace(namespace, type.getName()));

		for (MetamodelNodeBlock st : type.getSuperTypes())
			labels.addAll(computeLabelsFromType(st));

		labels.add(NeoCoreBootstrapper.addNeoCoreNamespace(EOBJECT));

		return new ArrayList<>(labels);
	}

	private Object inferType(ModelPropertyStatement ps, ModelNodeBlock nb) {
		String propName = EMSLUtil.getNameOfType(ps);
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

	private Object inferTypeForEdgeAttribute(ValueExpression value, String relName, String propName,
			MetamodelNodeBlock nodeType) {
		if (MARKERS.contains(propName)) {
			return PrimitiveBoolean.class.cast(value).isTrue();
		}

		if (propName.equals(TYPE_AS_ATTRIBUTE) && relName.equals(CORR)) {
			return PrimitiveString.class.cast(value).getLiteral();
		}

		var typedValue = EMSLUtil.allRelationsOf(nodeType).stream()//
				.filter(et -> et.getName().equals(relName))//
				.flatMap(et -> et.getProperties().stream())//
				.filter(etPs -> etPs.getName().equals(propName))//
				.map(etPs -> etPs.getType())//
				.map(t -> EMSLUtil.parseStringWithType(value, t))//
				.findAny();

		return typedValue.orElseThrow(() -> new IllegalStateException("Unable to infer type of " + value));
	}

	private Object inferTypeForNodeAttribute(ValueExpression value, String propName, MetamodelNodeBlock nodeType) {
		if (MARKERS.contains(propName)) {
			return PrimitiveBoolean.class.cast(value).isTrue();
		}

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

	public long noOfNodesInDatabase() {
		var result = executeQuery("MATCH (n) return count(n)");
		return (long) result.get(0).asMap().values().iterator().next();
	}

	public long noOfEdgesInDatabase() {
		var result = executeQuery("MATCH ()-[r]->() return count(r)");
		return (long) result.get(0).asMap().values().iterator().next();
	}

	public long noOfElementsInDatabase() {
		return noOfNodesInDatabase() + noOfEdgesInDatabase();
	}

	public void prepareModelWithTranslateAttribute(String modelName) {
		executeQueryForSideEffect(CypherBuilder.prepareTranslateAttributeForNodes(modelName));
		executeQueryForSideEffect(CypherBuilder.prepareTranslateAttributeForEdges(modelName));
	}

	public void removeTranslateAttributesFromModel(String modelName) {
		executeQueryForSideEffect(CypherBuilder.removeTranslationAttributeForNodes(modelName));
		executeQueryForSideEffect(CypherBuilder.removeTranslationAttributeForEdges(modelName));
	}

	/**
	 * Ids can contain negative or positive ids -- absolute values are used in Neo4j
	 * for deletion.
	 * 
	 * @param ids
	 */
	public void deleteEdges(Collection<Long> ids) {
		Map<String, Object> params = Map.of("ids", ids);
		executeQueryForSideEffect(CypherBuilder.deleteEdgesQuery("ids"), params);
	}

	public void deleteEdgesOfType(String type) {
		executeQueryForSideEffect(CypherBuilder.deleteEdgesOfType(type));
	}

	public void deleteAllCorrs() {
		deleteEdgesOfType(NeoCoreConstants.CORR);
	}

	public void deleteNodes(Collection<Long> ids) {
		Map<String, Object> params = Map.of("ids", ids);
		executeQueryForSideEffect(CypherBuilder.deleteNodesQuery("ids"), params);
	}

	/**
	 * Determine all elements contained in the provided model excluding the model
	 * node itself and all other "meta" edges such as elementOf and conformsTo
	 * edges. Edge IDs are differentiated from node IDs by making them negative.
	 * 
	 * @param model
	 * @return
	 */
	public Collection<Long> getAllElementsOfModel(String model) {
		var allNodes = executeQuery(CypherBuilder.getAllNodesInModel(model));
		var allEdges = executeQuery(CypherBuilder.getAllRelsInModel(model));
		var allIDs = new HashSet<Long>();
		allNodes.forEach(n -> n.values().forEach(v -> allIDs.add(v.asLong())));
		allEdges.forEach(r -> r.values().forEach(v -> allIDs.add(v.asLong() * -1)));
		return allIDs;
	}

	/**
	 * Determine all nodes contained in the provided model. The model node is
	 * included or excluded depending on the provided flag.
	 * 
	 * @param model            The ename of the model
	 * @return IDs of all nodes
	 */
	public Collection<Long> getAllNodesOfModel(String model) {
		var allNodes = executeQuery(CypherBuilder.getAllNodesInModel(model));
		var allIDs = new HashSet<Long>();
		allNodes.forEach(n -> n.values().forEach(v -> allIDs.add(v.asLong())));
		return allIDs;
	}

	/**
	 * Determine all relations contained in the provided model excluding all "meta"
	 * edges such as elementOf and conformsTo edges.
	 * 
	 * @param model The ename of the model
	 * @return IDs of all relations
	 */
	public Collection<Long> getAllRelsOfModel(String model) {
		var allEdges = executeQuery(CypherBuilder.getAllRelsInModel(model));
		var allIDs = new HashSet<Long>();
		allEdges.forEach(r -> r.values().forEach(v -> allIDs.add(v.asLong())));
		return allIDs;
	}

	public Collection<Long> getAllCorrs(String sourceModel, String targetModel) {
		var allCorrs = executeQuery(CypherBuilder.getAllCorrs(sourceModel, targetModel));
		var allIDs = new HashSet<Long>();
		allCorrs.forEach(n -> n.values().forEach(v -> allIDs.add(v.asLong() * -1)));
		return allIDs;
	}

	/**
	 * Determine all elements contained in the triple consisting of the provided
	 * source and target models, and all corrs between elements in both models. All
	 * meta edges are returned. Edge IDs are differentiated from node IDs by making
	 * them negative.
	 * 
	 * @param sourceModel
	 * @param targetModel
	 * @return
	 */
	public Collection<Long> getAllElementIDsInTriple(String sourceModel, String targetModel) {
		var allSrcNodes = executeQuery(CypherBuilder.getAllNodesInModel(sourceModel));
		var allTrgNodes = executeQuery(CypherBuilder.getAllNodesInModel(targetModel));

		var allSrcEdges = executeQuery(CypherBuilder.getAllRelsInModel(sourceModel));
		var allTrgEdges = executeQuery(CypherBuilder.getAllRelsInModel(targetModel));

		var allCorrs = executeQuery(CypherBuilder.getAllCorrs(sourceModel, targetModel));

		var allIDs = new HashSet<Long>();

		allSrcNodes.forEach(n -> n.values().forEach(v -> allIDs.add(v.asLong())));
		allTrgNodes.forEach(n -> n.values().forEach(v -> allIDs.add(v.asLong())));

		allSrcEdges.forEach(r -> r.values().forEach(v -> allIDs.add(v.asLong() * -1)));
		allTrgEdges.forEach(r -> r.values().forEach(v -> allIDs.add(v.asLong() * -1)));

		allCorrs.forEach(c -> c.values().forEach(v -> allIDs.add(v.asLong() * -1)));

		return allIDs;
	}

	public Collection<Long> getElementsInDelta(String sourceModel, String targetModel, String delta) {
		var srcNodes = executeQuery(CypherBuilder.getAllNodesInDelta(sourceModel, delta));
		var trgNodes = executeQuery(CypherBuilder.getAllNodesInDelta(targetModel, delta));

		var srcEdges = executeQuery(CypherBuilder.getAllEdgesInDelta(sourceModel, delta));
		var trgEdges = executeQuery(CypherBuilder.getAllEdgesInDelta(targetModel, delta));
		var corrs = executeQuery(CypherBuilder.getAllCorrsInDelta(sourceModel, targetModel, delta));

		var allIDs = new HashSet<Long>();

		srcNodes.forEach(n -> n.values().forEach(v -> allIDs.add(v.asLong())));
		trgNodes.forEach(n -> n.values().forEach(v -> allIDs.add(v.asLong())));

		srcEdges.forEach(r -> r.values().forEach(v -> allIDs.add(v.asLong() * -1)));
		trgEdges.forEach(r -> r.values().forEach(v -> allIDs.add(v.asLong() * -1)));
		corrs.forEach(r -> r.values().forEach(v -> allIDs.add(v.asLong() * -1)));

		return allIDs;
	}

	public Collection<Long> getCreateDelta(String sourceModel, String targetModel) {
		return getElementsInDelta(sourceModel, targetModel, NeoCoreConstants._CR_PROP);
	}

	public Collection<Long> getDeleteDelta(String sourceModel, String targetModel) {
		return getElementsInDelta(sourceModel, targetModel, NeoCoreConstants._DE_PROP);
	}

	public Collection<Long> getExistingElements(String sourceModel, String targetModel) {
		Collection<Long> allElementIDs = getAllElementIDsInTriple(sourceModel, targetModel);
		Collection<Long> allDeltaElements = getElementsInDelta(sourceModel, targetModel, NeoCoreConstants._EX_PROP);
		allDeltaElements.retainAll(allElementIDs);
		return allDeltaElements;
		
//		return getAllElementIDsInTriple(sourceModel, targetModel).stream()
//				.filter(e -> getElementsInDelta(sourceModel, targetModel, NeoCoreConstants._EX_PROP).contains(e))
//				.collect(Collectors.toSet());
	}

	public void setDeltaAttributes(Collection<Long> ids) {

	}

	public void prepareModelWithContextDeltaAttribute(String sourceModelName, String targetModelName) {

		executeQueryForSideEffect(
				CypherBuilder.prepareDeltaAttributeForNodes(sourceModelName, NeoCoreConstants._EX_PROP));
		executeQueryForSideEffect(
				CypherBuilder.prepareDeltaAttributeForEdges(sourceModelName, NeoCoreConstants._EX_PROP));

		executeQueryForSideEffect(
				CypherBuilder.prepareDeltaAttributeForNodes(targetModelName, NeoCoreConstants._EX_PROP));
		executeQueryForSideEffect(
				CypherBuilder.prepareDeltaAttributeForEdges(targetModelName, NeoCoreConstants._EX_PROP));

		executeQueryForSideEffect(CypherBuilder.prepareDeltaAttributeForCorrs(sourceModelName, targetModelName,
				NeoCoreConstants._EX_PROP));
	}

	public void removeContextDeltaAttributesFromModel(String sourceModelName, String targetModelName) {
		executeQueryForSideEffect(
				CypherBuilder.removeDeltaAttributeForNodes(sourceModelName, NeoCoreConstants._EX_PROP));
		executeQueryForSideEffect(
				CypherBuilder.removeDeltaAttributeForEdges(sourceModelName, NeoCoreConstants._EX_PROP));

		executeQueryForSideEffect(
				CypherBuilder.removeDeltaAttributeForNodes(targetModelName, NeoCoreConstants._EX_PROP));
		executeQueryForSideEffect(
				CypherBuilder.removeDeltaAttributeForEdges(targetModelName, NeoCoreConstants._EX_PROP));

		executeQueryForSideEffect(CypherBuilder.removeDeltaAttributeForCorrs(sourceModelName, targetModelName,
				NeoCoreConstants._EX_PROP));
	}
}