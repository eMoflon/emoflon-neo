package org.emoflon.neo.emsl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emoflon.neo.emsl.eMSL.ActionOperator;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.AttributeCondition;
import org.emoflon.neo.emsl.eMSL.AttributeExpression;
import org.emoflon.neo.emsl.eMSL.Correspondence;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.Entity;
import org.emoflon.neo.emsl.eMSL.EnumValue;
import org.emoflon.neo.emsl.eMSL.LinkAttributeExpTarget;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock;
import org.emoflon.neo.emsl.eMSL.MetamodelRelationStatement;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatementType;
import org.emoflon.neo.emsl.eMSL.NodeAttributeExpTarget;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.PrimitiveBoolean;
import org.emoflon.neo.emsl.eMSL.PrimitiveInt;
import org.emoflon.neo.emsl.eMSL.PrimitiveString;
import org.emoflon.neo.emsl.eMSL.RefinementCommand;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.emsl.eMSL.SuperType;
import org.emoflon.neo.emsl.eMSL.TripleRule;
import org.emoflon.neo.emsl.eMSL.Value;
import org.emoflon.neo.emsl.util.EntityAttributeDispatcher;
import org.emoflon.neo.emsl.util.EntityCloner;
import org.emoflon.neo.emsl.util.FlattenerErrorType;
import org.emoflon.neo.emsl.util.FlattenerException;

public class EMSLFlattener {

	EntityAttributeDispatcher dispatcher;
	
	public EMSLFlattener() {
		dispatcher = new EntityAttributeDispatcher();
	}
	
	/**
	 * Returns the flattened Entity.
	 * 
	 * @param entity 					that should be flattened.
	 * @param alreadyRefinedEntityNames list of names of entities that have already 
	 * 									appeared in the refinement path (against
	 * 									loops).
	 * @return 							the flattened entity.
	 * @throws 							FlattenerException is thrown if the entity could not be flattened.
	 */
	public Entity flattenEntity(Entity entity, ArrayList<String> alreadyRefinedEntityNames) throws FlattenerException {
		if (entity != null) {
			@SuppressWarnings("unchecked")
			EList<RefinementCommand> refinements = (EList<RefinementCommand>) dispatcher
					.getSuperRefinementTypes(entity);

			// check for loop in refinements

			if (alreadyRefinedEntityNames.contains(dispatcher.getName(entity)))
				throw new FlattenerException(entity, FlattenerErrorType.INFINITE_LOOP, alreadyRefinedEntityNames);
			// if none has been found, add current name to list

			// check if anything has to be done, if not return
			if (refinements.isEmpty())
				return entity;
			if (!(entity instanceof TripleRule)) {
				// 1. step: collect nodes with edges
				var<String, ArrayList<ModelNodeBlock>> collectedNodeBlocks = collectNodes(entity, refinements, alreadyRefinedEntityNames, true);
				dispatcher.getNodeBlocks(entity).forEach(nb -> {
					if (collectedNodeBlocks.keySet().contains(nb.getName())) {
						collectedNodeBlocks.get(nb.getName()).add(nb);
					}
					else {
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
			} else if (entity instanceof TripleRule) {
			// --------------- Source ------------------ //
				// 1. step: collect nodes with edges
				var<String, ArrayList<ModelNodeBlock>> collectedSrcNodeBlocks = collectNodes(entity, refinements, alreadyRefinedEntityNames, true);
				((TripleRule) entity).getSrcNodeBlocks().forEach(nb -> {
					if (collectedSrcNodeBlocks.keySet().contains(nb.getName())) {
						collectedSrcNodeBlocks.get(nb.getName()).add(nb);
					}
					else {
						var<ModelNodeBlock> tmp = new ArrayList<ModelNodeBlock>();
						tmp.add(nb);
						collectedSrcNodeBlocks.put(nb.getName(), tmp);
					}
				});
				// 2. step: merge nodes and edges
				var mergedSrcNodes = mergeNodes(entity, refinements, collectedSrcNodeBlocks);
				
				// 3. step: add merged nodeBlocks to entity
				((TripleRule) entity).getSrcNodeBlocks().clear();
				((TripleRule) entity).getSrcNodeBlocks().addAll((mergedSrcNodes));
				
			// --------------- Target ------------------ //
				// 1. step: collect nodes with edges
				var<String, ArrayList<ModelNodeBlock>> collectedTrgNodeBlocks = collectNodes(entity, refinements, alreadyRefinedEntityNames, false);
				((TripleRule) entity).getTrgNodeBlocks().forEach(nb -> {
					if (collectedTrgNodeBlocks.keySet().contains(nb.getName())) {
						collectedTrgNodeBlocks.get(nb.getName()).add(nb);
					}
					else {
						var<ModelNodeBlock> tmp = new ArrayList<ModelNodeBlock>();
						tmp.add(nb);
						collectedTrgNodeBlocks.put(nb.getName(), tmp);
					}
				});
				// 2. step: merge nodes and edges
				var mergedTrgNodes = mergeNodes(entity, refinements, collectedTrgNodeBlocks);
				
				// 3. step: add merged nodeBlocks to entity
				((TripleRule) entity).getTrgNodeBlocks().clear();
				((TripleRule) entity).getTrgNodeBlocks().addAll((mergedTrgNodes));
				
			// -------------- Correspondences ---------------- //
				var corrs = new ArrayList<Correspondence>();
				corrs.addAll(((TripleRule) entity).getCorrespondences());
				((TripleRule) entity).getSuperRefinementTypes()
						.forEach(s -> corrs.addAll(EcoreUtil.copyAll(((TripleRule) s.getReferencedType()).getCorrespondences())));
				((TripleRule) entity).getCorrespondences().clear();
				((TripleRule) entity).getCorrespondences().addAll(mergeCorrespondences(corrs, mergedSrcNodes, mergedTrgNodes));
			}
			// 4. step: merge attribute conditions in rules/patterns(/tripleRules)
			var collectedAttributeConditions = new ArrayList<AttributeCondition>();
			collectedAttributeConditions.addAll(dispatcher.getAttributeConditions(entity));
			for (var s : refinements) {
				if (((RefinementCommand) s).getReferencedType() instanceof AtomicPattern) {
					((AtomicPattern) ((RefinementCommand) s).getReferencedType()).getAttributeConditions()
							.forEach(c -> collectedAttributeConditions.add(EcoreUtil.copy(c)));
				} else {
					collectedAttributeConditions
							.addAll(dispatcher.getAttributeConditions((Entity) ((RefinementCommand) s).getReferencedType()));
				}
			}
			var mergedAttributeConditions = mergeAttributeConditions(collectedAttributeConditions);
			dispatcher.getAttributeConditions(entity).clear();
			mergedAttributeConditions.forEach(c -> dispatcher.getAttributeConditions(entity).add(EcoreUtil.copy(c)));
			
			if (entity instanceof Pattern) {
				var atomicPattern = ((Pattern) entity).getBody();
				((Pattern) entity).setBody(atomicPattern);
			}
			
			checkForResolvedProxies(entity);
		}
		
		return entity;
	}
	
	
	/**
	 * Returns a flattened copy of the given Entity.
	 * 
	 * @param originalEntity 			that is to be copied and flattened.
	 * @param alreadyRefinedEntityNames list of names that of entities that have
	 * 									already appeared in the refinement path
	 * 									(against loops).
	 * @return 							flattened copy of given Entity.
	 * @throws FlattenerException 		is thrown if the entity could not be flattened.
	 */
	public Entity flattenCopyOfEntity(Entity originalEntity, ArrayList<String> alreadyRefinedEntityNames) throws FlattenerException {
		var entity = (Entity) new EntityCloner().cloneEntity(originalEntity);
		if (entity != null) {			
			@SuppressWarnings("unchecked")
			EList<RefinementCommand> refinements = (EList<RefinementCommand>) dispatcher
					.getSuperRefinementTypes(entity);
			
			// check for loop in refinements
	
			if (alreadyRefinedEntityNames.contains(dispatcher.getName(entity))) 
				throw new FlattenerException(entity, FlattenerErrorType.INFINITE_LOOP, alreadyRefinedEntityNames);
			// if none has been found, add current name to list
			
			
			// check if anything has to be done, if not return
			if (refinements.isEmpty())
				return entity;
			if (!(entity instanceof TripleRule)) {
				// 1. step: collect nodes with edges
				var<String, ArrayList<ModelNodeBlock>> collectedNodeBlocks = collectNodes(entity, refinements, alreadyRefinedEntityNames, true);
				dispatcher.getNodeBlocks(entity).forEach(nb -> {
					if (collectedNodeBlocks.keySet().contains(nb.getName())) {
						collectedNodeBlocks.get(nb.getName()).add(nb);
					}
					else {
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
			} else if (entity instanceof TripleRule) {
			// --------------- Source ------------------ //
				// 1. step: collect nodes with edges
				var<String, ArrayList<ModelNodeBlock>> collectedSrcNodeBlocks = collectNodes(entity, refinements, alreadyRefinedEntityNames, true);
				((TripleRule) entity).getSrcNodeBlocks().forEach(nb -> {
					if (collectedSrcNodeBlocks.keySet().contains(nb.getName())) {
						collectedSrcNodeBlocks.get(nb.getName()).add(nb);
					}
					else {
						var<ModelNodeBlock> tmp = new ArrayList<ModelNodeBlock>();
						tmp.add(nb);
						collectedSrcNodeBlocks.put(nb.getName(), tmp);
					}
				});
				// 2. step: merge nodes and edges
				var mergedSrcNodes = mergeNodes(entity, refinements, collectedSrcNodeBlocks);
				
				// 3. step: add merged nodeBlocks to entity
				((TripleRule) entity).getSrcNodeBlocks().clear();
				((TripleRule) entity).getSrcNodeBlocks().addAll((mergedSrcNodes));
				
			// --------------- Target ------------------ //
				// 1. step: collect nodes with edges
				var<String, ArrayList<ModelNodeBlock>> collectedTrgNodeBlocks = collectNodes(entity, refinements, alreadyRefinedEntityNames, false);
				((TripleRule) entity).getTrgNodeBlocks().forEach(nb -> {
					if (collectedTrgNodeBlocks.keySet().contains(nb.getName())) {
						collectedTrgNodeBlocks.get(nb.getName()).add(nb);
					}
					else {
						var<ModelNodeBlock> tmp = new ArrayList<ModelNodeBlock>();
						tmp.add(nb);
						collectedTrgNodeBlocks.put(nb.getName(), tmp);
					}
				});
				// 2. step: merge nodes and edges
				var mergedTrgNodes = mergeNodes(entity, refinements, collectedTrgNodeBlocks);
				
				// 3. step: add merged nodeBlocks to entity
				((TripleRule) entity).getTrgNodeBlocks().clear();
				((TripleRule) entity).getTrgNodeBlocks().addAll((mergedTrgNodes));
				
			// -------------- Correspondences ---------------- //
				var corrs = new ArrayList<Correspondence>();
				corrs.addAll(((TripleRule) entity).getCorrespondences());
				((TripleRule) entity).getSuperRefinementTypes().forEach(s -> corrs.addAll(EcoreUtil.copyAll(((TripleRule) s.getReferencedType()).getCorrespondences())));
				((TripleRule) entity).getCorrespondences().clear();
				((TripleRule) entity).getCorrespondences().addAll(mergeCorrespondences(corrs, mergedSrcNodes, mergedTrgNodes));
			}
			// 4. step: merge attribute conditions in rules/patterns(/tripleRules)
			var collectedAttributeConditions = new ArrayList<AttributeCondition>();
			collectedAttributeConditions.addAll(dispatcher.getAttributeConditions(entity));
			for (var s : refinements) {
				if (((RefinementCommand) s).getReferencedType() instanceof AtomicPattern) {
					((AtomicPattern) ((RefinementCommand) s).getReferencedType()).getAttributeConditions().forEach(c -> collectedAttributeConditions.add(EcoreUtil.copy(c)));
				} else {
					collectedAttributeConditions.addAll(dispatcher.getAttributeConditions((Entity) ((RefinementCommand) s).getReferencedType()));
				}
			}
			var mergedAttributeConditions = mergeAttributeConditions(collectedAttributeConditions);
			dispatcher.getAttributeConditions(entity).clear();
			mergedAttributeConditions.forEach(c -> dispatcher.getAttributeConditions(entity).add(EcoreUtil.copy(c)));
			
			if (entity instanceof Pattern) {
				var atomicPattern = ((Pattern) entity).getBody();
				((Pattern) entity).setBody(atomicPattern);
			}
			
			checkForResolvedProxies(entity);
		}
		
		return entity;
	}
	
	
	/**
	 * Merges the correspondences given in corrs and
	 * re-sets the sources and targets such that they 
	 * are the ones from the merging process.
	 * @param corrs 		that have to be merged.
	 * @param srcNodeBlocks nodes of the new entity.
	 * @param trgNodeBlocks nodes of the new entity.
	 * @return List containing the merged correspondences.
	 */
	private ArrayList<Correspondence> mergeCorrespondences(ArrayList<Correspondence> corrs, ArrayList<ModelNodeBlock> srcNodeBlocks, ArrayList<ModelNodeBlock> trgNodeBlocks) {
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
	 * This method creates all NodeBlocks that have to be imported into the Entity from the SuperEntities.
	 * @param refinementList 			List of RefinementCommands holding all entities that should be refined.
	 * @param alreadyRefinedEntityNames List of entity names that have already appeared in the refinement path (against loops).
	 * @return HashMap of NodeBlocks mapped to their name that have to be added to the refining Entity.
	 * @throws FlattenerException is thrown if an error occurs during collecting the nodes, like an infinite loop is detected
	 */
	private HashMap<String, ArrayList<ModelNodeBlock>> collectNodes(Entity entity, EList<RefinementCommand> refinementList, ArrayList<String> alreadyRefinedEntityNames, boolean isSrc) throws FlattenerException {
		var<String, ArrayList<ModelNodeBlock>> nodeBlocks = new HashMap<String, ArrayList<ModelNodeBlock>>();
		
		for (var r : refinementList) {
			
			if (entity.eClass() != r.getReferencedType().eClass()) {
				if (!(entity instanceof Pattern && r.getReferencedType() instanceof AtomicPattern)) {
					throw new FlattenerException(entity, FlattenerErrorType.NON_COMPLIANT_SUPER_ENTITY, r.getReferencedType());
				}
			}
			checkSuperEntityTypeForCompliance(entity, r.getReferencedType()); 
			
			// add current entity to list of names
			var alreadyRefinedEntityNamesCopy = new ArrayList<String>();
			alreadyRefinedEntityNames.forEach(n -> alreadyRefinedEntityNamesCopy.add(n));
			alreadyRefinedEntityNamesCopy.add((String) dispatcher.getName(entity));
			
			// recursively flatten superEntities
			if (!(r.getReferencedType() instanceof AtomicPattern) || (r.getReferencedType() instanceof AtomicPattern && r.getReferencedType().eContainer() != null)) {
				var nodeBlocksOfSuperEntity = new ArrayList<ModelNodeBlock>();
				
				Entity flattenedSuperEntity;
				if (r.getReferencedType() instanceof AtomicPattern) {
					flattenedSuperEntity = (flattenEntity((Entity) r.getReferencedType().eContainer(), alreadyRefinedEntityNamesCopy));
				} else {
					flattenedSuperEntity = (flattenEntity((Entity) r.getReferencedType(), alreadyRefinedEntityNamesCopy));
				}
				
				// check if a superEntity possesses a condition block
				if (r.getReferencedType() instanceof AtomicPattern && ((Pattern) r.getReferencedType().eContainer()).getCondition() != null
						|| r.getReferencedType() instanceof Rule && ((Rule) r.getReferencedType()).getCondition() != null
						|| r.getReferencedType() instanceof TripleRule && !((TripleRule) r.getReferencedType()).getNacs().isEmpty()) {
					throw new FlattenerException(entity, FlattenerErrorType.REFINE_ENTITY_WITH_CONDITION, r.getReferencedType());
				}
				
				if (flattenedSuperEntity != null) {
					var nodeBlocksOfFlattenedSuperEntity = dispatcher.getNodeBlocks((Entity) flattenedSuperEntity);
					
					if (entity instanceof TripleRule && isSrc)
						nodeBlocksOfFlattenedSuperEntity = ((TripleRule) flattenedSuperEntity).getSrcNodeBlocks();
					else if (entity instanceof TripleRule && !isSrc) {
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
						}
						else {
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
												|| (rel.getProxyTarget() != null && rel.getProxyTarget().equals(node.getName()))) {
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
		}
		return nodeBlocks;
	}
	
	/**
	 * This method takes a HashMap of lists of NodeBlocks mapped to the names of the contained NodeBlocks.
	 * All of those NodeBlocks with the same name (mapping) are then merged into one that is added to the model. During
	 * the merging the least common subtype of all NodeBlocks with the same name is searched for. All of the edges with the same
	 * target and same type are also merged.
	 * 
	 * @param nodeBlocks HashMap of Lists of ModelNodeBlocks that are mapped to the names of the NodeBlocks contained in such a list.
	 * @return ArrayList of ModelNodeBlocks that only contains the ModelNodeBlocks that were created during merging.
	 * @throws FlattenerException is thrown if something went wrong during the merging process.
	 */
	private ArrayList<ModelNodeBlock> mergeNodes(Entity entity, EList<RefinementCommand> refinementList, HashMap<String, ArrayList<ModelNodeBlock>> nodeBlocks) throws FlattenerException {
		var<ModelNodeBlock> mergedNodes = new ArrayList<ModelNodeBlock>();
		
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
				
				private boolean recursiveContainment(MetamodelNodeBlock o1, MetamodelNodeBlock o2, boolean containment) {
					var wrapper = new Object() { boolean contains = false; };
					
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
			newNb.setAction(EMSLFactory.eINSTANCE.createAction());
			
			// check and merge action
			boolean green = false;
			boolean red = false;
			boolean black = false;
			for (var nb : blocksWithKey) {
				if (nb.getAction() != null && nb.getAction().getOp() == ActionOperator.CREATE)
					green = true;
				else if (nb.getAction() != null && nb.getAction().getOp() == ActionOperator.DELETE)
					red = true;
				else
					black = true;
			}
			if (green && !red && !black)
				newNb.getAction().setOp(ActionOperator.CREATE);
			else if (!green && red && !black)
				newNb.getAction().setOp(ActionOperator.DELETE);
			else
				newNb.setAction(null);
			
			
			mergedNodes.add(newNb);
		}
		
		return mergeEdgesOfNodeBlocks(entity, nodeBlocks, mergePropertyStatementsOfNodeBlocks(entity, nodeBlocks, mergedNodes));
	}
	
	/**
	 * This method takes a list of collected NodeBlocks that were collected from all the refinements, and a list of merged nodes
	 * and adds the merged RelationStatements to the mergedNodes which are then returned.
	 * @param nodeBlocks 	that were collected from the refinements.
	 * @param mergedNodes 	nodeBlocks that were except for the relationStatements already merged.
	 * @return list of ModelNodeBlocks that now have merged RelationStatements.
	 * @throws FlattenerException is thrown if something went wrong during the merging process.
	 */
	private ArrayList<ModelNodeBlock> mergeEdgesOfNodeBlocks(Entity entity, HashMap<String, ArrayList<ModelNodeBlock>> nodeBlocks, ArrayList<ModelNodeBlock> mergedNodes) throws FlattenerException {
		
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
					// collect edges that have no names -> simple edges (only one type) => merging does not change
					if (rel.getName() == null) {
						if (rel.getTarget() != null) {
							if (!edges.containsKey(rel.getTypes().get(0).getType().getName())) {
								edges.put(rel.getTypes().get(0).getType().getName(), new HashMap<String, ArrayList<ModelRelationStatement>>());
							}
							if (!edges.get(rel.getTypes().get(0).getType().getName()).containsKey(rel.getTarget().getName())) {
								edges.get(rel.getTypes().get(0).getType().getName()).put(rel.getTarget().getName(), new ArrayList<ModelRelationStatement>());
							}
							edges.get(rel.getTypes().get(0).getType().getName()).get(rel.getTarget().getName()).add(rel);
						} else if (rel.getProxyTarget() != null) {
							if (!edges.containsKey(rel.getTypes().get(0).getType().getName())) {
								edges.put(rel.getTypes().get(0).getType().getName(), new HashMap<String, ArrayList<ModelRelationStatement>>());
							}
							if (!edges.get(rel.getTypes().get(0).getType().getName()).containsKey(rel.getProxyTarget())) {
								edges.get(rel.getTypes().get(0).getType().getName()).put(rel.getProxyTarget(), new ArrayList<ModelRelationStatement>());
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
			
			// iterate over all types and targets to create new RelationStatement that is the result of the merging
			for (var typename : edges.keySet()) {
				for (var targetname : edges.get(typename).keySet()) {
					var newRel = EMSLFactory.eINSTANCE.createModelRelationStatement();
					
					// merge PropertyStatements of Edges
					var properties = new HashMap<String, ArrayList<ModelPropertyStatement>>();
					for (var e : edges.get(typename).get(targetname)) {
						// collect propertyStatements
						e.getProperties().forEach(p -> {
							if (!properties.containsKey(p.getType().getName())) {
								properties.put(p.getType().getName(), new ArrayList<ModelPropertyStatement>());
							}
							properties.get(p.getType().getName()).add(p);
						});
					}
					// merge statements	and check statements for compliance
					for (var propertyName : properties.keySet()) {
						var props = properties.get(propertyName);
						ModelPropertyStatement basis = null;
						if (properties.size() > 0) {
							basis = props.get(0);
						}
						for (var p : props) {
							if (p.getType().getType() != basis.getType().getType()) {
								// incompatible types/operands found
								if (p.eContainer().eContainer().eContainer() instanceof AtomicPattern) {
									throw new FlattenerException(entity, FlattenerErrorType.NO_COMMON_SUBTYPE_OF_PROPERTIES, basis, p, (SuperType) p.eContainer().eContainer().eContainer());
								} else {
									throw new FlattenerException(entity, FlattenerErrorType.NO_COMMON_SUBTYPE_OF_PROPERTIES, basis, p, (SuperType) p.eContainer().eContainer().eContainer());
								}
							} else if (basis.getOp() != p.getOp()) {
								if (p.eContainer().eContainer().eContainer() instanceof AtomicPattern) {
									throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_OPERATORS, basis, p, (SuperType) p.eContainer().eContainer().eContainer());
								} else {
									throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_OPERATORS, basis, p, (SuperType) p.eContainer().eContainer().eContainer());
								}
							}
							compareValueOfModelPropertyStatement(entity, basis, p);
						}
						newRel.getProperties().add(copyModelPropertyStatement(basis));
					}
					
					// check and merge action
					newRel.setAction(EMSLFactory.eINSTANCE.createAction());
					boolean green = false;
					boolean red = false;
					boolean black = false;
					for (var e : edges.get(typename).get(targetname)) {
						if (e.getAction() != null && e.getAction().getOp() == ActionOperator.CREATE)
							green = true;
						else if (e.getAction() != null && e.getAction().getOp() == ActionOperator.DELETE)
							red = true;
						else
							black = true;
					}
					if (green && !red && !black)
						newRel.getAction().setOp(ActionOperator.CREATE);
					else if (!green && red && !black)
						newRel.getAction().setOp(ActionOperator.DELETE);
					else
						newRel.setAction(null);
					
					// create new ModelRelationStatementType for the new ModelRelationStatement
					var newRelType = EMSLFactory.eINSTANCE.createModelRelationStatementType();
					newRelType.setType((edges.get(typename).get(targetname).get(0).getTypes().get(0).getType()));
					// collect all types of the edges that are to be merged (should be one type each in this case) to merge the bounds
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
					throw new FlattenerException(entity, FlattenerErrorType.NO_INTERSECTION_IN_MODEL_RELATION_STATEMENT_TYPE_LIST);
				
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
				
				// merge PropertyStatements of Edges
				var properties = new HashMap<String, ArrayList<ModelPropertyStatement>>();
				for (var e : namedEdges.get(n)) {
					// collect propertyStatements
					e.getProperties().forEach(p -> {
						if (!properties.containsKey(p.getType().getName())) {
							properties.put(p.getType().getName(), new ArrayList<ModelPropertyStatement>());
						}
						properties.get(p.getType().getName()).add(p);
					});
				}
				
				// merge statements	and check statements for compliance
				for (var propertyName : properties.keySet()) {
					var props = properties.get(propertyName);
					ModelPropertyStatement basis = null;
					if (properties.size() > 0) {
						basis = props.get(0);
					}
					for (var p : props) {
						if (p.getType().getType() != basis.getType().getType()) {
							// incompatible types/operands found
							if (p.eContainer().eContainer().eContainer() instanceof AtomicPattern) {
								throw new FlattenerException(entity, FlattenerErrorType.NO_COMMON_SUBTYPE_OF_PROPERTIES, basis, p, (SuperType) p.eContainer().eContainer().eContainer());
							} else {
								throw new FlattenerException(entity, FlattenerErrorType.NO_COMMON_SUBTYPE_OF_PROPERTIES, basis, p, (SuperType) p.eContainer().eContainer().eContainer());
							}
						} else if (basis.getOp() != p.getOp()) {
							if (p.eContainer().eContainer().eContainer() instanceof AtomicPattern) {
								throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_OPERATORS, basis, p, (SuperType) p.eContainer().eContainer().eContainer());
							} else {
								throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_OPERATORS, basis, p, (SuperType) p.eContainer().eContainer().eContainer());
							}
						}
						compareValueOfModelPropertyStatement(entity, basis, p);
					}
					newRel.getProperties().add(copyModelPropertyStatement(basis));
				}
				
				// check and merge action
				newRel.setAction(EMSLFactory.eINSTANCE.createAction());
				boolean green = false;
				boolean red = false;
				boolean black = false;
				for (var e : namedEdges.get(n)) {
					if (e.getAction() != null && e.getAction().getOp() == ActionOperator.CREATE)
						green = true;
					else if (e.getAction() != null && e.getAction().getOp() == ActionOperator.DELETE)
						red = true;
					else
						black = true;
				}
				if (green && !red && !black)
					newRel.getAction().setOp(ActionOperator.CREATE);
				else if (!green && red && !black)
					newRel.getAction().setOp(ActionOperator.DELETE);
				else
					newRel.setAction(null);
				
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
		
		// ----------------- remove double edges ----------------- //
		
		for (var nb : mergedNodes) {
			var duplicates = new ArrayList<ModelRelationStatement>();
			for (var relation : nb.getRelations()) {
				if (relation.getTypes().size() > 1)
					continue;
				for (var other : nb.getRelations()) {
					if (other.getTypes().size() > 1 || relation == other)
						continue;
					if (relation.getTypes().get(0).getType() == other.getTypes().get(0).getType()	
							&& relation.getTarget() == other.getTarget()
							&& relation.getTypes().get(0).getLower() == other.getTypes().get(0).getLower()
							&& relation.getTypes().get(0).getUpper() == other.getTypes().get(0).getUpper()) {
						if (!duplicates.contains(other)) {
							duplicates.add(other);
						}
					}
				}
			}
			nb.getRelations().stream().filter(r -> duplicates.contains(r));
			for (var relation : nb.getRelations()) {
				if (relation.getTypes().size() == 1 && relation.getName() != null)
					relation.setName(null);
			}
		}
		
		return mergedNodes;
	}
	
	/**
	 * This method merges the lower and upper lengths of simple paths in ModelRelationStatementTypes.
	 * The result is the maximum of the lower and the minimum of the upper limits.
	 * @param entity that is to be flattened.
	 * @param types whose lower and upper limits must be merged.
	 * @return Array of two values representing the new lower and upper path lengths.
	 * @throws FlattenerException is thrown if the lower limit of the path length is greater than the upper limit (does not make sense).
	 */
	private Object[] mergeModelRelationStatementPathLimits(Entity entity, ArrayList<ModelRelationStatementType> types) throws FlattenerException {
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
	
	/**
	 * This method merges the ModelPropertyStatements of NodeBlocks. Throws an error if the operator, value or type
	 * of the statements that are to merged are not equal.
	 * @param nodeBlocks that were collected and merged into the new NodeBlocks.
	 * @param mergedNodes result of the mergeNodes function. These nodeBlocks get the PropertyStatements.
	 * @return list of mergedNodeBlocks with the new and merged ModelPropertyStatements.
	 * @throws FlattenerException is thrown if something went wrong during the merging process.
	 */
	private ArrayList<ModelNodeBlock> mergePropertyStatementsOfNodeBlocks(Entity entity, HashMap<String, ArrayList<ModelNodeBlock>> nodeBlocks, ArrayList<ModelNodeBlock> mergedNodes) throws FlattenerException {
		for (var name : nodeBlocks.keySet()) {
			var nodeBlocksWithKey = nodeBlocks.get(name);
			var newProperties = new ArrayList<ModelPropertyStatement>();
			
			// collect ModelPropertyStatements with same name
			var propertyStatementsSortedByName = new HashMap<String, ArrayList<ModelPropertyStatement>>();
			for (var nb : nodeBlocksWithKey) {
				for (var p : nb.getProperties()) {
					if (p.getType() == null) {
						continue;
					}
					if (!propertyStatementsSortedByName.containsKey(p.getType().getName())) {
						propertyStatementsSortedByName.put(p.getType().getName(), new ArrayList<ModelPropertyStatement>());
					}
					propertyStatementsSortedByName.get(p.getType().getName()).add(p);
				}
			}
			
			// check statements for compliance
			for (var propertyName : propertyStatementsSortedByName.keySet()) {
				var properties = propertyStatementsSortedByName.get(propertyName);
				ModelPropertyStatement basis = null;
				if (properties.size() > 0) {
					basis = properties.get(0);
				}
				for (var p : properties) {
					if (p.getType().getType() != basis.getType().getType())	{
						if (p.eContainer().eContainer() instanceof AtomicPattern) {
							throw new FlattenerException(entity, FlattenerErrorType.NO_COMMON_SUBTYPE_OF_PROPERTIES, basis, p, (SuperType) p.eContainer().eContainer());
						} else {
							throw new FlattenerException(entity, FlattenerErrorType.NO_COMMON_SUBTYPE_OF_PROPERTIES, basis, p, (SuperType) p.eContainer().eContainer()); // incompatible types found
						}
					} else if (basis.getOp() != p.getOp()) {
						if (p.eContainer().eContainer() instanceof AtomicPattern) {
							throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_OPERATORS, basis, p, (SuperType) p.eContainer().eContainer());
						} else {
							throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_OPERATORS, basis, p, (SuperType) p.eContainer().eContainer()); // incompatible operators found
						}
					}
					compareValueOfModelPropertyStatement(entity, basis, p);
				}
				newProperties.add(copyModelPropertyStatement(basis));
			}
			
			// add merged properties to the new nodeblock
			mergedNodes.forEach(nb -> {
				if (nb.getName().equals(name)) {
					nb.getProperties().addAll(newProperties);
				}
			});
		}
		
		return mergedNodes;
	}
	
	/**
	 * This method merges the attribute conditions given as a list. It searches for duplicates in all collected conditions and removes them.
	 * @param conditionList list of conditions that are to be merged.
	 * @return list of merged attribute conditions.
	 */
	private ArrayList<AttributeCondition> mergeAttributeConditions(ArrayList<AttributeCondition> conditionList) {
		var mergedConditions = new ArrayList<AttributeCondition>();
		
		for (var c : conditionList) {
			boolean alreadyIn = false;
			for (var other : mergedConditions) {
				if (c == other)
					continue;
				if (c.getOperator() == other.getOperator()) {
					int numberOfIdenticalBindings = 0;
					for (var b : c.getBindings()) {
						for (var otherB : other.getBindings()) {
							if (b.getName().equals(otherB.getName()) && equalValues(b.getValue(), otherB.getValue()) 
									&& (b.isPre() && otherB.isPre() || !b.isPre() && !otherB.isPre())
									&& (b.isPost() && otherB.isPost() || !b.isPost() && !otherB.isPost())) {
								numberOfIdenticalBindings++;
							}
						}
					}
					
					if (numberOfIdenticalBindings == c.getBindings().size()) {
						alreadyIn = true;
					}
				}
				
			}
			if (!alreadyIn) {
				mergedConditions.add(c);
			}
		}
		return mergedConditions;
	}
	
	/**
	 * Compares the two given Values and returns whether they are equal or not.
	 * @param val1 first value to be compared.
	 * @param val2 second value to be compared.
	 * @return true if val1 and val2 are equal, else false.
	 */
	@SuppressWarnings("unlikely-arg-type")
	private boolean equalValues(Value val1, Value val2) {
		if (val1.eClass() != val2.eClass())
			return false;
		else if (val1 instanceof AttributeExpression && val2 instanceof AttributeExpression
				&& ((AttributeExpression) val1).getNode().getName().equals(((AttributeExpression) val2).getNode().getName())) {
			if (((AttributeExpression) val1).getTarget() instanceof LinkAttributeExpTarget && ((AttributeExpression) val2).getTarget() instanceof LinkAttributeExpTarget) {
				if (((LinkAttributeExpTarget) ((AttributeExpression) val1).getTarget()).getLink().equals(((LinkAttributeExpTarget) ((AttributeExpression) val2).getTarget()).getLink())
						&& ((LinkAttributeExpTarget) ((AttributeExpression) val1).getTarget()).getAttribute() == ((LinkAttributeExpTarget) ((AttributeExpression) val2).getTarget()).getAttribute()) {
					return true;
				} 
			} else if (((AttributeExpression) val1).getTarget() instanceof NodeAttributeExpTarget && ((AttributeExpression) val2).getTarget() instanceof NodeAttributeExpTarget
					&& ((NodeAttributeExpTarget) ((AttributeExpression) val1).getTarget()).getAttribute() == ((NodeAttributeExpTarget) ((AttributeExpression) val2).getTarget()).getAttribute()) {
				return true;
			}
		} else if (val1 instanceof EnumValue && val2 instanceof EnumValue
				&& ((EnumValue) val1).getLiteral().equals(((EnumValue) val2).getLiteral())) {
			return true;
		} else if (val1 instanceof PrimitiveInt && val2 instanceof PrimitiveInt
				&& ((PrimitiveInt) val1).getLiteral() == ((PrimitiveInt) val2).getLiteral()) {
			return true;
		} else if (val1 instanceof PrimitiveBoolean && val2 instanceof PrimitiveBoolean
				&& ( ((PrimitiveBoolean) val1).isTrue() && ((PrimitiveBoolean) val2).isTrue() || !((PrimitiveBoolean) val1).isTrue() && !((PrimitiveBoolean) val2).isTrue())) {
			return true;
		} else if (val1 instanceof PrimitiveString && val2 instanceof PrimitiveString
				&& ((PrimitiveString) val1).getLiteral().equals(((PrimitiveString) val2).getLiteral())) {
			return true;
		} else {
			if (val1.equals('_') && val2.equals('_')) {
				return true;
			} else if (val1.getName() != null && val2.getName() != null && val1.getName().equals(val2.getName())) {
				return true;
			}
		}
		
		return false;
	}
	
	
	/**
	 * This method creates a new NodeBlock from the given NodeBlock that was referenced in the RefinementStatement. It also applies
	 * the relabeling of the input.
	 * @param nb Referenced NodeBlock that will be created.
	 * @param oldLabel Old name of the NodeBlock, must not be present if no relabeling is to be done.
	 * @param newLabel New name of the NodeBlock, must not be present if no relabeling is to be done.
	 * @return The newly created NodeBlock based on the NodeBlock passed as parameter.
	 */
	private ModelNodeBlock copyModelNodeBlock(ModelNodeBlock nb, RefinementCommand refinement) {
		var newNb = EMSLFactory.eINSTANCE.createModelNodeBlock();
		
		// apply relabeling
		if (refinement.getRelabeling() != null) {
			for (var r : refinement.getRelabeling()) {
				if (r.getOldLabel() != null  
						&& nb.getName().equals(r.getOldLabel())) {
					newNb.setName(r.getNewLabel());
					break;
				}
			}
		}
		
		if (newNb.getName() == null)
			newNb.setName(nb.getName());
		
		newNb.setType(nb.getType()); 
		newNb.setAction(EcoreUtil.copy(nb.getAction()));
		
		// add relations to new nodeblock
		for (var rel : nb.getRelations()) {
			var newRel = EMSLFactory.eINSTANCE.createModelRelationStatement();
			
			// apply relabeling
			for (var relabeling : refinement.getRelabeling()) {
				if (relabeling.getOldLabel().equals(rel.getName()))
					newRel.setName(relabeling.getNewLabel());
			}
			if (newRel.getName() == null)
				newRel.setName(rel.getName());
			
			// copy action, properties target etc.
			if (rel.getAction() != null) {
				newRel.setAction(EMSLFactory.eINSTANCE.createAction());
				newRel.getAction().setOp(rel.getAction().getOp());
			}
			for (var prop : rel.getProperties()) {
				newRel.getProperties().add(copyModelPropertyStatement(prop));
			}
			rel.getTypes().forEach(t -> {
				var newRelType = EMSLFactory.eINSTANCE.createModelRelationStatementType();
				newRelType.setLower(t.getLower());
				newRelType.setUpper(t.getUpper());
				newRelType.setType(t.getType());
				newRel.getTypes().add(newRelType);
			});
			newRel.setTarget(rel.getTarget());			
			newNb.getRelations().add(newRel);
		}
		
		// add properties to new nodeblock
		for (var prop : nb.getProperties()) {
			newNb.getProperties().add(copyModelPropertyStatement(prop));
		}
		
		return newNb;
	}
	
	/**
	 * Creates a new ModelPropertyStatement that is equal to the one that is given.
	 * @param oldStatement that is to be copied.
	 * @return a new ModelPropertyStatement with equal attributes of the given one.
	 */
	private ModelPropertyStatement copyModelPropertyStatement(ModelPropertyStatement oldStatement) {
		var newProp = EMSLFactory.eINSTANCE.createModelPropertyStatement();
		newProp.setOp(oldStatement.getOp());
		newProp.setType(oldStatement.getType());
		
		// create new Value for Property
		if (oldStatement.getValue() instanceof PrimitiveBoolean) {
			newProp.setValue(EMSLFactory.eINSTANCE.createPrimitiveBoolean());
			if (((PrimitiveBoolean) oldStatement.getValue()).isTrue())
				((PrimitiveBoolean) newProp.getValue()).setTrue(true);
			else 
				((PrimitiveBoolean) newProp.getValue()).setTrue(false);
		} else if (oldStatement.getValue() instanceof PrimitiveInt) {
			newProp.setValue(EMSLFactory.eINSTANCE.createPrimitiveInt());
			((PrimitiveInt) newProp.getValue()).setLiteral(((PrimitiveInt) oldStatement.getValue()).getLiteral());
		} else if (oldStatement.getValue() instanceof PrimitiveString) {
			newProp.setValue(EMSLFactory.eINSTANCE.createPrimitiveString());
			((PrimitiveString) newProp.getValue()).setLiteral(((PrimitiveString) oldStatement.getValue()).getLiteral());
		} else if (oldStatement.getValue() instanceof EnumValue) {
			oldStatement.getValue();
			newProp.setValue(EMSLFactory.eINSTANCE.createEnumValue());
			((EnumValue) newProp.getValue()).setLiteral(((EnumValue) oldStatement.getValue()).getLiteral());
		}
		
		return newProp;
	}
	
	/**
	 * Compares the two given PropertyStatements for equal values. If the values are not equal an according exception is thrown.
	 * @param entity that contains the PropertyStatements.
	 * @param p1 	first statement in the comparison.
	 * @param p2	second statement in the comparison.
	 * @throws FlattenerException is thrown if the values of the two statements are 
	 * 							  not equal.
	 */
	private void compareValueOfModelPropertyStatement(Entity entity, ModelPropertyStatement p1, ModelPropertyStatement p2) throws FlattenerException {
		
		if (p1.getValue() instanceof PrimitiveBoolean && p2.getValue() instanceof PrimitiveBoolean
				&& (((PrimitiveBoolean) p1.getValue()).isTrue() && ((PrimitiveBoolean) p2.getValue()).isTrue()
						|| !((PrimitiveBoolean) p1.getValue()).isTrue()
							&& !((PrimitiveBoolean) p2.getValue()).isTrue())) {
			return;
		} else if (p1.getValue() instanceof PrimitiveInt && p2.getValue() instanceof PrimitiveInt
				&& ((PrimitiveInt) p1.getValue()).getLiteral() == ((PrimitiveInt) p2.getValue()).getLiteral()) {
			return;
		} else if (p1.getValue() instanceof PrimitiveString && p2.getValue() instanceof PrimitiveString
				&& ((PrimitiveString) p1.getValue()).getLiteral()
						.equals(((PrimitiveString) p2.getValue()).getLiteral())) {
			return;
		} else if (p1.getValue() instanceof EnumValue && p2.getValue() instanceof EnumValue
				&& ((EnumValue) p1.getValue()).getLiteral() == ((EnumValue) p2.getValue()).getLiteral()) {
			return;
		}
		if (p2.eContainer().eContainer() instanceof AtomicPattern) {
			throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_VALUES, p1, p2,
					(SuperType) p2.eContainer().eContainer().eContainer());
		} else if (p2.eContainer().eContainer().eContainer() instanceof AtomicPattern) {
			throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_VALUES, p1, p2,
					(SuperType) p2.eContainer().eContainer().eContainer());
		} else if (p2.eContainer().eContainer() instanceof Rule
				|| p2.eContainer().eContainer() instanceof Model) {
			throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_VALUES, p1, p2,
					(SuperType) p2.eContainer().eContainer());
		} else if (p2.eContainer().eContainer().eContainer() instanceof Rule
				|| p2.eContainer().eContainer().eContainer() instanceof Model) {
			throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_VALUES, p1, p2,
					(SuperType) p2.eContainer().eContainer().eContainer());
		}
	}
	
	/**
	 * Iterates over all relations in the newly flattened entity and checks if a
	 * proxy target could not be resolved to a nodeBlock from one of the
	 * superEntities.
	 * 
	 * @param entity that is to be checked if all proxies were resolved.
	 * @throws FlattenerException is thrown if a proxy was not resolved during
	 * 							  flattening.
	 */
	private void checkForResolvedProxies(Entity entity) throws FlattenerException {
		for (var nb : dispatcher.getNodeBlocks(entity)) {
			for (var relation : nb.getRelations()) {
				if (!(relation.getTarget() instanceof ModelNodeBlock)) {
					throw new FlattenerException(entity, FlattenerErrorType.NON_RESOLVABLE_PROXY, relation);
				}
			}
		}
	}

	/**
	 * Checks if the type of a superEntity matches the type of the entity that is to
	 * be flattened.
	 * 
	 * @param entity 	  that is to be flattened.
	 * @param superEntity that is to be refined.
	 * @throws FlattenerException is thrown if the type of superEntity is not supported.
	 */
	private void checkSuperEntityTypeForCompliance(Entity entity, SuperType superEntity) throws FlattenerException {
		if (entity instanceof Metamodel && !(superEntity instanceof Metamodel)
				|| !(entity instanceof Metamodel) && superEntity instanceof Metamodel)
			throw new FlattenerException(entity, FlattenerErrorType.NON_COMPLIANT_SUPER_ENTITY, superEntity);
		else if (entity instanceof TripleRule && !(superEntity instanceof TripleRule)
				|| !(entity instanceof TripleRule) && superEntity instanceof TripleRule)
			throw new FlattenerException(entity, FlattenerErrorType.NON_COMPLIANT_SUPER_ENTITY, superEntity);
	}
	
	/**
	 * Compares two correspondences.
	 * @param corr1 first correspondence in comparison.
	 * @param corr2 second correspondence in comparison.
	 * @return whether two correspondences are equal or not.
	 */
	private boolean isEqualCorrespondence(Correspondence corr1, Correspondence corr2) {
		return (corr1.getAction() == null && corr2.getAction() == null || (corr1.getAction() != null && corr2.getAction() != null && corr1.getAction().getOp() == corr2.getAction().getOp()))
				&& corr1.getSource().getName().equals(corr2.getSource().getName())
				&& corr1.getTarget().getName().equals(corr2.getTarget().getName())
				&& corr1.getType().getName().equals(corr2.getType().getName())
				&& corr1.getType().getSource() == corr2.getType().getSource()
				&& corr1.getType().getTarget() == corr2.getType().getTarget();
	}
}
