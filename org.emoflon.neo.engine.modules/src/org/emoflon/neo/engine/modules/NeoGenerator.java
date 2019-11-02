package org.emoflon.neo.engine.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.emoflon.neo.emsl.eMSL.ConstraintBody;
import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.Parameter;
import org.emoflon.neo.emsl.eMSL.Rule;
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
import org.emoflon.neo.neo4j.adapter.patterns.AttributeMask;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoRule;
import org.emoflon.neo.neo4j.adapter.rules.NeoRuleFactory;

public class NeoGenerator extends Generator<NeoMatch, NeoCoMatch> {

	private List<IParameterValueGenerator<DataType, ?>> parameterValueGenerators;
	private Map<NeoRule, AttributeMask> ruleMasks = new HashMap<>();
	private Map<NeoRule, Collection<String>> boundParameters = new HashMap<>();
	private Map<NeoRule, Collection<String>> freeParameters = new HashMap<>();
	private Map<Parameter, ParameterData> parameterData = new HashMap<>();

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
	}

	@Override
	protected void determineMatches(Map<IRule<NeoMatch, NeoCoMatch>, Schedule> scheduledRules,
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer) {

		mapParameters(scheduledRules.keySet());

		ruleMasks.clear();
		scheduledRules.forEach((rule, count) -> {
			if (!(rule instanceof NeoRule))
				throw new IllegalStateException("Unexpected type of rule: " + rule.getClass());

			NeoRule neoRule = (NeoRule) rule;
			AttributeMask mask = new AttributeMask();

			maskParameters(boundParameters.get(neoRule), mask);
			ruleMasks.put(neoRule, mask);

			matchContainer.addAll(NeoRuleFactory.copyNeoRuleWithNewMask(neoRule, mask).determineMatches(count), rule);
		});
	}

	@Override
	protected void applyMatches(IRule<NeoMatch, NeoCoMatch> r, Collection<NeoMatch> matches,
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer) {
		if (!(r instanceof NeoRule))
			throw new IllegalStateException("Unexpected type of rule: " + r.getClass());

		NeoRule rule = (NeoRule) r;
		AttributeMask mask = ruleMasks.get(rule);
		maskParameters(freeParameters.get(rule), mask);
		matches.forEach(
				match -> parameterData.forEach((param, data) -> match.addParameter(param.getName(), data.getValue())));
		var comatches = NeoRuleFactory.copyNeoRuleWithNewMask(rule, mask).applyAll(matches);
		matchContainer.appliedRule(rule, matches, comatches);
//		AttributeMask mask = new AttributeMask();
//		maskParameters(rule.getEMSLRule(), mask, matches);
//		var comatches = NeoRuleFactory.copyNeoRuleWithNewMask(rule, mask).applyAll(matches);
//		matchContainer.appliedRule(rule, matches, comatches);
	}

	private void mapParameters(Collection<IRule<NeoMatch, NeoCoMatch>> rules) {
		// TODO can we persist this data through multiple steps?
		boundParameters.clear();
		freeParameters.clear();
		parameterData.clear();

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

						parameterData.put(param, new ParameterData(prop.getType().getType(),
								nodeBlock.getName() + "." + prop.getType().getName()));

						if (nodeBlock.getAction() == null) { // -> bound
							if (freeParameters.containsKey(rule))
								freeParameters.get(rule).remove(param.getName());

							if (!boundParameters.containsKey(rule))
								boundParameters.put(rule, new HashSet<>());
							boundParameters.get(rule).add(param.getName());
						} else { // -> free
							if (boundParameters.containsKey(rule)
									&& boundParameters.get(rule).contains(param.getName()))
								continue;
							else {
								if (!freeParameters.containsKey(rule))
									freeParameters.put(rule, new HashSet<>());
								freeParameters.get(rule).add(param.getName());
							}
						}
					}
		}
	}

	private void maskParameters(Collection<String> parameterNames, AttributeMask mask) {
		if (mask == null || parameterNames == null || parameterNames.isEmpty())
			return;

		Map<String, Object> parameterValues = new HashMap<>();
		parameterData.forEach((param, data) -> {
			if (parameterNames.contains(param.getName())) {
				if (!data.hasValue()) {
					if (!parameterValues.containsKey(param.getName()))
						parameterValues.put(param.getName(), generateValueFor(data.getType(), param.getName()));
					data.setValue(parameterValues.get(param.getName()));
				}
				mask.maskAttribute(data.getAttributeName(), data.getValue());
			}
		});
	}

	@Deprecated
	private void maskParameters(Rule rule, AttributeMask mask, Collection<NeoMatch> matches) {
		Map<String, DataType> params = new HashMap<>();

		for (ModelNodeBlock nodeBlock : rule.getNodeBlocks())
			for (ModelPropertyStatement prop : nodeBlock.getProperties())
				if (prop.getValue() instanceof Parameter) {
					Parameter param = (Parameter) prop.getValue();

					if (!params.containsKey(param.getName()))
						params.put(param.getName(), prop.getType().getType());

					mask.maskAttribute(nodeBlock.getName() + "." + prop.getType().getName(),
							new ParameterPlaceHolder(param.getName()));
				}

		matches.forEach(m -> params.forEach(
				(parameterName, dataType) -> m.addParameter(parameterName, generateValueFor(dataType, parameterName))));
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
	protected MatchContainer<NeoMatch, NeoCoMatch> createMatchContainer() {
		return new NeoMatchContainer(allRules);
	}
}

class ParameterData {
	private DataType type;
	private String attributeName;
	private Object value;

	ParameterData(DataType type, String attributeName) {
		this.type = type;
		this.attributeName = attributeName;
	}

	public DataType getType() {
		return type;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public boolean hasValue() {
		return value != null;
	}

	public void setValue(Object value) {
		this.value = value;
	}

	public Object getValue() {
		return value;
	}
}

@Deprecated
class ParameterPlaceHolder {
	private String name;

	public ParameterPlaceHolder(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return EMSLUtil.PARAM_NAME_FOR_MATCH + "." + name;
	}
}
