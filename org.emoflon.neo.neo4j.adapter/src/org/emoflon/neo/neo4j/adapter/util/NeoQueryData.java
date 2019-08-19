package org.emoflon.neo.neo4j.adapter.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.eclipse.emf.common.util.EList;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;

public class NeoQueryData {
	private Collection<String> matchElements;
	private Collection<String> optionalElements;

	private int cCount;

	/**
	 * initialize Helper
	 */
	public NeoQueryData() {
		this.matchElements = new HashSet<String>();
		this.optionalElements = new HashSet<String>();
		this.cCount = 0;
	}

	/**
	 * Increases the constraint counter (new numbering in unique id of nodes and
	 * relation)
	 * 
	 * @return cCount int the new constraint unique id
	 */
	public int addConstraint() {
		return cCount++;
	}

	/**
	 * Creates an new Node in the Node List of MATCH clauses if it is not contain
	 * already in the list and return the unique name of the node
	 * 
	 * @param name of the new node variable
	 * @return name of the new node variable for including in queries
	 */
	public String newPatternNode(String name) {
		matchElements.add(name);
		return name;
	}

	/**
	 * Creates an new Relation in the Node List of MATCH clauses if it is not
	 * contain already in the list and return the unique name of the relation
	 * 
	 * @param name   of the source node from the relation
	 * @param index  of the relation of one node
	 * @param relVar of the relation variable
	 * @param toName of the target node of the relation
	 * @return name of the new relation variable for including in queries
	 */
	public String newPatternRelation(String name, int index, List<String> relVar, String toName) {
		var relName = EMSLUtil.relationNameConvention(name, relVar, toName, index);
		matchElements.add(relName);
		return relName;
	}

	/**
	 * Creates an new Node in the Node List of OPTIONAL MATCH clauses if it is not
	 * contain already in the list and return the unique name of the node
	 * 
	 * @param name of the new node variable
	 * @return name of the new node variable for including in queries
	 */
	public String newConstraintNode(String name) {
		if (matchElements.contains(name)) {
			return name;
		} else {
			optionalElements.add(name + "_" + cCount);
			return name + "_" + cCount;
		}
	}

	/**
	 * Creates an new Relation in the Node List of MATCH clauses if it is not
	 * contain already in the list and return the unique name of the relation
	 * 
	 * @param name   of the source node from the relation
	 * @param index  of the relation of one node
	 * @param relVar of the relation variable
	 * @param toName of the target node of the relation
	 * @return name of the new relation variable for including in queries
	 */
	public String newConstraintReference(String name, int index, List<String> relVar, String toName) {
		var relName = EMSLUtil.relationNameConvention(name, relVar, toName, index);
		if (matchElements.contains(relName)) {
			return relName;
		} else {
			optionalElements.add(relName + "_" + cCount);
			return relName + "_" + cCount;
		}
	}

	/**
	 * Return the List of all Nodes from MATCH and OPTIONAL MATCH (union, but no
	 * duplicates) clauses
	 * 
	 * @return all Nodes from pattern and constraints
	 */
	public Collection<String> getAllElements() {
		var list = new HashSet<>(matchElements);
		list.addAll(optionalElements);
		return list;
	}

	public Collection<String> getMatchElements() {
		return matchElements;
	}

	/**
	 * Return the List of all Nodes from OPTIONAL MATCH clauses
	 * 
	 * @return all Nodes from the constraints
	 */
	public Collection<String> getOptionalMatchElements() {
		return optionalElements;
	}

	/**
	 * Creates and extracts all necessary information data from the Atomic Pattern.
	 * Create new NeoNode for any AtomicPattern node and corresponding add Relations
	 * and Properties and save them to the node in an node list.
	 * 
	 * @param mnb Collection of all nodes of a AtomicPattern
	 * @return NeoNode ArrayList of all Nodes and their Relation and Properties of
	 *         the AtomicPattern
	 */
	public List<NeoNode> extractNodesAndRelations(EList<ModelNodeBlock> mnb) {
		List<NeoNode> tempNodes = new ArrayList<NeoNode>();

		for (var n : mnb) {
			var node = new NeoNode(n.getType().getName(), newConstraintNode(n.getName()));

			n.getProperties().forEach(p -> node.addProperty(//
					p.getType().getName(), //
					EMSLUtil.handleValue(p.getValue())));

			n.getRelations()
					.forEach(r -> node.addRelation(
							newConstraintReference(node.getVarName(), n.getRelations().indexOf(r),
									EMSLUtil.getAllTypes(r), r.getTarget().getName()),
							EMSLUtil.getAllTypes(r), //
							r.getLower(), r.getUpper(), //
							r.getProperties(), //
							r.getTarget().getType().getName(), //
							newConstraintNode(r.getTarget().getName())));

			tempNodes.add(node);
		}

		return tempNodes;
	}
	
	public void removeMatchElement(String name) {
		if(matchElements.contains(name)) {
			matchElements.remove(name);
		}
	}
	
	public void removeOptionalElement(String name) {
		if(optionalElements.contains(name)) {
			optionalElements.remove(name);
		}
	}
}
