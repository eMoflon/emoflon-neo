package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.function.Consumer;

import org.neo4j.driver.v1.AuthTokens;
import org.neo4j.driver.v1.Driver;
import org.neo4j.driver.v1.GraphDatabase;
import org.neo4j.driver.v1.Session;
import org.neo4j.driver.v1.StatementResult;
import org.neo4j.driver.v1.Transaction;
import org.neo4j.driver.v1.TransactionWork;

public class NeoCoreBuilder implements AutoCloseable {
	private static final String METAMETAMODEL = "_Metametamodel_";
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
			NodeCommand neocore = cb.createNode()//
					.withLabel(METAMETAMODEL)//
					.withStringProperty("_uri_", "org.emoflon.neo.NeoCore");
			cb.createEdge()//
					.withLabel("_conformsTo_")//
					.from(neocore)//
					.to(neocore);

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
					.withStringProperty("_uri_", "org.emoflon.neo.NeoCore"));
		});
		
		return result.stream().count() == 0;
	}
}