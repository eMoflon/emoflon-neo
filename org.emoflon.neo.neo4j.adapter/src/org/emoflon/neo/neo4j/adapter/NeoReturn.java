package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

public class NeoReturn {

	private Collection<String> nodesS;
	private Collection<NeoNode> nodesN;

	private String optionalMatch;
	private String whereClause;
	private Collection<String> ifThenWith;
	private Collection<String> ifThenWhere;

	public NeoReturn() {
		this.nodesS = new ArrayList<>();
		this.nodesN = new ArrayList<>();
		this.optionalMatch = "";
		this.whereClause = "";
		this.ifThenWith = new ArrayList<String>();
		this.ifThenWhere = new ArrayList<String>();
	}

	public void addNodes(Collection<NeoNode> nodes) {
		for (NeoNode n : nodes) {

			if(!nodesS.contains(n.getVarName())) {				
				this.nodesS.add(n.getVarName());
				this.nodesN.add(n);
			}

			for (NeoRelation rel : n.getRelations()) {
				if(!nodesS.contains(rel.getVarName()))
					this.nodesS.add(rel.getVarName());
			}
		}
	}

	public Collection<NeoNode> getNodes() {
		return nodesN;
	}
	public Collection<String> getNodesAsString() {
		return nodesS;
	}

	public Collection<String> getIfThenWith() {
		return ifThenWith;
	}
	public Collection<String> getIfThenWhere() {
		return ifThenWhere;
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
	
	public void addIfThenWith(String with) {
		ifThenWith.add(with);
	}
	public void addIfThenWhere(String where) {
		ifThenWhere.add(where);
	}

}
