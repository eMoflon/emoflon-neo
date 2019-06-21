package org.emoflon.neo.neo4j.adapter;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.engine.api.constraints.IPositiveConstraint;
import org.emoflon.neo.engine.api.rules.IMatch;

/**
 * TODO[Jannik] Implement constraints
 * 
 */
public class NeoPositiveConstraint implements IPositiveConstraint {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private NeoCoreBuilder builder;
	
	private AtomicPattern ap;
	private NeoPattern p;
	private String name;
			
	public NeoPositiveConstraint(AtomicPattern ap, NeoCoreBuilder builder) {
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

		if(getMatch() != null)
			return true;
		else
			return false;

	}

	@Override
	public IMatch getMatch() {
		
		logger.info("Searching matches for Pattern: " + ap.getName());
		
		var cypherQuery = CypherPatternBuilder.readQuery(p.getNodes(), true);
		logger.debug(cypherQuery);

		var result = builder.executeQuery(cypherQuery);

		while (result.hasNext()) {
			var record = result.next();
			logger.info("Found match(es).");
			return new NeoMatch(p, record);
		}
		logger.info("Not matches found.");
		return null;
		
	}

}
