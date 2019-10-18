package org.emoflon.neo.emsl.compiler

import org.emoflon.neo.emsl.eMSL.ModelNodeBlock
import org.emoflon.neo.emsl.compiler.TGGCompilerUtils.ParameterDomain

class ParameterData {
	
	String value;
	ParameterDomain domain;
	ModelNodeBlock containingBlock;
	String containingPropertyName;
	
	new(String paramName, ParameterDomain domain, ModelNodeBlock containingBlock, String containingPropertyName) {
		this.value = '''<«paramName»>'''
		this.domain = domain
		this.containingBlock = containingBlock
		this.containingPropertyName = containingPropertyName
	}
	
	def getPrintValue() {
		if(value !== null)
			value
		else if(containingBlock !== null)
			'''«containingBlock.name»::«containingPropertyName»'''
		else
			null
	}
	
	def getDomain() {
		domain
	}
	
	def getContainingBlock() {
		containingBlock
	}
	
	def getContainingPropertyName() {
		containingPropertyName
	}
	
	def map(ModelNodeBlock containingBlock, String containingPropertyName) {
		value = null
		this.containingBlock = containingBlock
		this.containingPropertyName = containingPropertyName
	}
}
