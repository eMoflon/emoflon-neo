package org.emoflon.neo.neo4j.adapter;

import java.util.Map;

public abstract class NeoMask {
	/**
	 * @return a map of node names to node ids for filtering matches.
	 */
	public abstract Map<String, Long> getMaskedNodes();

	/**
	 * @return a map of attribute labels to values for filtering matches. Attribute
	 *         labels are of the form <node label>.<attribute name>
	 */
	public abstract Map<String, Object> getMaskedAttributes();
}
