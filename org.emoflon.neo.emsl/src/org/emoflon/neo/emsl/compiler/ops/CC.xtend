package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.ILPOperation
import org.emoflon.neo.emsl.eMSL.Action

class CC extends ILPOperation {
	override String getNameExtension() {
		return "_CC"
	}
	
	override getAction(Action action, boolean isSrc) {
		return ""
	}
	
	override requiresSrcModelCreation() {
		false
	}
	
	override requiresTrgModelCreation() {
		false
	}
	
	override requiresCorrModelCreation() {
		true
	}
}
