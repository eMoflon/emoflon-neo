package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.Operation
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator

class MODELGEN implements Operation {
	override String getNameExtension() {
		return "_GEN"
	}

	override String getAction(Action pAction, boolean pIsSrc) {
		if(pAction === null || !ActionOperator::CREATE.equals(pAction.getOp())) return "" else return "++"
	}

	override String getTranslation(Action pAction, boolean pIsSrc) {
		return ""
	}
}
