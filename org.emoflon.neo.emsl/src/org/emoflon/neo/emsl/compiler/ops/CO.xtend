package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.ILPOperation

class CO extends ILPOperation {
	override String getNameExtension() {
		return "_CO"
	}

	override requiresSrcModelCreation() {
		false
	}
	
	override requiresTrgModelCreation() {
		false
	}
	
	override requiresCorrModelCreation() {
		false
	}
}
