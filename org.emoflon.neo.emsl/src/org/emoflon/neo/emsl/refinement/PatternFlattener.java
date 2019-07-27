package org.emoflon.neo.emsl.refinement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.Entity;
import org.emoflon.neo.emsl.eMSL.MetamodelRelationStatement;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatementType;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.RefinementCommand;
import org.emoflon.neo.emsl.util.FlattenerErrorType;
import org.emoflon.neo.emsl.util.FlattenerException;

public class PatternFlattener extends AbstractEntityFlattener {

	@Override
	public <T extends Entity> T flatten(T entity, Set<String> alreadyRefinedEntityNames) throws FlattenerException {
		if (entity != null) {
			@SuppressWarnings("unchecked")
			var refinements = (List<RefinementCommand>) dispatcher.getSuperRefinementTypes(entity);

			// check for loop in refinements

			if (alreadyRefinedEntityNames.contains(dispatcher.getName(entity)))
				throw new FlattenerException(entity, FlattenerErrorType.INFINITE_LOOP, alreadyRefinedEntityNames);
			// if none has been found, add current name to list

			// check if anything has to be done, if not return
			if (refinements.isEmpty())
				return entity;

			// 1. step: collect nodes with edges
			var collectedNodeBlocks = collectNodes(entity, refinements, alreadyRefinedEntityNames, true);
			dispatcher.getNodeBlocks(entity).forEach(nb -> {
				if (collectedNodeBlocks.keySet().contains(nb.getName())) {
					collectedNodeBlocks.get(nb.getName()).add(nb);
				} else {
					var tmp = new ArrayList<ModelNodeBlock>();
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
	
	@Override
	protected Map<String, List<ModelNodeBlock>> collectNodes(Entity entity, List<RefinementCommand> refinementList,
			Set<String> alreadyRefinedEntityNames, boolean isSrc) throws FlattenerException {
		var nodeBlocks = new HashMap<String, List<ModelNodeBlock>>();

		for (var r : refinementList) {

			if (!(r.getReferencedType() instanceof AtomicPattern)) {
				throw new FlattenerException(entity, FlattenerErrorType.NON_COMPLIANT_SUPER_ENTITY,
						r.getReferencedType());
			}

			// add current entity to list of names to detect infinite loop
			var alreadyRefinedEntityNamesCopy = new HashSet<String>(alreadyRefinedEntityNames);
			alreadyRefinedEntityNamesCopy.add(dispatcher.getName(entity));

			// recursively flatten superEntities
			var nodeBlocksOfSuperEntity = new ArrayList<ModelNodeBlock>();

			var flattenedSuperEntity = (flatten((Entity) r.getReferencedType().eContainer(),
						alreadyRefinedEntityNamesCopy));

			// check if a superEntity possesses a condition block
			if (r.getReferencedType() instanceof AtomicPattern
					&& ((Pattern) r.getReferencedType().eContainer()).getCondition() != null) {
				throw new FlattenerException(entity, FlattenerErrorType.REFINE_ENTITY_WITH_CONDITION,
						r.getReferencedType());
			}

			if (flattenedSuperEntity != null) {
				EList<ModelNodeBlock> nodeBlocksOfFlattenedSuperEntity = dispatcher.
						getNodeBlocks(flattenedSuperEntity);

				for (var nb : nodeBlocksOfFlattenedSuperEntity) {

					// create new NodeBlock
					var newNb = copyModelNodeBlock(nb, r);

					// add nodeBlock to list according to its name
					if (!nodeBlocks.containsKey(newNb.getName())) {
						var newList = new ArrayList<ModelNodeBlock>();
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
	protected List<ModelNodeBlock> mergeEdgesOfNodeBlocks(Entity entity, Map<String, List<ModelNodeBlock>> nodeBlocks,
			List<ModelNodeBlock> mergedNodes) throws FlattenerException {

		// collect all edges in hashmap; first key is type name, second is target name
		for (var name : nodeBlocks.keySet()) {
			var namedEdges = new HashMap<String, ArrayList<ModelRelationStatement>>();
			var edges = new HashMap<String, HashMap<String, ArrayList<ModelRelationStatement>>>();
			for (var nb : nodeBlocks.get(name)) {

				// ----------- simple edges ----------- //
				for (var rel : nb.getRelations()) {
					if (rel.getTypes() == null) {
						continue;
					}
					// collect edges that have no names -> simple edges (only one type) => merging
					// does not change
					if (rel.getName() == null) {
						if (rel.getTarget() != null) {
							if (!edges.containsKey(rel.getTypes().get(0).getType().getName())) {
								edges.put(rel.getTypes().get(0).getType().getName(),
										new HashMap<String, ArrayList<ModelRelationStatement>>());
							}
							if (!edges.get(rel.getTypes().get(0).getType().getName())
									.containsKey(rel.getTarget().getName())) {
								edges.get(rel.getTypes().get(0).getType().getName()).put(rel.getTarget().getName(),
										new ArrayList<ModelRelationStatement>());
							}
							edges.get(rel.getTypes().get(0).getType().getName()).get(rel.getTarget().getName())
									.add(rel);
						} else if (rel.getProxyTarget() != null) {
							if (!edges.containsKey(rel.getTypes().get(0).getType().getName())) {
								edges.put(rel.getTypes().get(0).getType().getName(),
										new HashMap<String, ArrayList<ModelRelationStatement>>());
							}
							if (!edges.get(rel.getTypes().get(0).getType().getName())
									.containsKey(rel.getProxyTarget())) {
								edges.get(rel.getTypes().get(0).getType().getName()).put(rel.getProxyTarget(),
										new ArrayList<ModelRelationStatement>());
							}
							edges.get(rel.getTypes().get(0).getType().getName()).get(rel.getProxyTarget()).add(rel);
						}
					} else if (rel.getName() != null) {
						if (!namedEdges.containsKey(rel.getName())) {
							namedEdges.put(rel.getName(), new ArrayList<ModelRelationStatement>());
						}
						namedEdges.get(rel.getName()).add(rel);
					}
				}
			}

			// iterate over all types and targets to create new RelationStatement that is
			// the result of the merging
			for (var typename : edges.keySet()) {
				for (var targetname : edges.get(typename).keySet()) {
					var newRel = EMSLFactory.eINSTANCE.createModelRelationStatement();

					// merge statements and check statements for compliance
					newRel.getProperties().addAll(
							collectAndMergePropertyStatementsOfRelations(edges.get(typename).get(targetname), entity));

					// check and merge action
					newRel.setAction(mergeActionOfRelations(edges.get(typename).get(targetname)));

					// create new ModelRelationStatementType for the new ModelRelationStatement
					var newRelType = EMSLFactory.eINSTANCE.createModelRelationStatementType();
					newRelType.setType((edges.get(typename).get(targetname).get(0).getTypes().get(0).getType()));
					// collect all types of the edges that are to be merged (should be one type each
					// in this case) to merge the bounds
					var typesOfEdges = new ArrayList<ModelRelationStatementType>();
					for (var e : edges.get(typename).get(targetname)) {
						typesOfEdges.addAll(e.getTypes());
					}
					var bounds = mergeModelRelationStatementPathLimits(entity, typesOfEdges);
					if (bounds != null) {
						newRelType.setLower(bounds[0].toString());
						newRelType.setUpper(bounds[1].toString());
					}
					newRel.getTypes().add(newRelType);
					mergedNodes.forEach(nb -> {
						if (nb.getName().equals(targetname)) {
							newRel.setTarget(nb);
						}
						if (nb.getName().equals(name)) {
							nb.getRelations().add(newRel);
						}
					});
				}
			}

			// ------------ edges with multiple types ------------- //

			for (var n : namedEdges.keySet()) {
				var newRel = EMSLFactory.eINSTANCE.createModelRelationStatement();
				newRel.setName(n);

				var intersection = new ArrayList<MetamodelRelationStatement>();
				namedEdges.get(n).get(0).getTypes().forEach(t -> intersection.add(t.getType()));
				for (var e : namedEdges.get(n)) {
					var typesOfOther = new ArrayList<MetamodelRelationStatement>();
					e.getTypes().forEach(t -> typesOfOther.add(t.getType()));
					intersection.retainAll(typesOfOther);
				}

				if (intersection.isEmpty())
					throw new FlattenerException(entity,
							FlattenerErrorType.NO_INTERSECTION_IN_MODEL_RELATION_STATEMENT_TYPE_LIST);

				for (var t : intersection) {
					// create new ModelRelationStatementType for each remaining type
					var newRelType = EMSLFactory.eINSTANCE.createModelRelationStatementType();
					newRelType.setType(t);
					newRel.getTypes().add(newRelType);

					var typesOfEdges = new ArrayList<ModelRelationStatementType>();
					for (var e : namedEdges.get(n)) {
						for (var tmp : e.getTypes()) {
							if (t == tmp.getType()) {
								typesOfEdges.add(tmp);
							}
						}
					}
					var bounds = mergeModelRelationStatementPathLimits(entity, typesOfEdges);
					if (bounds != null) {
						newRelType.setLower(bounds[0].toString());
						newRelType.setUpper(bounds[1].toString());
					}
				}

				// merge statements and check statements for compliance
				newRel.getProperties().addAll(collectAndMergePropertyStatementsOfRelations(namedEdges.get(n), entity));

				// check and merge action
				newRel.setAction(mergeActionOfRelations(namedEdges.get(n)));

				mergedNodes.forEach(nb -> {
					if (nb.getName().equals(namedEdges.get(n).get(0).getTarget().getName())) {
						newRel.setTarget(nb);
					}
					if (nb.getName().equals(name)) {
						nb.getRelations().add(newRel);
					}
				});
			}
		}

		return removeDuplicateEdges(mergedNodes);
	}
	
	/**
	 * This method merges the lower and upper lengths of simple paths in
	 * ModelRelationStatementTypes. The result is the maximum of the lower and the
	 * minimum of the upper limits.
	 * 
	 * @param entity that is to be flattened.
	 * @param types  whose lower and upper limits must be merged.
	 * @return Array of two values representing the new lower and upper path
	 *         lengths.
	 * @throws FlattenerException is thrown if the lower limit of the path length is
	 *                            greater than the upper limit (does not make
	 *                            sense).
	 */
	protected Object[] mergeModelRelationStatementPathLimits(Entity entity, ArrayList<ModelRelationStatementType> types)
			throws FlattenerException {
		var bounds = new Object[2];
		bounds[0] = 1;
		bounds[1] = "*";

		boolean empty = true;
		for (var t : types) {
			if (t.getLower() != null && t.getUpper() != null) {
				empty = false;
			}
		}
		if (empty)
			return null;

		for (var t : types) {
			if (t.getLower() != null && t.getUpper() != null) {
				try {
					if (Integer.parseInt(t.getLower()) > Integer.parseInt(bounds[0].toString())) {
						bounds[0] = Integer.parseInt(t.getLower());
					}
				} catch (NumberFormatException e) {
					if (t.getLower().equals("*") && !bounds[0].equals("*")) {
						bounds[0] = "*";
					}
				}
				try {
					if (Integer.parseInt(t.getUpper()) < Integer.parseInt(bounds[1].toString())) {
						bounds[1] = Integer.parseInt(t.getUpper());
					}
				} catch (NumberFormatException e) {
					if (!t.getUpper().equals("*") && bounds[1].equals("*")) {
						bounds[1] = Integer.parseInt(t.getUpper());
					}
				}
			}
			if (bounds[0] instanceof Integer) {
				if (bounds[1] instanceof Integer && (int) bounds[0] > (int) bounds[1]) {
					// lower bound is greater than upper => does not make sense => exception
					throw new FlattenerException(entity, FlattenerErrorType.PATH_LENGTHS_NONSENSE, t);
				}
			} else if (bounds[0].equals("*") && !bounds[1].equals("*")) {
				throw new FlattenerException(entity, FlattenerErrorType.PATH_LENGTHS_NONSENSE, t);
			}
		}

		return bounds;
	}
}
