package org.emoflon.neo.engine.api.patterns;

import java.util.Collections;
import java.util.Map;

public class EmptyMask implements IMask {

	@Override
	public Map<String, Long> getMaskedNodes() {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, Object> getMaskedAttributes() {
		return Collections.emptyMap();
	}

	@Override
	public Map<String, Object> getParameters() {
		return Collections.emptyMap();
	}

}
