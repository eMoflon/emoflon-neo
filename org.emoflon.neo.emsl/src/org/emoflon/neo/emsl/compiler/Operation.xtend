package org.emoflon.neo.emsl.compiler

import org.emoflon.neo.emsl.compiler.ops.BWD
import org.emoflon.neo.emsl.compiler.ops.CC
import org.emoflon.neo.emsl.compiler.ops.CO
import org.emoflon.neo.emsl.compiler.ops.FWD
import org.emoflon.neo.emsl.compiler.ops.MODELGEN
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator
import org.emoflon.neo.emsl.eMSL.Correspondence
import org.emoflon.neo.emsl.eMSL.Parameter
import java.util.Map
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.ConditionOperator
import org.emoflon.neo.emsl.eMSL.TripleRuleNAC
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock
import com.google.common.collect.BiMap

interface Operation {
	def static Operation[] getAllOps() {
		return (#[new MODELGEN(), new FWD(), new BWD(), new CO(), new CC()] as Operation[])
	}

	def String getNameExtension()

	def String getAction(Action action, boolean isSrc)

	def String getTranslation(Action action, boolean isSrc)

	def String getConditionOperator(ConditionOperator propOp, boolean isSrc)

	def void handleParameters(Map<Parameter, String> paramsToValues, Map<Parameter, String> paramsToProperty, Map<Parameter, Boolean> paramsToDomain, Map<String, Collection<Parameter>> paramGroups)

	def String compileCorrespondence(Correspondence corr) {
		val isGreen = (corr.action !== null && ActionOperator::CREATE.equals(corr.action.getOp()))
		'''
			«IF isGreen»++«ENDIF»-corr->«corr.target.name»
			{
				._type_ «IF isGreen»:=«ELSE»:«ENDIF» "«corr.type.name»"
			}
		'''
	}

	def String compileNACs(String ruleName, Collection<TripleRuleNAC> nacs, BiMap<MetamodelNodeBlock, String> nodeTypeNames) {
		return ""
	}
}
