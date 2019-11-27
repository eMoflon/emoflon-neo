package org.emoflon.neo.engine.api.patterns;

import java.util.List;
import java.util.Map;

public interface IMatch {
	IPattern<? extends IMatch> getPattern();
	
	boolean containsElement(String elt);
	long getElement(String elt);
	
	void addParameter(String param, Object value);
	void addAllParameters(Map<String, Object> parameters);
	
	Map<String, Object> convertToMap();
	List<String> getKeysForElements();
	List<Long> getElements();
}
