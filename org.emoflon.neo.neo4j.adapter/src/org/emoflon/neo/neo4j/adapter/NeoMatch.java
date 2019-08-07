package org.emoflon.neo.neo4j.adapter;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IPattern;
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
	private Map<String, Long> ids;

	/**
	 * @param pattern the corresponding pattern to the match
	 * @param record  one result record of the query execution
	 */
	public NeoMatch(NeoPattern pattern, Record record) {
		this.pattern = pattern;

		ids = new HashMap<>();
		extractIds(record);
	}

	/**
	 * Extracts the node and relations id out of the result set in regards to the
	 * nodes variable name and add it to the result list
	 * 
	 * @param record of the query execution
	 */
	private void extractIds(Record record) {
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

	/**
	 * Return the ID of matched nodes as a result list
	 * 
	 * @return list of ID in regards to the variable name of matched nodes
	 */
	public long getIdForNode(NeoNode node) {
		return ids.get(node.getVarName());
	}

	/**
	 * Return the ID of a specific node in regards to the given node variable name
	 * 
	 * @param node NeoNode define the node name search for in the ID HashMap
	 * @return id of the NeoNode in the Result
	 */
	public long getIdForRelation(NeoRelation rel) {
		return ids.get(rel.getVarName());
	}

	public Record getData() {
		return pattern.getData(this);
	}

	/**
	 * Return the correspondong pattern of the match
	 * 
	 * @return NeoPattern corresponding to the match
	 */
	@Override
	public IPattern<NeoMatch> getPattern() {
		return pattern;
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
		return pattern.isStillValid(this);
	}
}
