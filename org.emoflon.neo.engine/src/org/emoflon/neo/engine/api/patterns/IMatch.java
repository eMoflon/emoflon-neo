package org.emoflon.neo.engine.api.patterns;

import java.util.Map;

public interface IMatch {
	IPattern<?> getPattern();
	
	Map<String, Long> getNodeIDs();
	Map<String, Long> getEdgeIDs();
}
