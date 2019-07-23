package org.emoflon.neo.emsl.refinement;

import java.util.Set;

import org.emoflon.neo.emsl.eMSL.Entity;
import org.emoflon.neo.emsl.util.FlattenerException;

public interface IEntityFlattener {
	<T extends Entity> T flatten(T entity, Set<String> alreadyRefinedEntityNames) throws FlattenerException;
}
