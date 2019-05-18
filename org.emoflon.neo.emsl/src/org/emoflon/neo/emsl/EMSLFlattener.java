package org.emoflon.neo.emsl;

import java.util.ArrayList;

import org.eclipse.emf.common.util.EList;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
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
		pattern.getNodeBlocks().addAll(collectNodes(refinements, alreadyRefinedPatternNames));
		
		// IDEA: collect edges and connect to correct nodeblock-objects
		var collectedEdges = collectEdges(refinements);
		
		return pattern;
	}
	
	/**
	 * This method creates all NodeBlocks that have to be imported into the Pattern from the SuperEntities.
	 * @param refinementList List of RefinementCommands holding all pattern that should be refined.
	 * @param alreadyRefinedPatternNames List of Pattern names that have already appeared in the refinement path (against loops).
	 * @return List of NodeBlocks that have to be added to the refining Pattern.
	 */
	private ArrayList<ModelNodeBlock> collectNodes(EList<RefinementCommand> refinementList, ArrayList<String> alreadyRefinedPatternNames) {
		var nodeBlocks = new ArrayList<ModelNodeBlock>();
		
		// collect all NodeBlocks from the flattened refinements
		for (var r : refinementList) {			
			// TODO [Maximilian]: initialize nodeBlocks with data from parent pattern
			if ((r.getReferencedType() instanceof AtomicPattern)) {
				if (alreadyRefinedPatternNames.toString().contains(((AtomicPattern) r.getReferencedType()).getName())) {
					// check for cycles in refinements, if found throw unusual exception; TODO [Maximilian]: change to more appropriate error handling
					throw new RuntimeException();
				}
				else {
					var alreadyRefined = alreadyRefinedPatternNames;
					alreadyRefined.add(((AtomicPattern) r.getReferencedType()).getName());
					for (var nb : (flattenPattern((AtomicPattern) r.getReferencedType(), alreadyRefined).getNodeBlocks())) {
						// create new NodeBlock TODO [Maximilian]: add relabeling stuff to CORRECTLY initialize new nodeblock
						var newNb = EMSLFactory.eINSTANCE.createModelNodeBlock();
						newNb.setName(nb.getName());
						newNb.setType(nb.getType());
						newNb.setAction(nb.getAction());
						nodeBlocks.add(newNb);
					}
				}
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
}
