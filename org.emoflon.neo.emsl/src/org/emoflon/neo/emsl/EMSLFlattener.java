package org.emoflon.neo.emsl;

import java.rmi.dgc.Lease;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

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
		var refinements = pattern.getSuperRefinementTypes();
		
		// IDEA: recursively collect all pattern that should be refined
		
		// check if anything has to be done, if not return
		if (refinements.isEmpty())
			return pattern;
		
		// IDEA: collect NodeBlocks of the to-be-refined pattern
		//collectNodes(refinements, alreadyRefinedPatternNames).values().forEach(n -> pattern.getNodeBlocks().addAll(n));
		
		var createdNodeBlocks = collectNodes(refinements, alreadyRefinedPatternNames);
		
		pattern.getNodeBlocks().forEach(nb -> {
			if (createdNodeBlocks.keySet().contains(nb.getName())) {
				createdNodeBlocks.get(nb.getName()).add(nb);
			}
			else {
				var foo = new ArrayList<ModelNodeBlock>();
				foo.add(nb);
				createdNodeBlocks.put(nb.getName(), foo);
			}
		});
		
		//pattern.getNodeBlocks().clear();
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
		var nodeBlocks = new HashMap<String, ArrayList<ModelNodeBlock>>();
		
		for (var r : refinementList) {			
			if (r.getReferencedType() instanceof AtomicPattern) {				
				if (alreadyRefinedPatternNames.toString().contains(((AtomicPattern) r.getReferencedType()).getName())) {
					// check for cycles in refinements, if found: throw unusual exception; TODO [Maximilian]: change to more appropriate error handling
					throw new RuntimeException();
				}
				else {
					var alreadyRefined = alreadyRefinedPatternNames;
					alreadyRefined.add(((AtomicPattern) r.getReferencedType()).getName());
					for (var nb : ((AtomicPattern) r.getReferencedType()).getNodeBlocks()) {
						
						// create new NodeBlock
						var newNb = createNewModelNodeBlock(nb, r);
						
						// add nodeBlock to list according to its name
						if (!nodeBlocks.containsKey(newNb.getName())) {
							var newList = new ArrayList<ModelNodeBlock>();
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
			if (((AtomicPattern) r.getReferencedType()).getSuperRefinementTypes() != null) {
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
		var relationStatements = new ArrayList<ModelRelationStatement>();
		
		// get edges, maybe call this function from the collectNodes to connect there already instead of later;
		// might contradict the idea to first collect everything and the construct the union
		
		return relationStatements;
	}
	
	private ArrayList<ModelNodeBlock> mergeAllNodes(HashMap<String, ArrayList<ModelNodeBlock>> nodeBlocks) {
		var mergedNodes = new ArrayList<ModelNodeBlock>();
		
		// take all nodeBlocks with the same name/key out of the HashMap and merge
		for (var name : nodeBlocks.keySet()) {
			var blocksWithKey = nodeBlocks.get(name);
			// merge nodes with the same name
			var newNb = EMSLFactory.eINSTANCE.createModelNodeBlock();
			mergedNodes.add(newNb);
			newNb.setName(name);
				
			// collect types
			var types = new ArrayList<MetamodelNodeBlock>();
			for (var nb : blocksWithKey) {
				if (!types.contains(nb.getType())) {
					types.add(nb.getType());
				}
			}
			
			// choose least common subtype
			// take first entry
			if (!types.isEmpty()) {
				MetamodelNodeBlock leastCommonSubtype = types.remove(0);
				// go over all other types to check if equal or common sub/supertype
				for (var nb : types) {
					if (!leastCommonSubtype.getName().equals(nb.getName())) {
						// name not equal, check if sub-/supertype
						if (nb.getSuperTypes().contains(leastCommonSubtype)) {
							// leastCommonSubtype is superType of nb, hence, new leastType found, check if common
							if (validateSuperType(types, nb)) {
								leastCommonSubtype = nb;
							}
						}
						else if (leastCommonSubtype.getSuperTypes().contains(nb.getSuperTypes())) {
							// is subType, hence, if leastCommonSubtype already accepted, nothing more to do
						}
					}
					else {
						// name is equal, so already least common subtype -> nothing more to do
					}
				}
				newNb.setType(leastCommonSubtype);
			}
			
		}
		return mergedNodes;
	}
	
	/**
	 * This method validates if the given MetamodelNodeBlock is a common subtype of the types given in the parameters.
	 * @param types List of MetamodelNodeBlocks that are to be checked if the applicant is a common subtype.
	 * @param applicant MetamodelNodeBlock that is a candidate for the new leastCommonSubtype.
	 * @return true if the given applicant is a common subtype.
	 */
	private boolean validateSuperType(ArrayList<MetamodelNodeBlock> types, MetamodelNodeBlock applicant) {
		boolean val = true;
		
		for (var t : types) {
			if (t.getName() != applicant.getName() && !t.getSuperTypes().contains(applicant)) {
				val = false;
			}
		}
		
		return val;
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
	
	// TODO[Maximilian] clone(ModelNodeBlock nb) recursively create instances
}
