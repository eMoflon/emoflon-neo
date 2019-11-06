package org.emoflon.neo.cypher.patterns;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.eclipse.xtext.xbase.lib.Pair;
import org.emoflon.neo.cypher.common.NeoAssertion;
import org.emoflon.neo.cypher.common.NeoElement;
import org.emoflon.neo.cypher.common.NeoNode;
import org.emoflon.neo.cypher.common.NeoRelation;
import org.emoflon.neo.emsl.eMSL.Action;
import org.emoflon.neo.emsl.eMSL.ActionOperator;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.refinement.EMSLFlattener;
import org.emoflon.neo.emsl.util.FlattenerException;

import com.google.common.collect.Streams;

public class NeoBasicPattern {
	private String name;
	protected List<NeoNode> nodes;
	protected Collection<NeoRelation> relations;
	protected Map<ModelNodeBlock, NeoNode> nodeBlockToNeoNode;

	public NeoBasicPattern(AtomicPattern pattern) throws FlattenerException {
		this(pattern, false);
	}
	
	public NeoBasicPattern(AtomicPattern pattern, boolean isCoPattern) throws FlattenerException {
		this(pattern.getName(), ((AtomicPattern) EMSLFlattener.flatten(pattern)).getNodeBlocks(), isCoPattern);
	}

	public NeoBasicPattern(String name, Collection<ModelNodeBlock> nodeBlocks, boolean isCoPattern) {
		this.name = name;
		nodes = new ArrayList<>();
		relations = new ArrayList<>();
		nodeBlockToNeoNode = new HashMap<>();

		// Extract nodes
		for (var nb : nodeBlocks) {
			if (isBlack(nb.getAction())//
					|| (!isCoPattern && isRed(nb.getAction()))//
					|| (isCoPattern && isGreen(nb.getAction()))) {
				var nn = new NeoNode(nb);
				nodes.add(nn);
				nodeBlockToNeoNode.put(nb, nn);
			}
		}

		// Extract relations
		for (var nb : nodeBlocks) {
			for (var rel : nb.getRelations()) {
				if (isBlack(rel.getAction())//
						|| (!isCoPattern && isRed(rel.getAction()))//
						|| (isCoPattern && isGreen(rel.getAction()))) {
					var nr = new NeoRelation(rel, nodeBlockToNeoNode);
					relations.add(nr);
				}
			}
		}
	}

	private boolean isGreen(Action action) {
		return action != null && action.getOp().equals(ActionOperator.CREATE);
	}

	private boolean isRed(Action action) {
		return action != null && action.getOp().equals(ActionOperator.DELETE);
	}

	protected boolean isBlack(Action action) {
		return action == null;
	}

	public String getName() {
		return name;
	}

	/**
	 * Return the names of all elements in the pattern. Note that paths do not have
	 * names and are thus omitted.
	 * 
	 * @return
	 */
	public Collection<String> getElements() {
		return Streams//
				.concat(nodes.stream(), relations.stream())//
				.map(NeoElement::getName)//
				.filter(Predicate.not(String::isBlank))//
				.collect(Collectors.toList());
	}

	public Collection<NeoNode> getNodes() {
		return nodes;
	}

	public Collection<NeoRelation> getRelations() {
		return relations;
	}

	public Collection<Pair<NeoNode, NeoNode>> getInjectiveChecks() {
		var pairsToCheck = new ArrayList<Pair<NeoNode, NeoNode>>();
		for (var i = 0; i < nodes.size(); i++) {
			for (var j = i + 1; j < nodes.size(); j++) {
				var classTypesI = nodes.get(i).getLabels();
				var classTypesJ = nodes.get(j).getLabels();

				if (classTypesJ.contains(nodes.get(i).getType()) || classTypesI.contains(nodes.get(j).getType()))
					pairsToCheck.add(Pair.of(nodes.get(i), nodes.get(j)));
			}
		}

		return pairsToCheck;
	}
	
	public Collection<NeoAssertion> getInequalityChecks() {
		var relevantElements = new ArrayList<NeoElement>();
		relevantElements.addAll(nodes);
		relevantElements.addAll(relations);

		return relevantElements.stream()//
				.flatMap(elt -> elt.getInequalityChecks().stream())//
				.collect(Collectors.toList());
	}
}
