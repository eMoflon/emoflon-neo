package org.emoflon.neo.emsl.refinement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.xtext.EcoreUtil2;
import org.emoflon.neo.emsl.eMSL.AttributeExpression;
import org.emoflon.neo.emsl.eMSL.ConditionOperator;
import org.emoflon.neo.emsl.eMSL.ConstraintArgValue;
import org.emoflon.neo.emsl.eMSL.Correspondence;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.Parameter;
import org.emoflon.neo.emsl.eMSL.RefinementCommand;
import org.emoflon.neo.emsl.eMSL.SuperType;
import org.emoflon.neo.emsl.eMSL.TripleRule;
import org.emoflon.neo.emsl.eMSL.ValueExpression;
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
			Map<String, List<ModelNodeBlock>> collectedSrcNodeBlocks = collectNodes(entity, refinements,
					alreadyRefinedEntityNames, true);
			entity.getSrcNodeBlocks().forEach(nb -> {
				if (collectedSrcNodeBlocks.keySet().contains(nb.getName())) {
					collectedSrcNodeBlocks.get(nb.getName()).add(nb);
				} else {
					List<ModelNodeBlock> tmp = new ArrayList<ModelNodeBlock>();
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
			Map<String, List<ModelNodeBlock>> collectedTrgNodeBlocks = collectNodes(entity, refinements,
					alreadyRefinedEntityNames, false);
			entity.getTrgNodeBlocks().forEach(nb -> {
				if (collectedTrgNodeBlocks.keySet().contains(nb.getName())) {
					collectedTrgNodeBlocks.get(nb.getName()).add(nb);
				} else {
					List<ModelNodeBlock> tmp = new ArrayList<ModelNodeBlock>();
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
							TripleRule flattenedTmpCopy = ((TripleRule) flatten(tmpCopy, new HashSet<String>()));
							var flattenedCorrs = EcoreUtil.copyAll(flattenedTmpCopy.getCorrespondences());
							// apply relabeling of nodes to fix node references of correspondences
							if (((RefinementCommand) s).getRelabeling().size() > 0) {
								for (var corr : flattenedCorrs) {
									for (var relabeling : ((RefinementCommand) s).getRelabeling()) {
										String srcNodeLabel = null;
										String trgNodeLabel = null;
										// handle source of correspondence
										if (corr.getSource() != null && relabeling.getOldLabel().equals(corr.getSource().getName())) {
											// add node with updated name
											srcNodeLabel = relabeling.getNewLabel();
										} else if (corr.getProxySource() != null && relabeling.getOldLabel().equals(corr.getProxySource())) {
											// add node with updated name
											srcNodeLabel = relabeling.getNewLabel();
										}
										if (srcNodeLabel != null) {
											corr.setProxySource(srcNodeLabel);
											corr.setSource(null);
										}
										// handle target of correspondence
										if (corr.getTarget() != null && relabeling.getOldLabel().equals(corr.getTarget().getName())) {
											// add node with updated name
											trgNodeLabel = relabeling.getOldLabel();
										} else if (corr.getProxyTarget() != null && relabeling.getOldLabel().equals(corr.getProxyTarget())) {
											// add node with updated name
											trgNodeLabel = relabeling.getNewLabel();
										}
										if (trgNodeLabel != null) {
											corr.setProxyTarget(trgNodeLabel);
											corr.setTarget(null);
										}
									}
								}
							}
							corrs.addAll(flattenedCorrs);
						} catch (FlattenerException e) {
							// nothing to do here because recursive errors must have already appeared
						}
					});
			entity.getCorrespondences().clear();
			entity.getCorrespondences().addAll(mergeCorrespondences(entity, corrs, mergedSrcNodes, mergedTrgNodes));

			// -------------- Attribute Constraints ---------------- //
			
			// 4. step: collect all attribute constraints transitively
			var attributeConstraints = getAllSuperRules(entity).stream()//
				.flatMap(tr -> tr.getAttributeConstraints().stream())//
				.map(attrConstr -> EcoreUtil.copy(attrConstr))//
				.collect(Collectors.toSet());
			
			entity.getAttributeConstraints().addAll(attributeConstraints);
			
			// Handle parameters in black nodes (remove and change all references to this parameter to attribute expressions?)
			// This includes attribute constraints *and* any nodes that reference this parameter!
			var blackParameters = getParametersInBlackNodes(entity);
			for(var blackParam : blackParameters.keySet()) {
				var referencesToParam = getReferencesToParameter(blackParam, entity);
				for(var ref : referencesToParam)
					changeToAttributeExpression(ref, blackParameters);
				
				for(var ref : referencesToParam)
					removeRepresentative(ref, blackParameters);
			}
			
			// 5. Resolve proxies
			checkForResolvedProxies(entity);
		}

		return entity;
	}
	
	private void removeRepresentative(Parameter parameter, Map<String, ModelPropertyStatement> representatives) {
		var rep = representatives.get(parameter.getName());
		if(parameter.eContainer() instanceof ModelPropertyStatement) {
			var prop = (ModelPropertyStatement) parameter.eContainer();
			var block = (ModelNodeBlock) prop.eContainer();
			// For representatives, simply remove property involving parameter
			if(prop.equals(rep)) {
				block.getProperties().remove(prop);
				return;
			}
			// Otherwise convert to attribute expression
			else {
				prop.setValue(createAttrExprForProperty(rep));
				return;
			}
		}
	}
	
	private void changeToAttributeExpression(Parameter parameter, Map<String, ModelPropertyStatement> representatives) {
		var rep = representatives.get(parameter.getName());
		if(parameter.eContainer() instanceof ConstraintArgValue) {
			var constrArgValue = (ConstraintArgValue) parameter.eContainer();
			constrArgValue.setValue(createAttrExprForProperty(rep));
		}
	}
	
	private AttributeExpression createAttrExprForProperty(ModelPropertyStatement rep) {
		var attrExpr = EMSLFactory.eINSTANCE.createAttributeExpression();
		attrExpr.setNode((ModelNodeBlock) rep.eContainer());
		var attrExprTrg = EMSLFactory.eINSTANCE.createNodeAttributeExpTarget();
		attrExprTrg.setAttribute(rep.getType());
		attrExpr.setTarget(attrExprTrg);
		return attrExpr;
	}
	
	private Collection<Parameter> getReferencesToParameter(String param, TripleRule entity) {
		var allNodes = new ArrayList<ModelNodeBlock>();
		allNodes.addAll(entity.getSrcNodeBlocks());
		allNodes.addAll(entity.getTrgNodeBlocks());
		
		var attrConstrs = entity.getAttributeConstraints();
		
		var allParameters = new ArrayList<ValueExpression>();
		allParameters.addAll(allNodes.stream()//
			.flatMap(n -> n.getProperties().stream())//
			.filter(prop -> prop.getValue() instanceof Parameter)//
			.filter(prop -> ((Parameter) prop.getValue()).getName().equals(param))//
			.map(prop -> prop.getValue())//
			.collect(Collectors.toList()));
			
		allParameters.addAll(attrConstrs.stream()//
				.flatMap(attrConstr -> attrConstr.getValues().stream())//
				.map(v -> v.getValue())//
				.filter(val -> val instanceof Parameter)
				.filter(val -> ((Parameter)val).getName().equals(param))
				.collect(Collectors.toList())//
				);
		
		return allParameters.stream()//
				.map(p -> Parameter.class.cast(p))//
				.collect(Collectors.toList());
	}
	
	private Map<String, ModelPropertyStatement> getParametersInBlackNodes(TripleRule entity) {
		var allNodes = new ArrayList<ModelNodeBlock>();
		allNodes.addAll(entity.getSrcNodeBlocks());
		allNodes.addAll(entity.getTrgNodeBlocks());
		
		return allNodes.stream()//
			.filter(n -> n.getAction() == null)//
			.flatMap(n -> n.getProperties().stream())//
			.filter(prop -> prop.getOp() == ConditionOperator.ASSIGN)//
			.filter(prop -> prop.getValue() instanceof Parameter)//
			.collect(Collectors.toMap(prop -> ((Parameter)prop.getValue()).getName(), prop -> prop))//
			;		
	}
	
	private Set<TripleRule> getAllSuperRules(TripleRule entity) {
		var tripleRules = new HashSet<TripleRule>();
		for (var refComm : entity.getSuperRefinementTypes()) {
			var tr = (TripleRule) refComm.getReferencedType();
			tripleRules.add(tr);
			tripleRules.addAll(getAllSuperRules(tr));
		}
		
		return tripleRules;
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
						List<ModelNodeBlock> newList = new ArrayList<ModelNodeBlock>();
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
