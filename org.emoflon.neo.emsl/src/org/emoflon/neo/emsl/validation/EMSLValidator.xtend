/*
 * generated by Xtext 2.16.0
 */
package org.emoflon.neo.emsl.validation

import org.eclipse.xtext.validation.Check
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement
import org.emoflon.neo.emsl.eMSL.PrimitiveInt
import org.emoflon.neo.emsl.eMSL.BuiltInType
import org.emoflon.neo.emsl.eMSL.BuiltInDataTypes
import org.emoflon.neo.emsl.eMSL.EMSLPackage
import org.emoflon.neo.emsl.eMSL.PrimitiveBoolean
import org.emoflon.neo.emsl.eMSL.PrimitiveString
import org.emoflon.neo.emsl.eMSL.UserDefinedType
import org.emoflon.neo.emsl.eMSL.EnumValue
import org.emoflon.neo.emsl.eMSL.MetamodelPropertyStatement
import org.emoflon.neo.emsl.eMSL.Pattern
import org.emoflon.neo.emsl.EMSLFlattener
import org.emoflon.neo.emsl.util.FlattenerException
import org.emoflon.neo.emsl.util.FlattenerErrorType
import org.emoflon.neo.emsl.eMSL.AtomicPattern
import java.util.ArrayList

/**
 * This class contains custom validation rules. 
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
class EMSLValidator extends AbstractEMSLValidator {

	@Check(NORMAL)
	def checkPropertyStatementOfNodeBlock(ModelPropertyStatement p) {
		
		if (p.type instanceof MetamodelPropertyStatement) {
			if (p.type.type instanceof BuiltInType) {
				var propertyType = (p.type.type as BuiltInType).reference
				
				if (!(p.value instanceof PrimitiveInt && propertyType == BuiltInDataTypes.EINT) &&
					!(p.value instanceof PrimitiveBoolean && propertyType == BuiltInDataTypes.EBOOLEAN) &&
					!(p.value instanceof PrimitiveString && propertyType == BuiltInDataTypes.ESTRING)
				)
					error("The value of this PropertyStatement must be of type " + propertyType.getName, 
						EMSLPackage.Literals.MODEL_PROPERTY_STATEMENT__VALUE)
			} else if (p.type.type instanceof UserDefinedType) {
				var propertyType = (p.type.type as UserDefinedType).reference
				var literals = propertyType.literals
				if (!(p.value instanceof EnumValue && literals.contains((p.value as EnumValue).literal))) {
					error("The value of this PropertyStatement must be of type " + propertyType.getName, 
						EMSLPackage.Literals.MODEL_PROPERTY_STATEMENT__VALUE)
				}
			}
		}	
	}
	
	@Check(NORMAL)
	def checkFlattening(AtomicPattern pattern) {
		try {
			new EMSLFlattener().flattenCopyOfPattern(pattern.eContainer as Pattern, new ArrayList);
		} catch (FlattenerException e) {
			if (e.errorType == FlattenerErrorType.INFINITE_LOOP) {
				error("You have created an infinite loop in your refinements. The pattern \"" + 
					(e.entity as Pattern).body.name + "\" appears at least twice.", 
					EMSLPackage.Literals.ATOMIC_PATTERN__SUPER_REFINEMENT_TYPES)
			} else if (e.errorType == FlattenerErrorType.NO_COMMON_SUBTYPE_OF_NODES) {
				error("The type " + e.nodeBlock.type.name + " in your refinements is not mergeable.", 
					//(e.entity as Pattern).body, 
					EMSLPackage.Literals.ATOMIC_PATTERN__NAME)
				
			} else if (e.errorType == FlattenerErrorType.NO_COMMON_SUBTYPE_OF_PROPERTIES) {
				error("The types of the properties you are trying to refine are not compatible. The types " + 
					(e.property1 as ModelPropertyStatement).type.name + " and " + 
					(e.property2 as ModelPropertyStatement).type.name + " must be the same.", 
					e.property2, 
					EMSLPackage.Literals.MODEL_PROPERTY_STATEMENT__TYPE)
					
			} else if (e.errorType == FlattenerErrorType.REFINE_ENTITY_WITH_CONDITION) {
				error("Using Entities that have conditions are not allowed to be refined.", 
					EMSLPackage.Literals.ATOMIC_PATTERN__SUPER_REFINEMENT_TYPES)
			}	
		}
	}
	
}
