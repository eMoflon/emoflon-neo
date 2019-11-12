package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.ILPOperation

class FWD_OPT extends ILPOperation {
	override String getNameExtension() {
		return "_FWD_OPT"
	}

	override requiresSrcModelCreation() {
		false
	}
	
	override requiresTrgModelCreation() {
		true
	}
	
	override requiresCorrModelCreation() {
		true
	}
}