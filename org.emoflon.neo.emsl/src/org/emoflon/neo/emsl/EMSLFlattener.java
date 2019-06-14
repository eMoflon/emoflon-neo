package org.emoflon.neo.emsl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.eclipse.emf.common.util.EList;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.Entity;
import org.emoflon.neo.emsl.eMSL.EnumValue;
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.PrimitiveBoolean;
import org.emoflon.neo.emsl.eMSL.PrimitiveInt;
import org.emoflon.neo.emsl.eMSL.PrimitiveString;
import org.emoflon.neo.emsl.eMSL.RefinementCommand;
import org.emoflon.neo.emsl.util.FlattenerErrorType;
import org.emoflon.neo.emsl.util.FlattenerException;

public class EMSLFlattener {

	public EMSLFlattener() {
		
	}
	
	/**
	 * Returns the flattened Pattern, i.e. (for now) collects all NodeBlocks from the superEntities.
	 * 
	 * @param pattern that should be flattened.
	 * @param alreadyRefinedPatternNames list of names of pattern that have already appeared in the refinement path (against loops).
	 * @return the flattened pattern.
	 * @throws FlattenerException is thrown if an error during the flattening process occurs.
	 */
	public Pattern flattenPattern(Pattern p) throws FlattenerException {
		var pattern = p.getBody();
		var<RefinementCommand> refinements = pattern.getSuperRefinementTypes();
		var<String> alreadyRefinedPatternNames = new ArrayList<String>();
		// TODO [Maximilian]: find out why NodeBlocks are not deleted if removed from the file; implement their removal;
		
		// check if anything has to be done, if not return
		if (refinements.isEmpty())
			return p;
		
		var<String, ArrayList<ModelNodeBlock>> collectedNodeBlocks = collectNodes(p, refinements, alreadyRefinedPatternNames);
		
		pattern.getNodeBlocks().forEach(nb -> {
			if (collectedNodeBlocks.keySet().contains(nb.getName())) {
				collectedNodeBlocks.get(nb.getName()).add(nb);
			}
			else {
				var<ModelNodeBlock> tmp = new ArrayList<ModelNodeBlock>();
				tmp.add(nb);
				collectedNodeBlocks.put(nb.getName(), tmp);
			}
		});

		var mergedNodes = mergeNodes(p, refinements, collectedNodeBlocks);
		
		pattern.getNodeBlocks().clear();
		pattern.getNodeBlocks().addAll((mergedNodes));
		
		p.setBody(pattern);
		return p;
	}
	
	/**
	 * This method creates all NodeBlocks that have to be imported into the Pattern from the SuperEntities.
	 * @param refinementList List of RefinementCommands holding all pattern that should be refined.
	 * @param alreadyRefinedPatternNames List of Pattern names that have already appeared in the refinement path (against loops).
	 * @return HashMap of NodeBlocks mapped to their name that have to be added to the refining Pattern.
	 * @throws FlattenerException is thrown if an error occurs during collecting the nodes, like an infinite loop is detected
	 */
	private HashMap<String, ArrayList<ModelNodeBlock>> collectNodes(Pattern p, EList<RefinementCommand> refinementList, ArrayList<String> alreadyRefinedPatternNames) throws FlattenerException {
		var<String, ArrayList<ModelNodeBlock>> nodeBlocks = new HashMap<String, ArrayList<ModelNodeBlock>>();
		
		for (var r : refinementList) {			
			if (r.getReferencedType() instanceof AtomicPattern) {				
				if (alreadyRefinedPatternNames.contains(((AtomicPattern) r.getReferencedType()).getName())) {
					// check for cycles in refinements, if found: throw unusual exception
					throw new FlattenerException(p, FlattenerErrorType.INFINITE_LOOP, alreadyRefinedPatternNames);
				}
				else {
					var<String> alreadyRefined = alreadyRefinedPatternNames;
					alreadyRefined.add(((AtomicPattern) r.getReferencedType()).getName());
					for (var nb : ((AtomicPattern) r.getReferencedType()).getNodeBlocks()) {
						
						// create new NodeBlock
						var newNb = createNewModelNodeBlock(nb, r);
						
						// add nodeBlock to list according to its name
						if (!nodeBlocks.containsKey(newNb.getName())) {
							var<ModelNodeBlock> newList = new ArrayList<ModelNodeBlock>();
							newList.add(newNb);
							nodeBlocks.put(newNb.getName(), newList);
						}
						else {
							nodeBlocks.get(newNb.getName()).add(newNb);
						}
					}
				}
			}
			
			// recursively collect nodes
			if (r.getReferencedType() instanceof AtomicPattern && ((AtomicPattern) r.getReferencedType()).getSuperRefinementTypes() != null) {
				var tmp = collectNodes(p, ((AtomicPattern) r.getReferencedType()).getSuperRefinementTypes(), alreadyRefinedPatternNames);
				tmp.forEach((key, value) -> {
					if (nodeBlocks.containsKey(key)) {
						tmp.get(key).addAll(tmp.get(key));
					}
					else {
						nodeBlocks.put(key, value);
					}
				});
			}
		}
		
		for (var name : nodeBlocks.keySet()) {
			for (var n : nodeBlocks.get(name)) {
				for (var rel : n.getRelations()) {
					if (nodeBlocks.containsKey(rel.getTarget().getName()) && nodeBlocks.get(rel.getTarget().getName()) != null) {
						rel.setTarget(nodeBlocks.get(rel.getTarget().getName()).get(0));
					} else {
						// relabeling happened, find new target
						for (var r : refinementList) {
							for (var relabeling : r.getRelabeling()) {
								if (nodeBlocks.containsKey(relabeling.getNewLabel()) && nodeBlocks.get(relabeling.getNewLabel()) != null) {
									rel.setTarget(nodeBlocks.get(relabeling.getNewLabel()).get(0));
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
			// merge nodes with the same name
			
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
						//e.printStackTrace();
						//throw new FlattenerException(entity, FlattenerErrorType.NO_COMMON_SUBTYPE_OF_NODES, new ArrayList<String>());
						throw new FlattenerException(entity, FlattenerErrorType.NO_COMMON_SUBTYPE_OF_NODES, nb);
					}
				}
			}
			
			// create new NodeBlock that will be added to the pattern
			var newNb = EMSLFactory.eINSTANCE.createModelNodeBlock();
			newNb.setType(nodeBlockTypeQueue.peek());
			newNb.setName(name);
			
			mergedNodes.add(newNb);
		}
		
		return mergeEdgesOfNodeBlocks(entity, refinementList, nodeBlocks, mergePropertyStatementsOfNodeBlocks(entity, nodeBlocks, mergedNodes));
	}
	
	/**
	 * This method takes a list of collected NodeBlocks that were collected from all the refinements, and a list of merged nodes
	 * and adds the merged RelationStatements to the mergedNodes which are then returned.
	 * @param nodeBlocks that were collected from the refinements.
	 * @param mergedNodes nodeBlocks that were except for the relationStatements already merged.
	 * @return list of ModelNodeBlocks that now have merged RelationStatements.
	 * @throws FlattenerException is thrown if something went wrong during the merging process.
	 */
	private ArrayList<ModelNodeBlock> mergeEdgesOfNodeBlocks(Entity entity, EList<RefinementCommand> refinementList, HashMap<String, ArrayList<ModelNodeBlock>> nodeBlocks, ArrayList<ModelNodeBlock> mergedNodes) throws FlattenerException {
		
		// collect all edges in hashmap; first key is type name, second is target name
		for (var name : nodeBlocks.keySet()) {
			var edges = new HashMap<String, HashMap<String, ArrayList<ModelRelationStatement>>>();
			for (var nb : nodeBlocks.get(name)) {
				for (var rel : nb.getRelations()) {
					if (!edges.containsKey(rel.getType().getName())) {
						edges.put(rel.getType().getName(), new HashMap<String, ArrayList<ModelRelationStatement>>());
					}
					if (!edges.get(rel.getType().getName()).containsKey(rel.getTarget().getName())) {
						edges.get(rel.getType().getName()).put(rel.getTarget().getName(), new ArrayList<ModelRelationStatement>());
					}
					edges.get(rel.getType().getName()).get(rel.getTarget().getName()).add(rel);
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
					// merge statements
					// check statements for compliance
					for (var propertyName : properties.keySet()) {
						var props = properties.get(propertyName);
						ModelPropertyStatement basis = null;
						if (properties.size() > 0) {
							basis = props.get(0);
						}
						for (var p : props) {
							if (p.getType().getType() != basis.getType().getType() || 
									basis.getOp() != p.getOp()) 
							{
								throw new FlattenerException(entity, FlattenerErrorType.NO_COMMON_SUBTYPE_OF_PROPERTIES, basis, p); // incompatible types/operands found
							}
						}
						var newProp = EMSLFactory.eINSTANCE.createModelPropertyStatement();
						newProp.setOp(basis.getOp());
						newProp.setType(basis.getType());
						newProp.setValue(basis.getValue());
						newRel.getProperties().add(newProp);
					}
					
					newRel.setType(edges.get(typename).get(targetname).get(0).getType());
					mergedNodes.forEach(nb -> {
						if (nb.getName().equals(targetname)) {
							newRel.setTarget(nb);
						}
						if (nb.getName().equals(name)) {
							nb.getRelations().add(newRel);
						}
					});
					for (var r : newRel.getProperties()) {
						r.getType();
					}
				}
			}
		}
		
		return mergedNodes;
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
					if (!(p.getType().getType() == basis.getType().getType() && 
							basis.getOp() == p.getOp())) 
					{
						throw new FlattenerException(entity, FlattenerErrorType.NO_COMMON_SUBTYPE_OF_PROPERTIES, basis, p); // incompatible types found
					}
				}
				var newProp = EMSLFactory.eINSTANCE.createModelPropertyStatement();
				newProp.setOp(basis.getOp());
				newProp.setType(basis.getType());
				newProp.setValue(basis.getValue());
				newProperties.add(newProp);
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
	 * This method creates a new NodeBlock from the given NodeBlock that was referenced in the RefinementStatement. It also applies
	 * the relabeling of the input.
	 * @param nb Referenced NodeBlock that will be created.
	 * @param oldLabel Old name of the NodeBlock, must not be present if no relabeling is to be done.
	 * @param newLabel New name of the NodeBlock, must not be present if no relabeling is to be done.
	 * @return The newly created NodeBlock based on the NodeBlock passed as parameter.
	 */
	private ModelNodeBlock createNewModelNodeBlock(ModelNodeBlock nb, RefinementCommand refinement) {
		var newNb = EMSLFactory.eINSTANCE.createModelNodeBlock();
		
		// apply relabeling
		if (refinement.getRelabeling() != null) {
			for (var r : refinement.getRelabeling()) {
				if (r.getOldLabel() != null 
						&& r.getOldLabel().eContainer() != null
						&& r.getOldLabel().eContainer() instanceof AtomicPattern
						&& ((AtomicPattern) r.getOldLabel().eContainer()).getName() != null 
						&& nb.getName().equals(((ModelNodeBlock) r.getOldLabel()).getName())) {
					newNb.setName(r.getNewLabel());
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
			newRel.setAction(rel.getAction());
			newRel.setType(rel.getType());
			newRel.setTarget(rel.getTarget());			
			newNb.getRelations().add(newRel);
		}
		
		// add properties to new nodeblock
		for (var prop : nb.getProperties()) {
			var newProp = EMSLFactory.eINSTANCE.createModelPropertyStatement();
			newProp.setOp(prop.getOp());
			newProp.setType(prop.getType());
			
			// create new Value for Property
			if (prop.getValue() instanceof PrimitiveBoolean) {
				newProp.setValue(EMSLFactory.eINSTANCE.createPrimitiveBoolean());
				if (((PrimitiveBoolean) prop.getValue()).isTrue())
					((PrimitiveBoolean) newProp.getValue()).setTrue(true);
				else 
					((PrimitiveBoolean) newProp.getValue()).setTrue(false);
			} else if (prop.getValue() instanceof PrimitiveInt) {
				newProp.setValue(EMSLFactory.eINSTANCE.createPrimitiveInt());
				((PrimitiveInt) newProp.getValue()).setLiteral(((PrimitiveInt) prop.getValue()).getLiteral());
			} else if (prop.getValue() instanceof PrimitiveString) {
				newProp.setValue(EMSLFactory.eINSTANCE.createPrimitiveString());
				((PrimitiveString) newProp.getValue()).setLiteral(((PrimitiveString) prop.getValue()).getLiteral());
			} else if (prop.getValue() instanceof EnumValue) {
				prop.getValue();
				newProp.setValue(EMSLFactory.eINSTANCE.createEnumValue());
				((EnumValue) newProp.getValue()).setLiteral(((EnumValue) prop.getValue()).getLiteral());
			}
			
			newNb.getProperties().add(newProp);
		}
		
		return newNb;
	}
}
