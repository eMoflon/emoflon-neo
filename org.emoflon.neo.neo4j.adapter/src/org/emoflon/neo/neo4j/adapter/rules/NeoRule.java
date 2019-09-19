package org.emoflon.neo.neo4j.adapter.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.ActionOperator;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;
import org.emoflon.neo.neo4j.adapter.common.NeoRelation;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.EmptyMask;
import org.emoflon.neo.neo4j.adapter.patterns.NeoAttributeExpression;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPattern;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPatternFactory;
import org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;
import org.emoflon.neo.neo4j.adapter.util.NeoUtil;
import org.neo4j.driver.v1.exceptions.DatabaseException;

public class NeoRule implements IRule<NeoMatch, NeoCoMatch> {
	protected static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	protected boolean useSPOSemantics;
	protected NeoPattern contextPattern;
	protected IBuilder builder;
	protected NeoMask mask;
	protected NeoQueryData queryData;

	private ArrayList<NeoNode> nodes;
	private ArrayList<NeoNode> modelNodes;
	private ArrayList<NeoRelation> modelRel;
	private HashMap<String, NeoRelation> modelEContainerRel;

	private HashMap<String, NeoNode> blackNodes;
	private HashMap<String, NeoRelation> blackRel;
	private HashMap<String, NeoNode> redNodes;
	private HashMap<String, NeoRelation> redRel;
	private HashMap<String, NeoNode> greenNodes;
	private HashMap<String, NeoRelation> greenRel;
	
	private ArrayList<NeoAttributeExpression> attrExpr;
	private ArrayList<NeoAttributeExpression> attrAssign;

	public NeoRule(Rule r, IBuilder builder, NeoMask mask, NeoQueryData neoQuery) {

		if (mask == null)
			this.mask = new EmptyMask();
		else
			this.mask = mask;

		useSPOSemantics = true;
		this.builder = builder;
		this.queryData = neoQuery;

		this.nodes = new ArrayList<NeoNode>();
		this.modelNodes = new ArrayList<NeoNode>();
		this.modelRel = new ArrayList<NeoRelation>();
		this.modelEContainerRel = new HashMap<String,NeoRelation>();
		this.blackNodes = new HashMap<String, NeoNode>();
		this.blackRel = new HashMap<String, NeoRelation>();
		this.redNodes = new HashMap<String, NeoNode>();
		this.redRel = new HashMap<String, NeoRelation>();
		this.greenNodes = new HashMap<String, NeoNode>();
		this.greenRel = new HashMap<String, NeoRelation>();
		
		this.attrExpr = new ArrayList<NeoAttributeExpression>();
		this.attrAssign = new ArrayList<NeoAttributeExpression>();

		var flatRule = NeoUtil.getFlattenedRule(r);
		var nodeBlocks = flatRule.getNodeBlocks();

		extractNodesAndRelations(nodeBlocks);

		contextPattern = NeoPatternFactory.createNeoPattern(flatRule.getName(), nodeBlocks, flatRule.getCondition(),
				builder, mask);
	}

	private void extractNodesAndRelations(Collection<ModelNodeBlock> nodeBlocks) {
		
		for (var n : nodeBlocks) {

			var labels = new ArrayList<String>(computeLabelsOfNode(n));
			var neoNode = new NeoNode(labels, n.getName());

			computePropertiesOfNode(n, neoNode, mask);

			for (var r : n.getRelations()) {
				
				var varName = EMSLUtil.relationNameConvention(neoNode.getVarName(), EMSLUtil.getAllTypes(r),
						r.getTarget().getName(), n.getRelations().indexOf(r));
				
				var propsR = queryData.getRelationPropertiesAndAttributes(r.getProperties(), varName, this.attrExpr);
				attrExpr.addAll(propsR.getElemAttrExpr());
				attrAssign.addAll(propsR.getElemAttrAsgn());

				var neoRel = new NeoRelation(neoNode,
						varName,
						EMSLUtil.getAllTypes(r), //
						r.getLower(), r.getUpper(), //
						propsR.getElemProps(), //
						r.getTarget().getType().getName(), //
						r.getTarget().getName());
				
				var eCont = computeEContainerReferences(r,neoRel);
				
				if (r.getAction() != null) {
					switch (r.getAction().getOp()) {
					case CREATE:
						extractRelationPropertiesFromMask(neoRel);
						greenRel.put(neoRel.getVarName(), neoRel);
						if(eCont != null && !alreadyAContainer(eCont)) {
							greenRel.put(eCont.getVarName(), eCont);
						}
						break;
					case DELETE:
						if (!neoRel.isPath()) {
							redRel.put(neoRel.getVarName(), neoRel);
							if(eCont != null && !modelEContainerRel.containsKey(eCont.getFromNodeVar())) {
								redRel.put(eCont.getVarName(), eCont);
								modelEContainerRel.put(eCont.getFromNodeVar(), eCont);
							}
						}
						neoNode.addRelation(neoRel);
						break;
					default:
						throw new UnsupportedOperationException();
					}
				} else {
					blackRel.put(neoRel.getVarName(), neoRel);
					neoNode.addRelation(neoRel);
				}
			}

			if (n.getAction() != null) {
				switch (n.getAction().getOp()) {
				case CREATE:
					extractNodePropertiesFromMask(neoNode);
					neoNode.addProperty("ename", EMSLUtil.returnValueAsString(neoNode.getVarName()));
					neoNode.addLabel("EObject");
					greenNodes.put(neoNode.getVarName(), neoNode);
					break;
				case DELETE:
					redNodes.put(neoNode.getVarName(), neoNode);
					nodes.add(neoNode);
					break;
				default:
					throw new UnsupportedOperationException();
				}
			} else {
				blackNodes.put(neoNode.getVarName(), neoNode);
				nodes.add(neoNode);
			}
		}

		addModelNodesAndRefs();
	}
	
	private NeoRelation computeEContainerReferences(ModelRelationStatement r, NeoRelation neoRel) {
		
		for(var t : r.getTypes()) {
			
			if (t.getType().getKind().getName().equals("COMPOSITION") || t.getType().getKind().getName().equals("AGGREGATION")) {
				
				var oppositeNode = new NeoNode(neoRel.getToNodeLabel(), neoRel.getToNodeVar());
				var typeRel = new ArrayList<String>();
				typeRel.add("eContainer");
				
				var eConNeoRel = new NeoRelation(
						oppositeNode,
						"eContainer_" + neoRel.getVarName(),
						typeRel, 
						r.getLower(),
						r.getUpper(),
						new ArrayList<ModelPropertyStatement>(),
						neoRel.getFromNodeLabel(),
						neoRel.getFromNodeVar());
				
				eConNeoRel.addProperty("isComposition", EMSLUtil.returnValueAsString(t.getType().getKind().getName().equals("COMPOSITION")));	
				return eConNeoRel;
			}
		}
		
		return null;
	}
	
	private boolean alreadyAContainer(NeoRelation rel) {
		
		var labels = rel.getRelTypes();
		
		for(var l : labels) {
			for(var r : greenRel.values()) {
					
				if(r.getRelTypes().contains(l)) {
					return true;
				}
			}
		}
		return false;
	}

	private void computePropertiesOfNode(ModelNodeBlock node, NeoNode neoNode, NeoMask neoMask) {
		var props = queryData.getNodePropertiesAndAttributes(node.getProperties(), neoNode.getVarName(), this.attrExpr);
		props.getElemProps().forEach(prop -> neoNode.addProperty(prop.getName(), prop.getValue()));
		attrExpr.addAll(props.getElemAttrExpr());
		attrAssign.addAll(props.getElemAttrAsgn());
	}

	private void extractNodePropertiesFromMask(NeoNode neoNode) {

		for (var propMask : mask.getMaskedAttributes().entrySet()) {
			var varName = mask.getVarName(propMask.getKey());
			if (neoNode.getVarName().equals(varName)) {
				neoNode.addProperty(//
						mask.getAttributeName(propMask.getKey()), //
						EMSLUtil.returnValueAsString(propMask.getValue()));
			}
		}
	}

	private void extractRelationPropertiesFromMask(NeoRelation neoRel) {

		for (var propMask : mask.getMaskedAttributes().entrySet()) {
			var varName = mask.getVarName(propMask.getKey());
			if (neoRel.getVarName().equals(varName)) {
				neoRel.addProperty(//
						mask.getAttributeName(propMask.getKey()), //
						EMSLUtil.returnValueAsString(propMask.getValue()));
			}

		}
	}

	private Collection<String> computeLabelsOfNode(ModelNodeBlock node) {
		var labels = new ArrayList<String>();
		if (node.getAction() != null && node.getAction().getOp() == ActionOperator.CREATE) {
			return NeoCoreBuilder.computeLabelsFromType(node.getType());
		} else {
			labels.add(node.getType().getName());
			return labels;
		}
	}

	protected void addModelNodesAndRefs() {
		greenNodes.forEach((varName, n) -> {

			// Match corresponding EClass Node
			var eclassNode = new NeoNode("EClass", "eClass_" + n.getVarName());
			eclassNode.addProperty("ename", EMSLUtil.returnValueAsString(n.getClassTypes().iterator().next()));
			modelNodes.add(eclassNode);

			var metaType = new ArrayList<String>();
			metaType.add("metaType");

			var metaTypeRel = new NeoRelation(n, n.getVarName() + "_metaType_" + "eClass_" + n.getVarName(), metaType,
					"", "", new ArrayList<>(), "EClass", "eClass_" + n.getVarName());

			modelRel.add(metaTypeRel);
		});
	}

	@Override
	public void useSPOSemantics(boolean spoSemantics) {
		this.useSPOSemantics = spoSemantics;
	}

	@Override
	public String getName() {
		return contextPattern.getName();
	}

	@Override
	public void setMatchInjectively(Boolean injective) {
		contextPattern.setMatchInjectively(injective);
	}

	@Override
	public Collection<NeoMatch> determineMatches() {
		return contextPattern.determineMatches();
	}

	@Override
	public Collection<NeoMatch> determineMatches(int limit) {
		return contextPattern.determineMatches(limit);
	}

	@Override
	public Optional<NeoCoMatch> apply(NeoMatch m) {
		logger.info("Execute Rule " + getName());
		var cypherQuery = CypherPatternBuilder.ruleExecutionQuery(nodes, useSPOSemantics, redNodes.values(),
				greenNodes.values(), blackNodes.values(), redRel.values(), greenRel.values(), blackRel.values(),
				modelNodes, modelRel, modelEContainerRel.values(), attrExpr, attrAssign);
		logger.debug(m.getParameters().toString() + "\n" + cypherQuery);
		var result = builder.executeQueryWithParameters(cypherQuery, m.getParameters());

		if (result == null) {
			throw new DatabaseException("400", "Execution Error: See console log for more details.");
		} else {
			if (result.hasNext()) {
				var record = result.next();
				return Optional.of(new NeoCoMatch(contextPattern, record));
			} else {
				return Optional.empty();
			}
		}
	}

	public String getQuery() {
		return contextPattern.getQuery();
	}
}
