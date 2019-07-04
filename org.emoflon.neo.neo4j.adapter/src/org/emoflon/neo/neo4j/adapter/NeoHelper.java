package org.emoflon.neo.neo4j.adapter;

import java.util.Collection;
import java.util.HashMap;

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
	
	public String newConstraintNode(String name, AtomicPattern ap) {
		
		if(matchNodes.containsKey(name)) {
			return name;
		} else {
			optionalNodes.put(ap.getName() + "_" + ap.hashCode() + "_" + name, ap.getName());
			return ap.getName() + "_" + ap.hashCode() + "_" + name;
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
	
	
	
	/**
	public HashMap<String, String> addMatchNodes(Collection<NeoNode> nodes) {
		
		var iCounter = 0;
		
		for(NeoNode node : nodes) {
			
			iCounter = 0;
			matchNodes.put(node.getVarName(), node.getVarName());
			
			for(NeoRelation rel: node.getRelations()) {
				
				matchNodes.put(rel.getVarName(), node.getVarName() + "_" +rel.getVarName() + "_" + iCounter + "_" + rel.getToNodeVar());
				iCounter ++;
				
			}
		}
		optionalCount++;		
		return null;
		
	}
	
	public HashMap<String, HashMap<String, String>> addOptionalConstraintNodes(Collection<NeoNode> nodes, AtomicPattern ap) {
		
		var iCounter = 0;
		
		for(NeoNode node : nodes) {
			
			iCounter = 0;
			matchNodes.put(node.getVarName(), ap.getName() + optionalCount + "_" + "_" + node.getVarName());
			
			for(NeoRelation rel: node.getRelations()) {
				
				matchNodes.put(rel.getVarName(), ap.getName() + "_" + optionalCount + "_" + node.getVarName() + "_" +rel.getVarName() + "_" + iCounter + "_" + rel.getToNodeVar());
				iCounter ++;
				
			}
		}
		optionalCount++;	
		return null;
	}
	
	public HashMap<String, HashMap<String, String>> addOptionalConstraintIfThenNodes(Collection<NeoNode> nodesIf, Collection<NeoNode> nodesThen, AtomicPattern ap) {
		
		var iCounter = 0;
		
		for(NeoNode node : nodesIf) {
			
			iCounter = 0;
			optionalNodes.put(node.getVarName(), ap.getName() + optionalCount + "_" + "_" + node.getVarName());
			
			for(NeoRelation rel: node.getRelations()) {
				
				optionalNodes.put(rel.getVarName(), ap.getName() + optionalCount + "_" + "_" + node.getVarName() + "_" +rel.getVarName() + "_" + iCounter + "_" + rel.getToNodeVar());
				iCounter ++;
				
			}
		}
		for(NeoNode node : nodesThen) {
			
			iCounter = 0;
			if(!optionalNodes.containsKey(node.getVarName()))
				optionalNodes.put(node.getVarName(), ap.getName() + optionalCount + "_" + "_" + node.getVarName());
			
			for(NeoRelation rel: node.getRelations()) {
				if(!optionalNodes.containsKey(rel.getVarName()))
					optionalNodes.put(rel.getVarName(), ap.getName() + optionalCount + "_" + "_" + node.getVarName() + "_" +rel.getVarName() + "_" + iCounter + "_" + rel.getToNodeVar());
				iCounter ++;
				
			}
		}
		optionalCount++;
		return null;
	}
	
	public HashMap<String, HashMap<String, String>> addOptionalConditionNodes(Collection<NeoNode> nodes, AtomicPattern ap) {
		
		var iCounter = 0;
		
		for(NeoNode node : nodes) {
			
			iCounter = 0;
			if(matchNodes.containsKey(node.getVarName()))
				optionalNodes.put(node.getVarName(), node.getVarName());
			else
				optionalNodes.put(node.getVarName(), ap.getName() + optionalCount + "_" + "_" + node.getVarName());
			
			for(NeoRelation rel: node.getRelations()) {
				
				if(matchNodes.containsKey(rel.getVarName()))
					matchNodes.put(rel.getVarName(), node.getVarName() + "_" +rel.getVarName() + "_" + iCounter + "_" + rel.getToNodeVar());
				else
					matchNodes.put(rel.getVarName(), ap.getName() + "_" + optionalCount + "_" + node.getVarName() + "_" +rel.getVarName() + "_" + iCounter + "_" + rel.getToNodeVar());
				iCounter ++;
				
			}
		}
		optionalCount++;	
		return null;
	}
	*/

}
