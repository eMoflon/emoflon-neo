package org.emoflon.neo.emsl.refinement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.emoflon.neo.emsl.eMSL.Action;
import org.emoflon.neo.emsl.eMSL.ActionOperator;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.Entity;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.RefinementCommand;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.emsl.util.FlattenerErrorType;
import org.emoflon.neo.emsl.util.FlattenerException;

public class RuleFlattener extends PatternFlattener {

	@Override
	protected Map<String, List<ModelNodeBlock>> collectNodes(Entity entity, List<RefinementCommand> refinementList,
			Set<String> alreadyRefinedEntityNames) throws FlattenerException {
		var nodeBlocks = new HashMap<String, List<ModelNodeBlock>>();

		for (var r : refinementList) {

			if (!(r.getReferencedType() instanceof Rule)) {
				throw new FlattenerException(entity, FlattenerErrorType.NON_COMPLIANT_SUPER_ENTITY,
						r.getReferencedType());
			}

			// add current entity to list of names to detect infinite loop
			var alreadyRefinedEntityNamesCopy = new HashSet<String>(alreadyRefinedEntityNames);
			alreadyRefinedEntityNamesCopy.add(dispatcher.getName(entity));

			// recursively flatten superEntities
			var nodeBlocksOfSuperEntity = new ArrayList<ModelNodeBlock>();

			Entity flattenedSuperEntity = (flatten((Entity) r.getReferencedType(), alreadyRefinedEntityNamesCopy));

			// check if a superEntity possesses a condition block
			if (r.getReferencedType() instanceof Rule && ((Rule) r.getReferencedType()).getCondition() != null) {
				throw new FlattenerException(entity, FlattenerErrorType.REFINE_ENTITY_WITH_CONDITION,
						r.getReferencedType());
			}

			if (flattenedSuperEntity != null) {
				EList<ModelNodeBlock> nodeBlocksOfFlattenedSuperEntity = dispatcher.getNodeBlocks(flattenedSuperEntity);

				for (var nb : nodeBlocksOfFlattenedSuperEntity) {

					// create new NodeBlock
					var newNb = copyModelNodeBlock(nb, r);

					// add nodeBlock to list according to its name
					if (!nodeBlocks.containsKey(newNb.getName())) {
						var<ModelNodeBlock> newList = new ArrayList<ModelNodeBlock>();
						newList.add(newNb);
						nodeBlocks.put(newNb.getName(), newList);
					} else {
						nodeBlocks.get(newNb.getName()).add(newNb);
					}
					nodeBlocksOfSuperEntity.add(newNb);
				}

				reAdjustTargetsOfEdges(nodeBlocksOfSuperEntity, r);
			}
		}
		return nodeBlocks;
	}

	/**
	 * Takes a list of nodes and merges their actions with the "black wins"
	 * principle.
	 * 
	 * @param nodes list of nodes that provide actions must be merged
	 * @return an action if a merged action can be determined, null if not
	 */
	protected Action mergeActionOfNodes(List<ModelNodeBlock> nodes) {
		var action = EMSLFactory.eINSTANCE.createAction();

		boolean green = false;
		boolean red = false;
		boolean black = false;
		for (var nb : nodes) {
			if (nb.getAction() != null && nb.getAction().getOp() == ActionOperator.CREATE)
				green = true;
			else if (nb.getAction() != null && nb.getAction().getOp() == ActionOperator.DELETE)
				red = true;
			else
				black = true;
		}
		if (green && !red && !black)
			action.setOp(ActionOperator.CREATE);
		else if (!green && red && !black)
			action.setOp(ActionOperator.DELETE);
		else
			action = null;

		return action;
	}
}
