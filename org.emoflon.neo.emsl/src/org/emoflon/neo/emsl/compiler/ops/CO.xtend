package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.ILPOperation
import org.emoflon.neo.emsl.eMSL.Action

class CO extends ILPOperation {
	override String getNameExtension() {
		return "_CO"
	}
	
	override getAction(Action action, boolean isSrc) {
		return ""
	}
	
	override requiresSrcModelRule() {
		true
	}
	
	override requiresTrgModelRule() {
		true
	}
	
	override requiresModelCreation() {
		false
	}
	
	override requiresCorrModelCreation() {
		false
	}
}
