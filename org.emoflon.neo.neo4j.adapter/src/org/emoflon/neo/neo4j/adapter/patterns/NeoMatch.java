package org.emoflon.neo.neo4j.adapter.patterns;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.patterns.IPattern;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;
import org.emoflon.neo.neo4j.adapter.common.NeoRelation;
import org.neo4j.driver.v1.Record;

/**
 * Class for storing a match of a pattern matching created after a query
 * execution
 * 
 * @author Jannik Hinz
 *
 */
public class NeoMatch implements IMatch {
	private NeoPattern pattern;
	protected Map<String, Long> nodeIDs;
	protected Map<String, Long> edgeIDs;
	protected UUID uuid;

	/**
	 * @param pattern the corresponding pattern to the match
	 * @param record  one result record of the query execution
	 */
	public NeoMatch(NeoPattern pattern, Record record) {
		this.pattern = pattern;

		nodeIDs = new HashMap<>();
		edgeIDs = new HashMap<>();
		extractIdsPattern(record);
		uuid = UUID.randomUUID();
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
				nodeIDs.put(n.getVarName(), (Long) recMap.get(n.getVarName()));

			for (var r : n.getRelations()) {
				if (recMap.containsKey(r.getVarName()))
					edgeIDs.put(r.getVarName(), (Long) recMap.get(r.getVarName()));
			}
		}
	}

	public long getIdForNode(NeoNode node) {
		return nodeIDs.get(node.getVarName());
	}

	public long getIdForNode(String varName) {
		return nodeIDs.get(varName);
	}

	public long getIdForRelation(NeoRelation rel) {
		return edgeIDs.get(rel.getVarName());
	}

	public Record getData() {
		return pattern.getData(this);
	}
	
	public Map<String, Object> getParameters() {
		var map = new HashMap<String,Object>();
		map.putAll(nodeIDs);
		map.putAll(edgeIDs);
		map.put("uuid", getUUID());
		return map;
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

	@Override
	public Map<String, Long> getNodeIDs() {
		return nodeIDs;
	}

	@Override
	public Map<String, Long> getEdgeIDs() {
		return edgeIDs;
	}
	
	public String getUUID() {
		return uuid.toString();
	}
}
