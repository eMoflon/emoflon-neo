package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.engine.api.constraints.INegativeConstraint;
import org.emoflon.neo.engine.api.rules.IMatch;

public class NeoNegativeConstraint implements INegativeConstraint {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private NeoCoreBuilder builder;

	private AtomicPattern ap;
	private NeoPattern p;
	private String name;

	public NeoNegativeConstraint(AtomicPattern ap, NeoCoreBuilder builder) {
		this.builder = builder;
		this.ap = ap;
		this.name = ap.getName();
		this.p = new NeoPattern(ap, builder);
	}

	public String getName() {
		return name;
	}

	public AtomicPattern getPattern() {
		return ap;
	}
	
	public Collection<NeoNode> getNodes() {
		return p.getNodes();
	}
	
	public String getQueryString_OptionalMatch() { 
		return "OPTIONAL " + CypherPatternBuilder.matchQuery(p.getNodes());
	}
	
	public String getQueryString_Where() {
		return CypherPatternBuilder.whereNegativeConstraintQuery(p.getNodes());
	}

	@Override
	public boolean isSatisfied() {

		if (getViolations() == null)
			return true;
		else
			return false;

	}

	@Override
	public Collection<IMatch> getViolations() {
		logger.info("Check constraint: FORBID " + ap.getName());

		var cypherQuery = CypherPatternBuilder.readQuery(p.getNodes(), true);
		logger.debug(cypherQuery);

		var result = builder.executeQuery(cypherQuery);

		var matches = new ArrayList<IMatch>();
		while (result.hasNext()) {
			matches.add(new NeoMatch(p, result.next()));
		}

		if (!matches.isEmpty()) {
			logger.info("Found match(es). Constraint: FORBID " + ap.getName() + " is NOT complied!");
			return matches;
		}
		
		logger.info("Not matches found. Constraint: FORBID " + ap.getName() + " is complied!");
		return null;
	}

}
