package org.emoflon.neo.emsl.compiler.ops.ilp

import java.util.Collection
import java.util.Collections
import java.util.Map
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ConditionOperator
import org.emoflon.neo.emsl.eMSL.Parameter
import org.emoflon.neo.emsl.eMSL.TripleRuleNAC
import java.util.Collections
import org.emoflon.neo.emsl.compiler.ParameterData
import org.emoflon.neo.emsl.compiler.Operation

abstract class ILPOperation implements Operation {

	override getTranslation(Action action, Domain domain) {
		return ""
	}

	override getConditionOperator(ConditionOperator propOp, Domain domain) {
		if (propOp === ConditionOperator.ASSIGN)
			ConditionOperator.EQ.literal
		else
			propOp.literal
	}

	override handleParameters(Map<Parameter, ParameterData> paramsToData,
		Map<String, Collection<Parameter>> paramGroups) {

		for (group : paramGroups.values) {
			val representative = paramsToData.get(selectParamGroupRepresentative(group, paramsToData))
			if (representative !== null) {
				for (param : group)
					paramsToData.get(param).map(representative.containingBlock, representative.containingPropertyName)
				representative.unmap()
			}
		}
	}
	
	override String additionalFields(String tggName) {
		if (isExact)
			return additionalFields(tggName, "Sat4J")
		else
			return additionalFields(tggName, "MOEA")
	}
	
	def String additionalFields(String tggName, String solver)
	
	def Parameter selectParamGroupRepresentative(Collection<Parameter> paramGroup, Map<Parameter, ParameterData> paramsToData) 
	
	override Iterable<TripleRuleNAC> preprocessNACs(Iterable<TripleRuleNAC> nacs) {
		return Collections.emptyList
	}
	
	override String additionalConstructors(String appName){
		'''
			public «appName»(String srcModelName, String trgModelName, SupportedILPSolver solver) {
				this(srcModelName, trgModelName);
				«appName».solver = solver;
			}
		'''
	}
	
	def boolean isExact() {
		true
	}

	override additionalImports(String tggName, String packagePath) {
		'''
			import static «packagePath».run.«tggName»_GEN_Run.SRC_MODEL_NAME;
			import static «packagePath».run.«tggName»_GEN_Run.TRG_MODEL_NAME;
					
			import java.util.Collection;
			import java.util.Collections;
			import java.util.List;
			import org.emoflon.neo.engine.api.constraints.IConstraint;
			import «packagePath».tgg.API_«tggName»_GEN;
			import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
			import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
			import org.emoflon.neo.engine.modules.startup.NoOpStartup;
			import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
			import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
			import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;
			import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;
		'''
	}
}
