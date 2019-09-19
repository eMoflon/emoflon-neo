package org.emoflon.neo.emsl.compiler

import org.emoflon.neo.emsl.compiler.ops.BWD
import org.emoflon.neo.emsl.compiler.ops.CC
import org.emoflon.neo.emsl.compiler.ops.CO
import org.emoflon.neo.emsl.compiler.ops.FWD
import org.emoflon.neo.emsl.compiler.ops.MODELGEN
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator
import org.emoflon.neo.emsl.eMSL.Correspondence

interface Operation {
	def static Operation[] getAllOps() {
		return (#[new MODELGEN(), new FWD(), new BWD(), new CO(), new CC()] as Operation[])
	}

	def String getNameExtension()

	def String getAction(Action pAction, boolean pIsSrc)

	def String getTranslation(Action pAction, boolean pIsSrc)

	def String compileCorrespondence(Correspondence corr) {
		val isGreen = (corr.action !== null && ActionOperator::CREATE.equals(corr.action.getOp()))
		'''
			«IF isGreen»++«ENDIF»-corr->«corr.target.name»
			{
				._type_ «IF isGreen»:=«ELSE»:«ENDIF» "«corr.type.name»"
			}
		'''
	}

}
