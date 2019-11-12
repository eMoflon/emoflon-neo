package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.ILPOperation

class CC extends ILPOperation {
	override String getNameExtension() {
		return "_CC"
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
