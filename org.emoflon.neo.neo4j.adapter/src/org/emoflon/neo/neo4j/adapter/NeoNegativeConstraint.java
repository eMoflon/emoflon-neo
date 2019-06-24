package org.emoflon.neo.neo4j.adapter;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.engine.api.constraints.IPositiveConstraint;
import org.emoflon.neo.engine.api.rules.IMatch;

public class NeoNegativeConstraint implements IPositiveConstraint {

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

	@Override
	public boolean isSatisfied() {

		if (getMatch() == null)
			return true;
		else
			return false;

	}

	@Override
	public IMatch getMatch() {

		logger.info("Check constraint: FORBID " + ap.getName());

		var cypherQuery = CypherPatternBuilder.readQuery(p.getNodes(), true);
		logger.debug(cypherQuery);

		var result = builder.executeQuery(cypherQuery);

		while (result.hasNext()) {
			logger.info("Found match(es). Constraint: FORBID " + ap.getName() + " is NOT complied!");
			return new NeoMatch(p, result.next());
		}
		logger.info("Not matches found. Constraint: FORBID " + ap.getName() + " is complied!");
		return null;

	}

}
