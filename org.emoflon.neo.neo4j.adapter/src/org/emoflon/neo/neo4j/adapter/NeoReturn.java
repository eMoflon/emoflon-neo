package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

public class NeoReturn {
	
	private Collection<String> nodes;
	private Collection<NeoNode> nodesC;
	
	private String optionalMatch;
	private String whereClause;
	
	public NeoReturn() {
		this.nodes = new ArrayList<>();
		this.nodesC = new ArrayList<>();
		this.optionalMatch = "";
		this.whereClause = "";
	}
	
	public void addNodes(Collection<NeoNode> nodes) {
		for(NeoNode node : nodes) {
			
			if(!this.nodes.contains(node.getVarName())) {
				this.nodes.add(node.getVarName());
				this.nodesC.add(node);
			}
			
			for(NeoRelation rel: node.getRelations()) {
				if(!this.nodes.contains(rel.getVarName())) {
					this.nodes.add(rel.getVarName());
				}
			}
		}
	}
	public Collection<NeoNode> getNodes() {
		return nodesC;
	}
	public Collection<String> getNodesAsString() {
		return nodes;
	}
	
	public String getOptionalMatchString() {
		return optionalMatch;
	}
	public void addOptionalMatch(String opt) {
		optionalMatch += opt;
	}
	public String getWhereClause() {
		return whereClause;
	}
	public void addWhereClause(String where) {
		whereClause += where;
	}

}
