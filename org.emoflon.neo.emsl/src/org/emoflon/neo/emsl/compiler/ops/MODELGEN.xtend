package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.Operation
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator
import java.util.Map
import org.emoflon.neo.emsl.eMSL.Parameter
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.ConditionOperator
import org.emoflon.neo.emsl.compiler.ParameterData
import org.emoflon.neo.emsl.eMSL.TripleRuleNAC
import java.util.HashMap

class MODELGEN implements Operation {
	override String getNameExtension() {
		return "_GEN"
	}

	override String getAction(Action action, boolean isSrc) {
		if(action === null || !ActionOperator::CREATE.equals(action.getOp())) return "" else return "++"
	}

	override String getTranslation(Action action, boolean isSrc) {
		return ""
	}
	
	override handleParameters(Map<Parameter, ParameterData> paramsToData, Map<String, Collection<Parameter>> paramGroups) {
		// Nothing to do here, all parameters are already mapped to their correct String representation
	}
	
	override getConditionOperator(ConditionOperator propOp, boolean isSrc) {
		propOp.literal
	}

	override Iterable<TripleRuleNAC> preprocessNACs(Iterable<TripleRuleNAC> nacs) {
		return nacs
	}

	override Map<String, String> generateModelCreationRules(Iterable<String> srcMetaModelNames, Iterable<String> trgMetaModelNames) {
		val creationRules = new HashMap
		creationRules.put(
			"createSrcModel",
			'''
				rule createSrcModel {
					++ srcM : Model {
						.ename := <__srcModelName>
						«FOR srcMetaModel : srcMetaModelNames»
							++ -conformsTo-> mm«srcMetaModel»
						«ENDFOR»
					}
				
					«FOR srcMetaModel : srcMetaModelNames»
						mm«srcMetaModel» : MetaModel {
							.ename : "«srcMetaModel»"
						}
					«ENDFOR»
				} when forbid srcModelExists
				
				pattern srcModelExists {
					srcM : Model {
						.ename : <__srcModelName>
					}
				}
			''')
		
		creationRules.put(
			"createTrgModel",
			'''
				rule createTrgModel {
					++ trgM : Model {
						.ename := <__trgModelName>
						«FOR trgMetaModel : trgMetaModelNames»
							++ -conformsTo-> mm«trgMetaModel»
						«ENDFOR»
					}
				
					«FOR trgMetaModel : trgMetaModelNames»
						mm«trgMetaModel» : MetaModel {
							.ename : "«trgMetaModel»"
						}
					«ENDFOR»
				} when forbid trgModelExists
				
				pattern trgModelExists {
					trgM : Model {
						.ename : <__trgModelName>
					}
				}
			''')
		
		return creationRules
	}
}
