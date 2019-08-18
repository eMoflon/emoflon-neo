package org.emoflon.neo.neo4j.adapter.constraints;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.api.rules.IPattern;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;
import org.emoflon.neo.neo4j.adapter.common.NeoRelation;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.neo4j.driver.v1.Record;

/**
 * Class for storing a match or violation match of constraints, created after a
 * query execution
 * 
 * @author Jannik Hinz
 *
 */
public class NeoConstraintMatch implements IMatch {
	@SuppressWarnings("unused")
	private static final Logger logger = Logger.getLogger(NeoConstraintMatch.class);

	private Collection<NeoNode> pNodes;
	private Map<String, Long> ids;

	/**
	 * @param pNodes list of all Nodes including relations of the constraint
	 * @param record one result record of the query execution
	 */
	public NeoConstraintMatch(Collection<NeoNode> pNodes, Record record) {
		this.pNodes = pNodes;

		ids = new HashMap<String, Long>();
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

		for (var n : pNodes) {
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
	public Map<String, Long> getResults() {
		return ids;
	}

	/**
	 * Return the ID of a specific node in regards to the given node variable name
	 * 
	 * @param node NeoNode define the node name search for in the ID HashMap
	 * @return id of the NeoNode in the Result
	 */
	public long getIdForNode(NeoNode node) {
		return ids.get(node.getVarName());
	}

	/**
	 * Return the ID of a specific relation in regards to the given relation
	 * variable name
	 * 
	 * @param rel NeoRelation define the relation name search for in the ID HashMap
	 * @return id of the NeoRelation in the Result
	 */
	public long getIdForRelation(NeoRelation rel) {
		return ids.get(rel.getVarName());
	}

	/**
	 * @throws UnsupportedOperationException constraint do not have any pattern
	 */
	@Override
	public IPattern<NeoMatch> getPattern() {
		throw new UnsupportedOperationException();
	}

	/**
	 * @throws UnsupportedOperationException constraints can not be validated and
	 *                                       must be run again
	 */
	@Override
	public boolean isStillValid() {
		throw new UnsupportedOperationException();
	}
}
