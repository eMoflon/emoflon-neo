package org.emoflon.neo.cypher.patterns;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;

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
public class NeoMatch extends HashMap<String, Object> implements IMatch {
	private static final long serialVersionUID = 1L;
	protected NeoPattern pattern;
	
	/**
	 * @param pattern the corresponding pattern to the match
	 * @param record  one result record of the query execution
	 */
	public NeoMatch(NeoPattern pattern, Record record) {
		super(record.asMap()); 
		this.pattern = pattern;
	}

	public NeoMatch(NeoMatch other) {
		pattern = other.pattern;
		putAll(other);
	}

	public String getHashCode() {
		return String.valueOf(hashCode());
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
		return (Long) get(element);
	}
}
