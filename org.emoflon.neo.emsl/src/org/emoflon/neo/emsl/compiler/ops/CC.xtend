package org.emoflon.neo.emsl.compiler.ops

import org.emoflon.neo.emsl.compiler.ILPOperation
import org.emoflon.neo.emsl.eMSL.Action
import java.util.Collection
import org.emoflon.neo.emsl.eMSL.Parameter
import org.emoflon.neo.emsl.compiler.ParameterData
import java.util.Map

class CC extends ILPOperation {

	/*
	 * --------------------------------
	 * GT rule generation methods
	 * --------------------------------
	 */

	override String getNameExtension() {
		return "_CC"
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
		true
	}

	override selectParamGroupRepresentative(Collection<Parameter> paramGroup, Map<Parameter, ParameterData> paramsToData) {
		paramGroup.head
	}

	/*
	 * --------------------------------
	 * app generation methods
	 * --------------------------------
	 */
	
	override additionalImports(String tggName) {
		// TODO
		'''
		'''
	}
	
	override additionalFields(String tggName) {
		// TODO
		'''
		'''
	}
	
	override createGeneratorMethodBody(String tggName) {
		// TODO
		'''
		'''
	}
	
	override additionalMethods() {
		// TODO
		'''
		'''
	}
	
	override exportMetamodels() {
		false
	}
}
