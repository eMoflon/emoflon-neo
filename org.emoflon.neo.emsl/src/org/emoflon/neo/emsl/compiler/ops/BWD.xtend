package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.Operation
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator

class BWD implements Operation {
	override String getNameExtension() {
		return "_BWD"
	}

	override String getAction(Action pAction, boolean pIsSrc_finalParam_) {
		var pIsSrc = pIsSrc_finalParam_
		if(!pIsSrc || pAction === null || !ActionOperator::CREATE.equals(pAction.getOp())) return "" else return "++"
	}

	override String getTranslation(Action pAction, boolean pIsSrc_finalParam_) {
		var pIsSrc = pIsSrc_finalParam_
		if(!pIsSrc) if(pAction === null ||
			!ActionOperator::CREATE.equals(
				pAction.getOp())) return "~_tr_ : true" else return "~_tr_ : false\n~_tr_ := true" else return ""
	}
}
