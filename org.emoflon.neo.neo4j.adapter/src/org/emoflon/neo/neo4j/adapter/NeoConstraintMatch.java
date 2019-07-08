package org.emoflon.neo.neo4j.adapter;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.neo4j.driver.v1.Record;

public class NeoConstraintMatch implements IMatch {
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	private Collection<NeoNode> pNodes;
	private Map<String, Long> ids;

	public NeoConstraintMatch(Collection<NeoNode> pNodes, Record record) {
		this.pNodes = pNodes;

		ids = new HashMap<>();
		extractIds(record);
	}

	private void extractIds(Record record) {
		var recMap = record.asMap();

		for (var n : pNodes) {
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
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isStillValid() {
		throw new UnsupportedOperationException();
	}
}
