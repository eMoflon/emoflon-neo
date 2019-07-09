package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;

public class NeoHelper {

	private Collection<String> matchNodes;
	private Collection<String> optionalNodes;
	
	private int cCount;
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	public NeoHelper() {
		
		this.matchNodes = new ArrayList<String>();
		this.optionalNodes = new ArrayList<String>();
		this.cCount = 0;
	}
	
	public int addConstraint() {
		return cCount++;
	}
	
	public String newPatternNode(String name) {
		if(!matchNodes.contains(name))
			matchNodes.add(name);
		return name;	
	}
	
	public String newPatternRelation(String name, int index, String relVar, String toName) {
		
		logger.info(name + "_" + index + "_" + relVar + "_" + toName);
		
		matchNodes.add( name + "_" + index + "_" + relVar + "_" + toName);
		return name + "_" + index + "_" + relVar + "_" + toName;
	}
	
	public String newConstraintNode(String name, AtomicPattern ap, int uuid) {
		
		if(matchNodes.contains(name)) {
			return name;
		} else {
			optionalNodes.add(name + "_" + uuid);
			return name + "_" + uuid;
		}		
	
	}

	public String newConstraintReference(String name, int index, String relVar, String toName, AtomicPattern ap, int uuid) {
		
		if(matchNodes.contains(name + "_" + index + "_" + relVar + "_" + toName)) {
			return name + "_" + index + "_" + relVar + "_" + toName;
		} else {
			optionalNodes.add(name + "_" + index + "_" + relVar + "_" + toName + "_" + uuid);
			return name + "_" + index + "_" + relVar + "_" + toName + "_" + uuid;
		}
	}
	
	public Collection<String> getNodes() {
		var list = matchNodes;
		for(String node : optionalNodes) {
			if(!list.contains(node))
				list.add(node);
		}
		return list;
	}

}
