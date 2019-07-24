package org.emoflon.neo.emsl.refinement;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.emoflon.neo.emsl.eMSL.Entity;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.RefinementCommand;
import org.emoflon.neo.emsl.util.FlattenerErrorType;
import org.emoflon.neo.emsl.util.FlattenerException;

public class PatternFlattener extends AbstractEntityFlattener implements IEntityFlattener {

	@Override
	public <T extends Entity> T flatten(T entity, Set<String> alreadyRefinedEntityNames) throws FlattenerException {
		if (entity != null) {
			@SuppressWarnings("unchecked")
			var refinements = (EList<RefinementCommand>) dispatcher.getSuperRefinementTypes(entity);

			// check for loop in refinements

			if (alreadyRefinedEntityNames.contains(dispatcher.getName(entity)))
				throw new FlattenerException(entity, FlattenerErrorType.INFINITE_LOOP, alreadyRefinedEntityNames);
			// if none has been found, add current name to list

			// check if anything has to be done, if not return
			if (refinements.isEmpty())
				return entity;

			// 1. step: collect nodes with edges
			var<String, ArrayList<ModelNodeBlock>> collectedNodeBlocks = collectNodes(entity, refinements,
					alreadyRefinedEntityNames, true);
			dispatcher.getNodeBlocks(entity).forEach(nb -> {
				if (collectedNodeBlocks.keySet().contains(nb.getName())) {
					collectedNodeBlocks.get(nb.getName()).add(nb);
				} else {
					var<ModelNodeBlock> tmp = new ArrayList<ModelNodeBlock>();
					tmp.add(nb);
					collectedNodeBlocks.put(nb.getName(), tmp);
				}
			});

			// 2. step: merge nodes and edges
			var mergedNodes = mergeNodes(entity, refinements, collectedNodeBlocks);

			// 3. step: add merged nodeBlocks to entity
			dispatcher.getNodeBlocks(entity).clear();
			dispatcher.getNodeBlocks(entity).addAll((mergedNodes));

			// 4. step: merge attribute conditions
			mergeAttributeConditions(entity, refinements);

			checkForResolvedProxies(entity);
		}

		return entity;
	}
}
