package org.emoflon.neo.engine.api.patterns;

import java.util.Map;

public interface IMatch extends Map<String, Object> {
	IPattern<? extends IMatch> getPattern();
}
