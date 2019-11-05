package org.emoflon.neo.engine.api.patterns;

import java.util.Map;

public interface IMask {
	/**
	 * @return a map of node names to node ids for filtering matches.
	 */
	Map<String, Long> getMaskedNodes();

	/**
	 * @return a map of attribute labels to values for filtering matches. Attribute
	 *         labels are of the form <node label>.<attribute name>
	 */
	Map<String, Object> getMaskedAttributes();

	Map<String, Object> getParameters();
	
	static IMask empty() {
		return new EmptyMask();
	}
}
