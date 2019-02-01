package org.emoflon.neo.neo4j.adapter;

import java.util.function.Consumer;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

public class NeoCoreBuilder implements AutoCloseable {
	private final Driver driver;

	public NeoCoreBuilder(String uri, String user, String password) {
		driver = GraphDatabase.driver(uri, AuthTokens.basic(user, password));
	}

	public NeoCoreBuilder() {
		this("bolt://localhost:11020", "neo4j", "password");
	}

	@Override
	public void close() throws Exception {
		driver.close();
	}

	public void bootstrapNeoCore() {
		executeActionAsTransaction((cb) -> {
			NodeCommand neocore = cb.createNode()//
					.withLabel("_Metametamodel_")//
					.withStringProperty("_uri_", "org.emoflon.neo.NeoCore");
			cb.createEdge().withLabel("_conformsTo_").from(neocore).to(neocore);

			NodeCommand eclass = cb.createNode()//
					.withLabel("EClass")//
					.withStringProperty("name", "EClass")//
					.elementOf(neocore);
			eclass.withType(eclass);

			NodeCommand eref = cb.createNode()//
					.withLabel("EClass")//
					.withStringProperty("name", "EReference")//
					.withType(eclass)//
					.elementOf(neocore);

			NodeCommand erefs = cb.createNode()//
					.withLabel("EReference")//
					.withStringProperty("name", "eReferences")//
					.withType(eref)//
					.elementOf(neocore);

			NodeCommand eRefType = cb.createNode()//
					.withLabel("EReference")//
					.withStringProperty("name", "eReferenceType")//
					.withType(eref)//
					.elementOf(neocore);	
			
			NodeCommand eSupType = cb.createNode()
					.withLabel("EClass")
					.withStringProperty("name","eSuperTypes")
					.withType(eclass)
					.elementOf(neocore);
			
			NodeCommand eAttrEle = cb.createNode()
					.withLabel("EAttribute")
					.withStringProperty("name","EAttributedElements")
					.elementOf(neocore);	
			   eAttrEle.withType(eAttrEle);
			   
			   NodeCommand eAttr = cb.createNode()
						.withLabel("EAttribute")
						.withStringProperty("name","EAttribute")
						.withType(eAttrEle)
						.elementOf(neocore);
			   
			   NodeCommand eStructure = cb.createNode()
						.withLabel("EReference")
						.withStringProperty("name","EStructuralFeature")
						.withType(eref)
						.elementOf(neocore);
			   
			   NodeCommand eData = cb.createNode()
						.withLabel("EAttribute")
						.withStringProperty("name", "EDataType")
						.elementOf(neocore);
			          eData.withType(eData);
			   

				NodeCommand eAttrType = cb.createNode()
						.withLabel("EAttribute")
						.withStringProperty("name", "eAttributeType")
						.withType(eAttr)
						.elementOf(neocore);
			
				NodeCommand eclassifier = cb.createNode()
						.withLabel("EClass")
						.withStringProperty("name", "EClassifier")
						.elementOf(neocore);
				eclassifier.withType(eclassifier);
				

				NodeCommand etypedele = cb.createNode()
						.withLabel("EStructuralFeature")
						.withStringProperty("name", "ETypedElement")
						.elementOf(neocore);
				etypedele.withType(etypedele);
				
				
				NodeCommand etype = cb.createNode()
						.withLabel("ETypedElement")
						.withStringProperty("name", "eType")
						.elementOf(neocore);
				etype.withType(etype);
				
				NodeCommand enamedele = cb.createNode()
						.withLabel("ENamedElement")
						.withStringProperty("name", "ENamedElement")
						.elementOf(neocore);
				enamedele.withType(enamedele);
			
				
            cb.createEdge().withLabel("eReferences").from(eclass).to(erefs);
			cb.createEdge().withLabel("eReferenceType").from(erefs).to(eref);
			cb.createEdge().withLabel("eReferences").from(eref).to(eRefType);
			cb.createEdge().withLabel("eReferenceType").from(eRefType).to(eclass);
			cb.createEdge().withLabel("eSuperTypes").from(eclass).to(eclass);
			cb.createEdge().withLabel("eSuperTypes").from(eclass).to(eAttrEle);
			cb.createEdge().withLabel("eSuperTypes").from(eref).to(eAttrEle);
			cb.createEdge().withLabel("eAttributes").from(eAttrEle).to(eAttr);
			cb.createEdge().withLabel("eSuperTypes").from(eref).to(eStructure);
			cb.createEdge().withLabel("eSuperTypes").from(eAttr).to(eStructure);
			cb.createEdge().withLabel("eAttributeType").from(eAttr).to(eAttrType);
			cb.createEdge().withLabel("eDataType").from(eAttrType).to(eData);
			cb.createEdge().withLabel("eSuperTypes").from(eclass).to(eclassifier);
			cb.createEdge().withLabel("eSuperTypes").from(eData).to(eclassifier);
			cb.createEdge().withLabel("eSuperTypes").from(eStructure).to(etypedele);
			cb.createEdge().withLabel("eType").from(etypedele).to(eclassifier);
			cb.createEdge().withLabel("eSuperTypes").from(etypedele).to(enamedele);
			cb.createEdge().withLabel("eSuperTypes").from(eclassifier).to(enamedele);
			});
	}

	public void executeActionAsTransaction(Consumer<CypherBuilder> action) {
		try (Session session = driver.session()) {
			session.writeTransaction(new TransactionWork<StatementResult>() {
				@Override
				public StatementResult execute(Transaction tx) {
					CypherBuilder cb = new CypherBuilder();
					action.accept(cb);
					String cypherCommand = cb.buildCommand();
					StatementResult result = tx.run(cypherCommand);
					return result;
				}
			});
		}
	}
}