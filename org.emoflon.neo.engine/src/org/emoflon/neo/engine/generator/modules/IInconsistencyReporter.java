package org.emoflon.neo.engine.generator.modules;

import java.util.Collection;

public interface IInconsistencyReporter {
	void reportInconsistencies(Collection<Long> inconsistentNodeIds, Collection<Long> inconsistentRelationshipIds);
}
