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
import org.emoflon.neo.emsl.eMSL.AttributeCondition;
import org.emoflon.neo.emsl.eMSL.AttributeExpression;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.EnumValue;
import org.emoflon.neo.emsl.eMSL.LinkAttributeExpTarget;
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
import org.emoflon.neo.emsl.eMSL.Value;
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

			// 4. step: merge attribute conditions
			mergeAttributeConditions(entity, refinements);

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

			var flattenedSuperEntity = flatten(r.getReferencedType(), alreadyRefinedEntityNamesCopy);

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
				mergePropertyStatementsOfNodeBlocks(entity, nodeBlocks, mergedNodes));
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
	 * Collects and merges the property statements of the given edges.
	 * 
	 * @param edges  whose properties will be merged.
	 * @param entity that contains the properties (directly or indirectly).
	 * @return HashSet containing the merged ModelPropertyStatements.
	 * @throws FlattenerException is thrown if two properties are not mergeable.
	 */
	private HashSet<ModelPropertyStatement> collectAndMergePropertyStatementsOfRelations(
			ArrayList<ModelRelationStatement> edges, SuperType entity) throws FlattenerException {
		var properties = new HashMap<String, ArrayList<ModelPropertyStatement>>();
		var mergedProperties = new HashSet<ModelPropertyStatement>();

		// collect PropertyStatements of Edges
		if (edges != null) {
			for (var e : edges) {
				// collect propertyStatements
				e.getProperties().forEach(p -> {
					if (!properties.containsKey(p.getType().getName())) {
						properties.put(p.getType().getName(), new ArrayList<ModelPropertyStatement>());
					}
					properties.get(p.getType().getName()).add(p);
				});
			}

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
				mergedProperties.add(EcoreUtil.copy(basis));
			}
		}

		return mergedProperties;
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
	private Action mergeActionOfRelations(ArrayList<ModelRelationStatement> edges, SuperType entity) throws FlattenerException {
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
			Map<String, List<ModelNodeBlock>> nodeBlocks, List<ModelNodeBlock> mergedNodes) throws FlattenerException {
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
						propertyStatementsSortedByName.put(p.getType().getName(),
								new ArrayList<ModelPropertyStatement>());
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
					if (p.getType().getType() != basis.getType().getType()) {
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
									basis, p, (SuperType) p.eContainer().eContainer()); // incompatible operators found
						}
					}
					compareValueOfModelPropertyStatement(entity, basis, p);
				}
				newProperties.add(EcoreUtil.copy(basis));
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
	 * This method merges the attribute conditions given as a list. It searches for
	 * duplicates in all collected conditions and removes them.
	 * 
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
	 * 
	 * @param val1 first value to be compared.
	 * @param val2 second value to be compared.
	 * @return true if val1 and val2 are equal, else false.
	 */
	@SuppressWarnings("unlikely-arg-type")
	private boolean equalValues(Value val1, Value val2) {
		if (val1.eClass() != val2.eClass())
			return false;
		else if (val1 instanceof AttributeExpression && val2 instanceof AttributeExpression
				&& ((AttributeExpression) val1).getNode().getName()
						.equals(((AttributeExpression) val2).getNode().getName())) {
			if (((AttributeExpression) val1).getTarget() instanceof LinkAttributeExpTarget
					&& ((AttributeExpression) val2).getTarget() instanceof LinkAttributeExpTarget) {
				if (((LinkAttributeExpTarget) ((AttributeExpression) val1).getTarget()).getLink()
						.equals(((LinkAttributeExpTarget) ((AttributeExpression) val2).getTarget()).getLink())
						&& ((LinkAttributeExpTarget) ((AttributeExpression) val1).getTarget())
								.getAttribute() == ((LinkAttributeExpTarget) ((AttributeExpression) val2).getTarget())
										.getAttribute()) {
					return true;
				}
			} else if (((AttributeExpression) val1).getTarget() instanceof NodeAttributeExpTarget
					&& ((AttributeExpression) val2).getTarget() instanceof NodeAttributeExpTarget
					&& ((NodeAttributeExpTarget) ((AttributeExpression) val1).getTarget())
							.getAttribute() == ((NodeAttributeExpTarget) ((AttributeExpression) val2).getTarget())
									.getAttribute()) {
				return true;
			}
		} else if (val1 instanceof EnumValue && val2 instanceof EnumValue
				&& ((EnumValue) val1).getLiteral().equals(((EnumValue) val2).getLiteral())) {
			return true;
		} else if (val1 instanceof PrimitiveInt && val2 instanceof PrimitiveInt
				&& ((PrimitiveInt) val1).getLiteral() == ((PrimitiveInt) val2).getLiteral()) {
			return true;
		} else if (val1 instanceof PrimitiveBoolean && val2 instanceof PrimitiveBoolean
				&& (((PrimitiveBoolean) val1).isTrue() && ((PrimitiveBoolean) val2).isTrue()
						|| !((PrimitiveBoolean) val1).isTrue() && !((PrimitiveBoolean) val2).isTrue())) {
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
		} else if (p2.eContainer().eContainer() instanceof Rule || p2.eContainer().eContainer() instanceof Model) {
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

	protected void mergeAttributeConditions(SuperType entity, List<RefinementCommand> refinements) {
		var collectedAttributeConditions = new ArrayList<AttributeCondition>();
		collectedAttributeConditions.addAll(dispatcher.getAttributeConditions(entity));
		for (var s : refinements) {
			if (s.getReferencedType() instanceof AtomicPattern) {
				((AtomicPattern) s.getReferencedType()).getAttributeConditions()
						.forEach(c -> collectedAttributeConditions.add(EcoreUtil.copy(c)));
			} else {
				collectedAttributeConditions.addAll(dispatcher.getAttributeConditions(s.getReferencedType()));
			}
		}
		var mergedAttributeConditions = mergeAttributeConditions(collectedAttributeConditions);
		dispatcher.getAttributeConditions(entity).clear();
		mergedAttributeConditions.forEach(c -> dispatcher.getAttributeConditions(entity).add(EcoreUtil.copy(c)));
	}

	protected List<ModelNodeBlock> mergeEdgesOfNodeBlocks(SuperType entity,
			Map<String, List<ModelNodeBlock>> nodeBlocks, List<ModelNodeBlock> mergedNodes) throws FlattenerException {

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
				newRel.setAction(mergeActionOfRelations(namedEdges.get(n), entity));

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
	protected Object[] mergeModelRelationStatementPathLimits(SuperType entity,
			ArrayList<ModelRelationStatementType> types) throws FlattenerException {
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
