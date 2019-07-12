package org.emoflon.neo.emsl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.eclipse.emf.common.util.EList;
import org.emoflon.neo.emsl.eMSL.ActionOperator;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.AttributeCondition;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.Entity;
import org.emoflon.neo.emsl.eMSL.EnumValue;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock;
import org.emoflon.neo.emsl.eMSL.MetamodelRelationStatement;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatementType;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.PrimitiveBoolean;
import org.emoflon.neo.emsl.eMSL.PrimitiveInt;
import org.emoflon.neo.emsl.eMSL.PrimitiveString;
import org.emoflon.neo.emsl.eMSL.RefinementCommand;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.emsl.eMSL.SuperType;
import org.emoflon.neo.emsl.eMSL.TripleRule;
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
	 * @param entity that should be flattened.
	 * @param alreadyRefinedEntityNames list of names of entities that have already appeared in the refinement path (against loops).
	 * @return the flattened entity.
	 * @throws FlattenerException is thrown if the entity could not be flattened.
	 */
	public Entity flattenEntity(Entity entity, ArrayList<String> alreadyRefinedEntityNames) throws FlattenerException {
		if (entity != null) {			
			@SuppressWarnings("unchecked")
			EList<RefinementCommand> refinements = (EList<RefinementCommand>) dispatcher.getSuperRefinementTypes(entity);
			
			// check for loop in refinements
	
			if (alreadyRefinedEntityNames.contains(dispatcher.getName(entity))) 
				throw new FlattenerException(entity, FlattenerErrorType.INFINITE_LOOP, alreadyRefinedEntityNames);
			// if none has been found, add current name to list
			
			
			// check if anything has to be done, if not return
			if (refinements.isEmpty())
				return entity;
			
			// 1. step: collect nodes with edges
			var<String, ArrayList<ModelNodeBlock>> collectedNodeBlocks = collectNodes(entity, refinements, alreadyRefinedEntityNames);
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
			
			// 3. step: add merged nodeBlocks to entity and return
			dispatcher.getNodeBlocks(entity).clear();
			dispatcher.getNodeBlocks(entity).addAll((mergedNodes));
			
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
	 * @param originalEntity that is to be copied and flattened.
	 * @param alreadyRefinedEntityNames list of names that of entities that have already appeared in the refinement path (against loops). 
	 * @return flattened copy of given Entity.
	 * @throws FlattenerException is thrown if the entity could not be flattened.
	 */
	public Entity flattenCopyOfEntity(Entity originalEntity, ArrayList<String> alreadyRefinedEntityNames) throws FlattenerException {
		
		var entity = (Entity) new EntityCloner().cloneEntity(originalEntity);
		
		if (entity != null) {			
			@SuppressWarnings("unchecked")
			EList<RefinementCommand> refinements = (EList<RefinementCommand>) dispatcher.getSuperRefinementTypes(entity);
			
			// check for loop in refinements
	
			if (alreadyRefinedEntityNames.contains(dispatcher.getName(entity))) 
				throw new FlattenerException(entity, FlattenerErrorType.INFINITE_LOOP, alreadyRefinedEntityNames);
			// if none has been found, add current name to list
			
			
			// check if anything has to be done, if not return
			if (refinements.isEmpty())
				return entity;
			
			// 1. step: collect nodes with edges
			var<String, ArrayList<ModelNodeBlock>> collectedNodeBlocks = collectNodes(entity, refinements, alreadyRefinedEntityNames);
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
			
			// 4. step: merge attribute conditions in rules/patterns(/tripleRules)
			var mergedAttributeConditions = mergeAttributeConditions(entity);
			dispatcher.getAttributeConditions(entity).clear();
			dispatcher.getAttributeConditions(entity).addAll(mergedAttributeConditions);
			
			if (entity instanceof Pattern) {
				var atomicPattern = ((Pattern) entity).getBody();
				((Pattern) entity).setBody(atomicPattern);
			}
			
			checkForResolvedProxies(entity);
		}
		
		return entity;
	}
	
	
	/**
	 * This method creates all NodeBlocks that have to be imported into the Entity from the SuperEntities.
	 * @param refinementList List of RefinementCommands holding all entities that should be refined.
	 * @param alreadyRefinedEntityNames List of entity names that have already appeared in the refinement path (against loops).
	 * @return HashMap of NodeBlocks mapped to their name that have to be added to the refining Entity.
	 * @throws FlattenerException is thrown if an error occurs during collecting the nodes, like an infinite loop is detected
	 */
	private HashMap<String, ArrayList<ModelNodeBlock>> collectNodes(Entity entity, EList<RefinementCommand> refinementList, ArrayList<String> alreadyRefinedEntityNames) throws FlattenerException {
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
						|| r.getReferencedType() instanceof TripleRule && ((TripleRule) r.getReferencedType()).getNacs() != null) {
					throw new FlattenerException(entity, FlattenerErrorType.REFINE_ENTITY_WITH_CONDITION, r.getReferencedType());
				}
				
				if (flattenedSuperEntity != null) {
					for (var nb : dispatcher.getNodeBlocks((Entity) flattenedSuperEntity)) {
						
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
	 * @param nodeBlocks that were collected from the refinements.
	 * @param mergedNodes nodeBlocks that were except for the relationStatements already merged.
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
					if (rel.getTypeList() == null) {
						continue;
					}
					// collect edges that have no names -> simple edges (only one type) => merging does not change
					if (rel.getName() == null) {
						if (rel.getTarget() != null) {
							if (!edges.containsKey(rel.getTypeList().get(0).getType().getName())) {
								edges.put(rel.getTypeList().get(0).getType().getName(), new HashMap<String, ArrayList<ModelRelationStatement>>());
							}
							if (!edges.get(rel.getTypeList().get(0).getType().getName()).containsKey(rel.getTarget().getName())) {
								edges.get(rel.getTypeList().get(0).getType().getName()).put(rel.getTarget().getName(), new ArrayList<ModelRelationStatement>());
							}
							edges.get(rel.getTypeList().get(0).getType().getName()).get(rel.getTarget().getName()).add(rel);
						} else if (rel.getProxyTarget() != null) {
							if (!edges.containsKey(rel.getTypeList().get(0).getType().getName())) {
								edges.put(rel.getTypeList().get(0).getType().getName(), new HashMap<String, ArrayList<ModelRelationStatement>>());
							}
							if (!edges.get(rel.getTypeList().get(0).getType().getName()).containsKey(rel.getProxyTarget())) {
								edges.get(rel.getTypeList().get(0).getType().getName()).put(rel.getProxyTarget(), new ArrayList<ModelRelationStatement>());
							}
							edges.get(rel.getTypeList().get(0).getType().getName()).get(rel.getProxyTarget()).add(rel);
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
					newRelType.setType((edges.get(typename).get(targetname).get(0).getTypeList().get(0).getType()));
					// collect all types of the edges that are to be merged (should be one type each in this case) to merge the bounds
					var typesOfEdges = new ArrayList<ModelRelationStatementType>();
					for (var e : edges.get(typename).get(targetname)) {
						typesOfEdges.addAll(e.getTypeList());
					}
					var bounds = mergeModelRelationStatementPathLimits(entity, typesOfEdges);
					if (bounds != null) {
						newRelType.setLower(bounds[0].toString());
						newRelType.setUpper(bounds[1].toString()); 
					}
					newRel.getTypeList().add(newRelType);
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
				namedEdges.get(n).get(0).getTypeList().forEach(t -> intersection.add(t.getType()));
				for (var e : namedEdges.get(n)) {
					var typesOfOther = new ArrayList<MetamodelRelationStatement>();
					e.getTypeList().forEach(t -> typesOfOther.add(t.getType()));
					intersection.retainAll(typesOfOther);
				}
				
				if (intersection.isEmpty()) 
					throw new FlattenerException(entity, FlattenerErrorType.NO_INTERSECTION_IN_MODEL_RELATION_STATEMENT_TYPE_LIST);
				
				for (var t : intersection) {
					// create new ModelRelationStatementType for each remaining type
					var newRelType = EMSLFactory.eINSTANCE.createModelRelationStatementType();
					newRelType.setType(t);
					newRel.getTypeList().add(newRelType);
					
					var typesOfEdges = new ArrayList<ModelRelationStatementType>();
					for (var e : namedEdges.get(n)) {
						for (var tmp : e.getTypeList()) {
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
				if (relation.getTypeList().size() > 1)
					continue;
				for (var other : nb.getRelations()) {
					if (other.getTypeList().size() > 1 || relation == other)
						continue;
					if (relation.getTypeList().get(0).getType() == other.getTypeList().get(0).getType()	
							&& relation.getTarget() == other.getTarget()
							&& relation.getTypeList().get(0).getLower() == other.getTypeList().get(0).getLower()
							&& relation.getTypeList().get(0).getUpper() == other.getTypeList().get(0).getUpper()) {
						if (!duplicates.contains(other)) {
							duplicates.add(other);
						}
					}
				}
			}
			nb.getRelations().stream().filter(r -> duplicates.contains(r));
			for (var relation : nb.getRelations()) {
				if (relation.getTypeList().size() == 1 && relation.getName() != null)
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
	
	private ArrayList<AttributeCondition> mergeAttributeConditions(Entity entity) {
		var conditions = new ArrayList<AttributeCondition>();
		
		return conditions;
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
		newNb.setAction(nb.getAction());
		
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
			rel.getTypeList().forEach(t -> {
				var newRelType = EMSLFactory.eINSTANCE.createModelRelationStatementType();
				newRelType.setLower(t.getLower());
				newRelType.setUpper(t.getUpper());
				newRelType.setType(t.getType());
				newRel.getTypeList().add(newRelType);
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
	 * @param p1 first statement in the comparison.
	 * @param p2 second statement in the comparison.
	 * @throws FlattenerException is thrown if the values of the two statements are not equal.
	 */
	private void compareValueOfModelPropertyStatement(Entity entity, ModelPropertyStatement p1, ModelPropertyStatement p2) throws FlattenerException {
		
		if (p1.getValue() instanceof PrimitiveBoolean && p2.getValue() instanceof PrimitiveBoolean
				&& (((PrimitiveBoolean) p1.getValue()).isTrue() && ((PrimitiveBoolean) p2.getValue()).isTrue()
				|| !((PrimitiveBoolean) p1.getValue()).isTrue() && !((PrimitiveBoolean) p2.getValue()).isTrue())) {
			return;
		} else if (p1.getValue() instanceof PrimitiveInt && p2.getValue() instanceof PrimitiveInt
				&& ((PrimitiveInt) p1.getValue()).getLiteral() == ((PrimitiveInt) p2.getValue()).getLiteral()) {
			return;
		} else if (p1.getValue() instanceof PrimitiveString && p2.getValue() instanceof PrimitiveString
				&& ((PrimitiveString) p1.getValue()).getLiteral().equals(((PrimitiveString) p2.getValue()).getLiteral())) {
			return;
		} else if (p1.getValue() instanceof EnumValue && p2.getValue() instanceof EnumValue
				&& ((EnumValue) p1.getValue()).getLiteral() == ((EnumValue) p2.getValue()).getLiteral()) {
			return;
		}
		if (p2.eContainer().eContainer() instanceof AtomicPattern) {
			throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_VALUES, p1, p2, (SuperType) p2.eContainer().eContainer().eContainer());
		} else if (p2.eContainer().eContainer().eContainer() instanceof AtomicPattern) {
			throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_VALUES, p1, p2, (SuperType) p2.eContainer().eContainer().eContainer());
		} else if (p2.eContainer().eContainer() instanceof Rule || p2.eContainer().eContainer() instanceof Model) {
			throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_VALUES, p1, p2, (SuperType) p2.eContainer().eContainer());
		} else if (p2.eContainer().eContainer().eContainer() instanceof Rule || p2.eContainer().eContainer().eContainer() instanceof Model) {
			throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_VALUES, p1, p2, (SuperType) p2.eContainer().eContainer().eContainer());
		}
	}
	
	/**
	 * Iterates over all relations in the newly flattened entity and checks if a proxy target could not be resolved to
	 * a nodeBlock from one of the superEntities.
	 * @param entity that is to be checked if all proxies were resolved.
	 * @throws FlattenerException is thrown if a proxy was not resolved during flattening.
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
	 * Checks if the type of a superEntity matches the type of the entity that is to be flattened. 
	 * @param entity 				that is to be flattened.
	 * @param superEntity			that is to be refined.
	 * @throws FlattenerException	is thrown if the type of superEntity is not supported.
	 */
	private void checkSuperEntityTypeForCompliance(Entity entity, SuperType superEntity) throws FlattenerException {
		if (entity instanceof Metamodel && !(superEntity instanceof Metamodel)
				|| !(entity instanceof Metamodel) && superEntity instanceof Metamodel)
			throw new FlattenerException(entity, FlattenerErrorType.NON_COMPLIANT_SUPER_ENTITY, superEntity);
		else if (entity instanceof TripleRule && !(superEntity instanceof TripleRule)
				|| !(entity instanceof TripleRule) && superEntity instanceof TripleRule)
			throw new FlattenerException(entity, FlattenerErrorType.NON_COMPLIANT_SUPER_ENTITY, superEntity);
	}
}
