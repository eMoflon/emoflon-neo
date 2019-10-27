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
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.Parameter;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;
import org.emoflon.neo.neo4j.adapter.common.NeoProperty;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.NeoAttributeExpression;
import org.emoflon.neo.neo4j.adapter.patterns.NeoAttributeReturn;
import org.emoflon.neo.neo4j.adapter.patterns.NeoAttributeReturnRelation;

public class NeoQueryData {
	private HashMap<String, ArrayList<String>> patternNodes;
	private HashMap<String, ArrayList<String>> patternElements;
	private HashMap<String, ArrayList<String>> optionalNodes;
	private HashMap<String, ArrayList<String>> optionalElements;
	private HashMap<String, String> equalElements;

	private ArrayList<NeoAttributeExpression> attrExprPattern;
	private ArrayList<NeoAttributeExpression> attrExprOptional;
	private ArrayList<NeoAttributeExpression> attrAssign;

	private int constraintCount;
	private boolean context;

	public NeoQueryData(boolean context) {
		this.context = context;
		this.patternElements = new HashMap<String, ArrayList<String>>();
		this.patternNodes = new HashMap<String, ArrayList<String>>();
		this.optionalElements = new HashMap<String, ArrayList<String>>();
		this.optionalNodes = new HashMap<String, ArrayList<String>>();
		this.equalElements = new HashMap<String, String>();
		this.attrExprPattern = new ArrayList<NeoAttributeExpression>();
		this.attrExprOptional = new ArrayList<NeoAttributeExpression>();
		this.attrAssign = new ArrayList<NeoAttributeExpression>();
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
		attrExprOptional.clear();
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
				optionalElements.put(name + "_" + constraintCount, list);
				optionalNodes.put(name + "_" + constraintCount, list);
				equalElements.put(name, name + "_" + constraintCount);
				return name + "_" + constraintCount;
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

	// FIXME[Anjorin]: Combine with similar functionality in CypherPatternBuilder
	public ArrayList<String> getAllNodesRequireInjectivityChecksCondition() {
		var elem = new ArrayList<String>();
		for (var p1 : optionalNodes.keySet()) {
			for (var p2 : optionalNodes.keySet()) {
				if (!p1.equals(p2)) {
					var l1 = optionalNodes.get(p1);
					var l2 = optionalNodes.get(p2);

					boolean equal = false;

					var l = l1.get(0);
					if (!equal && l2.contains(l)) {
						if (!elem.contains(p1 + "<>" + p2) && !elem.contains(p2 + "<>" + p1)) {
							elem.add(p1 + "<>" + p2);
						}
						equal = true;
					}
				}
			}
		}

		var x = getAllNodesRequireInjectivityChecksPatternAndCondition();
		elem.addAll(x);
		return elem;
	}

	// FIXME[Anjorin]: Combine with similar functionality in CypherPatternBuilder
	private ArrayList<String> getAllNodesRequireInjectivityChecksPatternAndCondition() {
		var elem = new ArrayList<String>();
		for (var pElem : patternNodes.keySet()) {
			for (var oElem : optionalNodes.keySet()) {
				if (equalElements.containsKey(pElem) && equalElements.get(pElem).equals(oElem)) {
				} else {
					var labelsP = patternNodes.get(pElem);
					var labelsO = optionalNodes.get(oElem);

					boolean equal = false;
					var lp = labelsP.get(0);
					var lo = labelsO.get(0);
					if (!equal && (labelsO.contains(lp) || labelsP.contains(lo))) {
						if (!elem.contains(pElem + "<>" + oElem) && !elem.contains(oElem + "<>" + pElem)) {
							elem.add(pElem + "<>" + oElem);
						}
						equal = true;
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

	public Collection<NeoAttributeExpression> getAttributeExpressions() {
		return attrExprPattern;
	}

	public Collection<NeoAttributeExpression> getAttributeExpressionsOptional() {
		return attrExprOptional;
	}

	public Collection<NeoAttributeExpression> getAttributeAssignments() {
		return attrAssign;
	}

	private List<NeoNode> extractNodesAndRelations(List<ModelNodeBlock> mnb,
			BiFunction<String, Collection<String>, String> registerNewNode,
			BiFunction<String, String, String> registerNewRelation, ArrayList<NeoAttributeExpression> attrExpr) {
		List<NeoNode> nodes = new ArrayList<NeoNode>();

		for (var n : extractContextNodes(mnb)) {

			var node = new NeoNode(NeoCoreBuilder.computeLabelsFromType(n.getType()),
					registerNewNode.apply(n.getName(), NeoCoreBuilder.computeLabelsFromType(n.getType())));

			var props = getNodePropertiesAndAttributes(n.getProperties(), node.getVarName(), attrExpr);
			props.getElemProps().forEach(prop -> node.addProperty(prop.getName(), prop.getValue()));
			attrExpr.addAll(props.getElemAttrExpr());
			attrAssign.addAll(props.getElemAttrAsgn());

			for (var r : extractContextRelations(n.getRelations())) {
				var varName = EMSLUtil.relationNameConvention(node.getVarName(), EMSLUtil.getAllTypes(r),
						r.getTarget().getName(), n.getRelations().indexOf(r));

				if (r.getLower() == null && r.getUpper() == null) {
					varName = registerNewRelation.apply(varName, r.getTypes().get(0).getType().getName());
				}

				var propsR = getRelationPropertiesAndAttributes(r.getProperties(), varName, attrExpr);
				attrExpr.addAll(propsR.getElemAttrExpr());
				attrAssign.addAll(propsR.getElemAttrAsgn());

				node.addRelation(varName, EMSLUtil.getAllTypes(r), //
						r.getLower(), r.getUpper(), //
						propsR.getElemProps(), //
						r.getTarget(), //
						registerNewNode.apply(r.getTarget().getName(),
								NeoCoreBuilder.computeLabelsFromType(r.getTarget().getType())));
			}

			nodes.add(node);
		}

		return nodes;
	}

	public NeoAttributeReturn getNodePropertiesAndAttributes(Collection<ModelPropertyStatement> props,
			String nodeVarName, ArrayList<NeoAttributeExpression> attrExpr) {

		var nodeProps = new ArrayList<NeoProperty>();
		var nodeAttrExpr = new ArrayList<NeoAttributeExpression>();
		var nodeAttrAsgn = new ArrayList<NeoAttributeExpression>();

		for (var p : props) {
			if (p.getValue() instanceof Parameter)
				continue;

			switch (p.getOp()) {
			case EQ:
				nodeProps.add(new NeoProperty(p.getType().getName(), EMSLUtil.handleValue(p.getValue())));
				break;
			case GREATER:
			case GREATEREQ:
			case LESS:
			case LESSEQ:
			case NOTEQ:
				nodeAttrExpr.add(new NeoAttributeExpression(nodeVarName, p.getType().getName(), //
						EMSLUtil.handleValue(p.getValue()), p.getOp()));
				break;
			case ASSIGN:
				nodeAttrAsgn.add(new NeoAttributeExpression(nodeVarName, p.getType().getName(), //
						EMSLUtil.handleValue(p.getValue()), p.getOp()));
				break;
			default:
				throw new UnsupportedOperationException(p.getOp().toString());
			}
		}
		return new NeoAttributeReturn(nodeProps, nodeAttrExpr, nodeAttrAsgn);
	}

	public NeoAttributeReturnRelation getRelationPropertiesAndAttributes(Collection<ModelPropertyStatement> props,
			String varName, ArrayList<NeoAttributeExpression> attrExpr) {

		var relProps = new ArrayList<ModelPropertyStatement>();
		var relAttrExpr = new ArrayList<NeoAttributeExpression>();
		var relAttrAsgn = new ArrayList<NeoAttributeExpression>();

		for (var p : props) {
			switch (p.getOp()) {
			case EQ:
				relProps.add(p);
				break;
			case GREATER:
			case GREATEREQ:
			case LESS:
			case LESSEQ:
			case NOTEQ:
				relAttrExpr.add(new NeoAttributeExpression(varName, p.getType().getName(), //
						EMSLUtil.handleValue(p.getValue()), p.getOp()));
				break;
			case ASSIGN:
				relAttrAsgn.add(new NeoAttributeExpression(varName, p.getType().getName(), //
						EMSLUtil.handleValue(p.getValue()), p.getOp()));
				break;
			default:
				throw new UnsupportedOperationException(p.getOp().toString());
			}
		}
		return new NeoAttributeReturnRelation(relProps, relAttrExpr, relAttrAsgn);

	}

	private List<ModelRelationStatement> extractContextRelations(EList<ModelRelationStatement> relations) {
		return relations.stream()//
				.filter(rel -> isRelevant(rel.getAction()))//
				.collect(Collectors.toList());
	}

	private boolean isRelevant(Action action) {
		if (context)
			return action == null || action.getOp().equals(ActionOperator.DELETE);
		else
			return action == null || action.getOp().equals(ActionOperator.CREATE);
	}

	private List<ModelNodeBlock> extractContextNodes(List<ModelNodeBlock> mnb) {
		return mnb.stream()//
				.filter(n -> isRelevant(n.getAction()))//
				.collect(Collectors.toList());
	}

	public List<NeoNode> extractPatternNodesAndRelations(List<ModelNodeBlock> mnb) {
		return extractNodesAndRelations(mnb, this::registerNewPatternNode, this::registerNewPatternRelation,
				attrExprPattern);
	}

	public List<NeoNode> extractConstraintNodesAndRelations(List<ModelNodeBlock> mnb) {
		return extractNodesAndRelations(mnb, this::registerNewConstraintNode, this::registerNewConstraintRelation,
				attrExprOptional);
	}

}
