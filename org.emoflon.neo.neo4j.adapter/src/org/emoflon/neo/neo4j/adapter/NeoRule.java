package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
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
	protected boolean injective;
	protected NeoHelper helper;
	protected Rule r;

	protected IBuilder builder;
	protected NeoMask mask;

	public NeoRule(Rule r, NeoCoreBuilder builder, NeoMask mask) {
		nodes = new ArrayList<>();
		injective = true;
		helper = new NeoHelper();
		this.builder = builder;
		try {
			this.r = (Rule) EMSLFlattener.flatten(r);
		} catch (FlattenerException e) {
			e.printStackTrace();
		}
		extractNodesAndRelations();
		logger.debug(nodes.toString());
	}

	public NeoRule(Rule r, NeoCoreBuilder builder) {
		this(r,builder, new EmptyMask());
	}
	
	/**
	 * Creates and extracts all necessary information data from the flattened
	 * Pattern. Create new NeoNode for any AtomicPattern node and corresponding add
	 * Relations and Properties and save them to the node in an node list.
	 */
	private void extractNodesAndRelations() {
		
		for (var n : r.getNodeBlocks()) {
			
			logger.debug(n.toString());
			
			var node = new NeoNode(n.getType().getName(), helper.newPatternNode(n.getName()));

			n.getProperties().forEach(p -> node.addProperty(//
					p.getType().getName(), //
					EMSLUtil.handleValue(p.getValue())));

			//extractPropertiesFromMask(node);

			n.getRelations()
					.forEach(r -> node.addRelation(
							helper.newPatternRelation(node.getVarName(), n.getRelations().indexOf(r),
									EMSLUtil.getAllTypes(r), r.getTarget().getName()),
							EMSLUtil.getAllTypes(r), //
							r.getLower(), r.getUpper(), //
							r.getProperties(), //
							r.getTarget().getType().getName(), //
							r.getTarget().getName()));

			nodes.add(node);
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

}
