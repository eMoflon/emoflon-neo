package org.emoflon.neo.emsl.compiler

import org.emoflon.neo.emsl.eMSL.ModelNodeBlock
import org.emoflon.neo.emsl.compiler.TGGCompilerUtils.ParameterDomain
import java.util.Optional

class ParameterData {
	String value;
	ParameterDomain domain;
	ModelNodeBlock containingBlock;
	String containingPropertyName;
	Optional<String> boundValue;
	
	new(String paramName, ParameterDomain domain, ModelNodeBlock containingBlock, String containingPropertyName) {
		this.value = '''<«paramName»>'''
		this.domain = domain
		this.containingBlock = containingBlock
		this.containingPropertyName = containingPropertyName
		boundValue = Optional.empty
	}
	
	def getPrintValue() {
		if(value !== null)
			Optional.of(value)
		else if(containingBlock !== null)
			Optional.of('''«containingBlock.name»::«containingPropertyName»''')
		else
			Optional.empty
	}
	
	def getBoundValue(){
		if(!boundValue.present)
			boundValue = printValue
	
		return boundValue
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
	
	def unmap() {
		if(!boundValue.present)
			boundValue = printValue 
		
		map(null, null)
	}
}
