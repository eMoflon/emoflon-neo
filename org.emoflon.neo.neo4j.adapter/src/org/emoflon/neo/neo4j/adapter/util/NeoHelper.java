package org.emoflon.neo.neo4j.adapter.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.refinement.EMSLFlattener;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.neo4j.adapter.common.NeoNode;
import org.emoflon.neo.neo4j.adapter.common.NeoRelation;
import org.emoflon.neo.neo4j.adapter.models.NeoCoreBuilder;

/**
 * Helper class for managing nodes, relations and their unique names in queries.
 * 
 * @author Jannik Hinz
 *
 */
public class NeoHelper {

	// Note: nodes and relations are stored in one list at a time
	private Collection<String> matchElements;
	private Collection<String> optionalElements;

	private int cCount;
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	/**
	 * initialize Helper
	 */
	public NeoHelper() {
		this.matchElements = new HashSet<String>();
		this.optionalElements = new HashSet<String>();
		this.cCount = 0;
	}

	/**
	 * Increases the constraint counter (new numbering in unique id of nodes and
	 * relation)
	 * 
	 * @return cCount int the new constraint unique id
	 */
	public int addConstraint() {
		return cCount++;
	}

	/**
	 * Creates an new Node in the Node List of MATCH clauses if it is not contain
	 * already in the list and return the unique name of the node
	 * 
	 * @param name of the new node variable
	 * @return name of the new node variable for including in queries
	 */
	public String newPatternNode(String name) {
		matchElements.add(name);
		return name;
	}

	/**
	 * Creates an new Relation in the Node List of MATCH clauses if it is not
	 * contain already in the list and return the unique name of the relation
	 * 
	 * @param name   of the source node from the relation
	 * @param index  of the relation of one node
	 * @param relVar of the relation variable
	 * @param toName of the target node of the relation
	 * @return name of the new relation variable for including in queries
	 */
	public String newPatternRelation(String name, int index, List<String> relVar, String toName) {
		var relName = EMSLUtil.relationNameConvention(name, relVar, toName, index);
		matchElements.add(relName);
		return relName;
	}

	/**
	 * Creates an new Node in the Node List of OPTIONAL MATCH clauses if it is not
	 * contain already in the list and return the unique name of the node
	 * 
	 * @param name of the new node variable
	 * @return name of the new node variable for including in queries
	 */
	public String newConstraintNode(String name) {
		if (matchElements.contains(name)) {
			return name;
		} else {
			optionalElements.add(name + "_" + cCount);
			return name + "_" + cCount;
		}
	}

	/**
	 * Creates an new Relation in the Node List of MATCH clauses if it is not
	 * contain already in the list and return the unique name of the relation
	 * 
	 * @param name   of the source node from the relation
	 * @param index  of the relation of one node
	 * @param relVar of the relation variable
	 * @param toName of the target node of the relation
	 * @return name of the new relation variable for including in queries
	 */
	public String newConstraintReference(String name, int index, List<String> relVar, String toName) {
		var relName = EMSLUtil.relationNameConvention(name, relVar, toName, index);
		if (matchElements.contains(relName)) {
			return relName;
		} else {
			optionalElements.add(relName + "_" + cCount);
			return relName + "_" + cCount;
		}
	}

	/**
	 * Return the List of all Nodes from MATCH and OPTIONAL MATCH (union, but no
	 * duplicates) clauses
	 * 
	 * @return all Nodes from pattern and constraints
	 */
	public Collection<String> getNodes() {
		var list = new HashSet<>(matchElements);
		list.addAll(optionalElements);
		return list;
	}

	/**
	 * Return the List of all Nodes from MATCH clauses
	 * 
	 * @return all Nodes from the pattern
	 */
	public Collection<String> getMatchNodes() {
		return matchElements;
	}

	/**
	 * Return the List of all Nodes from OPTIONAL MATCH clauses
	 * 
	 * @return all Nodes from the constraints
	 */
	public Collection<String> getOptionalMatchNodes() {
		return optionalElements;
	}

	/**
	 * Creates and extracts all necessary information data from the Atomic Pattern.
	 * Create new NeoNode for any AtomicPattern node and corresponding add Relations
	 * and Properties and save them to the node in an node list.
	 * 
	 * @param mnb Collection of all nodes of a AtomicPattern
	 * @return NeoNode ArrayList of all Nodes and their Relation and Properties of
	 *         the AtomicPattern
	 */
	public List<NeoNode> extractNodesAndRelations(EList<ModelNodeBlock> mnb) {
		List<NeoNode> tempNodes = new ArrayList<NeoNode>();

		for (var n : mnb) {
			var node = new NeoNode(n.getType().getName(), newConstraintNode(n.getName()));

			n.getProperties().forEach(p -> node.addProperty(//
					p.getType().getName(), //
					EMSLUtil.handleValue(p.getValue())));

			n.getRelations()
					.forEach(r -> node.addRelation(
							newConstraintReference(node.getVarName(), n.getRelations().indexOf(r),
									EMSLUtil.getAllTypes(r), r.getTarget().getName()),
							EMSLUtil.getAllTypes(r), //
							r.getLower(), r.getUpper(), //
							r.getProperties(), //
							r.getTarget().getType().getName(), //
							newConstraintNode(r.getTarget().getName())));

			tempNodes.add(node);
		}

		return tempNodes;
	}
    
    public static List<String> extractElementsOnlyInConclusionPattern(Collection<NeoNode> ifPattern, Collection<NeoNode> thenPattern) {
        List<String> temp = new ArrayList<String>();
        List<String> only = new ArrayList<String>();
        
        for(NeoNode n: ifPattern) {
            temp.add(n.getVarName());
            for(NeoRelation r: n.getRelations()) {
                temp.add(r.getVarName());
            }
        }
        for(NeoNode n:thenPattern) {
            if(!temp.contains(n.getVarName()))
                only.add(n.getVarName());
            for(NeoRelation r: n.getRelations()) {
                if(!temp.contains(r.getVarName())) {
                    only.add(r.getVarName());
                }
            }
        }
        return only;
    }

	public static AtomicPattern getFlattenedPattern(AtomicPattern ap) {
		try {
			return (AtomicPattern) EMSLFlattener.flatten(ap);
		} catch (FlattenerException e) {
			logger.error("EMSL Flattener was unable to process the pattern.");
			e.printStackTrace();
			return ap;
		}

	}

	public static Pattern getFlattenedPattern(Pattern p) {
		try {
			return EMSLFlattener.flattenPattern(p);
		} catch (FlattenerException e) {
			logger.error("EMSL Flattener was unable to process the pattern.");
			e.printStackTrace();
			return p;
		}

	}
	
	public void removeMatchElement(String name) {
		if(matchElements.contains(name)) {
			matchElements.remove(name);
		}
	}
	
	public void removeOptionalElement(String name) {
		if(optionalElements.contains(name)) {
			optionalElements.remove(name);
		}
	}
}
