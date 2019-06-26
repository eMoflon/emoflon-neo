package org.emoflon.neo.neo4j.adapter;

import org.apache.log4j.Logger;

public class NeoCondition {
	
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private NeoConstraint c;
	private NeoPattern p;
	
	private String name;

	public NeoCondition(NeoConstraint c, NeoPattern p, String name) {
		
		this.c = c;
		this.p = p;
		
		this.name = name;
	}
	
	public boolean isSatisfied() {
		
		logger.info(c.getNodes().toString());
		return false;
	}

}
