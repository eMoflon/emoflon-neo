package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.AndBody;
import org.emoflon.neo.emsl.eMSL.OrBody;

/*
 * Class for creating nested AndBodies used in NeoConstraints or NeoConditions
 * for proofing if a constraint or condition is satisfied
 */
public class NeoOrBody {

	private OrBody body;
	private NeoCoreBuilder builder;

	/*
	 * @param body of the current OrBody
	 * @param builder for creating and running Cypher queries
	 */
	public NeoOrBody(OrBody body, NeoCoreBuilder builder) {

		this.body = body;
		this.builder = builder;

	}

	/*
	 * Calculates and creates the nested AND constraints bodies and conditions an
	 * return if they satisfy or not
	 * 
	 * @return boolean true iff the complete nested AND Body and their child satisfy
	 * or false if not
	 */
	public boolean isSatisfied() {

		// for all child in the constraint body
		for (AndBody b : body.getChildren()) {
			var andbody = new NeoAndBody(b, builder);

			if (andbody.isSatisfied()) {
				return true;
			}

		}

		return false;

	}

	/*
	 * Returns a NeoNode Collection of all nodes in the nested constraint or body
	 * 
	 * @return NeoNode Collection of all nodes in the nested constraint or body
	 */
	public Collection<NeoNode> getNodes() {

		Collection<NeoNode> nodes = new ArrayList<>();

		for (AndBody b : body.getChildren()) {
			var andbody = new NeoAndBody(b, builder);
			for (NeoNode node : andbody.getNodes()) {
				nodes.add(node);
			}

		}
		return nodes;

	}

	/*
	 * Returns the OPTIONAL MATCH cypher string for all nested bodies and
	 * constraints
	 * 
	 * @return String OPTIONAL MATCH cypher string of all nested bodies and
	 * constraints
	 */
	public String getOptionalQuery() {

		var query = "";

		for (AndBody b : body.getChildren()) {

			var andbody = new NeoAndBody(b, builder);
			query += andbody.getOptionalMatch();

		}
		return query;
	}

	/*
	 * Returns the WHERE cypher string for all nested bodies and constraints
	 * 
	 * @return String WHERE cypher string of all nested bodies and constraints
	 */
	public String getQueryString_Where() {

		var query = "(";
		var first = true;

		for (AndBody b : body.getChildren()) {

			var andbody = new NeoAndBody(b, builder);
			if (first) {
				first = false;
			} else {
				query += " OR ";
			}
			query += andbody.getQueryString_Where();

		}
		return query + ")";
	}
}
