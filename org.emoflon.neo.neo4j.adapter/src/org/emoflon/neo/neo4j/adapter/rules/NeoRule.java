package org.emoflon.neo.neo4j.adapter.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.ActionOperator;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;
import org.emoflon.neo.neo4j.adapter.common.NeoRelation;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBootstrapper;
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

import com.google.common.collect.Streams;

public class NeoRule implements IRule<NeoMatch, NeoCoMatch> {
	protected static final Logger logger = Logger.getLogger(NeoRule.class);

	protected boolean useSPOSemantics;
	protected NeoPattern contextPattern;
	protected NeoPattern coContextPattern;
	protected IBuilder builder;
	protected NeoMask mask;
	protected NeoQueryData queryData;

	private HashMap<String, NeoNode> blackNodes;
	private HashMap<String, NeoRelation> blackRel;
	private HashMap<String, NeoNode> redNodes;
	private HashMap<String, NeoRelation> redRel;
	private HashMap<String, NeoNode> greenNodes;
	private HashMap<String, NeoRelation> greenRel;

	private ArrayList<NeoAttributeExpression> attrExpr;
	private ArrayList<NeoAttributeExpression> attrAssign;

	/**
	 * This is the EMSL rule from the specification in concrete syntax. This is not
	 * mean to be modified in any way.
	 */
	private Rule rule;

	public NeoRule(Rule r, IBuilder builder, NeoMask mask, NeoQueryData neoQuery) {
		this.rule = r;

		if (mask == null)
			this.mask = new EmptyMask();
		else
			this.mask = mask;

		useSPOSemantics = true;
		this.builder = builder;
		this.queryData = neoQuery;

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
		
		coContextPattern = NeoPatternFactory.createNeoCoPattern(flatRule.getName(), nodeBlocks, flatRule.getCondition(),
				builder, mask);
		
		addMissingNodes(contextPattern);
	}

	private void addMissingNodes(NeoPattern pattern) {
		var varNamesInPattern = pattern.getNodes().stream().map(n -> n.getVarName()).collect(Collectors.toList());
		for(var entry : blackNodes.entrySet()) {
			if(!varNamesInPattern.contains(entry.getKey())) {
				pattern.addExtraNodes(List.of(entry.getValue()));
			}
		}
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

				var neoRel = new NeoRelation(neoNode, varName, EMSLUtil.getAllTypes(r), //
						r.getLower(), r.getUpper(), //
						propsR.getElemProps(), //
						r.getTarget().getType().getName(), //
						r.getTarget().getName());

				if (r.getAction() != null) {
					switch (r.getAction().getOp()) {
					case CREATE:
						extractRelationPropertiesFromMask(neoRel);
						greenRel.put(neoRel.getVarName(), neoRel);
						break;
					case DELETE:
						if (!neoRel.isPath()) {
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

			var typeNode = createBlackElementsForTyping(n);
			var typingEdge = createTypingEdge(typeNode, neoNode);

			if (n.getAction() != null) {
				switch (n.getAction().getOp()) {
				case CREATE:
					extractNodePropertiesFromMask(neoNode);
					neoNode.addProperty("ename", EMSLUtil.returnValueAsString(neoNode.getVarName()));
					neoNode.addLabel("EObject");
					greenNodes.put(neoNode.getVarName(), neoNode);
					greenRel.put(typingEdge.getVarName(), typingEdge);
					break;
				case DELETE:
					redNodes.put(neoNode.getVarName(), neoNode);
					neoNode.addRelation(typingEdge);
					redRel.put(typingEdge.getVarName(), typingEdge);
					break;
				default:
					throw new UnsupportedOperationException();
				}
			} else {
				blackNodes.put(neoNode.getVarName(), neoNode);
				neoNode.addRelation(typingEdge);
				blackRel.put(typingEdge.getVarName(), typingEdge);
			}
		}
	}

	private NeoRelation createTypingEdge(NeoNode typeNode, NeoNode neoNode) {
		return new NeoRelation(neoNode,
				neoNode.getVarName() + "_" + NeoCoreBootstrapper.META_TYPE + "_" + typeNode.getVarName(),
				NeoCoreBootstrapper.META_TYPE, NeoCoreBootstrapper.ECLASS, typeNode.getVarName());
	}

	private NeoNode createBlackElementsForTyping(ModelNodeBlock n) {
		var varNameOfMMNode = ((Metamodel) n.getType().eContainer()).getName();
		var mmNode = new NeoNode(NeoCoreBootstrapper.METAMODEL, varNameOfMMNode);
		mmNode.addProperty(NeoCoreBootstrapper.NAME_PROP, EMSLUtil.returnValueAsString(varNameOfMMNode));
		blackNodes.putIfAbsent(varNameOfMMNode, mmNode);

		var varNameOfTypeNode = n.getType().getName() + "_" + varNameOfMMNode;
		var typeNode = new NeoNode(NeoCoreBootstrapper.ECLASS, varNameOfTypeNode);
		typeNode.addProperty(NeoCoreBootstrapper.NAME_PROP, EMSLUtil.returnValueAsString(n.getType().getName()));
		blackNodes.putIfAbsent(varNameOfTypeNode, typeNode);

		var varNameOfElOfRel = varNameOfTypeNode + "_" + NeoCoreBootstrapper.META_EL_OF + "_" + varNameOfMMNode;
		var elOfRelnForTypeNode = new NeoRelation(blackNodes.get(varNameOfTypeNode), varNameOfElOfRel,
				NeoCoreBootstrapper.META_EL_OF, NeoCoreBootstrapper.METAMODEL, mmNode.getVarName());
		typeNode.addRelation(elOfRelnForTypeNode);
		blackRel.putIfAbsent(varNameOfElOfRel, elOfRelnForTypeNode);

		return blackNodes.get(varNameOfTypeNode);
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
		return applyAll(List.of(m)).map(r -> r.iterator().next());
	}

	@Override
	public Optional<Collection<NeoCoMatch>> applyAll(Collection<NeoMatch> matches) {
		logger.debug("Execute Rule " + getName());
		var cypherQuery = CypherPatternBuilder.ruleExecutionQueryCollection(getNodes(), useSPOSemantics, redNodes.values(),
				greenNodes.values(), blackNodes.values(), redRel.values(), greenRel.values(), blackRel.values(),
				attrExpr, attrAssign);

		var list = new ArrayList<Map<String, Object>>();
		matches.forEach(match -> list.add(match.getParameters()));

		var map = new HashMap<String, Object>();
		map.put("matches", list);

		logger.debug(map.toString() + "\n" + cypherQuery);
		var result = builder.executeQueryWithParameters(cypherQuery, map);

		if (result == null) {
			throw new DatabaseException("400", "Execution Error: See console log for more details.");
		} else {
			if (result.hasNext()) {
				var coMatches = new ArrayList<NeoCoMatch>();

				while (result.hasNext()) {
					var next = result.next();
					coMatches.add(new NeoCoMatch(this.coContextPattern, next, next.get("match_id").toString()));
				}
				logger.debug(coMatches.toString());
				return Optional.of(coMatches);

			} else {
				return Optional.empty();
			}
		}
	}

	public String getQuery() {
		return contextPattern.getQuery();
	}

	@Override
	public Stream<String> getContextElts() {
		return Streams.concat(blackNodes.keySet().stream(), blackRel.keySet().stream());
	}

	@Override
	public Stream<String> getCreatedElts() {
		return Streams.concat(greenNodes.keySet().stream(), greenRel.keySet().stream());
	}

	public Collection<NeoNode> getNodes() {
		var nodes = new ArrayList<NeoNode>();
		nodes.addAll(blackNodes.values());
		nodes.addAll(redNodes.values());
		return nodes;
	}

	@Override
	public boolean isStillApplicable(NeoMatch m) {
		return contextPattern.isStillValid(m);
	}

	@Override
	public Map<String, Boolean> isStillApplicable(Collection<NeoMatch> matches) {
		return contextPattern.isStillValid(matches);
	}

	@Override
	public String toString() {
		return getName();
	}

	public Rule getEMSLRule() {
		return rule;
	}

	public IBuilder getBuilder() {
		return builder;
	}
}
