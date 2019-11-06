package org.emoflon.neo.cypher.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.emoflon.neo.cypher.common.NeoDatabaseException;
import org.emoflon.neo.cypher.common.NeoElement;
import org.emoflon.neo.cypher.common.NeoNode;
import org.emoflon.neo.cypher.common.NeoProperty;
import org.emoflon.neo.cypher.common.NeoRelation;
import org.emoflon.neo.cypher.models.IBuilder;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.patterns.NeoPattern;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.engine.api.patterns.IMask;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.Schedule;
import org.neo4j.driver.v1.StatementResult;

import com.google.common.collect.Streams;

public class NeoRule implements IRule<NeoMatch, NeoCoMatch> {
	private static final Logger logger = Logger.getLogger(NeoRule.class);

	private boolean useSPO;
	private Rule emslRule;
	private IBuilder builder;
	private Map<String, NeoNode> greenNodes;
	private Map<String, NeoRelation> greenEdges;
	private Map<String, NeoNode> redNodes;
	private Map<String, NeoRelation> redEdges;
	private Map<String, NeoNode> blackNodes;
	private Map<String, NeoRelation> blackEdges;
	private NeoPattern precondition;
	private NeoPattern postcondition;

	public NeoRule(Rule r, IBuilder builder) throws FlattenerException {
		precondition = new NeoPattern(r.getName(), r.getNodeBlocks(), builder, r.getCondition(), false);
		postcondition = new NeoPattern(r.getName(), r.getNodeBlocks(), builder, r.getCondition(), true);

		this.useSPO = true;
		this.emslRule = r;
		this.builder = builder;

		greenNodes = new HashMap<>();
		greenEdges = new HashMap<>();
		redNodes = new HashMap<>();
		redEdges = new HashMap<>();
		blackNodes = new HashMap<>();
		blackEdges = new HashMap<>();

		var nbToNodes = new HashMap<ModelNodeBlock, NeoNode>();

		for (var nb : r.getNodeBlocks()) {
			nbToNodes.put(nb, new NeoNode(nb));
			if (nb.getAction() != null) {
				switch (nb.getAction().getOp()) {
				case CREATE:
					greenNodes.put(nb.getName(), nbToNodes.get(nb));
					break;
				case DELETE:
					redNodes.put(nb.getName(), nbToNodes.get(nb));
					break;
				default:
					throw new IllegalArgumentException("Unexpected value: " + nb.getAction());
				}
			} else {
				blackNodes.put(nb.getName(), nbToNodes.get(nb));
			}
		}

		for (var nb : r.getNodeBlocks()) {
			for (var rel : nb.getRelations()) {
				var neoRelation = new NeoRelation(rel, nbToNodes);
				if (rel.getAction() != null) {
					switch (rel.getAction().getOp()) {
					case CREATE:
						greenEdges.put(neoRelation.getName(), neoRelation);
						break;
					case DELETE:
						redEdges.put(neoRelation.getName(), neoRelation);
						break;
					default:
						throw new IllegalArgumentException("Unexpected value: " + rel.getAction());
					}
				} else {
					blackEdges.put(neoRelation.getName(), neoRelation);
				}
			}
		}
	}

	/******** Rule Application ********/

	@Override
	public Collection<NeoCoMatch> applyAll(Collection<NeoMatch> matches, IMask mask) {
		var cypherQuery = getQuery(mask);

		var parameters = new ArrayList<Map<String, Object>>();
		matches.forEach(m -> parameters.add(m.getParameters()));

		var result = builder.executeQuery(cypherQuery, Map.of(NeoMatch.getMatchesParameter(), parameters));
		logger.debug(parameters);
		logger.debug("\n" + cypherQuery);
		return extractCoMatches(result);
	}

	private Collection<NeoCoMatch> extractCoMatches(StatementResult result) {
		if (result == null) {
			throw new NeoDatabaseException();
		} else {
			var matches = new ArrayList<NeoCoMatch>();
			while (result.hasNext()) {
				var record = result.next();
				matches.add(new NeoCoMatch(postcondition, record));
			}
			return matches;
		}
	}

	public String getQuery(IMask mask) {
		return CypherRuleQueryGenerator.query(this, mask).toString();
	}

	@Override
	public Collection<NeoMatch> determineMatches(Schedule schedule, IMask mask) {
		return precondition.determineMatches(schedule, mask);
	}

	@Override
	public Map<String, Boolean> isStillValid(Collection<NeoMatch> matches) {
		return precondition.isStillValid(matches);
	}

	/******** Getter and Setter ********/

	@Override
	public void setSPOSemantics(boolean spoSemantics) {
		useSPO = spoSemantics;
	}

	@Override
	public boolean getSPOSemantics() {
		return useSPO;
	}

	@Override
	public Collection<String> getCreatedElts() {
		var createdNodes = greenNodes.keySet().stream();
		var createdEdges = greenEdges.keySet().stream();
		return Streams.concat(createdNodes, createdEdges).collect(Collectors.toList());
	}

	public Collection<String> getDeletedElts() {
		var deletedNodes = redNodes.keySet().stream();
		var deletedEdges = redEdges.keySet().stream();
		return Streams.concat(deletedNodes, deletedEdges).collect(Collectors.toList());
	}

	public Map<String, NeoNode> getCreatedNodes() {
		return greenNodes;
	}
	
	public Map<String, NeoRelation> getCreatedEdges(){
		return greenEdges;
	}

	@Override
	public boolean hasApplicationConditions() {
		return emslRule.getCondition() != null;
	}

	public Rule getEMSLRule() {
		return emslRule;
	}

	public Collection<NeoProperty> getAttributeAssignments() {
		var relevantElements = new ArrayList<NeoElement>();
		relevantElements.addAll(blackNodes.values());
		relevantElements.addAll(blackEdges.values());
		relevantElements.addAll(greenNodes.values());
		relevantElements.addAll(greenEdges.values());

		return relevantElements.stream()//
				.flatMap(elt -> elt.getAttributeAssignments().stream())//
				.collect(Collectors.toList());
	}

	@Override
	public String getName() {
		return emslRule.getName();
	}

	@Override
	public Collection<String> getElements() {
		return precondition.getElements();
	}
	
	public NeoPattern getPrecondition() {
		return precondition;
	}
}
