package org.emoflon.neo.emsl.compiler.ops.ilp

import java.util.ArrayList
import java.util.Collection
import java.util.Map
import org.emoflon.neo.emsl.compiler.ParameterData
import org.emoflon.neo.emsl.compiler.TGGCompilerValidations
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator
import org.emoflon.neo.emsl.eMSL.ConditionOperator
import org.emoflon.neo.emsl.eMSL.Parameter
import org.emoflon.neo.emsl.compiler.ops.ilp.ILPOperation

class MI extends ILPOperation {

	/*
	 * --------------------------------
	 * GT rule generation methods
	 * --------------------------------
	 */
	override String getNameExtension() {
		return "_MI"
	}

	override getAction(Action action, Domain domain) {
		if(action === null || !ActionOperator::CREATE.equals(action.getOp())) return "" else return "++"
	}

	override getConditionOperator(ConditionOperator propOp, Domain domain) {
		propOp.literal
	}

	override requiresCorrModelCreation() {
		true
	}

	override handleParameters(Map<Parameter, ParameterData> paramsToData,
		Map<String, Collection<Parameter>> paramGroups, int ruleID, Map<Domain,ArrayList<String>> greenElements) {

		for (group : paramGroups.values) {
			val representative = paramsToData.get(selectParamGroupRepresentative(group, paramsToData, ruleID, greenElements))
			if (representative !== null) {
				for (param : group)
					paramsToData.get(param).map(representative.containingBlock, representative.containingPropertyName)
				representative.unmap()
			}
		}
	}

	override selectParamGroupRepresentative(Collection<Parameter> paramGroup,
		Map<Parameter, ParameterData> paramsToData, int ruleID, Map<Domain,ArrayList<String>> greenElements) {
			if (greenElements === null)
				return paramGroup.head;
			for (Parameter p : paramGroup) {
				if (TGGCompilerValidations.binaryAND(ruleID, Math.pow(2,  getIndex(greenElements, paramsToData.get(p).containingBlock.name)).intValue) == 0)
					return p;
			}
		return null;
	}

	/*
	 * --------------------------------
	 * app generation methods
	 * --------------------------------
	 */

	override additionalFields(String tggName, String solver) {
		'''
			private static SupportedILPSolver solver = SupportedILPSolver.«solver»;
			private ModelIntegrationOperationalStrategy modelIntegration;
		'''
	}

	override createGeneratorMethodBody(String tggName, String packageName) {
		val fullOpName = '''«tggName»«nameExtension»'''
		'''
			var genAPI = new API_«tggName»_GEN(builder);
			var miAPI = new API_«fullOpName»(builder);
			var genRules = genAPI.getAllRulesFor«tggName.toFirstUpper»_GEN();
			var analyser = new TripleRuleAnalyser(new API_«packageName»(builder).getTripleRulesOf«tggName.toFirstUpper»());
			modelIntegration = new ModelIntegrationOperationalStrategy(//
					solver, //
					builder, //
					genRules, //
					miAPI.getAllRulesFor«fullOpName.toFirstUpper»(), //
					getNegativeConstraints(builder), //
					srcModelName, //
					trgModelName//
			);
			
			return new NeoGenerator(//
					miAPI.getAllRulesFor«fullOpName.toFirstUpper»(), //
					new PrepareContextDeltaAttributes(builder, srcModelName, trgModelName), //
					new NoMoreMatchesTerminationCondition(), //
					new MIRuleScheduler(analyser), //
					modelIntegration, //
					new MIReprocessor(analyser), //
					modelIntegration, //
					new HeartBeatAndReportMonitor(), //
					new ModelNameValueGenerator(srcModelName, trgModelName), //
					List.of(new LoremIpsumStringValueGenerator()));
		'''
	}

	override additionalMethods() {
		'''
			
			public ModelIntegrationOperationalStrategy runModelIntegration() throws Exception {
				run();
				return modelIntegration;
			}
			
			protected Collection<IConstraint> getNegativeConstraints(NeoCoreBuilder builder) {
				return Collections.emptyList();
			}
		'''
	}

	override exportMetamodels() {
		false
	}

	override boolean isMulti() {
		true
	}
	
	override boolean isExact() {
		false
	}

	override getDeltaCondition(Action action, int ruleID, Map<Domain,ArrayList<String>> greenElements, String element) {
		// Context
		if (action === null || !ActionOperator::CREATE.equals(action.getOp()))
			return ""
		// CO Rule : Marked Element
		else if (ruleID == 0)
			return "~_ex_ : true"
		// Optional Create Rule: Marked Element 
		else if (!isGreenInRule(ruleID, greenElements, element))
			//return "~_cr_ : true"
			return "~_ex_ : true"
		// Optional Create Rule: Created Element
		else
			return ""
	}

	override String getAction(Action action, int ruleID, Map<Domain,ArrayList<String>> greenElements, String element) {
		if (greenElements === null)
			return ""
		if (TGGCompilerValidations.binaryAND(ruleID, Math.pow(2, getIndex(greenElements, element)).intValue) > 0)
			return getAction(action, null)
		else
			return ""

	}
	
	def getIndex(Map<Domain,ArrayList<String>> greenElements, String element) {
		if (greenElements.get(Domain.CORR).contains(element))
			return greenElements.get(Domain.CORR).indexOf(element);
		if (greenElements.get(Domain.TRG).contains(element))
			return greenElements.get(Domain.TRG).indexOf(element) + greenElements.get(Domain.CORR).size;
		return greenElements.get(Domain.CORR).size + greenElements.get(Domain.TRG).size + greenElements.get(Domain.SRC).indexOf(element);
	}
	
	def boolean isGreenInRule(int ruleID, Map<Domain,ArrayList<String>> greenElements, String element) {
		return TGGCompilerValidations.binaryAND(ruleID, Math.pow(2,  getIndex(greenElements, element)).intValue) > 0;
	}
	
	override String getConditionOperator(ConditionOperator propOp, int ruleID, Map<Domain,ArrayList<String>> greenElements,
		String element) {
		if (greenElements === null)
			return super.getConditionOperator(propOp, null)
		if (isGreenInRule(ruleID, greenElements, element))
			return getConditionOperator(propOp, null)
		else
			return super.getConditionOperator(propOp, null)
	}
	
	override selectParamGroupRepresentative(Collection<Parameter> paramGroup, Map<Parameter, ParameterData> paramsToData) {
		paramGroup.head
	}
	
	override additionalImports(String tggName, String packagePath){
		'''
			«super.additionalImports(tggName, packagePath)»
			import org.emoflon.neo.engine.modules.updatepolicies.ModelIntegrationOperationalStrategy;
			import org.emoflon.neo.engine.modules.startup.PrepareContextDeltaAttributes;
			import org.emoflon.neo.engine.modules.ruleschedulers.MIRuleScheduler;
			import org.emoflon.neo.engine.modules.matchreprocessors.MIReprocessor;
		'''
	}
}
