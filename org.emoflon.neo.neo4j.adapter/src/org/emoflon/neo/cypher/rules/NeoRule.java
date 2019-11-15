package org.emoflon.neo.cypher.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
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

	public static final int BATCH_SIZE = 25000;

	private boolean useSPO;
	private Rule emslRule;
	private IBuilder builder;
	private Map<String, NeoNode> greenNodes;
	private Map<String, NeoRelation> greenRels;
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
		greenRels = new HashMap<>();
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
						greenRels.put(neoRelation.getName(), neoRelation);
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
		if(matches.isEmpty())
			return Collections.emptyList();
		
		var cypherQuery = getQuery(mask);
		logger.debug("\n" + cypherQuery);

		var numberOfMatches = matches.size();
		var batches = numberOfMatches / BATCH_SIZE;
		var matchItr = matches.iterator();
		var comatches = new ArrayList<NeoCoMatch>();
		logger.info("Applying " + matches.size() + " matches, in " + batches+1 + " batches of size " + BATCH_SIZE);
		for (int i = 0; i <= batches; i++)
			applyBatch(cypherQuery, matchItr, i, mask, comatches);

		return comatches;
	}

	private void applyBatch(String cypherQuery, Iterator<NeoMatch> matchItr, int batchNr, IMask mask,
			Collection<NeoCoMatch> comatches) {
		// Parameters from match and mask
		var parameters = new ArrayList<Map<String, Object>>();
		var start = batchNr * BATCH_SIZE;
		var end = start + BATCH_SIZE;
		for (int i = start; i < end && matchItr.hasNext(); i++) {
			var m = matchItr.next();
			m.putAll(mask.getParameters());
			parameters.add(m);
		}

		// Execute rule
		var params = new HashMap<String, Object>(Map.of(NeoMatch.getMatchesParameter(), parameters));
		var result = builder.executeQuery(cypherQuery, params);

		logger.info("Applied Batch: " + batchNr);
		
		extractCoMatches(result, comatches);
	}

	private void extractCoMatches(StatementResult result, Collection<NeoCoMatch> comatches) {
		if (result == null) {
			throw new NeoDatabaseException();
		} else {
			while (result.hasNext()) {
				var record = result.next();
				comatches.add(new NeoCoMatch(postcondition, record));
			}
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

	public Collection<String> getDeletedElts() {
		var deletedNodes = redNodes.keySet().stream();
		var deletedEdges = redEdges.keySet().stream();
		return Streams.concat(deletedNodes, deletedEdges).collect(Collectors.toList());
	}

	public Map<String, NeoNode> getCreatedNodes() {
		return greenNodes;
	}

	public Map<String, NeoRelation> getCreatedEdges() {
		return greenRels;
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
		relevantElements.addAll(greenRels.values());

		return relevantElements.stream()//
				.flatMap(elt -> elt.getAttributeAssignments().stream())//
				.collect(Collectors.toList());
	}

	@Override
	public String getName() {
		return emslRule.getName();
	}

	public NeoPattern getPrecondition() {
		return precondition;
	}

	@Override
	public String toString() {
		return getName();
	}

	@Override
	public Collection<String> getContextNodeLabels() {
		return precondition.getContextNodeLabels();
	}

	@Override
	public Collection<String> getContextRelLabels() {
		return precondition.getContextRelLabels();
	}

	@Override
	public Collection<String> getCreatedNodeLabels() {
		return greenNodes.keySet();
	}

	@Override
	public Collection<String> getCreatedRelLabels() {
		return greenRels.keySet();
	}
}
