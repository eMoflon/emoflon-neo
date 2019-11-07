package org.emoflon.neo.cypher.patterns;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.emoflon.neo.cypher.common.NeoNode;
import org.emoflon.neo.cypher.common.NeoRelation;
import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.patterns.IPattern;
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
	protected Map<String, Object> parameters;

	/**
	 * @param pattern the corresponding pattern to the match
	 * @param record  one result record of the query execution
	 */
	public NeoMatch(NeoPattern pattern, Record record) {
		this.pattern = pattern;

		nodeIDs = new HashMap<>();
		edgeIDs = new HashMap<>();
		extractIdsPattern(record);

		parameters = new HashMap<>();
		parameters.putAll(nodeIDs);
		parameters.putAll(edgeIDs);
		parameters.put("match_id", getHashCode());
	}

	public NeoMatch(NeoMatch other) {
		pattern = other.pattern;
		nodeIDs = other.nodeIDs;
		edgeIDs = other.edgeIDs;

		parameters = new HashMap<>(other.parameters);
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
			if (recMap.containsKey(n.getName()))
				nodeIDs.put(n.getName(), (Long) recMap.get(n.getName()));
		}

		for (var r : pattern.getRelations()) {
			if (recMap.containsKey(r.getName()))
				edgeIDs.put(r.getName(), (Long) recMap.get(r.getName()));
		}
	}

	public long getIdForNode(NeoNode node) {
		return nodeIDs.get(node.getName());
	}

	public long getIdForNode(String varName) {
		return nodeIDs.get(varName);
	}

	public long getIdForRelation(NeoRelation rel) {
		return edgeIDs.get(rel.getName());
	}

	public Map<String, Object> getParameters() {
		return Collections.unmodifiableMap(parameters);
	}

	public void addParameter(String key, Object value) {
		parameters.put(key, value);
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

	@Override
	public Map<String, Long> getNodeIDs() {
		return nodeIDs;
	}

	@Override
	public Map<String, Long> getEdgeIDs() {
		return edgeIDs;
	}

	public String getHashCode() {
		return Integer.toString(this.hashCode());
	}
	
	public static String getIdParameter() {
		return "match_id";
	}
	
	public static String getMatchesParameter() {
		return "matches";
	}

	public static String getMatchParameter() {
		return "match";
	}

	public static Collection<Record> getData(Collection<? extends NeoMatch> matches) {
		if (matches.size() > 0) {
			var pattern = (NeoPattern) matches.iterator().next().getPattern();
			return pattern.getData(matches);
		} else
			return Collections.emptyList();
	}
}
