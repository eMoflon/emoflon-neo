package org.emoflon.neo.cypher.patterns;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.patterns.IPattern;
import org.neo4j.driver.v1.Record;
import org.neo4j.driver.v1.Value;

/**
 * Class for storing a match of a pattern matching created after a query
 * execution
 * 
 * @author Jannik Hinz
 *
 */
public class NeoMatch extends NoOpMap implements IMatch {
	private NeoPattern pattern;
	protected Optional<Map<String, Object>> parameters = Optional.empty();
	protected Record record;

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
		this.record = other.record;
		parameters = other.parameters.map(p -> new HashMap<>(p));
	}

	public String getHashCode() {
		return String.valueOf(hashCode());
	}

	private Map<String, Object> getOrCreateParameters() {
		if(parameters.isEmpty())
			parameters = Optional.of(new HashMap<>());
		
		return parameters.get();
	}

	public void addParameter(String key, Object value) {
		getOrCreateParameters().put(key, value);
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

	public long getAsLong(String element) {
		return ((Value) get(element)).asLong();
	}

	// ***** Interface of Map ******

	@Override
	public int size() {
		return getOrEmptyParameters().size();
	}

	@Override
	public void putAll(Map<? extends String, ? extends Object> elements) {
		getOrCreateParameters().putAll(elements);
	}
 
	private Map<String, Object> getOrEmptyParameters(){
		return parameters.orElse(Collections.emptyMap());
	}
	
	@Override
	public Object get(Object key) {
		if(key.equals(getIdParameter()))
			return getHashCode();
		if (record.containsKey(key.toString()))
			return record.get(key.toString());
		else
			return getOrEmptyParameters().get(key);
	}
	
	@Override
	public String toString() {
		return "parameters: \n" + getOrEmptyParameters().toString() + "\nkeys: \n" + record.keys();
	}
	
	@Override
	public Set<Entry<String, Object>> entrySet() {
		var entries = new HashSet<>(record.asMap().entrySet());
		entries.addAll(getOrEmptyParameters().entrySet());
		entries.add(Map.entry(getIdParameter(), getHashCode()));
		return entries;
	}
	
	@Override
	public boolean containsKey(Object key) {
		return getOrEmptyParameters().containsKey(key) || record.containsKey(key.toString()); 
	}
}
