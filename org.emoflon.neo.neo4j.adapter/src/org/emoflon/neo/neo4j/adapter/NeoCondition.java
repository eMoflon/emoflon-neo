package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;

public class NeoCondition {
	
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private NeoConstraint c;
	private NeoPattern p;
	
	private Collection<String> nodesAndRefs;
	
	private String name;

	public NeoCondition(NeoConstraint c, NeoPattern p, String name) {
		
		this.c = c;
		this.p = p;
		this.nodesAndRefs = new ArrayList<>();
		this.name = name;
	}
	
	public boolean isSatisfied() {
		
		removeDuplicates();
		
		var queryOptionalMatch = c.getOptionalQuery();
		logger.info(queryOptionalMatch);
		
		var queryWith = CypherPatternBuilder.withConstraintQuery(nodesAndRefs);
		logger.info(queryWith);
		
		var queryWhere = c.getWhereQuery();
		logger.info(queryWhere);
		
		return false;
	}
	
	private void removeDuplicates() {
		for(NeoNode node : c.getNodes()) {
			
			if(!nodesAndRefs.contains(node.getVarName())) {
				nodesAndRefs.add(node.getVarName());
			}
			
			for(NeoRelation rel: node.getRelations()) {
				if(!nodesAndRefs.contains(rel.getVarName())) {
					nodesAndRefs.add(rel.getVarName());
				}
			}
		}
	}

}
