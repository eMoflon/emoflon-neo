package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.ILPOperation

class BWD_OPT extends ILPOperation {
	override String getNameExtension() {
		return "_BWD_OPT"
	}

	override requiresSrcModelCreation() {
		true
	}
	
	override requiresTrgModelCreation() {
		false
	}
	
	override requiresCorrModelCreation() {
		true
	}
}