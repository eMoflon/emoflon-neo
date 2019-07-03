package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.neo4j.driver.v1.StatementResult;

public class NeoAtomicPattern implements IPattern {
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	private NeoCoreBuilder builder;
	private AtomicPattern ap;
	private boolean injective;

	private List<NeoNode> nodes;

	public NeoAtomicPattern(AtomicPattern ap, NeoCoreBuilder builder) {
		nodes = new ArrayList<>();
		this.ap = ap;
		injective = true;
		this.builder = builder;
		extractNodesAndRelations();
	}

	private void extractNodesAndRelations() {

		for (ModelNodeBlock n : ap.getNodeBlocks()) {

			var node = new NeoNode(n.getType().getName(), n.getName() + "_" + ap.hashCode());

			for (ModelPropertyStatement p : n.getProperties()) {
				node.addProperty(p.getType().getName(), NeoUtil.handleValue(p.getValue()));
			}
			for (ModelRelationStatement r : n.getRelations()) {
				node.addRelation(new NeoRelation(node, //
						n.getRelations().indexOf(r), //
						r.getType().getName(), //
						r.getProperties(), //
						r.getTarget().getType().getName(), //
						r.getTarget().getName()+ "_" + ap.hashCode()));
			}
			nodes.add(node);
		}
	}

	@Override
	public String getName() {
		return ap.getName();
	}

	public List<NeoNode> getNodes() {
		return nodes;
	}

	/*
	 * Checks if a specifiy match is still valid, is still correctly in the database
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

	/*
	 * Set is the pattern should be injective or not
	 * @param injective is the pattern should be injective matched
	 */
	@Override
	public void setMatchInjectively(Boolean injective) {
		this.injective = injective;
	}
	
	/* Get the injectivity information of a pattern
	 * @return boolean true if the given pattern requires injective pattern matching
	 */
	public boolean isInjective() {
		return injective;
	}

	/*
	 * Runs the pattern matching and counts size of matches
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

	@Override
	public Collection<IMatch> determineMatches() {
		logger.info("Searching matches for Pattern: " + ap.getName());
		var cypherQuery = CypherPatternBuilder.readQuery(nodes, injective);
		logger.debug(cypherQuery);

		var result = builder.executeQuery(cypherQuery);

		var matches = new ArrayList<IMatch>();
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
