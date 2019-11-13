package org.emoflon.neo.cypher.patterns;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
	protected long[] nodeIDs;
	protected long[] edgeIDs;
	protected Map<String, Object> parameters;

	/**
	 * @param pattern the corresponding pattern to the match
	 * @param record  one result record of the query execution
	 */
	public NeoMatch(NeoPattern pattern, Record record) {
		this.pattern = pattern;

		nodeIDs = new long[pattern.getNodes().size()];
		edgeIDs = new long[pattern.getRelations().size()];
		extractIdsPattern(record);
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
				nodeIDs[pattern.getNodes().indexOf(n)] = (Long) recMap.get(n.getName());
		}

		for (var r : pattern.getRelations()) {
			if (recMap.containsKey(r.getName()))
				edgeIDs[pattern.getRelations().indexOf(r)] = (Long) recMap.get(r.getName());
		}
	}

	public Map<String, Object> getParameters() {
		var params = new HashMap<String, Object>();
		if(parameters != null)
			params.putAll(parameters);
			
		for (var n : pattern.getNodes())
			params.put(n.getName(), nodeIDs[pattern.getNodes().indexOf(n)]);
		for (var e : pattern.getRelations())
			params.put(e.getName(), edgeIDs[pattern.getRelations().indexOf(e)]);
		params.put("match_id", getHashCode());
		
		return Collections.unmodifiableMap(params);
	}

	public String getHashCode() {
		return String.valueOf(hashCode());
	}

	public void addParameter(String key, Object value) {
		if(parameters == null)
			parameters = new HashMap<>();
		
		parameters.put(key, value);
	}

	public IPattern<NeoMatch> getPattern() {
		return pattern;
	}

	@Override
	public long[] getNodeIDs() {
		return nodeIDs;
	}

	@Override
	public long[] getRelIDs() {
		return edgeIDs;
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

	@Override
	public boolean containsNode(String nodeName) {
		return pattern.getNodes().stream().anyMatch(n -> n.getName().equals(nodeName));
	}

	@Override
	public boolean containsRel(String relName) {
		return pattern.getRelations().stream().anyMatch(r -> r.getName().equals(relName));
	}

	@Override
	public long getNodeIDFor(String nodeName) {
		return pattern.getNodes().stream()//
				.filter(n -> n.getName().equals(nodeName))//
				.map(n -> nodeIDs[pattern.getNodes().indexOf(n)])//
				.findAny()//
				.orElseThrow();
	}

	@Override
	public long getRelIDFor(String relName) {
		return pattern.getRelations().stream()//
				.filter(r -> r.getName().equals(relName))//
				.map(r -> edgeIDs[pattern.getRelations().indexOf(r)])//
				.findAny()//
				.orElseThrow();
	}

	@Override
	public String getNameOfNode(long nodeID) {
		for (int i = 0; i < nodeIDs.length; i++) {
			if(nodeIDs[i] == nodeID)
				return pattern.getNodes().get(i).getName();
		}
		
		throw new IllegalArgumentException("Not in match: " + nodeID);
	}

	@Override
	public String getNameOfRel(long relID) {
		for (int i = 0; i < edgeIDs.length; i++) {
			if(edgeIDs[i] == relID)
				return pattern.getRelations().get(i).getName();
		}
		
		throw new IllegalArgumentException("Not in match: " + relID);
	}
}
