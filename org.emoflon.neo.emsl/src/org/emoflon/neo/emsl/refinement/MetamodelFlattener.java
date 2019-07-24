package org.emoflon.neo.emsl.refinement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emoflon.neo.emsl.eMSL.Entity;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock;
import org.emoflon.neo.emsl.eMSL.MetamodelRefinementCommand;
import org.emoflon.neo.emsl.util.FlattenerErrorType;
import org.emoflon.neo.emsl.util.FlattenerException;

public class MetamodelFlattener extends AbstractEntityFlattener implements IEntityFlattener {

	@SuppressWarnings("unchecked")
	@Override
	public <T extends Entity> T flatten(T t, Set<String> alreadyRefinedEntityNames) throws FlattenerException {
		Metamodel entity = (Metamodel) t;
		if (entity != null) {

			var refinements = (EList<MetamodelRefinementCommand>) dispatcher.getSuperRefinementTypes(entity);

			// check for loop in refinements
			// if none has been found, add current name to list
			if (alreadyRefinedEntityNames.contains(dispatcher.getName(entity)))
				throw new FlattenerException(entity, FlattenerErrorType.INFINITE_LOOP, alreadyRefinedEntityNames);

			// check if anything has to be done, if not return
			if (refinements.isEmpty())
				return (T) entity;

			// 1. step: collect nodes with edges
			var collectedNodeBlocks = collectNodes(entity, refinements, alreadyRefinedEntityNames);
			dispatcher.getMetamodelNodeBlocks(entity).forEach(nb -> {
				if (collectedNodeBlocks.keySet().contains(nb.getName())) {
					collectedNodeBlocks.get(nb.getName()).add(nb);
				} else {
					var tmp = new ArrayList<MetamodelNodeBlock>();
					tmp.add(nb);
					collectedNodeBlocks.put(nb.getName(), tmp);
				}
			});

			// 2. step: merge nodes and edges
			var mergedNodes = mergeNodes(entity, refinements, collectedNodeBlocks);

			// 3. step: add merged nodeBlocks to entity
			dispatcher.getMetamodelNodeBlocks(entity).clear();
			dispatcher.getMetamodelNodeBlocks(entity).addAll((mergedNodes));

			// TODO: This has to be implemented for metamodels
			checkForResolvedProxies(entity);
		}

		return (T) entity;
	}

	private Map<String, List<MetamodelNodeBlock>> collectNodes(Metamodel entity,
			EList<MetamodelRefinementCommand> refinementList, Set<String> alreadyRefinedEntityNames)
			throws FlattenerException {
		var nodeBlocks = new HashMap<String, List<MetamodelNodeBlock>>();

		for (var r : refinementList) {
			checkSuperEntityTypeForCompliance(entity, r.getReferencedType());

			// add current entity to list of names
			var alreadyRefinedEntityNamesCopy = new HashSet<String>();
			alreadyRefinedEntityNames.forEach(n -> alreadyRefinedEntityNamesCopy.add(n));
			alreadyRefinedEntityNamesCopy.add(dispatcher.getName(entity));

			// recursively flatten superEntities
			var nodeBlocksOfSuperEntity = new ArrayList<MetamodelNodeBlock>();

			Metamodel flattenedSuperEntity = flatten((Metamodel) r.getReferencedType(), alreadyRefinedEntityNamesCopy);

			if (flattenedSuperEntity != null) {
				var nodeBlocksOfFlattenedSuperEntity = dispatcher.getMetamodelNodeBlocks(flattenedSuperEntity);

				for (var nb : nodeBlocksOfFlattenedSuperEntity) {
					// create new NodeBlock
					var newNb = copyMetamodelNodeBlock(nb, r);

					// add nodeBlock to list according to its name
					if (!nodeBlocks.containsKey(newNb.getName())) {
						var newList = new ArrayList<MetamodelNodeBlock>();
						newList.add(newNb);
						nodeBlocks.put(newNb.getName(), newList);
					} else {
						nodeBlocks.get(newNb.getName()).add(newNb);
					}
					nodeBlocksOfSuperEntity.add(newNb);
				}

				// re-set targets of edges
				for (var nb : nodeBlocksOfSuperEntity) {
					for (var rel : nb.getRelations()) {
						boolean targetSet = false;
						if (targetSet)
							break;
						for (var ref : r.getRelabeling()) {
							if (targetSet)
								break;
							if ((rel.getTarget() != null && ref.getOldLabel().equals(rel.getTarget().getName()))
									|| rel.getProxyTarget() != null && ref.getOldLabel().equals(rel.getProxyTarget())) {
								for (var node : nodeBlocksOfSuperEntity) {
									if (ref.getNewLabel() != null && ref.getNewLabel().equals(node.getName())) {
										rel.setTarget(node);
										targetSet = true;
										break;
									}
								}
							} else {
								for (var node : nodeBlocksOfSuperEntity) {
									if (rel.getTarget() != null && rel.getTarget().getName().equals(node.getName())
											|| (rel.getProxyTarget() != null
													&& rel.getProxyTarget().equals(node.getName()))) {
										rel.setTarget(node);
										break;
									}
								}
							}
						}
					}
				}
			}
		}

		return nodeBlocks;
	}

	private MetamodelNodeBlock copyMetamodelNodeBlock(MetamodelNodeBlock nb, MetamodelRefinementCommand refinement) {
		var newNb = EcoreUtil.copy(nb);

		// apply relabeling
		if (refinement.getRelabeling() != null) {
			for (var r : refinement.getRelabeling()) {
				if (r.getOldLabel() != null && nb.getName().equals(r.getOldLabel())) {
					newNb.setName(r.getNewLabel());
					break;
				}
			}
		}

		// add relations to new nodeblock
		for (var rel : nb.getRelations()) {
			var newRel = EcoreUtil.copy(rel);
			// apply relabeling
			for (var relabeling : refinement.getRelabeling()) {
				if (relabeling.getOldLabel().equals(rel.getName()))
					newRel.setName(relabeling.getNewLabel());
			}
			newNb.getRelations().add(newRel);
		}

		// add properties to new nodeblock
		for (var prop : nb.getProperties()) {
			newNb.getProperties().add(EcoreUtil.copy(prop));
		}

		return newNb;
	}

	private List<MetamodelNodeBlock> mergeNodes(Metamodel entity, List<MetamodelRefinementCommand> refinements,
			Map<String, List<MetamodelNodeBlock>> collectedNodeBlocks) {
		// TODO Auto-generated method stub
		return null;
	}
}
