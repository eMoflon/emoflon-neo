package org.emoflon.neo.neo4j.adapter.rules;

import java.awt.Label;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.function.BiConsumer;

import org.eclipse.emf.common.util.EList;
import org.emoflon.neo.emsl.eMSL.ActionOperator;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;
import org.emoflon.neo.neo4j.adapter.common.NeoRelation;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.EmptyMask;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPattern;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPatternFactory;
import org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;
import org.emoflon.neo.neo4j.adapter.util.NeoUtil;
import org.apache.log4j.Logger;

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
	
	private HashMap<String, NeoNode> blackNodes;
	private HashMap<String, NeoRelation> blackRel;
	private HashMap<String, NeoNode> redNodes;
	private HashMap<String, NeoRelation> redRel;
	private HashMap<String, NeoNode> greenNodes;
	private HashMap<String, NeoRelation> greenRel;

	public NeoRule(Rule r, IBuilder builder, NeoMask mask, NeoQueryData neoQuery) {
		
		if(mask == null)
			this.mask = new EmptyMask();
		else
			this.mask = mask;
		
		useSPOSemantics = false;
		this.builder = builder;
		this.queryData = neoQuery;

		this.nodes = new ArrayList<NeoNode>();
		this.modelNodes = new ArrayList<NeoNode>();
		this.modelRel = new ArrayList<NeoRelation>();
		this.blackNodes = new HashMap<String, NeoNode>();
		this.blackRel = new HashMap<String, NeoRelation>();
		this.redNodes = new HashMap<String, NeoNode>();
		this.redRel = new HashMap<String, NeoRelation>();
		this.greenNodes = new HashMap<String, NeoNode>();
		this.greenRel = new HashMap<String, NeoRelation>();

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
			
			for(var r: n.getRelations()) {
				
				var neoRel = new NeoRelation(neoNode,
						EMSLUtil.relationNameConvention(neoNode.getVarName(),
								EMSLUtil.getAllTypes(r), r.getTarget().getName(), n.getRelations().indexOf(r)),
						EMSLUtil.getAllTypes(r), //
						r.getLower(), r.getUpper(), //
						r.getProperties(), //
						r.getTarget().getType().getName(), //
						r.getTarget().getName());
				
				extractPropertiesFromMaskRelation(r, neoRel, mask);
				
				if(r.getAction() != null) {
					switch(r.getAction().getOp()) {
					case CREATE:
						greenRel.put(neoRel.getVarName(), neoRel);
						break;
					case DELETE:
						if(!neoRel.isPath()) {
							redRel.put(neoRel.getVarName(), neoRel);
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
			
			if(n.getAction() != null) {
				switch(n.getAction().getOp()) {
				case CREATE:
					neoNode.addProperty("ename", "\"" + neoNode.getVarName() + "\"");
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

	private void computePropertiesOfNode(ModelNodeBlock node, NeoNode neoNode, NeoMask neoMask) {
		for (var p : node.getProperties()) {
			neoNode.addProperty(p.getType().getName(), EMSLUtil.handleValue(p.getValue()));
		}
		
		extractPropertiesFromMaskNodes(node, neoNode, mask);
	}

	private void extractPropertiesFromMaskNodes(ModelNodeBlock node, NeoNode neoNode, NeoMask mask) {
		for (var propMask : mask.getMaskedAttributes().entrySet()) {
			var varName = mask.getVarName(propMask.getKey());
			if (node.getName().equals(varName)) {
				neoNode.addProperty(//
						mask.getAttributeName(propMask.getKey()), //
						EMSLUtil.handleValue(propMask.getValue()));
			}
		}
	}
	
	private void extractPropertiesFromMaskRelation(ModelRelationStatement r, NeoRelation rel, NeoMask mask) {
		for (var propMask : mask.getMaskedAttributes().entrySet()) {
			var varName = mask.getVarName(propMask.getKey());
			
			if (r.getName().equals(varName)) {
				rel.addProperty(//
						mask.getAttributeName(propMask.getKey()), //
						EMSLUtil.handleValue(propMask.getValue()));
			}
		}
	}

	private Collection<String> computeLabelsOfNode(ModelNodeBlock node) {
		var labels = new ArrayList<String>();
		if (node.getAction() != null && node.getAction().getOp() == ActionOperator.CREATE) {
			return ((NeoCoreBuilder) builder).computeLabelsFromType(node.getType());
		} else {
			labels.add(node.getType().getName());
			return labels;
		}
	}
	
	protected void addModelNodesAndRefs() {
		greenNodes.forEach((varName,n) -> {
			
			// Match corresponding EClass Node
			var eclassNode = new NeoNode("EClass","eClass_" + n.getVarName());
			eclassNode.addProperty("ename", "\"" + n.getClassTypes().iterator().next() + "\"");
			modelNodes.add(eclassNode);
			
			var metaType = new ArrayList<String>();
			metaType.add("metaType");
			
			var metaTypeRel = new NeoRelation(
					n,
					n.getVarName()+"_metaType_"+"eClass_" + n.getVarName(), 
					metaType,
					"",
					"",
					new ArrayList<>(),
					"EClass",
					"eClass_" + n.getVarName());
			
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
	public Optional<NeoCoMatch> apply(NeoMatch match) {
		logger.info("Execute Rule " + getName());
		var cypherQuery = CypherPatternBuilder.ruleExecutionQuery(nodes, match, useSPOSemantics, 
				redNodes.values(), greenNodes.values(), blackNodes.values(), 
				redRel.values(), greenRel.values(), blackRel.values(), 
				modelNodes, modelRel);
		logger.debug(cypherQuery);
		var result = builder.executeQuery(cypherQuery);

		if (result.hasNext()) {
			var record = result.next();
			return Optional.of(new NeoCoMatch(contextPattern, record));
		} else {
			return Optional.empty();
		}
	}

	public String getQuery() {
		return contextPattern.getQuery();
	}
}
