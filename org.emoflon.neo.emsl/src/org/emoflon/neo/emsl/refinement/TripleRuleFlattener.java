package org.emoflon.neo.emsl.refinement;

import java.util.ArrayList;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emoflon.neo.emsl.eMSL.Correspondence;
import org.emoflon.neo.emsl.eMSL.Entity;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.RefinementCommand;
import org.emoflon.neo.emsl.eMSL.TripleRule;
import org.emoflon.neo.emsl.util.FlattenerErrorType;
import org.emoflon.neo.emsl.util.FlattenerException;

public class TripleRuleFlattener extends AbstractEntityFlattener {

	@Override
	@SuppressWarnings("unchecked")
	public <T extends Entity> T flatten(T t, Set<String> alreadyRefinedEntityNames) throws FlattenerException {
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
				return (T) entity;

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
					s -> corrs.addAll(EcoreUtil.copyAll(((TripleRule) s.getReferencedType()).getCorrespondences())));
			entity.getCorrespondences().clear();
			entity.getCorrespondences().addAll(mergeCorrespondences(corrs, mergedSrcNodes, mergedTrgNodes));

			// 4. step: merge attribute conditions
			mergeAttributeConditions(entity, refinements);

			checkForResolvedProxies(entity);
		}

		return (T) entity;
	}

	/**
	 * Merges the correspondences given in corrs and re-sets the sources and targets
	 * such that they are the ones from the merging process.
	 * 
	 * @param corrs         that have to be merged.
	 * @param srcNodeBlocks nodes of the new entity.
	 * @param trgNodeBlocks nodes of the new entity.
	 * @return List containing the merged correspondences.
	 */
	private ArrayList<Correspondence> mergeCorrespondences(ArrayList<Correspondence> corrs,
			ArrayList<ModelNodeBlock> srcNodeBlocks, ArrayList<ModelNodeBlock> trgNodeBlocks) {
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
						mergedCorrespondences.add(EcoreUtil.copy(c));
				}
			}
		}

		for (var c : mergedCorrespondences) {
			// set new src
			for (var n : srcNodeBlocks) {
				if (n.getName().equals(c.getSource().getName())) {
					c.setSource(n);
					break;
				}
			}
			// set new trg
			for (var n : trgNodeBlocks) {
				if (n.getName().equals(c.getTarget().getName())) {
					c.setTarget(n);
					break;
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
				&& corr1.getSource().getName().equals(corr2.getSource().getName())
				&& corr1.getTarget().getName().equals(corr2.getTarget().getName())
				&& corr1.getType().getName().equals(corr2.getType().getName())
				&& corr1.getType().getSource() == corr2.getType().getSource()
				&& corr1.getType().getTarget() == corr2.getType().getTarget();
	}
}
