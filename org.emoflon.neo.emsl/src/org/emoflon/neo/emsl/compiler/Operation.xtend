package org.emoflon.neo.emsl.compiler

import java.util.ArrayList
import java.util.Collection
import java.util.Collections
import java.util.Map
import org.emoflon.neo.emsl.compiler.ops.BWD
import org.emoflon.neo.emsl.compiler.ops.ilp.BWD_OPT
import org.emoflon.neo.emsl.compiler.ops.ilp.CC
import org.emoflon.neo.emsl.compiler.ops.ilp.CO
import org.emoflon.neo.emsl.compiler.ops.FWD
import org.emoflon.neo.emsl.compiler.ops.ilp.FWD_OPT
import org.emoflon.neo.emsl.compiler.ops.ilp.MI
import org.emoflon.neo.emsl.compiler.ops.MODELGEN
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ConditionOperator
import org.emoflon.neo.emsl.eMSL.Parameter
import org.emoflon.neo.emsl.eMSL.TripleRuleNAC
import org.emoflon.neo.emsl.compiler.Operation.Domain
import org.emoflon.neo.emsl.compiler.Operation.Domain
import org.emoflon.neo.emsl.compiler.Operation.Domain

interface Operation {
	enum Domain {SRC, CORR, TRG}
	
	def static Operation[] getAllOps() {
		return (#[new MODELGEN(), new FWD(), new BWD(), new CO(), new CC(), new BWD_OPT(), new FWD_OPT(), new MI()] as Operation[])
	}

	/*
	 * --------------------------------
	 * GT rule generation methods
	 * --------------------------------
	 */
	 
	def String getNameExtension()

	def String getAction(Action action, Domain domain)

	def String getTranslation(Action action, Domain domain)
	
	def String getDeltaCondition(Action action, int ruleID, Map<Domain,ArrayList<String>> greenElements, String element) {
		// default: no delta condition
		'''
		'''
	}

	def String getConditionOperator(ConditionOperator propOp, Domain domain)

	def void handleParameters(Map<Parameter, ParameterData> paramsToData, Map<String, Collection<Parameter>> paramGroups)

	def Iterable<TripleRuleNAC> preprocessNACs(Iterable<TripleRuleNAC> nacs)
	
	def Map<String, String> generateModelCreationRules(Iterable<String> srcMetaModelNames, Iterable<String> trgMetaModelNames) {
		Collections.emptyMap
	}

	def boolean requiresCorrModelCreation()

	/*
	 * --------------------------------
	 * app generation methods
	 * --------------------------------
	 */
	
	def String additionalImports(String tggName, String packagePath)
	
	def String additionalFields(String tggName)
	
	def String createGeneratorMethodBody(String tggName, String packageName)
	
	def String additionalMethods()

	def boolean exportMetamodels()
	
	/**
	 * This is true if more than one operational rule might be produced for just one rule.
	 * This is for example the case for model integration (concurrent synchronisation)
	 */
	def boolean isMulti() {
		false
	}
	
	def String getAction(Action action, int ruleID, Map<Domain,ArrayList<String>> greenElements, String element) {
		return ""
	}
	
	def String getConditionOperator(ConditionOperator propOp, int ruleID, Map<Domain,ArrayList<String>> greenElements, String element) {
		return ""
	}
	
	def selectParamGroupRepresentative(Collection<Parameter> paramGroup,
		Map<Parameter, ParameterData> paramsToData, int ruleID, Map<Domain,ArrayList<String>> greenElements) {
		return null		
	}
	
	def handleParameters(Map<Parameter, ParameterData> paramsToData,
		Map<String, Collection<Parameter>> paramGroups, int ruleID, Map<Domain,ArrayList<String>> greenElements) {
		// do nothing		
	}
	
	def String additionalConstructors(String appName) {
		
	}
}
