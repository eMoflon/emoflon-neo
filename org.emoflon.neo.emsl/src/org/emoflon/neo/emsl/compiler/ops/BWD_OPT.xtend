package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.ILPOperation
import org.emoflon.neo.emsl.eMSL.ActionOperator
import org.emoflon.neo.emsl.eMSL.Action

class BWD_OPT extends ILPOperation {
	override String getNameExtension() {
		return "_BWD_OPT"
	}
	
	override String getAction(Action action, boolean isSrc) {
		if(!isSrc || action === null || !ActionOperator::CREATE.equals(action.getOp())) return "" else return "++"
	}
	
	override requiresSrcModelRule() {
		true
	}
	
	override requiresTrgModelRule() {
		false
	}
	
	override requiresModelCreation() {
		true
	}
	
	override requiresCorrModelCreation() {
		true
	}
}