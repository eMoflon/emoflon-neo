package org.emoflon.neo.emsl.compiler

import org.emoflon.neo.emsl.eMSL.Parameter
import org.emoflon.neo.emsl.eMSL.ValueExpression
import org.emoflon.neo.emsl.eMSL.PrimitiveString
import org.emoflon.neo.emsl.eMSL.PrimitiveInt
import org.emoflon.neo.emsl.eMSL.PrimitiveBoolean
import org.emoflon.neo.emsl.eMSL.EnumValue
import org.emoflon.neo.emsl.eMSL.AttributeExpression
import org.emoflon.neo.emsl.eMSL.NodeAttributeExpTarget
import org.emoflon.neo.emsl.eMSL.LinkAttributeExpTarget
import org.emoflon.neo.emsl.eMSL.BinaryExpression
import org.emoflon.neo.emsl.eMSL.AtomicPattern
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock
import com.google.common.collect.BiMap
import java.util.Map
import java.util.HashSet
import java.util.Collection

class TGGCompilerUtils {
	enum ParameterDomain {
		SRC, TRG, NAC
	}
	
	def static String handleValue(ValueExpression value) {
		if(value instanceof PrimitiveString) return '''"«value.literal»"'''

		if(value instanceof PrimitiveInt) return '''«value.literal»'''

		if(value instanceof PrimitiveBoolean) return '''«value.^true»'''

		if(value instanceof EnumValue) return '''"«value.literal.name»"'''

		if (value instanceof AttributeExpression) {
			// node::<target>
			val node = value.node
			val target = value.target

			if (target instanceof NodeAttributeExpTarget) // node::attribute
				return '''«node.name»::«target.attribute.name»'''
			else if (target instanceof LinkAttributeExpTarget) // node::-link->target::attribute
				return '''«node.name»::-«target.link.name»->«target.target.name»::«target.attribute.name»'''
		}
		
		if(value instanceof BinaryExpression)
			return '''«handleValue(value.left)»«value.op»«handleValue(value.right)»'''
		
		if(value instanceof Parameter){
			return '''<«value.name»>'''
		}

		throw new IllegalArgumentException('''Not yet able to handle: «value»''')
	}

	def static String printAtomicPattern(AtomicPattern pattern, BiMap<MetamodelNodeBlock, String> nodeTypeNames, Map<Parameter, ParameterData> paramsToData) {
		val requiredNodeBlocks = new HashSet
		'''
			pattern «pattern.name» {
				«FOR nodeBlock : pattern.nodeBlocks»
					«simplePrintNodeBlock(nodeBlock, nodeTypeNames, paramsToData, requiredNodeBlocks)»
				«ENDFOR»
				
				«FOR nodeBlock : requiredNodeBlocks»
					«IF !pattern.nodeBlocks.exists[it.name.equals(nodeBlock.name)]»
						«nodeBlock.name» : «nodeTypeNames.get(nodeBlock.type)»
					«ENDIF»
				«ENDFOR»
			}
		'''
	}
	
	def static String simplePrintNodeBlock(ModelNodeBlock nodeBlock, BiMap<MetamodelNodeBlock, String> nodeTypeNames, Map<Parameter, ParameterData> paramsToData, Collection<ModelNodeBlock> requiredNodeBlocks) {
		'''
			«nodeBlock.name» : «nodeTypeNames.get(nodeBlock.type)» {
				«FOR relation : nodeBlock.relations»
					«simplePrintRelationStatement(relation, paramsToData, requiredNodeBlocks)»
				«ENDFOR»
				«FOR property : nodeBlock.properties»
					«simplePrintPropertyStatement(property, paramsToData, requiredNodeBlocks)»
				«ENDFOR»
			}
		'''
	}
	
	def static String simplePrintRelationStatement(ModelRelationStatement relationStatement, Map<Parameter, ParameterData> paramsToData, Collection<ModelNodeBlock> requiredNodeBlocks) {
		'''
			-«FOR type : relationStatement.types SEPARATOR '|'»«type.type.name»«ENDFOR»->«relationStatement.target.name»
			«IF  relationStatement.properties !== null && !relationStatement.properties.empty»
				{
					«FOR property : relationStatement.properties»
						«simplePrintPropertyStatement(property, paramsToData, requiredNodeBlocks)»
					«ENDFOR»
				}
			«ENDIF»
		'''
	}
	
	def static String simplePrintPropertyStatement(ModelPropertyStatement propertyStatement, Map<Parameter, ParameterData> paramsToData, Collection<ModelNodeBlock> requiredNodeBlocks) {
		if(propertyStatement.value instanceof Parameter) {
			val paramData = paramsToData.get(propertyStatement.value as Parameter)
			requiredNodeBlocks.add(paramData.containingBlock)
			val paramValue = paramData.printValue
			if(paramValue === null)
				""
			else
				'''.«propertyStatement.type.name» «propertyStatement.op» «paramValue»'''
		}
		else
			'''.«propertyStatement.type.name» «propertyStatement.op» «handleValue(propertyStatement.value)»'''
	}
}
