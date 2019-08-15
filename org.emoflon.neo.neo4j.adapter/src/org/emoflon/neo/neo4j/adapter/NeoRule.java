package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.ActionOperator;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.emsl.refinement.EMSLFlattener;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.api.rules.RuleApplicationSemantics;

// TODO [Jannik]
public class NeoRule implements IRule<NeoMatch, NeoCoMatch> {

	protected static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	protected List<NeoNode> nodes;
	protected List<NeoNode> nodesL;
	protected List<NeoNode> nodesR;
	protected List<NeoRelation> relL;
	protected List<NeoRelation> relR;

	protected boolean injective;
	protected boolean spoSemantics; // if false: DPO; if true SPO semantics
	protected NeoHelper helper;
	protected Rule r;

	protected IBuilder builder;
	protected NeoMask mask;

	public NeoRule(Rule r, NeoCoreBuilder builder, NeoMask mask) {
		nodes = new ArrayList<>();
		nodesL = new ArrayList<>();
		nodesR = new ArrayList<>();
		relL = new ArrayList<>();
		relR = new ArrayList<>();

		injective = true;
		spoSemantics = false;
		helper = new NeoHelper();
		this.builder = builder;
		try {
			this.r = (Rule) EMSLFlattener.flatten(r);
		} catch (FlattenerException e) {
			e.printStackTrace();
		}
		extractNodesAndRelations();
		
		logger.debug(nodes.toString());
		logger.debug(nodesL.toString());
		logger.debug(nodesR.toString());
		logger.debug(relL.toString());
		logger.debug(relR.toString());
	}

	public NeoRule(Rule r, NeoCoreBuilder builder) {
		this(r, builder, new EmptyMask());
	}

	/**
	 * Creates and extracts all necessary information data from the flattened
	 * Pattern. Create new NeoNode for any AtomicPattern node and corresponding add
	 * Relations and Properties and save them to the node in an node list.
	 */
	private void extractNodesAndRelations() {

		for (var n : r.getNodeBlocks()) {

			var node = new NeoNode(n.getType().getName(), helper.newPatternNode(n.getName()));

			if (n.getAction() != null) {

				switch (n.getAction().getOp()) {
				case CREATE:
					nodesR.add(node);
					logger.info("New ++ node: " + node.getVarName() + ":" + n.getType().getName());
					break;
				case DELETE:
					nodesL.add(node);
					logger.info("New -- node: " + node.getVarName() + ":" + n.getType().getName());
					break;
				default:
					throw new UnsupportedOperationException("Undefined Operator.");
				}

			} else {
				logger.info("New klebegraph node: " + node.getVarName() + ":" + n.getType().getName());
			}

			for (var p : n.getProperties()) {
				node.addProperty(p.getType().getName(), p.getValue().toString());
			}

			for (var r : n.getRelations()) {

				var rel = new NeoRelation(node,
						helper.newPatternRelation(node.getVarName(), n.getRelations().indexOf(r),
								EMSLUtil.getAllTypes(r), r.getTarget().getName()),
						EMSLUtil.getAllTypes(r), //
						r.getLower(), r.getUpper(), //
						r.getProperties(), //
						r.getTarget().getType().getName(), //
						r.getTarget().getName());

				if (r.getAction() != null) {

					switch (r.getAction().getOp()) {
					case CREATE:
						relR.add(rel);
						logger.info("New ++ relation: (" + node.getVarName() + ")-[" + rel.getVarName() + ":"
								+ rel.getLower() + rel.getUpper() + "]->(" + rel.getToNodeVar() + ":"
								+ rel.getToNodeLabel() + ")");
						break;
					case DELETE:
						relL.add(rel);
						node.addRelation(rel);
						logger.info("New -- relation: (" + node.getVarName() + ")-[" + rel.getVarName() + ":"
								+ rel.getLower() + rel.getUpper() + "]->(" + rel.getToNodeVar() + ":"
								+ rel.getToNodeLabel() + ")");
						break;
					default:
						throw new UnsupportedOperationException("Undefined Operator.");
					}

				} else {
					node.addRelation(rel);
					logger.info("New klebegraph relation: (" + node.getVarName() + ")-[" + rel.getVarName() + ":"
							+ rel.getLower() + rel.getUpper() + "]->(" + rel.getToNodeVar() + ":" + rel.getToNodeLabel()
							+ ")");
				}
			}

			if(n.getAction() == null || (n.getAction() != null && n.getAction().getOp() == ActionOperator.DELETE)) {				
				nodes.add(node);
			}
		}
	}

	protected void extractPropertiesFromMask(NeoNode node) {
		for (var propMask : mask.getMaskedAttributes().entrySet()) {
			var varName = mask.getVarName(propMask.getKey());
			if (node.getVarName().equals(varName)) {
				node.addProperty(//
						mask.getAttributeName(propMask.getKey()), //
						EMSLUtil.handleValue(propMask.getValue()));
			}

			for (var rel : node.getRelations()) {
				if (rel.getVarName().equals(varName)) {
					rel.addProperty(//
							mask.getAttributeName(propMask.getKey()), //
							EMSLUtil.handleValue(propMask.getValue()));
				}
			}
		}
	}

	@Override
	public String getName() {
		return r.getName();
	}

	@Override
	public void setMatchInjectively(Boolean injective) {
		this.injective = injective;
	}

	@Override
	public Collection<NeoMatch> determineMatches() {
		return determineMatches(0);
	}

	@Override
	public Collection<NeoMatch> determineMatches(int limit) {
		return null;
	}

	@Override
	public Optional<NeoCoMatch> apply(NeoMatch match, RuleApplicationSemantics ras) {
		// TODO[Jannik]
		return null;
	}

	public void setSPOSemantics(boolean spoSemantics) {
		this.spoSemantics = spoSemantics;
	}

}
