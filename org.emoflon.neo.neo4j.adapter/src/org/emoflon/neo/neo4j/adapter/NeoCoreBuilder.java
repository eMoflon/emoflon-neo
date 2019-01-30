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
		this("bolt://localhost:7687", "neo4j", "test");
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

			cb.createEdge().withLabel("eReferences").from(eclass).to(erefs);
			cb.createEdge().withLabel("eReferenceType").from(erefs).to(eref);
			cb.createEdge().withLabel("eReferences").from(eref).to(eRefType);
			cb.createEdge().withLabel("eReferenceType").from(eRefType).to(eclass);
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