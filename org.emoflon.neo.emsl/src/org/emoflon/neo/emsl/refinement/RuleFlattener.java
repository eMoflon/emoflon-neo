package org.emoflon.neo.emsl.refinement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.emoflon.neo.emsl.eMSL.Action;
import org.emoflon.neo.emsl.eMSL.ActionOperator;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.Entity;
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock;
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

	@Override
	protected List<ModelNodeBlock> mergeNodes(Entity entity, List<RefinementCommand> refinementList,
			Map<String, List<ModelNodeBlock>> nodeBlocks) throws FlattenerException {
		var mergedNodes = new ArrayList<ModelNodeBlock>();

		// take all nodeBlocks with the same name/key out of the HashMap and merge
		for (var name : nodeBlocks.keySet()) {
			var blocksWithKey = nodeBlocks.get(name);

			Comparator<MetamodelNodeBlock> comparator = new Comparator<MetamodelNodeBlock>() {
				@Override
				public int compare(MetamodelNodeBlock o1, MetamodelNodeBlock o2) {
					if (o1.getSuperTypes().contains(o2) || recursiveContainment(o1, o2, false)) {
						return -1;
					} else if (o2.getSuperTypes().contains(o1) || recursiveContainment(o2, o1, false)) {
						return 1;
					} else if (o1 == o2) {
						return 0;
					} else {
						// no common type could be found, merge not possible
						throw new AssertionError();
					}
				}

				private boolean recursiveContainment(MetamodelNodeBlock o1, MetamodelNodeBlock o2,
						boolean containment) {
					var wrapper = new Object() {
						boolean contains = false;
					};

					if (o1.getSuperTypes().contains(o2)) {
						return true;
					}

					o1.getSuperTypes().forEach(st -> {
						wrapper.contains = (recursiveContainment(st, o2, containment));
					});
					return wrapper.contains;
				}
			};

			// store/sort types in this PriorityQueue
			PriorityQueue<MetamodelNodeBlock> nodeBlockTypeQueue = new PriorityQueue<MetamodelNodeBlock>(comparator);

			// collect types
			for (var nb : blocksWithKey) {
				if (nb.getType() != null) {
					try {
						nodeBlockTypeQueue.add(nb.getType());
					} catch (AssertionError e) {
						throw new FlattenerException(entity, FlattenerErrorType.NO_COMMON_SUBTYPE_OF_NODES, nb);
					}
				}
			}

			// create new NodeBlock that will be added to the entity
			var newNb = EMSLFactory.eINSTANCE.createModelNodeBlock();
			newNb.setType(nodeBlockTypeQueue.peek());
			newNb.setName(name);
			newNb.setAction(mergeActionOfNodes(blocksWithKey));

			mergedNodes.add(newNb);
		}

		return mergeEdgesOfNodeBlocks(entity, nodeBlocks,
				mergePropertyStatementsOfNodeBlocks(entity, nodeBlocks, mergedNodes));
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
