package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.common.util.EList;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.refinement.EMSLFlattener;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.emsl.util.FlattenerException;

/**
 * Helper class for managing nodes, relations and their unique names in queries.
 * 
 * @author Jannik Hinz
 *
 */
public class NeoHelper {

	// Note: nodes and relations are stored in one list at a time
	private Collection<String> matchNodes;
	private Collection<String> optionalNodes;

	private int cCount;
	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	/**
	 * initialize Helper
	 */
	public NeoHelper() {
		this.matchNodes = new ArrayList<String>();
		this.optionalNodes = new ArrayList<String>();
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
		if (!matchNodes.contains(name))
			matchNodes.add(name);
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
	public String newPatternRelation(String name, int index, String relVar, String toName) {
		matchNodes.add(EMSLUtil.relationNameConvention(name, relVar, toName, index));
		return EMSLUtil.relationNameConvention(name, relVar, toName, index);
	}

	/**
	 * Creates an new Node in the Node List of OPTIONAL MATCH clauses if it is not
	 * contain already in the list and return the unique name of the node
	 * 
	 * @param name of the new node variable
	 * @return name of the new node variable for including in queries
	 */
	public String newConstraintNode(String name) {

		if (matchNodes.contains(name)) {
			return name;
		} else {
			optionalNodes.add(name + "_" + cCount);
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
	public String newConstraintReference(String name, int index, String relVar, String toName) {
		if (matchNodes.contains(EMSLUtil.relationNameConvention(name, relVar, toName, index))) {
			return EMSLUtil.relationNameConvention(name, relVar, toName, index);
		} else {
			optionalNodes.add(EMSLUtil.relationNameConvention(name, relVar, toName, index) + "_" + cCount);
			return EMSLUtil.relationNameConvention(name, relVar, toName, index) + "_" + cCount;
		}
	}

	/**
	 * Return the List of all Nodes from MATCH and OPTIONAL MATCH (union, but no
	 * duplicates) clauses
	 * 
	 * @return all Nodes from pattern and constraints
	 */
	public Collection<String> getNodes() {
		var list = matchNodes;
		for (String node : optionalNodes) {
			if (!list.contains(node))
				list.add(node);
		}
		return list;
	}

	/**
	 * Return the List of all Nodes from MATCH clauses
	 * 
	 * @return all Nodes from the pattern
	 */
	public Collection<String> getMatchNodes() {
		var list = matchNodes;
		for (String node : matchNodes) {
			list.add(node);
		}
		return list;
	}

	/**
	 * Return the List of all Nodes from OPTIONAL MATCH clauses
	 * 
	 * @return all Nodes from the constraints
	 */
	public Collection<String> getOptionalMatchNodes() {
		var list = optionalNodes;
		for (String node : optionalNodes) {
			list.add(node);
		}
		return list;
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

			var node = new NeoNode(n.getType().getName(), this.newConstraintNode(n.getName()));

			n.getProperties().forEach(p -> node.addProperty(//
					p.getType().getName(), //
					EMSLUtil.handleValue(p.getValue())));

			// TODO[Jannik] Think of how to handle optional edges with multiple types
			n.getRelations()
					.forEach(r -> node.addRelation(
							newConstraintReference(node.getVarName(), n.getRelations().indexOf(r),
									EMSLUtil.getOnlyType(r).getName(), r.getTarget().getName()),
							EMSLUtil.getOnlyType(r).getName(), //
							r.getProperties(), //
							r.getTarget().getType().getName(), //
							newConstraintNode(r.getTarget().getName())));

			tempNodes.add(node);
		}

		return tempNodes;
	}

	public AtomicPattern getFlattenedPattern(AtomicPattern ap) {
		try {
			return (AtomicPattern) EMSLFlattener.flatten(ap);
		} catch (FlattenerException e) {
			logger.error("EMSL Flattener was unable to process the pattern.");
			e.printStackTrace();
			return ap;
		}

	}

	public Pattern getFlattenedPattern(Pattern p) {
		try {
			return EMSLFlattener.flattenPattern(p);
		} catch (FlattenerException e) {
			logger.error("EMSL Flattener was unable to process the pattern.");
			e.printStackTrace();
			return p;
		}

	}

}
