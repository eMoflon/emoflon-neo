package org.emoflon.neo.emsl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.PriorityQueue;

import org.eclipse.emf.common.util.EList;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.eMSL.RefinementCommand;

public class EMSLFlattener {

	public EMSLFlattener() {
		
	}
	
	/**
	 * Returns the flattened Pattern, i.e. (for now) collects all NodeBlocks from the superEntities.
	 * 
	 * @param pattern that should be flattened.
	 * @param alreadyRefinedPatternNames list of names of pattern that have already appeared in the refinement path (against loops).
	 * @return the flattened pattern.
	 */
	public AtomicPattern flattenPattern(AtomicPattern pattern, ArrayList<String> alreadyRefinedPatternNames) {
		var<RefinementCommand> refinements = pattern.getSuperRefinementTypes();
		
		// TODO [Maximilian]: find out why NodeBlocks are not deleted if removed from the file; implement their removal; find out why Types are lost (e.g. sokoban:?)
		
		// check if anything has to be done, if not return
		if (refinements.isEmpty())
			return pattern;
		
		// IDEA: collect NodeBlocks of the to-be-refined pattern
		//collectNodes(refinements, alreadyRefinedPatternNames).values().forEach(n -> pattern.getNodeBlocks().addAll(n));
		
		var<String, ArrayList<ModelNodeBlock>> createdNodeBlocks = collectNodes(refinements, alreadyRefinedPatternNames);
		
		pattern.getNodeBlocks().forEach(nb -> {
			if (createdNodeBlocks.keySet().contains(nb.getName())) {
				createdNodeBlocks.get(nb.getName()).add(nb);
			}
			else {
				var<ModelNodeBlock> foo = new ArrayList<ModelNodeBlock>();
				foo.add(nb);
				createdNodeBlocks.put(nb.getName(), foo);
			}
		});

		var mergedNodes = mergeAllNodes(createdNodeBlocks);
		pattern.getNodeBlocks().clear();
		pattern.getNodeBlocks().addAll((mergedNodes));
		
		// IDEA: collect edges and connect to correct nodeblock-objects
		//var collectedEdges = collectEdges(refinements);
		
		return pattern;
	}
	
	/**
	 * This method creates all NodeBlocks that have to be imported into the Pattern from the SuperEntities.
	 * @param refinementList List of RefinementCommands holding all pattern that should be refined.
	 * @param alreadyRefinedPatternNames List of Pattern names that have already appeared in the refinement path (against loops).
	 * @return HashMap of NodeBlocks mapped to their name that have to be added to the refining Pattern.
	 */
	private HashMap<String, ArrayList<ModelNodeBlock>> collectNodes(EList<RefinementCommand> refinementList, ArrayList<String> alreadyRefinedPatternNames) {
		var<String, ArrayList<ModelNodeBlock>> nodeBlocks = new HashMap<String, ArrayList<ModelNodeBlock>>();
		
		for (var r : refinementList) {			
			if (r.getReferencedType() instanceof AtomicPattern) {				
				if (alreadyRefinedPatternNames.toString().contains(((AtomicPattern) r.getReferencedType()).getName())) {
					// check for cycles in refinements, if found: throw unusual exception; TODO [Maximilian]: change to more appropriate error handling
					throw new RuntimeException();
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
				var tmp = collectNodes(((AtomicPattern) r.getReferencedType()).getSuperRefinementTypes(), alreadyRefinedPatternNames);
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
		
		return nodeBlocks;
	}
	
	/**
	 * This method collects all RelationStatements from the NodeBlocks. Those RelationStatements are used after building the
	 * union to correctly connect two NodeBlocks.
	 * @param refinementList List of RefinementCommands holding all pattern that should be refined.
	 * @return List of RelationStatements that appear in the NodeBlocks of the Pattern listed in the RefinementCommands.
	 */
	private ArrayList<ModelRelationStatement> collectEdges(EList<RefinementCommand> refinementList) {
		var<ModelRelationStatement> relationStatements = new ArrayList<ModelRelationStatement>();
		
		// get edges, maybe call this function from the collectNodes to connect there already instead of later;
		// might contradict the idea to first collect everything and the construct the union
		
		return relationStatements;
	}
	
	/**
	 * This method takes a HashMap of lists of NodeBlocks mapped to the names of the contained NodeBlocks.
	 * All of those NodeBlocks with the same name (mapping) are then merged into one that is added to the model. During
	 * the merging the least common subtype of all NodeBlocks with the same name is searched for.
	 * @param nodeBlocks HashMap of Lists of ModelNodeBlocks that are mapped to the names of the NodeBlocks contained in such a list.
	 * @return ArrayList of ModelNodeBlocks that only contains the ModelNodeBlocks that were created during merging.
	 */
	private ArrayList<ModelNodeBlock> mergeAllNodes(HashMap<String, ArrayList<ModelNodeBlock>> nodeBlocks) {
		var<ModelNodeBlock> mergedNodes = new ArrayList<ModelNodeBlock>();
		
		// take all nodeBlocks with the same name/key out of the HashMap and merge
		for (var name : nodeBlocks.keySet()) {
			var blocksWithKey = nodeBlocks.get(name);
			// merge nodes with the same name
			var newNb = EMSLFactory.eINSTANCE.createModelNodeBlock();
			mergedNodes.add(newNb);
			newNb.setName(name);
			
			Comparator<MetamodelNodeBlock> comparator = new Comparator<MetamodelNodeBlock>() {
				@Override
				public int compare(MetamodelNodeBlock o1, MetamodelNodeBlock o2) {
					if (o1.getSuperTypes().contains(o2) || recursiveContainment(o1, o2, false)) {
						return 1;
					} else if (o2.getSuperTypes().contains(o1) || recursiveContainment(o2, o1, false)) {
						return -1;
					} else {
						return 0;
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
			PriorityQueue<MetamodelNodeBlock> queue = new PriorityQueue<MetamodelNodeBlock>(comparator);
			
			// collect types
			for (var nb : blocksWithKey) {
				if (nb.getType() != null) {
					queue.add(nb.getType());
				}
			}			
			newNb.setType(queue.peek());			
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
		if (newNb.getName() == null) {
			newNb.setName(nb.getName());
		}
		
		newNb.setType(nb.getType());
		newNb.setAction(nb.getAction());
		
		return newNb;
	}
}
