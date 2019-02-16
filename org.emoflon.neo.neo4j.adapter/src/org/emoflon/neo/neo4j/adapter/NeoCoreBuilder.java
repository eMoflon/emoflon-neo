package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Consumer;

import org.eclipse.emf.ecore.EClass;
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
	private static final String METAMETAMODEL = "_Metametamodel_";
	private static final String METAMODEL = "_Metamodel_";

	private final Driver driver;

	public NeoCoreBuilder(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}

	@Override
	public void close() throws Exception {
		driver.close();
	}

	public void bootstrapNeoCore() {
		executeActionAsTransaction((cb) -> {
			var neocore = cb.createNode()//
					.withLabel(METAMETAMODEL)//
					.withStringProperty(URI_PROP, ORG_EMOFLON_NEO_CORE);

			cb.createEdge()//
					.withLabel(CONFORMS_TO_PROP)//
					.from(neocore)//
					.to(neocore);

			var eclass = cb.createNode()//
					.withLabel(ECLASS)//
					.withStringProperty(NAME_PROP, ECLASS)//
					.elementOf(neocore);
			eclass.withType(eclass);

			var eref = cb.createNode()//
					.withLabel(ECLASS)//
					.withStringProperty(NAME_PROP, EREFERENCE)//
					.withType(eclass)//
					.elementOf(neocore);

			var erefs = cb.createNode()//
					.withLabel(EREFERENCE)//
					.withStringProperty(NAME_PROP, EREFERENCES)//
					.withType(eref)//
					.elementOf(neocore);

			var eRefType = cb.createNode()//
					.withLabel(EREFERENCE)//
					.withStringProperty(NAME_PROP, EREFERENCE_TYPE)//
					.withType(eref)//
					.elementOf(neocore);

			var eattr = cb.createNode()//
					.withLabel(ECLASS)//
					.withStringProperty(NAME_PROP, EATTRIBUTE)//
					.withType(eclass)//
					.elementOf(neocore);

			var name = cb.createNode()//
					.withLabel(EATTRIBUTE)//
					.withStringProperty(NAME_PROP, NAME_PROP)//
					.withType(eattr)//
					.elementOf(neocore);

			var eDataType = cb.createNode()//
					.withLabel(ECLASS)//
					.withStringProperty(NAME_PROP, EDATA_TYPE)//
					.withType(eclass)//
					.elementOf(neocore);

			var eAttrEle = cb.createNode()//
					.withLabel(ECLASS)//
					.withStringProperty(NAME_PROP, EATTRIBUTED_ELEMENTS)//
					.withType(eclass)//
					.elementOf(neocore);

			var eString = cb.createNode()//
					.withLabel(EDATA_TYPE)//
					.withStringProperty(NAME_PROP, ESTRING)//
					.withType(eDataType)//
					.elementOf(neocore);

			cb.createNode()//
					.withLabel(EDATA_TYPE)//
					.withStringProperty(NAME_PROP, EINT)//
					.withType(eDataType)//
					.elementOf(neocore);

			var eAttrType = cb.createNode()//
					.withLabel(EREFERENCE)//
					.withStringProperty(NAME_PROP, EATTRIBUTE_TYPE)//
					.withType(eref)//
					.elementOf(neocore);

			var eSupType = cb.createNode()//
					.withLabel(EREFERENCE)//
					.withStringProperty(NAME_PROP, ESUPER_TYPE)//
					.withType(eref)//
					.elementOf(neocore);

			var eclassifier = cb.createNode()//
					.withLabel(ECLASS)//
					.withStringProperty(NAME_PROP, ECLASSIFIER)//
					.withType(eclass)//
					.elementOf(neocore);

			var eTypedele = cb.createNode()//
					.withLabel(ECLASS)//
					.withStringProperty(NAME_PROP, ETYPED_ELEMENT)//
					.withType(eclass)//
					.elementOf(neocore);

			var enamedele = cb.createNode()//
					.withLabel(ECLASS)//
					.withStringProperty(NAME_PROP, ENAMED_ELEMENT)//
					.withType(eclass)//
					.elementOf(neocore);

			var eType = cb.createNode()//
					.withLabel(EREFERENCE)//
					.withStringProperty(NAME_PROP, ETYPE)//
					.withType(eref)//
					.elementOf(neocore);

			var eAttributes = cb.createNode()//
					.withLabel(EREFERENCE)//
					.withStringProperty(NAME_PROP, EATTRIBUTES)//
					.withType(eref)//
					.elementOf(neocore);

			var eStruct = cb.createNode()//
					.withLabel(ECLASS)//
					.withStringProperty(NAME_PROP, ESTRUCTURAL_FEATURE)//
					.withType(eclass)//
					.elementOf(neocore);

			var abstractattr = cb.createNode()//
					.withLabel(EATTRIBUTE)//
					.withStringProperty(NAME_PROP, ABSTRACT_PROP)//
					.withType(eattr)//
					.elementOf(neocore);

			var eBoolean = cb.createNode()//
					.withLabel(EDATA_TYPE)//
					.withStringProperty(NAME_PROP, EBOOLEAN)//
					.withType(eDataType)//
					.elementOf(neocore);

			cb.createEdge().withLabel(EREFERENCES).from(eclass).to(erefs);
			cb.createEdge().withLabel(EREFERENCE_TYPE).from(erefs).to(eref);
			cb.createEdge().withLabel(EREFERENCES).from(eref).to(eRefType);
			cb.createEdge().withLabel(EREFERENCES).from(eclass).to(eSupType);
			cb.createEdge().withLabel(EREFERENCE_TYPE).from(eSupType).to(eclass);
			cb.createEdge().withLabel(EATTRIBUTE).from(enamedele).to(name);
			cb.createEdge().withLabel(EATTRIBUTE_TYPE).from(name).to(eString);
			cb.createEdge().withLabel(EREFERENCES).from(eattr).to(eAttrType);
			cb.createEdge().withLabel(EREFERENCE_TYPE).from(eAttrType).to(eDataType);
			cb.createEdge().withLabel(EREFERENCES).from(eTypedele).to(eType);
			cb.createEdge().withLabel(EREFERENCE_TYPE).from(eType).to(eclassifier);
			cb.createEdge().withLabel(EREFERENCES).from(eAttrEle).to(eAttributes);
			cb.createEdge().withLabel(EREFERENCE_TYPE).from(eAttributes).to(eattr);
			cb.createEdge().withLabel(ESUPER_TYPE).from(eclass).to(eAttrEle);
			cb.createEdge().withLabel(ESUPER_TYPE).from(eref).to(eAttrEle);
			cb.createEdge().withLabel(ESUPER_TYPE).from(eref).to(eStruct);
			cb.createEdge().withLabel(ESUPER_TYPE).from(eattr).to(eStruct);
			cb.createEdge().withLabel(ESUPER_TYPE).from(eclass).to(eclassifier);
			cb.createEdge().withLabel(ESUPER_TYPE).from(eDataType).to(eclassifier);
			cb.createEdge().withLabel(ESUPER_TYPE).from(eStruct).to(eTypedele);
			cb.createEdge().withLabel(ESUPER_TYPE).from(eTypedele).to(enamedele);
			cb.createEdge().withLabel(ESUPER_TYPE).from(eclassifier).to(enamedele);
			cb.createEdge().withLabel(EATTRIBUTE).from(eclass).to(abstractattr);
			cb.createEdge().withLabel(EATTRIBUTE_TYPE).from(abstractattr).to(eBoolean);
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
			cb.returnWith(cb.matchNode()//
					.withLabel(METAMETAMODEL)//
					.withStringProperty(URI_PROP, ORG_EMOFLON_NEO_CORE));
		});

		return result.stream().count() == 0;
	}

	public static boolean canBeExported(EClass eclass) {
		return eclass.equals(EMSLPackage.eINSTANCE.getMetamodel()) || eclass.equals(EMSLPackage.eINSTANCE.getModel());
	}

	public void exportModelToNeo4j(Model model) {
		// TODO [Export of model]

		// 1. Somehow determine all required metamodels

		// 2. Export all metamodels

		// 3. Somehow determine all required models

		// 4. Export all models
	}

	public void exportMetamodelsToNeo4j(List<Metamodel> newMetamodels) {
		executeActionAsTransaction((cb) -> {
			// Match required classes from NeoCore
			var neocore = cb.matchNode()//
					.withLabel(METAMETAMODEL)//
					.withStringProperty(URI_PROP, ORG_EMOFLON_NEO_CORE);

			var eclass = cb.matchNode()//
					.withLabel(ECLASS)//
					.withStringProperty(NAME_PROP, ECLASS)//
					.elementOf(neocore);

			var eref = cb.matchNode()//
					.withLabel(ECLASS)//
					.withStringProperty(NAME_PROP, EREFERENCE)//
					.elementOf(neocore);

			var edatatype = cb.matchNode()//
					.withLabel(ECLASS)//
					.withStringProperty(NAME_PROP, EDATA_TYPE)//
					.elementOf(neocore);

			var eattribute = cb.matchNode()//
					.withLabel(ECLASS)//
					.withStringProperty(NAME_PROP, EATTRIBUTE)//
					.elementOf(neocore);

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

	public List<Metamodel> removeExisting(List<Metamodel> metamodels) {
		var newMetamodels = new ArrayList<Metamodel>();
		newMetamodels.addAll(metamodels);
		StatementResult result = executeActionAsTransaction(cb -> {
			var nc = cb.matchNode().withLabel(METAMODEL);
			cb.returnWith(nc);
		});
		result.forEachRemaining(mmNode -> newMetamodels.removeIf(mm -> mm.getName().equals(mmNode.get(0).get(URI_PROP).asString())));
		return newMetamodels;
	}

	private void handleAttributes(CypherBuilder cb, NodeCommand neocore, NodeCommand edatatype, NodeCommand eattribute,
			HashMap<NodeBlock, NodeCommand> blockToCommand, NodeCommand mmNode, NodeBlock nb) {
		for (var ps : nb.getPropertyStatements()) {
			var attr = cb.createNode()//
					.withLabel(EATTRIBUTE)//
					.withStringProperty(NAME_PROP, ps.getName())//
					.withType(eattribute)//
					.elementOf(mmNode);

			var attrOwner = blockToCommand.get(nb);
			var nameOfTypeofAttr = ps.getValue();

			var typeofattr = cb.matchNode()//
					.withLabel(EDATA_TYPE)//
					.withStringProperty(NAME_PROP, nameOfTypeofAttr)//
					.withType(edatatype)//
					.elementOf(neocore);

			cb.createEdge().withLabel(EATTRIBUTES).from(attrOwner).to(attr);
			cb.createEdge().withLabel(EATTRIBUTE_TYPE).from(attr).to(typeofattr);
		}
	}

	private void handleInheritance(CypherBuilder cb, HashMap<NodeBlock, NodeCommand> blockToCommand, NodeBlock nb) {
		for (var st : nb.getSuperTypes()) {
			var nodeblock = blockToCommand.get(nb);
			var sType = blockToCommand.get(st);
			cb.createEdge().withLabel(ESUPER_TYPE).from(nodeblock).to(sType);
		}
	}

	private void handleRelationStatement(CypherBuilder cb, NodeCommand neocore, NodeCommand eref, NodeCommand edatatype,
			NodeCommand eattribute, HashMap<NodeBlock, NodeCommand> blockToCommand, NodeCommand mmNode, NodeBlock nb) {
		for (var rs : nb.getRelationStatements()) {
			var ref = cb.createNode()//
					.withLabel(EREFERENCE)//
					.withStringProperty(NAME_PROP, rs.getName())//
					.withType(eref)//
					.elementOf(mmNode);

			var refOwner = blockToCommand.get(nb);
			var typeOfRef = blockToCommand.get(rs.getValue());

			cb.createEdge().withLabel(EREFERENCES).from(refOwner).to(ref);
			cb.createEdge().withLabel(EREFERENCE_TYPE).from(ref).to(typeOfRef);

			// Handle attributes of the relation
			rs.getPropertyStatements().forEach(ps -> {
				var attr = cb.createNode()//
						.withLabel(EATTRIBUTE)//
						.withStringProperty(NAME_PROP, ps.getName())//
						.withType(eattribute)//
						.elementOf(mmNode);

				var nameOfTypeofAttr = ps.getValue();

				var typeofattr = cb.matchNode()//
						.withLabel(EDATA_TYPE)//
						.withStringProperty(NAME_PROP, nameOfTypeofAttr)//
						.withType(edatatype)//
						.elementOf(neocore);

				cb.createEdge().withLabel(EATTRIBUTES).from(ref).to(attr);
				cb.createEdge().withLabel(EATTRIBUTE_TYPE).from(attr).to(typeofattr);
			});
		}
	}

	private void handleNodeBlocks(CypherBuilder cb, NodeCommand neocore, NodeCommand eclass,
			HashMap<NodeBlock, NodeCommand> blockToCommand, HashMap<Metamodel, NodeCommand> mmNodes,
			Metamodel metamodel) {
		var mmNode = cb.createNode()//
				.withLabel(METAMODEL)//
				.withStringProperty(URI_PROP, metamodel.getName());
		mmNodes.put(metamodel, mmNode);

		cb.createEdge()//
				.withLabel(CONFORMS_TO_PROP)//
				.from(mmNode)//
				.to(neocore);

		metamodel.getNodeBlocks().forEach(nb -> {
			var nbNode = cb.createNode()//
					.withLabel(ECLASS)//
					.withStringProperty(NAME_PROP, nb.getName())//
					.withProperty(ABSTRACT_PROP, String.valueOf(nb.isAbstract()))//
					.withType(eclass)//
					.elementOf(mmNode);
			blockToCommand.put(nb, nbNode);
		});
	}
}