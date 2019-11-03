package org.emoflon.neo.engine.modules;

import java.util.Collection;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Stream;

import org.emoflon.neo.emsl.eMSL.ConstraintBody;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.Parameter;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.neo4j.adapter.patterns.AttributeMask;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoRule;
import org.emoflon.neo.neo4j.adapter.rules.NeoRuleFactory;

public class NeoMatchContainer extends MatchContainer<NeoMatch, NeoCoMatch> {

	public NeoMatchContainer(Collection<? extends IRule<NeoMatch, NeoCoMatch>> allRules) {
		super(maskParameters(allRules));
	}

	private static Collection<NeoRule> maskParameters(Collection<? extends IRule<NeoMatch, NeoCoMatch>> allRules) {
		Collection<NeoRule> maskedRules = new HashSet<>();

		for (IRule<NeoMatch, NeoCoMatch> iRule : allRules) {
			if (!(iRule instanceof NeoRule))
				throw new IllegalStateException("Unexpected type of rule: " + iRule.getClass());
			NeoRule rule = (NeoRule) iRule;
			Collection<ModelNodeBlock> nodeBlocks = new HashSet<>();
			nodeBlocks.addAll(rule.getEMSLRule().getNodeBlocks());
			EMSLUtil.iterateConstraintPatterns((ConstraintBody) rule.getEMSLRule().getCondition(),
					pattern -> nodeBlocks.addAll(pattern.getNodeBlocks()));
			AttributeMask mask = new AttributeMask();

			for (ModelNodeBlock nodeBlock : nodeBlocks)
				for (ModelPropertyStatement prop : nodeBlock.getProperties())
					if (prop.getValue() instanceof Parameter)
						mask.maskAttribute(nodeBlock.getName() + "." + prop.getType().getName(),
								"$" + ((Parameter) prop.getValue()).getName());

			maskedRules.add(NeoRuleFactory.copyNeoRuleWithNewMask(rule, mask));
		}

		return maskedRules;
	}

	@Override
	public void appliedRule(IRule<NeoMatch, NeoCoMatch> rule, Collection<NeoMatch> appliedMatches,
			Optional<Collection<NeoCoMatch>> comatches) {
		int noOfApplications = appliedMatches.size();
		var before = ruleApplications.get(rule);
		var after = before + noOfApplications;
		ruleApplications.put(rule, after);

		comatches.ifPresent(cms -> cms.forEach(cm -> addCreatedElementIDsToRange(cm, rule)));
	}

	@Override
	protected Stream<String> getTypesFor(IRule<NeoMatch, NeoCoMatch> rule, String elt) {
		var neoRule = (NeoRule) rule;
		return neoRule.getCreatedNodes().get(elt).getLabels().stream();
	}

}
