package org.emoflon.neo.neo4j.adapter;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPattern;
import org.neo4j.driver.v1.Record;

/**
 * Class for storing a match of a pattern matching created after a query
 * execution
 * 
 * @author Jannik Hinz
 *
 */
public class NeoMatch implements IMatch {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	private NeoPattern pattern;
	private NeoRule rule;
	private Map<String, Long> ids;

	/**
	 * @param pattern the corresponding pattern to the match
	 * @param record  one result record of the query execution
	 */
	public NeoMatch(NeoPattern pattern, Record record) {
		this.pattern = pattern;

		ids = new HashMap<>();
		extractIdsPattern(record);
	}
	
	public NeoMatch(NeoRule rule, Record record) {
		this.rule = rule;

		ids = new HashMap<>();
		extractIdsRule(record);
	}

	/**
	 * Extracts the node and relations id out of the result set in regards to the
	 * nodes variable name and add it to the result list
	 * 
	 * @param record of the query execution
	 */
	private void extractIdsPattern(Record record) {
		var recMap = record.asMap();

		for (var n : pattern.getNodes()) {
			if (recMap.containsKey(n.getVarName()))
				ids.put(n.getVarName(), (Long) recMap.get(n.getVarName()));

			for (var r : n.getRelations()) {
				if (recMap.containsKey(r.getVarName()))
					ids.put(r.getVarName(), (Long) recMap.get(r.getVarName()));
			}
		}
	}
	private void extractIdsRule(Record record) {
		var recMap = record.asMap();

		for (var n : rule.getNodes()) {
			if (recMap.containsKey(n.getVarName()))
				ids.put(n.getVarName(), (Long) recMap.get(n.getVarName()));

			for (var r : n.getRelations()) {
				if (recMap.containsKey(r.getVarName()))
					ids.put(r.getVarName(), (Long) recMap.get(r.getVarName()));
			}
		}
	}

	public long getIdForNode(NeoNode node) {
		return ids.get(node.getVarName());
	}

	public long getIdForNode(String varName) {
		return ids.get(varName);
	}

	public long getIdForRelation(NeoRelation rel) {
		return ids.get(rel.getVarName());
	}

	public Record getData() {
		return pattern.getData(this);
	}

	/**
	 * Return the corresponding pattern of the match
	 * 
	 * @return NeoPattern corresponding to the match
	 */
	@Override
	public IPattern<NeoMatch> getPattern() {
		return pattern;
	}
	
	public IRule<NeoMatch, NeoCoMatch> getRule() {
		return rule;
	}

	/**
	 * Checks if the given match is still valid in the database by running a
	 * specific query in the database and return if this is still valid or not
	 * 
	 * @return true if the given Match is still valid (existing in the database) or
	 *         false if not
	 */
	@Override
	public boolean isStillValid() {
		if(pattern != null)
			return pattern.isStillValid(this);
		else if(rule != null)
			return rule.isStillApplicable(this);
		else
			return false;
	}
}
