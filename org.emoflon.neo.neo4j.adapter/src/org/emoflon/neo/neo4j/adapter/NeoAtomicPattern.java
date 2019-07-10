package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.neo4j.driver.v1.StatementResult;

/**
 * Class for handling Atomic Patterns defined in constraints or refined patterns
 * 
 * @author Jannik Hinz
 * 
 */
public class NeoAtomicPattern implements IPattern<NeoMatch> {
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	private NeoCoreBuilder builder;
	private AtomicPattern ap;
	private boolean injective;

	// List of all nodes in the pattern including their properties and relations
	private List<NeoNode> nodes;

	/**
	 * @param ap      the current AtomicPattern
	 * @param builder for creating and running cypher queries
	 */
	public NeoAtomicPattern(AtomicPattern ap, NeoCoreBuilder builder) {
		nodes = new ArrayList<>();
		this.ap = ap;
		injective = true;
		this.builder = builder;
		extractNodesAndRelations();
	}

	/**
	 * Extracts all nessecary information out of the atomic pattern for creating
	 * queries out of it.
	 */
	private void extractNodesAndRelations() {

		for (ModelNodeBlock n : ap.getNodeBlocks()) {

			// create a new NeoNode for every node in the atomic pattern
			var node = new NeoNode(n.getType().getName(), n.getName() + "_" + ap.hashCode());

			// add the properties to the recently defined NeoNode by adding new NeoProperty
			// to the NeoNode
			for (ModelPropertyStatement p : n.getProperties()) {
				node.addProperty(p.getType().getName(), EMSLUtil.handleValue(p.getValue()));
			}

			// add the relations to the recently defined NeoNode by adding new NeoRelation
			// to the NeoNode
			for (ModelRelationStatement r : n.getRelations()) {
				node.addRelation(node.getVarName(), r.getType().getName(), //
						r.getProperties(), //
						r.getTarget().getType().getName(), //
						r.getTarget().getName() + "_" + ap.hashCode());
			}
			nodes.add(node);
		}
	}

	/**
	 * Return the name of the AtomicPattern
	 * 
	 * @return String of the AtomicPattern
	 */
	@Override
	public String getName() {
		return ap.getName();
	}

	/**
	 * Return the list of all nodes as NeoNodes in the atomic pattern
	 * 
	 * @return Collection NeoNode all nodes as NeoNodes in the atomic pattern
	 */
	public List<NeoNode> getNodes() {
		return nodes;
	}

	/**
	 * Checks if a specific match is still valid, is still correctly in the database
	 * 
	 * @param m NeoMatch the match that should be checked
	 * @return true if the match is still valid or false if not
	 */
	public boolean isStillValid(NeoMatch m) {
		logger.info("Check if match for " + getName() + " is still valid");
		var cypherQuery = CypherPatternBuilder.isStillValidQuery(nodes, m, injective);
		logger.debug(cypherQuery);
		StatementResult result = builder.executeQuery(cypherQuery);
		return result.hasNext();
	}

	/**
	 * Set is the pattern should be injective or not
	 * 
	 * @param injective is the pattern should be injective matched
	 */
	@Override
	public void setMatchInjectively(Boolean injective) {
		this.injective = injective;
	}

	/**
	 * Get the injectivity information of a pattern
	 * 
	 * @return boolean true if the given pattern requires injective pattern matching
	 */
	public boolean isInjective() {
		return injective;
	}

	/**
	 * Runs the pattern matching and counts size of matches
	 * 
	 * @return Number of matches
	 */
	@Override
	public Number countMatches() {
		var matches = determineMatches();
		if (matches != null)
			return matches.size();
		else
			return 0;
	}

	/**
	 * Creates the specific Cypher query and executes it in the database, analyze
	 * the results and return the specific results
	 * 
	 * @return Collection<IMatch> List of Matches of the pattern matching process
	 */
	@Override
	public Collection<NeoMatch> determineMatches() {
		logger.info("Searching matches for Pattern: " + ap.getName());
		var cypherQuery = CypherPatternBuilder.readQuery(nodes, injective);
		logger.debug(cypherQuery);

		var result = builder.executeQuery(cypherQuery);

		var matches = new ArrayList<NeoMatch>();
		while (result.hasNext()) {
			var record = result.next();
			matches.add(new NeoMatch(null, record));
		}

		if (matches.isEmpty()) {
			logger.debug("NO MATCHES FOUND");
		}
		return matches;
	}

	@Override
	public Collection<NeoMatch> determineMatches(int limit) {
		logger.info("Searching matches for Pattern: " + ap.getName());
		var cypherQuery = CypherPatternBuilder.readQuery(nodes, injective, limit);
		logger.debug(cypherQuery);

		var result = builder.executeQuery(cypherQuery);

		var matches = new ArrayList<NeoMatch>();
		while (result.hasNext()) {
			var record = result.next();
			matches.add(new NeoMatch(null, record));
		}

		if (matches.isEmpty()) {
			logger.debug("NO MATCHES FOUND");
		}
		return matches;
	}
}
