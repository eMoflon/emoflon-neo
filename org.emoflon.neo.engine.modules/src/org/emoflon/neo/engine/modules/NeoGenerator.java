package org.emoflon.neo.engine.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.emoflon.neo.cypher.common.NeoMask;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.cypher.rules.NeoRule;
import org.emoflon.neo.emsl.eMSL.ConstraintBody;
import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.Parameter;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.Generator;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.Schedule;
import org.emoflon.neo.engine.generator.modules.IMatchReprocessor;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IParameterValueGenerator;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;
import org.emoflon.neo.engine.generator.modules.ITerminationCondition;
import org.emoflon.neo.engine.generator.modules.IUpdatePolicy;

public class NeoGenerator extends Generator<NeoMatch, NeoCoMatch> {

	private List<IParameterValueGenerator<DataType, ?>> parameterValueGenerators;
	private Map<NeoRule, NeoMask> ruleMasks = new HashMap<>();
	private Map<NeoRule, Collection<String>> boundParameters;
	private Map<NeoRule, Collection<String>> freeParameters;
	private Map<String, DataType> parameterDataTypes;

	public NeoGenerator(//
			Collection<NeoRule> allRules, //
			ITerminationCondition<NeoMatch, NeoCoMatch> terminationCondition, //
			IRuleScheduler<NeoMatch, NeoCoMatch> ruleScheduler, //
			IUpdatePolicy<NeoMatch, NeoCoMatch> updatePolicy, //
			IMatchReprocessor<NeoMatch, NeoCoMatch> matchReprocessor, //
			IMonitor<NeoMatch, NeoCoMatch> progressMonitor, //
			List<IParameterValueGenerator<DataType, ?>> parameterValueGenerators//
	) {
		super(//
				allRules, //
				terminationCondition, //
				ruleScheduler, //
				updatePolicy, //
				matchReprocessor, //
				progressMonitor//
		);
		this.parameterValueGenerators = new ArrayList<>(parameterValueGenerators);
		mapParameters(allRules);
	}

	private void mapParameters(Collection<NeoRule> rules) {
		boundParameters = new HashMap<>();
		freeParameters = new HashMap<>();
		rules.forEach(rule -> {
			boundParameters.put(rule, new HashSet<>());
			freeParameters.put(rule, new HashSet<>());
		});
		parameterDataTypes = new HashMap<>();

		for (IRule<NeoMatch, NeoCoMatch> iRule : rules) {
			if (!(iRule instanceof NeoRule))
				throw new IllegalStateException("Unexpected type of rule: " + iRule.getClass());
			NeoRule rule = (NeoRule) iRule;
			Collection<ModelNodeBlock> nodeBlocks = new HashSet<>();
			nodeBlocks.addAll(rule.getEMSLRule().getNodeBlocks());
			EMSLUtil.iterateConstraintPatterns((ConstraintBody) rule.getEMSLRule().getCondition(),
					pattern -> nodeBlocks.addAll(pattern.getNodeBlocks()));

			for (ModelNodeBlock nodeBlock : nodeBlocks)
				for (ModelPropertyStatement prop : nodeBlock.getProperties())
					if (prop.getValue() instanceof Parameter) {
						Parameter param = (Parameter) prop.getValue();

						if (!parameterDataTypes.containsKey(param.getName()))
							parameterDataTypes.put(param.getName(), prop.getType().getType());

						if (nodeBlock.getAction() == null) { // -> bound
							freeParameters.get(rule).remove(param.getName());
							boundParameters.get(rule).add(param.getName());
						} else if (boundParameters.get(rule).contains(param.getName())) // -> already bound
							continue;
						else // -> free
							freeParameters.get(rule).add(param.getName());
					}
		}
	}

	@Override
	protected void determineMatches(Map<IRule<NeoMatch, NeoCoMatch>, Schedule> scheduledRules,
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer) {

		ruleMasks.clear();
		scheduledRules.forEach((rule, schedule) -> {
			if (!(rule instanceof NeoRule))
				throw new IllegalStateException("Unexpected type of rule: " + rule.getClass());

			NeoRule neoRule = (NeoRule) rule;
			NeoMask mask = new NeoMask();

			// mask bound parameters
			boundParameters.get(rule).forEach(param -> {
				mask.addParameter(param, generateValueFor(parameterDataTypes.get(param), param));
			});

			ruleMasks.put(neoRule, mask);

			matchContainer.addAll(neoRule.determineMatches(schedule, mask), rule);
		});
	}

	@Override
	protected void applyMatches(IRule<NeoMatch, NeoCoMatch> rule, Collection<NeoMatch> matches,
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer) {
		if (!(rule instanceof NeoRule))
			throw new IllegalStateException("Unexpected type of rule: " + rule.getClass());

		NeoRule neoRule = (NeoRule) rule;

		// mask free parameters
		freeParameters.get(rule).forEach(param -> {
			matches.forEach(match -> match.addParameter(param, generateValueFor(parameterDataTypes.get(param), param)));
		});

		var comatches = neoRule.applyAll(matches, ruleMasks.get(neoRule));
		matchContainer.appliedRule(neoRule, matches, comatches);
	}

	private Object generateValueFor(DataType dataType, String parameterName) {
		var valueGen = parameterValueGenerators.stream()//
				.filter(vg -> vg.generatesValueFor(parameterName, dataType))//
				.findFirst();

		return valueGen.map(vg -> vg.generateValueFor(parameterName))//
				.orElseThrow(() -> new IllegalArgumentException(
						"Unable to generate value for: " + parameterName + ":" + dataType.eClass().getName()));
	}

	@Override
	protected MatchContainer<NeoMatch, NeoCoMatch> createMatchContainer(
			Collection<? extends IRule<NeoMatch, NeoCoMatch>> allRules) {
		return new NeoMatchContainer(allRules);
	}
}
