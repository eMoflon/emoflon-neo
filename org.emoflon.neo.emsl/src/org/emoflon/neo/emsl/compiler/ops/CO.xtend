package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.Operation
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.Correspondence

class CO implements Operation {
	override String getNameExtension() {
		return "_CO"
	}

	override String getAction(Action pAction, boolean pIsSrc) {
		return ""
	}

	override String getTranslation(Action pAction, boolean pIsSrc) {
		return ""
	}

	override String compileCorrespondence(Correspondence corr) {
		'''
			-corr->«corr.target.name»
			{
				._type_ : "«corr.type.name»"
			}
		'''
	}
}
