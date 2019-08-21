package org.emoflon.neo.engine.generator.modules;

import java.util.Collection;
import java.util.Collections;

import org.emoflon.neo.engine.api.rules.IMatch;
import org.emoflon.neo.engine.generator.IMatchReprocessor;

/**
 * Reprocessor that simply discards all remaining matches.
 */
public class ParanoidReprocessor implements IMatchReprocessor {
	@Override
	public Collection<IMatch> reprocess(Collection<IMatch> pRemainingMatches) {
		return Collections.emptySet();
	}
}
