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
	protected NeoPattern pattern;
	private Record record; 
	private Map<String, Object> parameters = new HashMap<>();
	
	/**
	 * @param pattern the corresponding pattern to the match
	 * @param record  one result record of the query execution
	 */
	public NeoMatch(NeoPattern pattern, Record record) {
		this.pattern = pattern;
		this.record = record;
	}

	public NeoMatch(NeoMatch other) {
		pattern = other.pattern;
		record = other.record;
	}

	@Override
	public IPattern<NeoMatch> getPattern() {
		return pattern;
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
	public boolean containsElement(String elt) {
		return record.containsKey(elt);
	}

	@Override
	public long getElement(String elt) {
		return record.get(elt).asLong();
	}

	@Override
	public void addParameter(String key, Object value) {
		parameters.put(key, value);
	}

	@Override
	public void addAllParameters(Map<String, Object> parameters) {
		parameters.putAll(parameters);
	}

	@Override
	public Map<String, Object> convertToMap() {
		var map = new HashMap<String, Object>(record.keys().size());
		map.putAll(parameters);
		map.putAll(record.asMap());
		map.put(getIdParameter(), hashCode());
		return map;
	}

	public String getMatchID() {
		return String.valueOf(hashCode());
	}	
}
