package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.ILPOperation
import org.emoflon.neo.emsl.eMSL.Action
import org.emoflon.neo.emsl.eMSL.ActionOperator

class FWD_OPT extends ILPOperation {
	override String getNameExtension() {
		return "_FWD_OPT"
	}
	
	override String getAction(Action action, boolean isSrc) {
		if(isSrc || action === null || !ActionOperator::CREATE.equals(action.getOp())) return "" else return "++"
	}
	
	override requiresSrcModelRule() {
		false
	}
	
	override requiresTrgModelRule() {
		true
	}
	
	override requiresModelCreation() {
		true
	}
	
	override requiresCorrModelCreation() {
		true
	}
	
}