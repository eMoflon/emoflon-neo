package org.emoflon.neo.emsl.refinement;

import java.util.Set;

import org.emoflon.neo.emsl.eMSL.Entity;
import org.emoflon.neo.emsl.util.FlattenerException;

public abstract class AbstractEntityFlattener {
	/**
	 * Returns the flattened Entity (internal usage)
	 * 
	 * @param entity                    that should be flattened.
	 * @param alreadyRefinedEntityNames list of names of entities that have already
	 *                                  appeared in the refinement path (against
	 *                                  loops).
	 * @return the flattened entity.
	 * @throws FlattenerException is thrown if the entity could not be flattened.
	 */
	abstract public <T extends Entity> T flatten(T entity, Set<String> alreadyRefinedEntityNames)
			throws FlattenerException;
}
