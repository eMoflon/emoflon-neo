package org.emoflon.neo.neo4j.adapter;

import java.util.Collections;
import java.util.Map;

public class EmptyMask extends NeoMask {

	@Override
	public Map<String, Long> getMaskedNodes() {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, Object> getMaskedAttributes() {
		return Collections.emptyMap();
	}

}
