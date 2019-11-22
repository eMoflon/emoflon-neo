package org.emoflon.neo.emsl.refinement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emoflon.neo.emsl.eMSL.Action;
import org.emoflon.neo.emsl.eMSL.ActionOperator;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.AttributeExpression;
import org.emoflon.neo.emsl.eMSL.BinaryExpression;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.EnumValue;
import org.emoflon.neo.emsl.eMSL.LinkAttributeExpTarget;
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock;
import org.emoflon.neo.emsl.eMSL.MetamodelRelationStatement;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatementType;
import org.emoflon.neo.emsl.eMSL.Parameter;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.PrimitiveBoolean;
import org.emoflon.neo.emsl.eMSL.PrimitiveInt;
import org.emoflon.neo.emsl.eMSL.PrimitiveString;
import org.emoflon.neo.emsl.eMSL.RefinementCommand;
import org.emoflon.neo.emsl.eMSL.SuperType;
import org.emoflon.neo.emsl.eMSL.ValueExpression;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.emsl.util.EntityAttributeDispatcher;
import org.emoflon.neo.emsl.util.FlattenerErrorType;
import org.emoflon.neo.emsl.util.FlattenerException;

public class RuleFlattener extends AbstractEntityFlattener {
	protected EntityAttributeDispatcher dispatcher;

	RuleFlattener() {
		dispatcher = new EntityAttributeDispatcher();
	}

	@Override
	public SuperType flatten(SuperType entity, Set<String> alreadyRefinedEntityNames) throws FlattenerException {
		if (entity != null) {
			var refinements = dispatcher.getSuperRefinementTypes(entity);

			// check for loop in refinements

			if (alreadyRefinedEntityNames.contains(dispatcher.getName(entity)))
				throw new FlattenerException(entity, FlattenerErrorType.INFINITE_LOOP, alreadyRefinedEntityNames);
			// if none has been found, add current name to list

			// check if anything has to be done, if not return
			if (refinements.isEmpty())
				return entity;

			// 1. step: collect nodes with edges
			var collectedNodeBlocks = collectNodes(entity, refinements, alreadyRefinedEntityNames);
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

			checkForResolvedProxies(entity);
		}

		return entity;
	}

	private Map<String, List<ModelNodeBlock>> collectNodes(SuperType entity, List<RefinementCommand> refinementList,
			Set<String> alreadyRefinedEntityNames) throws FlattenerException {
		var nodeBlocks = new HashMap<String, List<ModelNodeBlock>>();

		for (var r : refinementList) {

			if (!r.getReferencedType().eClass().equals(entity.eClass())) {
				throw new FlattenerException(entity, FlattenerErrorType.NON_COMPLIANT_SUPER_ENTITY,
						r.getReferencedType());
			}

			// add current entity to list of names to detect infinite loop
			var alreadyRefinedEntityNamesCopy = new HashSet<String>(alreadyRefinedEntityNames);
			alreadyRefinedEntityNamesCopy.add(dispatcher.getName(entity));

			// recursively flatten superEntities
			var nodeBlocksOfSuperEntity = new ArrayList<ModelNodeBlock>();

			EcoreUtil.resolveAll(r.getReferencedType());
			var tmpCopy = EcoreUtil.copy(r.getReferencedType());
			var flattenedSuperEntity = flatten(tmpCopy, alreadyRefinedEntityNamesCopy);

			// check if a superEntity possesses a condition block
			if (r.getReferencedType() instanceof AtomicPattern
					&& ((Pattern) r.getReferencedType().eContainer()).getCondition() != null) {
				throw new FlattenerException(entity, FlattenerErrorType.REFINE_ENTITY_WITH_CONDITION,
						r.getReferencedType());
			}

			if (flattenedSuperEntity != null) {
				List<ModelNodeBlock> nodeBlocksOfFlattenedSuperEntity = dispatcher.getNodeBlocks(flattenedSuperEntity);

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

	/**
	 * This method sets the targets of ModelRelationStatements of the given Nodes
	 * such that they reference the nodes created in the calling method
	 * (collectNodes) and no longer reference the nodes of the real superEntity.
	 * 
	 * @param nodeBlocksOfSuperEntity list of nodes that need their relations'
	 *                                targets adjusted.
	 * @param r                       RefinementCommand that includes any new labels
	 *                                for nodes.
	 * @return list of nodes with their relations' targets correctly referenced.
	 */
	protected ArrayList<ModelNodeBlock> reAdjustTargetsOfEdges(ArrayList<ModelNodeBlock> nodeBlocksOfSuperEntity,
			RefinementCommand r) {
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

		return nodeBlocksOfSuperEntity;
	}

	/**
	 * This method takes a HashMap of lists of NodeBlocks mapped to the names of the
	 * contained NodeBlocks. All of those NodeBlocks with the same name (mapping)
	 * are then merged into one that is added to the model. During the merging the
	 * least common subtype of all NodeBlocks with the same name is searched for.
	 * All of the edges with the same target and same type are also merged.
	 * 
	 * @param nodeBlocks HashMap of Lists of ModelNodeBlocks that are mapped to the
	 *                   names of the NodeBlocks contained in such a list.
	 * @return ArrayList of ModelNodeBlocks that only contains the ModelNodeBlocks
	 *         that were created during merging.
	 * @throws FlattenerException is thrown if something went wrong during the
	 *                            merging process.
	 */
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
	 * Takes a list of nodes and merges their actions with the "black wins"
	 * principle.
	 * 
	 * @param nodes list of nodes that provide actions must be merged
	 * @return an action if a merged action can be determined, null if not
	 * @throws FlattenerException
	 */
	protected Action mergeActionOfNodes(List<ModelNodeBlock> nodes, SuperType entity) throws FlattenerException {
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
		else if (green && red && !black)
			throw new FlattenerException(entity, FlattenerErrorType.ONLY_RED_AND_GREEN_NODES, nodes);
		else
			action = null;

		return action;
	}

	/**
	 * Iterates over all NodeBlocks given in mergedNodes and removes any edges that
	 * appear twice in a node.
	 * 
	 * @param mergedNodes that are to be checked for duplicate edges.
	 * @return list with nodes with their edges removed.
	 */
	private List<ModelNodeBlock> removeDuplicateEdges(List<ModelNodeBlock> mergedNodes) {
		for (var nb : mergedNodes) {
			var duplicates = new ArrayList<ModelRelationStatement>();
			for (var relation : nb.getRelations()) {
				if (relation.getTypes().size() > 1)
					continue;
				for (var other : nb.getRelations()) {
					if (other.getTypes().size() > 1 || relation == other)
						continue;
					if (relation.getTypes().get(0).getType() == other.getTypes().get(0).getType()
							&& relation.getTarget() == other.getTarget() && relation.getLower().equals(other.getLower())
							&& relation.getUpper().equals(other.getUpper())) {
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
	 * Collects and merges the property statements of the given edges.
	 * 
	 * @param edges  whose properties will be merged.
	 * @param entity that contains the properties (directly or indirectly).
	 * @return HashSet containing the merged ModelPropertyStatements.
	 * @throws FlattenerException is thrown if two properties are not mergeable.
	 */
	private HashSet<ModelPropertyStatement> collectAndMergePropertyStatementsOfRelations(
			ArrayList<ModelRelationStatement> edges, SuperType entity, List<ModelNodeBlock> mergedNodes,
			List<RefinementCommand> refinementList) throws FlattenerException {
		var properties = new HashMap<String, ArrayList<ModelPropertyStatement>>();
		var mergedProperties = new HashSet<ModelPropertyStatement>();

		// collect PropertyStatements of Edges
		if (edges != null) {
			for (var e : edges) {
				// collect propertyStatements
				e.getProperties().forEach(p -> {
					if (!properties.containsKey(EMSLUtil.getNameOfType(p))) {
						properties.put(EMSLUtil.getNameOfType(p), new ArrayList<ModelPropertyStatement>());
					}
					properties.get(EMSLUtil.getNameOfType(p)).add(p);
				});
			}

			for (var propertyName : properties.keySet()) {
				var props = properties.get(propertyName);
				ModelPropertyStatement basis = null;
				if (properties.size() > 0) {
					basis = props.get(0);
				}
				for (var p : props) {
					if (!sameDataType(p, basis)) {
						// incompatible types/operands found
						if (p.eContainer().eContainer().eContainer() instanceof AtomicPattern) {
							throw new FlattenerException(entity, FlattenerErrorType.NO_COMMON_SUBTYPE_OF_PROPERTIES,
									basis, p, (SuperType) p.eContainer().eContainer().eContainer());
						} else {
							throw new FlattenerException(entity, FlattenerErrorType.NO_COMMON_SUBTYPE_OF_PROPERTIES,
									basis, p, (SuperType) p.eContainer().eContainer().eContainer());
						}
					} else if (basis.getOp() != p.getOp()) {
						if (p.eContainer().eContainer().eContainer() instanceof AtomicPattern) {
							throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_OPERATORS,
									basis, p, (SuperType) p.eContainer().eContainer().eContainer());
						} else {
							throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_OPERATORS,
									basis, p, (SuperType) p.eContainer().eContainer().eContainer());
						}
					}
					compareValueOfModelPropertyStatement(entity, basis, p);
				}

				var newProp = EcoreUtil.copy(basis);

				if (basis.getValue() instanceof AttributeExpression) {
					newProp.setValue(readjustAttributeExpressionReferences((AttributeExpression) newProp.getValue(),
							basis, refinementList, mergedNodes));
				}
				if (newProp.getValue() instanceof BinaryExpression
						&& ((BinaryExpression) newProp.getValue()).getLeft() instanceof AttributeExpression) {
					((BinaryExpression) newProp.getValue()).setLeft((readjustAttributeExpressionReferences(
							(AttributeExpression) ((BinaryExpression) newProp.getValue()).getLeft(), basis,
							refinementList, mergedNodes)));
				}
				if (newProp.getValue() instanceof BinaryExpression
						&& ((BinaryExpression) newProp.getValue()).getRight() instanceof AttributeExpression) {
					((BinaryExpression) newProp.getValue()).setRight((readjustAttributeExpressionReferences(
							(AttributeExpression) ((BinaryExpression) newProp.getValue()).getRight(), basis,
							refinementList, mergedNodes)));
				}

				mergedProperties.add(newProp);
			}
		}

		// re-set targets of attribute expressions to the newly copied nodes
		for (var p : mergedProperties) {
			if (p.getValue() instanceof AttributeExpression) {
				for (var n : mergedNodes) {
					if (n.getName().equals(((AttributeExpression) p.getValue()).getNode().getName())) {
						((AttributeExpression) p.getValue()).setNode(n);
					}
					if (((AttributeExpression) p.getValue()).getTarget() instanceof LinkAttributeExpTarget
							&& ((LinkAttributeExpTarget) ((AttributeExpression) p.getValue()).getTarget()).getTarget()
									.getName().equals(n.getName())) {
						((LinkAttributeExpTarget) ((AttributeExpression) p.getValue()).getTarget()).setTarget(n);
					}
				}
			}
		}

		return mergedProperties;
	}

	private AttributeExpression readjustAttributeExpressionReferences(AttributeExpression exp,
			ModelPropertyStatement basis, List<RefinementCommand> refinementList, List<ModelNodeBlock> mergedNodes) {
		String newLabel = null;
		for (var refinement : refinementList) {
			if (basis.eContainer().eContainer() instanceof SuperType
					&& refinement.getReferencedType().getName()
							.equals(((SuperType) basis.eContainer().eContainer()).getName())
					|| (basis.eContainer().eContainer() != null
							&& basis.eContainer().eContainer().eContainer() instanceof SuperType
							&& refinement.getReferencedType().getName()
									.equals(((SuperType) basis.eContainer().eContainer().eContainer()).getName()))) {
				for (var relabeling : refinement.getRelabeling()) {
					if (basis.eContainer().eContainer() instanceof ModelNodeBlock
							&& ((ModelNodeBlock) basis.eContainer().eContainer()).getName()
									.equals(relabeling.getOldLabel())) {
						newLabel = relabeling.getNewLabel();
					} else if (basis.eContainer() instanceof ModelNodeBlock
							&& ((ModelNodeBlock) basis.eContainer()).getName().equals(relabeling.getOldLabel())) {
						newLabel = relabeling.getNewLabel();
					}
				}
			}
		}
		if (newLabel == null) {
			for (var n : mergedNodes) {
				if (n.getName().equals(exp.getNode().getName())) {
					exp.setNode(n);
				}
				if (exp.getTarget() instanceof LinkAttributeExpTarget
						&& ((LinkAttributeExpTarget) exp.getTarget()).getTarget().getName().equals(n.getName())) {
					((LinkAttributeExpTarget) exp.getTarget()).setTarget(n);
				}
			}
		}
		return exp;
	}

	private boolean sameDataType(ModelPropertyStatement p1, ModelPropertyStatement p2) {
		if (p1.getType() != null && p2.getType() != null)
			return p1.getType().getType() == p2.getType().getType();
		else
			return p1.getInferredType().equals(p2.getInferredType());
	}

	/**
	 * Takes a list of nodes and merges their actions with the "black wins"
	 * principle.
	 * 
	 * @param edges      HashMap containing the edges whose actions are merged.
	 * @param typename   parameter to decide which edges have to be merged.
	 * @param targetname parameter to decide which edges have to be merged.
	 * @return Action that is the result of the merging of all edges' actions.
	 * @throws FlattenerException
	 */
	private Action mergeActionOfRelations(ArrayList<ModelRelationStatement> edges, SuperType entity)
			throws FlattenerException {
		var action = EMSLFactory.eINSTANCE.createAction();

		boolean green = false;
		boolean red = false;
		boolean black = false;
		for (var e : edges) {
			if (e.getAction() != null && e.getAction().getOp() == ActionOperator.CREATE)
				green = true;
			else if (e.getAction() != null && e.getAction().getOp() == ActionOperator.DELETE)
				red = true;
			else
				black = true;
		}
		if (green && !red && !black)
			action.setOp(ActionOperator.CREATE);
		else if (!green && red && !black)
			action.setOp(ActionOperator.DELETE);
		else if (green && red && !black)
			throw new FlattenerException(entity, FlattenerErrorType.ONLY_RED_AND_GREEN_EDGES, edges);
		else
			return null;

		return action;
	}

	/**
	 * This method merges the ModelPropertyStatements of NodeBlocks. Throws an error
	 * if the operator, value or type of the statements that are to merged are not
	 * equal.
	 * 
	 * @param nodeBlocks  that were collected and merged into the new NodeBlocks.
	 * @param mergedNodes result of the mergeNodes function. These nodeBlocks get
	 *                    the PropertyStatements.
	 * @return list of mergedNodeBlocks with the new and merged
	 *         ModelPropertyStatements.
	 * @throws FlattenerException is thrown if something went wrong during the
	 *                            merging process.
	 */
	protected List<ModelNodeBlock> mergePropertyStatementsOfNodeBlocks(SuperType entity,
			Map<String, List<ModelNodeBlock>> nodeBlocks, List<ModelNodeBlock> mergedNodes,
			List<RefinementCommand> refinementList) throws FlattenerException {
		for (var name : nodeBlocks.keySet()) {
			var nodeBlocksWithKey = nodeBlocks.get(name);
			var newProperties = new ArrayList<ModelPropertyStatement>();

			// collect ModelPropertyStatements with same name
			var propertyStatementsSortedByName = new HashMap<String, HashMap<String, ArrayList<ModelPropertyStatement>>>();
			for (var nb : nodeBlocksWithKey) {
				for (var p : nb.getProperties()) {
					if (p.getType() == null) {
						continue;
					}
					if (!propertyStatementsSortedByName.containsKey(EMSLUtil.getNameOfType(p))) {
						propertyStatementsSortedByName.put(EMSLUtil.getNameOfType(p),
								new HashMap<String, ArrayList<ModelPropertyStatement>>());
					}
					if (!propertyStatementsSortedByName.get(EMSLUtil.getNameOfType(p))
							.containsKey(p.getOp().toString())) {
						propertyStatementsSortedByName.get(EMSLUtil.getNameOfType(p)).put(p.getOp().toString(),
								new ArrayList<ModelPropertyStatement>());
					}
					propertyStatementsSortedByName.get(EMSLUtil.getNameOfType(p)).get(p.getOp().toString()).add(p);
				}
			}

			// check statements for compliance
			for (var propertyName : propertyStatementsSortedByName.keySet()) {
				for (var operator : propertyStatementsSortedByName.get(propertyName).keySet()) {
					var properties = propertyStatementsSortedByName.get(propertyName).get(operator);
					ModelPropertyStatement basis = null;
					if (properties.size() > 0) {
						basis = properties.get(0);
					}
					for (var p : properties) {
						if (!sameDataType(p, basis)) {
							if (p.eContainer().eContainer() instanceof AtomicPattern) {
								throw new FlattenerException(entity, FlattenerErrorType.NO_COMMON_SUBTYPE_OF_PROPERTIES,
										basis, p, (SuperType) p.eContainer().eContainer());
							} else {
								throw new FlattenerException(entity, FlattenerErrorType.NO_COMMON_SUBTYPE_OF_PROPERTIES,
										basis, p, (SuperType) p.eContainer().eContainer()); // incompatible types found
							}
						} else if (basis.getOp() != p.getOp()) {
							if (p.eContainer().eContainer() instanceof AtomicPattern) {
								throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_OPERATORS,
										basis, p, (SuperType) p.eContainer().eContainer());
							} else {
								throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_OPERATORS,
										basis, p, (SuperType) p.eContainer().eContainer()); // incompatible operators
																							// found
							}
						}
						compareValueOfModelPropertyStatement(entity, basis, p);
					}

					var newProp = EcoreUtil.copy(basis);

					if (basis.getValue() instanceof AttributeExpression) {
						newProp.setValue(readjustAttributeExpressionReferences((AttributeExpression) newProp.getValue(),
								basis, refinementList, mergedNodes));
					}
					if (newProp.getValue() instanceof BinaryExpression
							&& ((BinaryExpression) newProp.getValue()).getLeft() instanceof AttributeExpression) {
						((BinaryExpression) newProp.getValue()).setLeft((readjustAttributeExpressionReferences(
								(AttributeExpression) ((BinaryExpression) newProp.getValue()).getLeft(), basis,
								refinementList, mergedNodes)));
					}
					if (newProp.getValue() instanceof BinaryExpression
							&& ((BinaryExpression) newProp.getValue()).getRight() instanceof AttributeExpression) {
						((BinaryExpression) newProp.getValue()).setRight((readjustAttributeExpressionReferences(
								(AttributeExpression) ((BinaryExpression) newProp.getValue()).getRight(), basis,
								refinementList, mergedNodes)));
					}

					newProperties.add(newProp);
				}
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
	 * This method creates a new NodeBlock from the given NodeBlock that was
	 * referenced in the RefinementStatement. It also applies the relabeling of the
	 * input.
	 * 
	 * @param nb       Referenced NodeBlock that will be created.
	 * @param oldLabel Old name of the NodeBlock, must not be present if no
	 *                 relabeling is to be done.
	 * @param newLabel New name of the NodeBlock, must not be present if no
	 *                 relabeling is to be done.
	 * @return The newly created NodeBlock based on the NodeBlock passed as
	 *         parameter.
	 */
	protected ModelNodeBlock copyModelNodeBlock(ModelNodeBlock nb, RefinementCommand refinement) {
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

	/**
	 * Compares the two given PropertyStatements for equal values. If the values are
	 * not equal an according exception is thrown.
	 * 
	 * @param entity that contains the PropertyStatements.
	 * @param p1     first statement in the comparison.
	 * @param p2     second statement in the comparison.
	 * @throws FlattenerException is thrown if the values of the two statements are
	 *                            not equal.
	 */
	private void compareValueOfModelPropertyStatement(SuperType entity, ModelPropertyStatement p1,
			ModelPropertyStatement p2) throws FlattenerException {
		if (p1.equals(p2))
			return;

		if (compareProperties(p1.getValue(), p2.getValue()))
			return;

		throw new FlattenerException(entity, FlattenerErrorType.PROPS_WITH_DIFFERENT_VALUES, p1, p2, null);
	}

	private boolean compareProperties(ValueExpression v1, ValueExpression v2) {
		if (v1 instanceof PrimitiveBoolean && v2 instanceof PrimitiveBoolean
				&& (((PrimitiveBoolean) v1).isTrue() && ((PrimitiveBoolean) v2).isTrue()
						|| !((PrimitiveBoolean) v1).isTrue() && !((PrimitiveBoolean) v2).isTrue())) {
			return true;
		} else if (v1 instanceof PrimitiveInt && v2 instanceof PrimitiveInt
				&& ((PrimitiveInt) v1).getLiteral() == ((PrimitiveInt) v2).getLiteral()) {
			return true;
		} else if (v1 instanceof PrimitiveString && v2 instanceof PrimitiveString
				&& ((PrimitiveString) v1).getLiteral().equals(((PrimitiveString) v2).getLiteral())) {
			return true;
		} else if (v1 instanceof EnumValue && v2 instanceof EnumValue
				&& ((EnumValue) v1).getLiteral() == ((EnumValue) v2).getLiteral()) {
			return true;
		} else if (v1 instanceof AttributeExpression && v2 instanceof AttributeExpression) {
			var ae1 = (AttributeExpression) v1;
			var ae2 = (AttributeExpression) v2;
			return ae1.getNode().getName().equals(ae2.getNode().getName())
					&& ae1.getTarget().getAttribute().equals(ae2.getTarget().getAttribute());
		} else if (v1 instanceof BinaryExpression && v2 instanceof BinaryExpression) {
			var be1 = (BinaryExpression) v1;
			var be2 = (BinaryExpression) v2;
			return be1.getOp().equals(be2.getOp()) && compareProperties(be1.getLeft(), be2.getLeft())
					&& compareProperties(be1.getRight(), be2.getRight());
		} else if (v1 instanceof Parameter && v2 instanceof Parameter) {
			var p1 = (Parameter) v1;
			var p2 = (Parameter) v2;
			return p1.getName().equals(p2.getName());
		}

		return false;
	}

	/**
	 * Iterates over all relations in the newly flattened entity and checks if a
	 * proxy target could not be resolved to a nodeBlock from one of the
	 * superEntities.
	 * 
	 * @param entity that is to be checked if all proxies were resolved.
	 * @throws FlattenerException is thrown if a proxy was not resolved during
	 *                            flattening.
	 */
	protected void checkForResolvedProxies(SuperType entity) throws FlattenerException {
		for (var nb : dispatcher.getNodeBlocks(entity)) {
			for (var relation : nb.getRelations()) {
				if (!(relation.getTarget() instanceof ModelNodeBlock)) {
					throw new FlattenerException(entity, FlattenerErrorType.NON_RESOLVABLE_PROXY, relation);
				}
			}
		}
	}

	protected List<ModelNodeBlock> mergeEdgesOfNodeBlocks(SuperType entity,
			Map<String, List<ModelNodeBlock>> nodeBlocks, List<ModelNodeBlock> mergedNodes,
			List<RefinementCommand> refinementList) throws FlattenerException {

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
					newRel.getProperties().addAll(collectAndMergePropertyStatementsOfRelations(
							edges.get(typename).get(targetname), entity, mergedNodes, refinementList));

					// check and merge action
					newRel.setAction(mergeActionOfRelations(edges.get(typename).get(targetname), entity));

					// create new ModelRelationStatementType for the new ModelRelationStatement
					var newRelType = EMSLFactory.eINSTANCE.createModelRelationStatementType();
					newRelType.setType((edges.get(typename).get(targetname).get(0).getTypes().get(0).getType()));
					// collect all types of the edges that are to be merged (should be one type each
					// in this case) to merge the bounds
					var typesOfEdges = new ArrayList<ModelRelationStatementType>();
					for (var e : edges.get(typename).get(targetname)) {
						typesOfEdges.addAll(e.getTypes());
					}
					newRel.getTypes().add(newRelType);

					var bounds = mergeModelRelationStatementPathLimits(entity, edges.get(typename).get(targetname));
					if (bounds == null)
						throw new FlattenerException(entity, FlattenerErrorType.PATH_LENGTHS_NONSENSE, newRel);
					newRel.setLower(bounds[0]);
					newRel.setUpper(bounds[1]);

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
				}
				var bounds = mergeModelRelationStatementPathLimits(entity, namedEdges.get(n));
				if (bounds == null) {
					throw new FlattenerException(entity, FlattenerErrorType.PATH_LENGTHS_NONSENSE, newRel);
				} else {
					if (bounds[0] != null)
						newRel.setLower(bounds[0].toString());
					if (bounds[1] != null)
						newRel.setUpper(bounds[1].toString());
				}

				// merge statements and check statements for compliance
				newRel.getProperties().addAll(collectAndMergePropertyStatementsOfRelations(namedEdges.get(n), entity,
						mergedNodes, refinementList));

				// check and merge action
				newRel.setAction(mergeActionOfRelations(namedEdges.get(n), entity));

				mergedNodes.forEach(nb -> {
					if (namedEdges.get(n).get(0).getTarget() != null
							&& nb.getName().equals(namedEdges.get(n).get(0).getTarget().getName())) {
						newRel.setTarget(nb);
					} else if (namedEdges.get(n).get(0).getProxyTarget() != null
							&& nb.getName().equals(namedEdges.get(n).get(0).getProxyTarget())) {
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
	protected String[] mergeModelRelationStatementPathLimits(SuperType entity, ArrayList<ModelRelationStatement> edges)
			throws FlattenerException {
		var bounds = new String[2];
		var lowerComparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				try {
					if (o1 == null) {
						return -1;
					} else if (Integer.parseInt(o1) < Integer.parseInt(o2)) {
						return 1;
					} else {
						return -1;
					}
				} catch (NumberFormatException e) {
					if (o1.equals("*"))
						return -1;
					else if (o2.equals("*"))
						return 1;
				}
				return 0;
			}

		};

		var upperComparator = new Comparator<String>() {
			@Override
			public int compare(String o1, String o2) {
				try {
					if (o1 == null) {
						return 1;
					} else if (Integer.parseInt(o1) < Integer.parseInt(o2)) {
						return -1;
					} else {
						return 1;
					}
				} catch (NumberFormatException e) {
					if (o1.equals("*"))
						return 1;
					else if (o2.equals("*"))
						return -1;
				}
				return 0;
			}

		};

		var lowerBoundQueue = new PriorityQueue<String>(lowerComparator);
		var upperBoundQueue = new PriorityQueue<String>(upperComparator);

		for (var rel : edges) {
			if (rel.getLower() != null)
				lowerBoundQueue.add(rel.getLower());
			if (rel.getUpper() != null)
				upperBoundQueue.add(rel.getUpper());
		}
		bounds[0] = lowerBoundQueue.peek();
		bounds[1] = upperBoundQueue.peek();

		try {
			if (Integer.parseInt(bounds[0]) > Integer.parseInt(bounds[1]))
				return null;
		} catch (NumberFormatException e) {
			if (bounds[1] != null && !bounds[1].equals("*"))
				return null;
		}

		return bounds;
	}
}
