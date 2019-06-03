package org.emoflon.neo.neo4j.adapter;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.neo4j.driver.v1.Record;

public class NeoMatch implements IMatch {
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	private NeoPattern pattern;
	private Map<String, Long> ids;

	public NeoMatch(NeoPattern pattern, Record record) {
		this.pattern = pattern;

		ids = new HashMap<>();
		extractIds(record);
	}

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

		logger.debug("Extracted ids: " + ids);
	}

	public long getIdForNode(NeoNode node) {
		return ids.get(node.getVarName());
	}

	public long getIdForRelation(NeoRelation rel) {
		return ids.get(rel.getVarName());
	}

	@Override
	public IPattern getPattern() {
		return pattern;
	}

	@Override
	public boolean isStillValid() {
		return pattern.isStillValid(this);
	}
}
