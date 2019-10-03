package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.Operation
import org.emoflon.neo.emsl.eMSL.Action

class CC implements Operation {
	override String getNameExtension() {
		return "_CC"
	}

	override String getAction(Action pAction, boolean pIsSrc) {
		return ""
	}

	override String getTranslation(Action pAction, boolean pIsSrc) {
		return ""
	}
}
