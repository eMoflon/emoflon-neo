package org.emoflon.neo.neo4j.adapter;

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
import org.emoflon.neo.emsl.EMSLFlattener;
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
import org.emoflon.neo.emsl.eMSL.UserDefinedType;
import org.emoflon.neo.emsl.eMSL.Value;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.StatementResult;

import com.google.common.collect.Lists;
import com.google.common.collect.Streams;

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
	private static final String EATTRIBUTED_ELEMENT = "EAttributedElement";
	private static final String METAMODEL = "MetaModel";
	private static final String MODEL = "Model";
	private static final String EOBJECT = "EObject";

	// EReferences
	private static final String EREFERENCE_TYPE = "eReferenceType";
	private static final String EREFERENCES = "eReferences";
	private static final String ESUPER_TYPE = "eSuperType";
	private static final String EATTRIBUTE_TYPE = "eAttributeType";
	private static final String EATTRIBUTES = "eAttributes";
	private static final String ELITERALS = "eLiterals";

	// EDataType
	private static final String EENUM = "EEnum";
	private static final String EENUM_LITERAL = "EEnumLiteral";
	private static final String ESTRING = "EString";
	private static final String EINT = "EInt";
	private static final String EBOOLEAN = "EBoolean";

	// Attributes
	private static final String NAME_PROP = "ename";
	private static final String ABSTRACT_PROP = "abstract";

	// Meta attributes and relations
	private static final String CONFORMS_TO_PROP = "conformsTo";

	// Lists of properties and labels for meta types
	private static final List<NeoProp> neoCoreProps = List.of(new NeoProp(NAME_PROP, EMSLUtil.ORG_EMOFLON_NEO_CORE));
	private static final List<String> neoCoreLabels = List.of(METAMODEL, MODEL, EOBJECT);

	private static final List<NeoProp> eclassProps = List.of(new NeoProp(NAME_PROP, ECLASS));
	private static final List<String> eclassLabels = List.of(ECLASS, EOBJECT, ECLASSIFIER, EATTRIBUTED_ELEMENT);

	private static final List<NeoProp> mmodelProps = List.of(new NeoProp(NAME_PROP, METAMODEL));
	private static final List<String> mmodelLabels = List.of(ECLASS, ECLASSIFIER, EATTRIBUTED_ELEMENT, EOBJECT);

	private static final List<NeoProp> modelProps = List.of(new NeoProp(NAME_PROP, MODEL));
	private static final List<String> modelLabels = List.of(ECLASS, ECLASSIFIER, EATTRIBUTED_ELEMENT, EOBJECT);

	private static final List<NeoProp> eobjectProps = List.of(new NeoProp(NAME_PROP, EOBJECT));
	private static final List<String> eobjectLabels = List.of(ECLASS, ECLASSIFIER, EATTRIBUTED_ELEMENT, EOBJECT);

	private static final List<NeoProp> erefProps = List.of(new NeoProp(NAME_PROP, EREFERENCE));
	private static final List<String> erefLabels = List.of(ECLASS, EATTRIBUTED_ELEMENT, ECLASSIFIER, EOBJECT);

	private static final List<NeoProp> eleofProps = List.of(new NeoProp(NAME_PROP, META_EL_OF));
	private static final List<String> eleofLabels = List.of(EREFERENCE, EATTRIBUTED_ELEMENT, ESTRUCTURAL_FEATURE,
			ETYPED_ELEMENT, EOBJECT);

	private static final List<NeoProp> conformtoProps = List.of(new NeoProp(NAME_PROP, CONFORMS_TO_PROP));
	private static final List<String> conformtoLabels = List.of(EREFERENCE, EATTRIBUTED_ELEMENT, ESTRUCTURAL_FEATURE,
			ETYPED_ELEMENT, EOBJECT);

	private static final List<NeoProp> erefsProps = List.of(new NeoProp(NAME_PROP, EREFERENCES));
	private static final List<String> erefsLabels = List.of(EREFERENCE, EATTRIBUTED_ELEMENT, ESTRUCTURAL_FEATURE,
			ETYPED_ELEMENT, EOBJECT);

	private static final List<NeoProp> eRefTypeProps = List.of(new NeoProp(NAME_PROP, EREFERENCE_TYPE));
	private static final List<String> eRefTypeLabels = List.of(EREFERENCE, EATTRIBUTED_ELEMENT, ESTRUCTURAL_FEATURE,
			ETYPED_ELEMENT, EOBJECT);

	private static final List<NeoProp> eattrProps = List.of(new NeoProp(NAME_PROP, EATTRIBUTE));
	private static final List<String> eattrLabels = List.of(ECLASS, EOBJECT, EATTRIBUTED_ELEMENT, ECLASSIFIER);

	private static final List<NeoProp> nameProps = List.of(new NeoProp(NAME_PROP, NAME_PROP));
	private static final List<String> nameLabels = List.of(EATTRIBUTE, EOBJECT, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT);

	private static final List<NeoProp> eDataTypeProps = List.of(new NeoProp(NAME_PROP, EDATA_TYPE));
	private static final List<String> eDataTypeLabels = List.of(ECLASS, EOBJECT, ECLASSIFIER, EATTRIBUTED_ELEMENT);

	private static final List<NeoProp> eAttrEleProps = List.of(new NeoProp(NAME_PROP, EATTRIBUTED_ELEMENT));
	private static final List<String> eAttrEleLabels = List.of(ECLASS, EOBJECT, ECLASSIFIER, EATTRIBUTED_ELEMENT);

	private static final List<NeoProp> eStringProps = List.of(new NeoProp(NAME_PROP, ESTRING));
	private static final List<String> eStringLabels = List.of(EDATA_TYPE, ECLASSIFIER, EOBJECT);

	private static final List<NeoProp> eintProps = List.of(new NeoProp(NAME_PROP, EINT));
	private static final List<String> eintLabels = List.of(EDATA_TYPE, ECLASSIFIER, EOBJECT);

	private static final List<NeoProp> eAttrTypeProps = List.of(new NeoProp(NAME_PROP, EATTRIBUTE_TYPE));
	private static final List<String> eAttrTypeLabels = List.of(EREFERENCE, EATTRIBUTED_ELEMENT, ESTRUCTURAL_FEATURE,
			ETYPED_ELEMENT, EOBJECT);

	private static final List<NeoProp> eSupTypeProps = List.of(new NeoProp(NAME_PROP, ESUPER_TYPE));
	private static final List<String> eSupTypeLabels = List.of(EREFERENCE, EATTRIBUTED_ELEMENT, ESTRUCTURAL_FEATURE,
			ETYPED_ELEMENT, EOBJECT);

	private static final List<NeoProp> eclassifierProps = List.of(new NeoProp(NAME_PROP, ECLASSIFIER));
	private static final List<String> eclassifierLabels = List.of(ECLASS, EOBJECT, ECLASSIFIER, EATTRIBUTED_ELEMENT);

	private static final List<NeoProp> eTypedeleProps = List.of(new NeoProp(NAME_PROP, ETYPED_ELEMENT));
	private static final List<String> eTypedeleLabels = List.of(ECLASS, EOBJECT, ECLASSIFIER, EATTRIBUTED_ELEMENT);

	private static final List<NeoProp> metaTypeProps = List.of(new NeoProp(NAME_PROP, META_TYPE));
	private static final List<String> metaTypeLabels = List.of(EREFERENCE, EATTRIBUTED_ELEMENT, ESTRUCTURAL_FEATURE,
			ETYPED_ELEMENT, EOBJECT);

	private static final List<NeoProp> eAttributesProps = List.of(new NeoProp(NAME_PROP, EATTRIBUTES));
	private static final List<String> eAttributesLabels = List.of(EREFERENCE, EATTRIBUTED_ELEMENT, ESTRUCTURAL_FEATURE,
			ETYPED_ELEMENT, EOBJECT);

	private static final List<NeoProp> eStructProps = List.of(new NeoProp(NAME_PROP, ESTRUCTURAL_FEATURE));
	private static final List<String> eStructLabels = List.of(ECLASS, ECLASSIFIER, EATTRIBUTED_ELEMENT, EOBJECT);

	private static final List<NeoProp> abstractattrProps = List.of(new NeoProp(NAME_PROP, ABSTRACT_PROP));
	private static final List<String> abstractattrLabels = List.of(EATTRIBUTE, EOBJECT, ESTRUCTURAL_FEATURE,
			ETYPED_ELEMENT);

	private static final List<NeoProp> eBooleanProps = List.of(new NeoProp(NAME_PROP, EBOOLEAN));
	private static final List<String> eBooleanLabels = List.of(EDATA_TYPE, ECLASSIFIER, EOBJECT);

	private static final List<NeoProp> eenumProps = List.of(new NeoProp(NAME_PROP, EENUM));
	private static final List<String> eenumLabels = List.of(ECLASS, ECLASSIFIER, EATTRIBUTED_ELEMENT, EOBJECT);

	private static final List<NeoProp> eenumLiteralProps = List.of(new NeoProp(NAME_PROP, EENUM_LITERAL));
	private static final List<String> eenumLiteralLabels = List.of(ECLASS, ECLASSIFIER, EATTRIBUTED_ELEMENT, EOBJECT);

	private static final List<NeoProp> eLiteralsProps = List.of(new NeoProp(NAME_PROP, ELITERALS));
	private static final List<String> eLiteralsLabels = List.of(EREFERENCE, EATTRIBUTED_ELEMENT, ESTRUCTURAL_FEATURE,
			ETYPED_ELEMENT, EOBJECT);

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

	private void exportModelToNeo4j(Model m) {
		bootstrapNeoCoreIfNecessary();

		ResourceSet rs = m.eResource().getResourceSet();
		EcoreUtil.resolveAll(rs);

		var models = collectReferencedModels(m);
		models.add(m);

		var metamodels = models.stream()//
				.flatMap(model -> collectDependentMetamodels(m).stream())//
				.collect(Collectors.toSet());

		var metamodelNames = metamodels.stream().map(Metamodel::getName).collect(Collectors.joining(","));
		logger.info("Trying to export metamodels: " + metamodelNames);
		var newMetamodels = removeExistingMetamodels(metamodels);

		// Remove abstract metamodels
		newMetamodels = newMetamodels.stream().filter(mm -> !mm.isAbstract()).collect(Collectors.toList());

		for (Metamodel mm : metamodels) {
			if (!newMetamodels.contains(mm))
				logger.info("Skipping metamodel " + mm.getName() + " as it is already present or is abstract.");
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

		// Remove abstract metamodels
		newMetamodels = newMetamodels.stream().filter(mm -> !mm.isAbstract()).collect(Collectors.toList());

		for (Metamodel mm : metamodels) {
			if (!newMetamodels.contains(mm))
				logger.info("Skipping metamodel " + mm.getName() + " as it is already present or is abstract.");
		}

		if (!newMetamodels.isEmpty())
			exportMetamodelsToNeo4j(newMetamodels);

		logger.info("Exported metamodels: " + newMetamodels);
	}

	public static boolean canBeExported(EClass eclass) {
		return eclass.equals(EMSLPackage.eINSTANCE.getMetamodel()) || eclass.equals(EMSLPackage.eINSTANCE.getModel());
	}

	public static boolean canBeCoppiedToClipboard(EClass eclass) {
		return eclass.equals(EMSLPackage.eINSTANCE.getAtomicPattern())
				|| eclass.equals(EMSLPackage.eINSTANCE.getPattern())
				|| eclass.equals(EMSLPackage.eINSTANCE.getConstraint())
				|| eclass.equals(EMSLPackage.eINSTANCE.getCondition());
	}

	private void bootstrapNeoCore() {
		executeActionAsCreateTransaction((cb) -> {
			var neocore = cb.createNode(neoCoreProps, neoCoreLabels);
			var eclass = cb.createNodeWithCont(eclassProps, eclassLabels, neocore);
			var mmodel = cb.createNodeWithContAndType(mmodelProps, mmodelLabels, eclass, neocore);
			var model = cb.createNodeWithContAndType(modelProps, modelLabels, eclass, neocore);
			var eobject = cb.createNodeWithContAndType(eobjectProps, eobjectLabels, eclass, neocore);
			var eref = cb.createNodeWithContAndType(erefProps, erefLabels, eclass, neocore);
			var eleof = cb.createNodeWithContAndType(eleofProps, eleofLabels, eref, neocore);
			var conformto = cb.createNodeWithContAndType(conformtoProps, conformtoLabels, eref, neocore);
			var erefs = cb.createNodeWithContAndType(erefsProps, erefsLabels, eref, neocore);
			var eRefType = cb.createNodeWithContAndType(eRefTypeProps, eRefTypeLabels, eref, neocore);
			var eattr = cb.createNodeWithContAndType(eattrProps, eattrLabels, eclass, neocore);
			var name = cb.createNodeWithContAndType(nameProps, nameLabels, eattr, neocore);
			var eDataType = cb.createNodeWithContAndType(eDataTypeProps, eDataTypeLabels, eclass, neocore);
			var eAttrEle = cb.createNodeWithContAndType(eAttrEleProps, eAttrEleLabels, eclass, neocore);
			var eString = cb.createNodeWithContAndType(eStringProps, eStringLabels, eDataType, neocore);
			@SuppressWarnings("unused")
			var eint = cb.createNodeWithContAndType(eintProps, eintLabels, eDataType, neocore);
			var eAttrType = cb.createNodeWithContAndType(eAttrTypeProps, eAttrTypeLabels, eref, neocore);
			var eSupType = cb.createNodeWithContAndType(eSupTypeProps, eSupTypeLabels, eref, neocore);
			var eclassifier = cb.createNodeWithContAndType(eclassifierProps, eclassifierLabels, eclass, neocore);
			var eTypedele = cb.createNodeWithContAndType(eTypedeleProps, eTypedeleLabels, eclass, neocore);
			var metaType = cb.createNodeWithContAndType(metaTypeProps, metaTypeLabels, eref, neocore);
			var eAttributes = cb.createNodeWithContAndType(eAttributesProps, eAttributesLabels, eref, neocore);
			var eStruct = cb.createNodeWithContAndType(eStructProps, eStructLabels, eclass, neocore);
			var abstractattr = cb.createNodeWithContAndType(abstractattrProps, abstractattrLabels, eattr, neocore);
			var eBoolean = cb.createNodeWithContAndType(eBooleanProps, eBooleanLabels, eDataType, neocore);
			var eenum = cb.createNodeWithContAndType(eenumProps, eenumLabels, eclass, neocore);
			var eenumLiteral = cb.createNodeWithContAndType(eenumLiteralProps, eenumLabels, eclass, neocore);
			var eLiterals = cb.createNodeWithContAndType(eLiteralsProps, eLiteralsLabels, eref, neocore);

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
			cb.createEdge(EATTRIBUTES, eclass, abstractattr);
			cb.createEdge(EATTRIBUTE_TYPE, abstractattr, eBoolean);
			cb.createEdge(EREFERENCES, model, conformto);
			cb.createEdge(EREFERENCE_TYPE, conformto, mmodel);
			cb.createEdge(EREFERENCES, eobject, eleof);
			cb.createEdge(EREFERENCE_TYPE, eleof, model);
			cb.createEdge(EREFERENCES, eenum, eLiterals);
			cb.createEdge(EREFERENCE_TYPE, eLiterals, eenumLiteral);
			cb.createEdge(ESUPER_TYPE, eclass, eAttrEle);
			cb.createEdge(ESUPER_TYPE, eclass, eclassifier);
			cb.createEdge(ESUPER_TYPE, eDataType, eclassifier);
			cb.createEdge(ESUPER_TYPE, eref, eAttrEle);
			cb.createEdge(ESUPER_TYPE, eref, eStruct);
			cb.createEdge(ESUPER_TYPE, eattr, eStruct);
			cb.createEdge(ESUPER_TYPE, eenum, eDataType);
			cb.createEdge(ESUPER_TYPE, mmodel, model);
			cb.createEdge(ESUPER_TYPE, eStruct, eTypedele);
			cb.createEdge(ESUPER_TYPE, eTypedele, eobject);
			cb.createEdge(ESUPER_TYPE, eclassifier, eobject);
			cb.createEdge(ESUPER_TYPE, model, eobject);
			cb.createEdge(ESUPER_TYPE, eAttrEle, eobject);
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
			cb.returnWith(
					cb.matchNode(List.of(new NeoProp(NAME_PROP, EMSLUtil.ORG_EMOFLON_NEO_CORE)), List.of(METAMODEL)));
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
			var neocore = cb.matchNode(neoCoreProps, neoCoreLabels);
			var model = cb.matchNodeWithContainer(modelProps, modelLabels, neocore);

			// Create nodes and edges in models
			var mNodes = new HashMap<Model, NodeCommand>();
			var blockToCommand = new HashMap<ModelNodeBlock, NodeCommand>();
			for (var newModel : newModels) {
				handleNodeBlocksInModel(cb, blockToCommand, mNodes, newModel, model);
			}
			for (var newModel : newModels) {
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
					List.of(EATTRIBUTE, EOBJECT, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT), eattribute, mmNode);
			var attrOwner = blockToCommand.get(nb);
			var nameOfTypeofAttr = inferType(ps);

			var dataType = ps.getType();
			NodeCommand typeofattr = null;
			if (dataType instanceof BuiltInType) {
				typeofattr = cb.matchNodeWithContainer(//
						List.of(new NeoProp(NAME_PROP, nameOfTypeofAttr)), //
						List.of(EDATA_TYPE, ECLASSIFIER, EOBJECT), neocore);
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
			var ref = cb.createNodeWithContAndType(//
					List.of(new NeoProp(NAME_PROP, rs.getName())), //
					List.of(EREFERENCE, EOBJECT, EATTRIBUTED_ELEMENT, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT), eref,
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
		}
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
					eclassLabels, eclass, mmNode);

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
			String relName = EMSLUtil.getOnlyType(rs).getName();
			return inferTypeForEdgeAttribute(ps.getValue(), relName, propName, nodeType);
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

	public void exportEMSLEntityToNeo4j(Entity entity) {
		try {
			var flattenedEntity = new EMSLFlattener().flattenEntity(entity, new ArrayList<String>());
			if (flattenedEntity instanceof Model) {
				exportModelToNeo4j((Model) flattenedEntity);
			} else if (flattenedEntity instanceof Metamodel)
				exportMetamodelToNeo4j((Metamodel) flattenedEntity);
			else
				throw new IllegalArgumentException("This type of entity cannot be exported: " + entity);
		} catch (FlattenerException e) {
			logger.error("EMSL Flattener was unable to process the entity.");
			e.printStackTrace();
		}
	}

}