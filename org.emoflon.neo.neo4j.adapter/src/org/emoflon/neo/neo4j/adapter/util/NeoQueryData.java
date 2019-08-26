package org.emoflon.neo.neo4j.adapter.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.emoflon.neo.emsl.eMSL.Action;
import org.emoflon.neo.emsl.eMSL.ActionOperator;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;

public class NeoQueryData {
	private HashMap<String, String> patternElements;
	private HashMap<String, String> optionalElements;
	private HashMap<String, String> equalElements;

	private int constraintCount;

	/**
	 * initialize Helper
	 */
	public NeoQueryData() {
		this.patternElements = new HashMap<String,String>();
		this.optionalElements = new HashMap<String,String>();
		this.equalElements = new HashMap<String,String>();
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
	public String registerNewPatternNode(String name, String label) {
		patternElements.put(name,label);
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
	public String registerNewPatternRelation(String relName, String label) {
		patternElements.put(relName,label);
		return relName;
	}

	/**
	 * Creates an new Node in the Node List of OPTIONAL MATCH clauses if it is not
	 * contain already in the list and return the unique name of the node
	 * 
	 * @param name of the new node variable
	 * @return name of the new node variable for including in queries
	 */
	private String registerNewConstraintNode(String name, String label) {
		if (patternElements.containsKey(name)) {
			if(patternElements.get(name).equals(label)) {
				return name;
			} else {
				optionalElements.put(name + "_" + constraintCount, label);
				equalElements.put(name, name + "_" + constraintCount);
				return name + "_" + constraintCount;
			}
		} else {
			optionalElements.put(name + "_" + constraintCount, label);
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
	private String registerNewConstraintRelation(String relName, String label) {
		if (patternElements.containsKey(relName)) {
			if(patternElements.get(relName).equals(label)) {
				return relName;
			} else {
				optionalElements.put(relName + "_" + constraintCount, label);
				equalElements.put(relName, relName + "_" + constraintCount);
				return relName + "_" + constraintCount;
			}
		} else {
			optionalElements.put(relName + "_" + constraintCount, label);
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
		var tempList = new HashSet<String>();
		tempList.addAll(patternElements.keySet());
		tempList.addAll(optionalElements.keySet());
		return Collections.unmodifiableCollection(tempList);
	}
	
	public HashMap<String,String> getAllMatchElementsMap() {
		return patternElements;
	}
	
	public HashMap<String,String> getEqualElements() {
		return equalElements;
	}

	public Collection<String> getMatchElements() {
		var tempList = new HashSet<String>(patternElements.keySet());
		return Collections.unmodifiableCollection(tempList);
	}

	/**
	 * Return the List of all Nodes from OPTIONAL MATCH clauses
	 * 
	 * @return all Nodes from the constraints
	 */
	public Collection<String> getOptionalMatchElements() {
		var tempList = new HashSet<String>(optionalElements.keySet());
		return Collections.unmodifiableCollection(tempList);
	}

	private List<NeoNode> extractNodesAndRelations(List<ModelNodeBlock> mnb, BiFunction<String, String, String> registerNewNode,
			BiFunction<String, String, String> registerNewRelation) {
		List<NeoNode> nodes = new ArrayList<NeoNode>();

		for (var n : extractContextNodes(mnb)) {
			var node = new NeoNode(n.getType().getName(), registerNewNode.apply(n.getName(), n.getType().getName()));

			n.getProperties().forEach(p -> node.addProperty(//
					p.getType().getName(), //
					EMSLUtil.handleValue(p.getValue())));

			for (var r : extractContextRelations(n.getRelations())) {
				var varName = EMSLUtil.relationNameConvention(node.getVarName(), EMSLUtil.getAllTypes(r),
						r.getTarget().getName(), n.getRelations().indexOf(r));

				if (r.getLower() == null && r.getUpper() == null) {
					varName = registerNewRelation.apply(varName, r.getTypes().get(0).getType().getName());
				}

				node.addRelation(varName, EMSLUtil.getAllTypes(r), //
						r.getLower(), r.getUpper(), //
						r.getProperties(), //
						r.getTarget().getType().getName(), //
						registerNewNode.apply(r.getTarget().getName(), r.getTarget().getType().getName()));
			}

			nodes.add(node);
		}

		return nodes;
	}

	private List<ModelRelationStatement> extractContextRelations(EList<ModelRelationStatement> relations) {
		return relations.stream()//
				.filter(rel -> redOrBlack(rel.getAction()))//
				.collect(Collectors.toList());
	}

	private boolean redOrBlack(Action action) {
		return action == null || action.getOp().equals(ActionOperator.DELETE);
	}

	private List<ModelNodeBlock> extractContextNodes(List<ModelNodeBlock> mnb) {
		return mnb.stream()//
				.filter(n -> redOrBlack(n.getAction()))//
				.collect(Collectors.toList());
	}

	public List<NeoNode> extractPatternNodesAndRelations(List<ModelNodeBlock> mnb) {
		return extractNodesAndRelations(mnb, this::registerNewPatternNode, this::registerNewPatternRelation);
	}

	public List<NeoNode> extractConstraintNodesAndRelations(List<ModelNodeBlock> mnb) {
		return extractNodesAndRelations(mnb, this::registerNewConstraintNode, this::registerNewConstraintRelation);
	}

	public void removeMatchElement(String name) {
		if (patternElements.containsKey(name)) {
			patternElements.remove(name);
		}
	}

	public void removeOptionalElement(String name) {
		if (optionalElements.containsKey(name)) {
			optionalElements.remove(name);
		}
	}
	
	public void removeEqualElement(String name) {
		if (equalElements.containsKey(name)) {
			equalElements.remove(name);
		}
	}
}
