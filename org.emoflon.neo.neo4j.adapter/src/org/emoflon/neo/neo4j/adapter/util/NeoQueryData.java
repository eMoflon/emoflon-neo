package org.emoflon.neo.neo4j.adapter.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
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
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;

public class NeoQueryData {
	private HashMap<String, ArrayList<String>> patternNodes;
	private HashMap<String, ArrayList<String>> patternElements;
	private HashMap<String, ArrayList<String>> optionalNodes;
	private HashMap<String, ArrayList<String>> optionalElements;
	private HashMap<String, String> equalElements;

	private int constraintCount;

	/**
	 * initialize Helper
	 */
	public NeoQueryData() {
		this.patternElements = new HashMap<String, ArrayList<String>>();
		this.patternNodes = new HashMap<String, ArrayList<String>>();
		this.optionalElements = new HashMap<String, ArrayList<String>>();
		this.optionalNodes = new HashMap<String, ArrayList<String>>();
		this.equalElements = new HashMap<String, String>();
		this.constraintCount = 0;
	}

	/**
	 * Increases the constraint counter (new numbering in unique id of nodes and
	 * relation)
	 * 
	 * @return cCount int the new constraint unique id
	 */
	public int incrementCounterForConstraintsInQuery() {
		optionalNodes.clear();
		return constraintCount++;
	}

	/**
	 * Creates an new Node in the Node List of MATCH clauses if it is not contain
	 * already in the list and return the unique name of the node
	 * 
	 * @param name of the new node variable
	 * @return name of the new node variable for including in queries
	 */

	public String registerNewPatternNode(String name, Collection<String> labels) {
		var list = new ArrayList<String>(labels);
		if (!patternElements.containsKey(name)) {
			patternElements.put(name, list);
			patternNodes.put(name, list);
		}
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
		var list = new ArrayList<String>();
		list.add(label);
		if (!patternElements.containsKey(relName)) {
			patternElements.put(relName, list);
		}
		return relName;
	}

	/**
	 * Creates an new Node in the Node List of OPTIONAL MATCH clauses if it is not
	 * contain already in the list and return the unique name of the node
	 * 
	 * @param name of the new node variable
	 * @return name of the new node variable for including in queries
	 */

	private String registerNewConstraintNode(String name, Collection<String> label) {
		var list = new ArrayList<String>(label);
		if (patternElements.containsKey(name)) {
			var labelsP = patternElements.get(name);
			var equal = true;
			for (var l : label) {
				if (!labelsP.contains(l))
					equal = false;
			}
			for (var l : labelsP) {
				if (!label.contains(l))
					equal = false;
			}

			if (equal) {
				return name;
			} else {
				if (!optionalElements.containsKey(name + "_" + constraintCount)) {
					optionalElements.put(name + "_" + constraintCount, list);
					optionalNodes.put(name + "_" + constraintCount, list);
					equalElements.put(name, name + "_" + constraintCount);
				}
				return name + "_" + constraintCount;
			}

		} else {
			if (!optionalElements.containsKey(name + "_" + constraintCount)) {
				optionalElements.put(name + "_" + constraintCount, list);
				optionalNodes.put(name + "_" + constraintCount, list);
			}
			return name + "_" + constraintCount;
		}
	}

	public ArrayList<String> getAllNodesRequireInjectivityChecksCondition() {

		var elem = new ArrayList<String>();

		for (var p1 : optionalNodes.keySet()) {

			for (var p2 : optionalNodes.keySet()) {

				if (!p1.equals(p2)) {

					var l1 = optionalNodes.get(p1);
					var l2 = optionalNodes.get(p2);

					boolean equal = false;

					for (var l : l1) {
						if (!equal && l2.contains(l)) {
							if (!elem.contains(p1 + "<>" + p2) && !elem.contains(p2 + "<>" + p1)) {
								elem.add(p1 + "<>" + p2);
							}
							equal = true;
						}
					}
					for (var l : l2) {
						if (!equal && l1.contains(l)) {
							if (!elem.contains(p1 + "<>" + p2) && !elem.contains(p2 + "<>" + p1)) {
								elem.add(p1 + "<>" + p2);
							}
							equal = true;
						}
					}
				}
			}
		}
		var x = getAllNodesRequireInjectivityChecksPatternAndCondition();
		elem.addAll(x);
		return elem;
	}

	private ArrayList<String> getAllNodesRequireInjectivityChecksPatternAndCondition() {

		var elem = new ArrayList<String>();

		for (var pElem : patternNodes.keySet()) {
			for (var oElem : optionalNodes.keySet()) {

				if (equalElements.containsKey(pElem) && equalElements.get(pElem).equals(oElem)) {
				} else {

					var labelsP = patternNodes.get(pElem);
					var labelsO = optionalNodes.get(oElem);

					boolean equal = false;

					for (var l : labelsP) {
						if (!equal && labelsO.contains(l)) {
							if (!elem.contains(pElem + "<>" + oElem) && !elem.contains(oElem + "<>" + pElem)) {
								elem.add(pElem + "<>" + oElem);
							}
							equal = true;
						}
					}
					for (var l : labelsO) {
						if (!equal && labelsP.contains(l)) {
							if (!elem.contains(pElem + "<>" + oElem) && !elem.contains(oElem + "<>" + pElem)) {
								elem.add(pElem + "<>" + oElem);
							}
							equal = true;
						}
					}
				}
			}
		}

		return elem;
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
		var list = new ArrayList<String>();
		list.add(label);
		if (patternElements.containsKey(relName)) {
			if (patternElements.get(relName).contains(label)) {
				return relName;
			} else {
				optionalElements.put(relName + "_" + constraintCount, list);
				equalElements.put(relName, relName + "_" + constraintCount);
				return relName + "_" + constraintCount;
			}
		} else {
			optionalElements.put(relName + "_" + constraintCount, list);
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
		var tempList = new ArrayList<String>();
		tempList.addAll(patternElements.keySet());
		tempList.addAll(optionalElements.keySet());
		return Collections.unmodifiableCollection(tempList);
	}

	public HashMap<String, ArrayList<String>> getAllMatchElementsMap() {
		return patternElements;
	}

	public HashMap<String, String> getEqualElements() {
		return equalElements;
	}

	public Collection<String> getMatchElements() {
		return Collections.unmodifiableCollection(patternElements.keySet());
	}

	/**
	 * Return the List of all Nodes from OPTIONAL MATCH clauses
	 * 
	 * @return all Nodes from the constraints
	 */
	public Collection<String> getOptionalMatchElements() {
		return Collections.unmodifiableCollection(optionalElements.keySet());
	}

	private List<NeoNode> extractNodesAndRelations(List<ModelNodeBlock> mnb,
			BiFunction<String, Collection<String>, String> registerNewNode,
			BiFunction<String, String, String> registerNewRelation) {
		List<NeoNode> nodes = new ArrayList<NeoNode>();

		for (var n : extractContextNodes(mnb)) {

			var node = new NeoNode(NeoCoreBuilder.computeLabelsFromType(n.getType()),
					registerNewNode.apply(n.getName(), NeoCoreBuilder.computeLabelsFromType(n.getType())));

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
						registerNewNode.apply(r.getTarget().getName(),
								NeoCoreBuilder.computeLabelsFromType(r.getTarget().getType())));
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
