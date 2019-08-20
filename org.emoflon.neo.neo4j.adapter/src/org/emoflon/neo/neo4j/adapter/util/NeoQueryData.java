package org.emoflon.neo.neo4j.adapter.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.function.Function;

import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;

public class NeoQueryData {
	private Collection<String> patternElements;
	private Collection<String> optionalElements;

	private int constraintCount;

	/**
	 * initialize Helper
	 */
	public NeoQueryData() {
		this.patternElements = new HashSet<String>();
		this.optionalElements = new HashSet<String>();
		this.constraintCount = 0;
	}

	/**
	 * Increases the constraint counter (new numbering in unique id of nodes and
	 * relation)
	 * 
	 * @return cCount int the new constraint unique id
	 */
	public int incrementCounterForConstraintsInQuery() {
		return constraintCount++;
	}

	/**
	 * Creates an new Node in the Node List of MATCH clauses if it is not contain
	 * already in the list and return the unique name of the node
	 * 
	 * @param name of the new node variable
	 * @return name of the new node variable for including in queries
	 */
	public String registerNewPatternNode(String name) {
		patternElements.add(name);
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
	public String registerNewPatternRelation(String relName) {
		patternElements.add(relName);
		return relName;
	}

	/**
	 * Creates an new Node in the Node List of OPTIONAL MATCH clauses if it is not
	 * contain already in the list and return the unique name of the node
	 * 
	 * @param name of the new node variable
	 * @return name of the new node variable for including in queries
	 */
	private String registerNewConstraintNode(String name) {
		if (patternElements.contains(name)) {
			return name;
		} else {
			optionalElements.add(name + "_" + constraintCount);
			return name + "_" + constraintCount;
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
	private String registerNewConstraintRelation(String relName) {
		if (patternElements.contains(relName)) {
			return relName;
		} else {
			optionalElements.add(relName + "_" + constraintCount);
			return relName + "_" + constraintCount;
		}
	}

	/**
	 * Return the List of all Nodes from MATCH and OPTIONAL MATCH (union, but no
	 * duplicates) clauses
	 * 
	 * @return all Nodes from pattern and constraints
	 */
	public Collection<String> getAllElements() {
		var allElements = new HashSet<>(patternElements);
		allElements.addAll(optionalElements);
		return Collections.unmodifiableCollection(allElements);
	}

	public Collection<String> getMatchElements() {
		return Collections.unmodifiableCollection(patternElements);
	}

	/**
	 * Return the List of all Nodes from OPTIONAL MATCH clauses
	 * 
	 * @return all Nodes from the constraints
	 */
	public Collection<String> getOptionalMatchElements() {
		return Collections.unmodifiableCollection(optionalElements);
	}

	private List<NeoNode> extractNodesAndRelations(List<ModelNodeBlock> mnb, Function<String, String> registerNewNode,
			Function<String, String> registerNewRelation) {
		List<NeoNode> nodes = new ArrayList<NeoNode>();

		for (var n : mnb) {
			var node = new NeoNode(n.getType().getName(), registerNewNode.apply(n.getName()));

			n.getProperties().forEach(p -> node.addProperty(//
					p.getType().getName(), //
					EMSLUtil.handleValue(p.getValue())));

			n.getRelations()
					.forEach(r -> node.addRelation(
							registerNewRelation.apply(EMSLUtil.relationNameConvention(node.getVarName(),
									EMSLUtil.getAllTypes(r), r.getTarget().getName(), n.getRelations().indexOf(r))),
							EMSLUtil.getAllTypes(r), //
							r.getLower(), r.getUpper(), //
							r.getProperties(), //
							r.getTarget().getType().getName(), //
							registerNewNode.apply(r.getTarget().getName())));

			nodes.add(node);
		}

		return nodes;
	}

	public List<NeoNode> extractPatternNodesAndRelations(List<ModelNodeBlock> mnb) {
		return extractNodesAndRelations(mnb, this::registerNewPatternNode, this::registerNewPatternRelation);
	}

	public List<NeoNode> extractConstraintNodesAndRelations(List<ModelNodeBlock> mnb) {
		return extractNodesAndRelations(mnb, this::registerNewConstraintNode, this::registerNewConstraintRelation);
	}

	public void removeMatchElement(String name) {
		if (patternElements.contains(name)) {
			patternElements.remove(name);
		}
	}

	public void removeOptionalElement(String name) {
		if (optionalElements.contains(name)) {
			optionalElements.remove(name);
		}
	}
}
