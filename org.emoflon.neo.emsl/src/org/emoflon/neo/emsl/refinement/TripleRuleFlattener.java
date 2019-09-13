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
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.emoflon.neo.emsl.eMSL.Correspondence;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.RefinementCommand;
import org.emoflon.neo.emsl.eMSL.SuperType;
import org.emoflon.neo.emsl.eMSL.TripleRule;
import org.emoflon.neo.emsl.util.FlattenerErrorType;
import org.emoflon.neo.emsl.util.FlattenerException;

public class TripleRuleFlattener extends RuleFlattener {

	@Override
	public SuperType flatten(SuperType t, Set<String> alreadyRefinedEntityNames) throws FlattenerException {
		var entity = (TripleRule) t;

		if (entity != null) {
			EList<RefinementCommand> refinements = (EList<RefinementCommand>) dispatcher
					.getSuperRefinementTypes(entity);

			// check for loop in refinements

			if (alreadyRefinedEntityNames.contains(dispatcher.getName(entity)))
				throw new FlattenerException(entity, FlattenerErrorType.INFINITE_LOOP, alreadyRefinedEntityNames);
			// if none has been found, add current name to list

			// check if anything has to be done, if not return
			if (refinements.isEmpty())
				return entity;

			// --------------- Source ------------------ //
			// 1. step: collect nodes with edges
			var<String, ArrayList<ModelNodeBlock>> collectedSrcNodeBlocks = collectNodes(entity, refinements,
					alreadyRefinedEntityNames, true);
			entity.getSrcNodeBlocks().forEach(nb -> {
				if (collectedSrcNodeBlocks.keySet().contains(nb.getName())) {
					collectedSrcNodeBlocks.get(nb.getName()).add(nb);
				} else {
					var<ModelNodeBlock> tmp = new ArrayList<ModelNodeBlock>();
					tmp.add(nb);
					collectedSrcNodeBlocks.put(nb.getName(), tmp);
				}
			});
			// 2. step: merge nodes and edges
			var mergedSrcNodes = mergeNodes(entity, refinements, collectedSrcNodeBlocks);

			// 3. step: add merged nodeBlocks to entity
			entity.getSrcNodeBlocks().clear();
			entity.getSrcNodeBlocks().addAll((mergedSrcNodes));

			// --------------- Target ------------------ //
			// 1. step: collect nodes with edges
			var<String, ArrayList<ModelNodeBlock>> collectedTrgNodeBlocks = collectNodes(entity, refinements,
					alreadyRefinedEntityNames, false);
			entity.getTrgNodeBlocks().forEach(nb -> {
				if (collectedTrgNodeBlocks.keySet().contains(nb.getName())) {
					collectedTrgNodeBlocks.get(nb.getName()).add(nb);
				} else {
					var<ModelNodeBlock> tmp = new ArrayList<ModelNodeBlock>();
					tmp.add(nb);
					collectedTrgNodeBlocks.put(nb.getName(), tmp);
				}
			});
			// 2. step: merge nodes and edges
			var mergedTrgNodes = mergeNodes(entity, refinements, collectedTrgNodeBlocks);

			// 3. step: add merged nodeBlocks to entity
			entity.getTrgNodeBlocks().clear();
			entity.getTrgNodeBlocks().addAll((mergedTrgNodes));

			// -------------- Correspondences ---------------- //
			var corrs = new ArrayList<Correspondence>();
			corrs.addAll(entity.getCorrespondences());
			entity.getSuperRefinementTypes().forEach(
					s -> {
						EcoreUtil.resolveAll(s.getReferencedType());
						TripleRule tmpCopy = (TripleRule) EcoreUtil.copy(s.getReferencedType());
						try {
							corrs.addAll(EcoreUtil.copyAll(((TripleRule) flatten(tmpCopy, new HashSet<String>())).getCorrespondences()));
						} catch (FlattenerException e) {
							// nothing to do here because recursive errors must have already appeared
						}
					});
			entity.getCorrespondences().clear();
			entity.getCorrespondences().addAll(mergeCorrespondences(entity, corrs, mergedSrcNodes, mergedTrgNodes));

			checkForResolvedProxies(entity);
		}

		return entity;
	}

	protected Map<String, List<ModelNodeBlock>> collectNodes(SuperType entity, List<RefinementCommand> refinementList,
			Set<String> alreadyRefinedEntityNames, boolean isSrc) throws FlattenerException {
		var nodeBlocks = new HashMap<String, List<ModelNodeBlock>>();

		for (var r : refinementList) {

			if (!(r.getReferencedType() instanceof TripleRule)) {
				throw new FlattenerException(entity, FlattenerErrorType.NON_COMPLIANT_SUPER_ENTITY,
						r.getReferencedType());
			}

			// add current entity to list of names to detect infinite loop
			var alreadyRefinedEntityNamesCopy = new HashSet<String>(alreadyRefinedEntityNames);
			alreadyRefinedEntityNamesCopy.add(dispatcher.getName(entity));

			// recursively flatten superEntities
			var nodeBlocksOfSuperEntity = new ArrayList<ModelNodeBlock>();

			EcoreUtil.resolveAll(r.getReferencedType());
			TripleRule tmpCopy = (TripleRule) EcoreUtil.copy(r.getReferencedType());
			var flattenedSuperEntity = flatten(tmpCopy, alreadyRefinedEntityNamesCopy);

			// check if a superEntity possesses a condition block
			if (r.getReferencedType() instanceof TripleRule
					&& !((TripleRule) r.getReferencedType()).getNacs().isEmpty()) {
				throw new FlattenerException(entity, FlattenerErrorType.REFINE_ENTITY_WITH_CONDITION,
						r.getReferencedType());
			}

			if (flattenedSuperEntity != null) {
				EList<ModelNodeBlock> nodeBlocksOfFlattenedSuperEntity = null;

				if (isSrc)
					nodeBlocksOfFlattenedSuperEntity = ((TripleRule) flattenedSuperEntity).getSrcNodeBlocks();
				else {
					nodeBlocksOfFlattenedSuperEntity = ((TripleRule) flattenedSuperEntity).getTrgNodeBlocks();
				}

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
	protected List<ModelNodeBlock> mergeNodes(SuperType entity, List<RefinementCommand> refinementList,
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
			newNb.setAction(mergeActionOfNodes(blocksWithKey, entity));

			mergedNodes.add(newNb);
		}

		return mergeEdgesOfNodeBlocks(entity, nodeBlocks,
				mergePropertyStatementsOfNodeBlocks(entity, nodeBlocks, mergedNodes, refinementList), refinementList);
	}

	/**
	 * Merges the correspondences given in corrs and re-sets the sources and targets
	 * such that they are the ones from the merging process.
	 * 
	 * @param corrs         that have to be merged.
	 * @param srcNodeBlocks nodes of the new entity.
	 * @param trgNodeBlocks nodes of the new entity.
	 * @return List containing the merged correspondences.
	 * @throws FlattenerException if the proxy target cannot be resolved.
	 */
	private List<Correspondence> mergeCorrespondences(SuperType entity, List<Correspondence> corrs, List<ModelNodeBlock> srcNodeBlocks,
			List<ModelNodeBlock> trgNodeBlocks) throws FlattenerException {
		var mergedCorrespondences = new ArrayList<Correspondence>();

		for (var c : corrs) {
			for (var other : corrs) {
				if (isEqualCorrespondence(c, other)) {
					continue;
				} else if (!(mergedCorrespondences.contains(c))) {
					boolean alreadyIn = false;
					for (var mergedCorr : mergedCorrespondences) {
						if (isEqualCorrespondence(c, mergedCorr)) {
							alreadyIn = true;
						}
					}
					if (!alreadyIn)
						mergedCorrespondences.add(EcoreUtil2.copy(c));
				}
			}
		}
		if (!mergedCorrespondences.isEmpty()) {
			for (var c : mergedCorrespondences) {
				// set new src
				for (var n : srcNodeBlocks) {
					if (c.getSource() != null && n.getName().equals(c.getSource().getName())
							|| c.getProxySource() != null && n.getName().equals(c.getProxySource())) {
						c.setSource(n);
						break;
					}
				}
				// set new trg
				for (var n : trgNodeBlocks) {
					if (c.getTarget() != null && n.getName().equals(c.getTarget().getName())
							|| c.getProxyTarget() != null && n.getName().equals(c.getProxyTarget())) {
						c.setTarget(n);
						break;
					}
				}
				if (c.getSource() == null) {
					throw new FlattenerException(entity, FlattenerErrorType.NON_RESOLVABLE_CORR_PROXY_SOURCE, c.getProxySource(), c);
				}
				if (c.getTarget() == null) {
					throw new FlattenerException(entity, FlattenerErrorType.NON_RESOLVABLE_CORR_PROXY_TARGET, c.getProxyTarget(), c);
				}
			}
		}

		return mergedCorrespondences;
	}

	/**
	 * Compares two correspondences.
	 * 
	 * @param corr1 first correspondence in comparison.
	 * @param corr2 second correspondence in comparison.
	 * @return whether two correspondences are equal or not.
	 */
	private boolean isEqualCorrespondence(Correspondence corr1, Correspondence corr2) {
		return (corr1.getAction() == null && corr2.getAction() == null || (corr1.getAction() != null
				&& corr2.getAction() != null && corr1.getAction().getOp() == corr2.getAction().getOp()))
				&& (corr1.getSource() != null && corr2.getSource() != null && corr1.getSource().getName().equals(corr2.getSource().getName())
						|| corr1.getSource() != null && corr2.getSource() == null && corr1.getSource().getName().equals(corr2.getProxySource())
						|| corr1.getSource() == null && corr2.getSource() != null && corr1.getProxySource().equals(corr2.getSource().getName())
						|| corr1.getSource() == null && corr2.getSource() == null && corr1.getProxySource().equals(corr2.getProxySource()))
				&& (corr1.getTarget() != null && corr2.getTarget() != null && corr1.getTarget().getName().equals(corr2.getTarget().getName())
						|| corr1.getTarget() != null && corr2.getTarget() == null && corr1.getTarget().getName().equals(corr2.getProxyTarget())
						|| corr1.getTarget() == null && corr2.getTarget() != null && corr1.getProxyTarget().equals(corr2.getTarget().getName())
						|| corr1.getTarget() == null && corr2.getTarget() == null && corr1.getProxyTarget().equals(corr2.getProxyTarget()))	
				&& corr1.getType().getName().equals(corr2.getType().getName())
				&& corr1.getType().getSource() == corr2.getType().getSource()
				&& corr1.getType().getTarget() == corr2.getType().getTarget();
	}

	@Override
	protected void checkForResolvedProxies(SuperType entity) throws FlattenerException {
		var tripleRule = (TripleRule) entity;
		for (var nb : tripleRule.getSrcNodeBlocks()) {
			for (var relation : nb.getRelations()) {
				if (!(relation.getTarget() instanceof ModelNodeBlock)) {
					throw new FlattenerException(entity, FlattenerErrorType.NON_RESOLVABLE_PROXY, relation);
				}
			}
		}
		for (var nb : tripleRule.getTrgNodeBlocks()) {
			for (var relation : nb.getRelations()) {
				if (!(relation.getTarget() instanceof ModelNodeBlock)) {
					throw new FlattenerException(entity, FlattenerErrorType.NON_RESOLVABLE_PROXY, relation);
				}
			}
		}
	}
}
