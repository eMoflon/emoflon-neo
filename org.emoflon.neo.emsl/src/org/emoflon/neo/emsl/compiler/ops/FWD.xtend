package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.Operation
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator

class FWD implements Operation {
	override String getNameExtension() {
		return "_FWD"
	}

	override String getAction(Action pAction, boolean pIsSrc) {
		if(pIsSrc || pAction === null || !ActionOperator::CREATE.equals(pAction.getOp())) return "" else return "++"
	}

	override String getTranslation(Action pAction, boolean pIsSrc) {
		if(pIsSrc) if(pAction === null ||
			!ActionOperator::CREATE.equals(
				pAction.getOp())) return "~_tr_ : true" else return "~_tr_ : false\n~_tr_ := true" else return ""
	}
}
