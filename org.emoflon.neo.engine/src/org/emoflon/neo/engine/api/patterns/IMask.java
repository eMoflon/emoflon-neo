package org.emoflon.neo.engine.api.patterns;

import java.util.Map;

public interface IMask {
	
	Map<String, Long> getMaskedNodes();

	Map<String, Object> getMaskedAttributes();

	Map<String, Object> getParameters();
	
	void maskNode(String node, Long value);

	void maskAttribute(String attribute, Object value);
	
	void addParameter(String parameter, Object value);
	
	static IMask empty() {
		return new EmptyMask();
	}
}
