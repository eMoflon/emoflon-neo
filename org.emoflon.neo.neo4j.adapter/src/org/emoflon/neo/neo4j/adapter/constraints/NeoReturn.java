package org.emoflon.neo.neo4j.adapter.constraints;

import java.util.ArrayList;
import java.util.Collection;

import org.emoflon.neo.neo4j.adapter.common.NeoNode;
import org.emoflon.neo.neo4j.adapter.common.NeoRelation;

/**
 * Class collects and provides access to all relevant data during pattern
 * matching and constraint validation, store the nodes, relations and there
 * variable including the query (parts) created during the constraint or
 * condition query creation
 * 
 * @author Jannik Hinz
 *
 */
public class NeoReturn {

	private Collection<String> nodesS;
	private Collection<NeoNode> nodesN;

	private String optionalMatch;
	private String whereClause;
	private Collection<String> ifThenWith;
	private Collection<String> ifThenWhere;

	/**
	 * 
	 */
	public NeoReturn() {
		this.nodesS = new ArrayList<>();
		this.nodesN = new ArrayList<>();
		this.optionalMatch = "";
		this.whereClause = "";
		this.ifThenWith = new ArrayList<String>();
		this.ifThenWhere = new ArrayList<String>();
	}

	/**
	 * Add new nodes to the NeoNode list, used in the result return set (but add
     * nodes an relations only, if they are not present in the list already)
	 * 
	 * @param nodes NeoNodes that should be added to the results set
	 */
	public void addNodes(Collection<NeoNode> nodes) {
		for (NeoNode n : nodes) {

			if (!nodesS.contains(n.getVarName())) {
				this.nodesS.add(n.getVarName());
				this.nodesN.add(n);
			}

			for (NeoRelation rel : n.getRelations()) {
				if (!nodesS.contains(rel.getVarName()))
					this.nodesS.add(rel.getVarName());
			}
		}
	}

	/**
	 * Return all current added NeoNode as a list
	 * 
	 * @return NeoNode list of all currently used nodes
	 */
	public Collection<NeoNode> getNodes() {
		return nodesN;
	}

	/**
	 * Return the current saved WITH query parts of If-Clauses
	 * 
	 * @return current saved WITH query parts of If-Clauses
	 */
	public Collection<String> getIfThenWith() {
		return ifThenWith;
	}

	/**
	 * Adds a new WITH query part of if-Clauses
	 * 
	 * @param with WITH query part, that should be executed later
	 */
	public void addIfThenWith(String with) {
		ifThenWith.add(with);
	}

	/**
	 * Return the current saved WHERE query parts of If-Clauses
	 * 
	 * @return current saved WHERE query parts of If-Clauses
	 */
	public Collection<String> getIfThenWhere() {
		return ifThenWhere;
	}

	/**
	 * Adds a new WHERE query part of if-Clauses
	 * 
	 * @param with WHERE query part, that should be executed later
	 */
	public void addIfThenWhere(String where) {
		ifThenWhere.add(where);
	}

	/**
	 * Return the current saved OPTIONAL MATCH query
	 * 
	 * @return current saved OPTIONAL MATCH query parts
	 */
	public String getOptionalMatchString() {
		return optionalMatch;
	}

	/**
	 * Adds a new OPTIONALL MATCH query to the existing ones
	 * 
	 * @param opt OPTIONAL MATCH query, that should be executed later
	 */
	public void addOptionalMatch(String opt) {
		optionalMatch += opt;
	}

	/**
	 * Return the current saved WHERE query
	 * 
	 * @return current saved WHERE query parts
	 */
	public String getWhereClause() {
		return whereClause;
	}
	
	/**
	 * Adds a new WHERE query to the existing ones
	 * 
	 * @param opt WHERE query, that should be executed later
	 */
	public void addWhereClause(String where) {
		whereClause += where;
	}

}
