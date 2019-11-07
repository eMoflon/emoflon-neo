package org.emoflon.neo.engine.modules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.emoflon.neo.cypher.common.NeoMask;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.cypher.rules.NeoRule;
import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.Parameter;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.engine.generator.Generator;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMatchReprocessor;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.engine.generator.modules.IParameterValueGenerator;
import org.emoflon.neo.engine.generator.modules.IRuleScheduler;
import org.emoflon.neo.engine.generator.modules.ITerminationCondition;
import org.emoflon.neo.engine.generator.modules.IUpdatePolicy;

public class NeoGenerator extends Generator<NeoMatch, NeoCoMatch> {

	private List<IParameterValueGenerator<DataType, ?>> parameterValueGenerators;

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
	protected void applyMatches(//
			IRule<NeoMatch, NeoCoMatch> r, //
			Collection<NeoMatch> matches, //
			MatchContainer<NeoMatch, NeoCoMatch> matchContainer//
	) {
		if (!(r instanceof NeoRule))
			throw new IllegalStateException("Unexpected type of rule: " + r.getClass());

		NeoRule rule = (NeoRule) r;
		var mask = new NeoMask();
		maskParameters(rule.getEMSLRule(), mask, matches);
		var comatches = rule.applyAll(matches, mask);
		matchContainer.appliedRule(rule, matches, comatches);
	}

	private void maskParameters(Rule rule, NeoMask mask, Collection<NeoMatch> matches) {
		Map<String, DataType> params = new HashMap<>();

		for (ModelNodeBlock nodeBlock : rule.getNodeBlocks())
			for (ModelPropertyStatement prop : nodeBlock.getProperties())
				if (prop.getValue() instanceof Parameter) {
					Parameter param = (Parameter) prop.getValue();

					if (!params.containsKey(param.getName()))
						params.put(param.getName(), prop.getType().getType());
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
