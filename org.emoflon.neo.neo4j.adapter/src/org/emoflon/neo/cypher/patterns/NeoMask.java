package org.emoflon.neo.cypher.patterns;

import java.util.HashMap;
import java.util.Map;

import org.emoflon.neo.engine.api.patterns.IMask;

public abstract class NeoMask implements IMask {
	private Map<String, Object> parameters;
	
	public NeoMask() {
		this.parameters = new HashMap<>();
	}
	
	/**
	 * @return a map of node names to node ids for filtering matches.
	 */
	@Override
	public abstract Map<String, Long> getMaskedNodes();

	/**
	 * @return a map of attribute labels to values for filtering matches. Attribute
	 *         labels are of the form <node label>.<attribute name>
	 */
	@Override
	public abstract Map<String, Object> getMaskedAttributes();

	@Override
	public Map<String, Object> getParameters() {
		return parameters;
	}
}
