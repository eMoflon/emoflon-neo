package org.emoflon.neo.neo4j.adapter;

import java.util.HashMap;
import java.util.UUID;

import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.Pattern;

public class NeoHelper {

	private HashMap matchNodes;
	private HashMap optionalNodes;
	

	public NeoHelper() {
		this.matchNodes = new HashMap<String, String>();
		this.optionalNodes = new HashMap<String, String>();
	}

	
	public String newPatternNode(String name, Pattern p) {
		matchNodes.put(name, p.getBody().getName());
		return name;	
	}
	
	public String newPatternRelation(String name, int index, String toName, Pattern p) {
		optionalNodes.put(name + "_" + index  + "_" + toName, p.getBody().getName());
		return name + "_" + index  + "_" + toName;
	}
	
	public String newConstraintNode(String name, AtomicPattern ap, int uuid) {
		
		if(matchNodes.containsKey(name)) {
			return name;
		} else {
			optionalNodes.put(name + "_" + uuid, ap.getName());
			return name + "_" + uuid;
		}		
	
	}


	public String newConstraintReference(String name, int index, String toName, AtomicPattern ap) {
		
		if(matchNodes.containsKey(name)) {
			return name;
		} else {
			optionalNodes.put(name + "_" + index  + "_" + toName, ap.getName());
			return name + "_" + index  + "_" + toName;
		}
	}

}
