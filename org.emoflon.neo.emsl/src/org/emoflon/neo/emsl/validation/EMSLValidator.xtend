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
import java.util.ArrayList
import org.emoflon.neo.emsl.eMSL.AttributeExpression
import org.emoflon.neo.emsl.eMSL.NodeAttributeExpTarget
import org.emoflon.neo.emsl.eMSL.LinkAttributeExpTarget
import org.emoflon.neo.emsl.eMSL.DataType
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock
import org.emoflon.neo.emsl.eMSL.Entity
import org.emoflon.neo.emsl.util.EntityAttributeDispatcher
import org.emoflon.neo.emsl.eMSL.Rule
import org.emoflon.neo.emsl.eMSL.RefinementCommand
import org.emoflon.neo.emsl.eMSL.AtomicPattern

/**
 * This class contains custom validation rules. 
 *
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#validation
 */
class EMSLValidator extends AbstractEMSLValidator {
	
	@Check
	def checkPropertyStatementOfNodeBlock(ModelPropertyStatement p) {
		
		if (p.type instanceof MetamodelPropertyStatement) {
			if (p.type.type instanceof BuiltInType) {
				var propertyType = (p.type.type as BuiltInType).reference
				
				if (!(p.value instanceof PrimitiveInt && propertyType == BuiltInDataTypes.EINT) &&
					!(p.value instanceof PrimitiveBoolean && propertyType == BuiltInDataTypes.EBOOLEAN) &&
					!(p.value instanceof PrimitiveString && propertyType == BuiltInDataTypes.ESTRING) &&
					!(p.value instanceof AttributeExpression && isOfCorrectType(p.value as AttributeExpression, p.type.type))
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
		
	def isOfCorrectType(AttributeExpression attrExpr, DataType type) {
		if(attrExpr.target instanceof NodeAttributeExpTarget){
			(attrExpr.target as NodeAttributeExpTarget).attribute.type.equals(type)
		} else if(attrExpr.target instanceof LinkAttributeExpTarget){
			return (attrExpr.target as LinkAttributeExpTarget).attribute.type.equals(type)
		}
	}
	
	@Check(NORMAL)
	def checkFlattening(Entity entity) {
		try {
			if (entity instanceof Pattern) {
				new EMSLFlattener().flattenCopyOfEntity(entity as Pattern, new ArrayList);
			} else if (entity instanceof Rule) {
				new EMSLFlattener().flattenCopyOfEntity(entity as Entity, new ArrayList);
			}
		} catch (FlattenerException e) {
			if (entity instanceof Pattern) {
				if (e.errorType == FlattenerErrorType.INFINITE_LOOP) {
					error("You have created an infinite loop in your refinements. The pattern \"" + 
						new EntityAttributeDispatcher().getName(entity) + "\" appears at least twice.", 
						EMSLPackage.Literals.ATOMIC_PATTERN__SUPER_REFINEMENT_TYPES)
				
				} else if (e.errorType == FlattenerErrorType.NO_COMMON_SUBTYPE_OF_NODES) {
					error("The type " + e.nodeBlock.type.name + " in your refinements is not mergeable.",  
						EMSLPackage.Literals.ATOMIC_PATTERN__NAME)
					
				} else if (e.errorType == FlattenerErrorType.REFINE_ENTITY_WITH_CONDITION) {
					error("Entities with conditions cannot be refined.", 
						EMSLPackage.Literals.ATOMIC_PATTERN__SUPER_REFINEMENT_TYPES)
				
				} else if (e.errorType == FlattenerErrorType.PROPS_WITH_DIFFERENT_VALUES) {
					error("The value of " + e.property2.type.name + " does not match with your other refinements",
						EMSLPackage.Literals.ATOMIC_PATTERN__SUPER_REFINEMENT_TYPES)
				} else if (e.errorType == FlattenerErrorType.NON_COMPLIANT_SUPER_ENTITY) {
					var dispatcher = new EntityAttributeDispatcher()
					for (s : dispatcher.getSuperRefinementTypes(entity)) {
						if (!((s as RefinementCommand).referencedType instanceof AtomicPattern) && dispatcher.getSuperTypeName(e.superEntity).equals(dispatcher.getName((s as RefinementCommand).referencedType as Entity))) {
							error("The type of entity you are trying to refine is not supported.",
								entity.body,
								EMSLPackage.Literals.ATOMIC_PATTERN__SUPER_REFINEMENT_TYPES)
						} else if ((s as RefinementCommand).referencedType instanceof AtomicPattern && dispatcher.getSuperTypeName(e.superEntity).equals(dispatcher.getName((s as RefinementCommand).referencedType as AtomicPattern))) {
							error("The type of entity you are trying to refine is not supported.",
								entity.body,
								EMSLPackage.Literals.ATOMIC_PATTERN__SUPER_REFINEMENT_TYPES)
						}
					}
				}
			} else if (entity instanceof Rule) {
				if (e.errorType == FlattenerErrorType.INFINITE_LOOP) {
					error("You have created an infinite loop in your refinements. The pattern \"" + 
						new EntityAttributeDispatcher().getName(entity) + "\" appears at least twice.", 
						EMSLPackage.Literals.RULE__SUPER_REFINEMENT_TYPES)
				
				} else if (e.errorType == FlattenerErrorType.NO_COMMON_SUBTYPE_OF_NODES) {
					error("The type " + e.nodeBlock.type.name + " in your refinements is not mergeable.",  
						EMSLPackage.Literals.RULE__NAME)
					
				} else if (e.errorType == FlattenerErrorType.REFINE_ENTITY_WITH_CONDITION) {
					error("Entities with conditions cannot be refined.", 
						EMSLPackage.Literals.RULE__SUPER_REFINEMENT_TYPES)
				
				} else if (e.errorType == FlattenerErrorType.PROPS_WITH_DIFFERENT_VALUES) {
					error("The value of " + e.property2.type.name + " does not match with your other refinements",
						EMSLPackage.Literals.RULE__SUPER_REFINEMENT_TYPES)
				} else if (e.errorType == FlattenerErrorType.NON_COMPLIANT_SUPER_ENTITY) {
					var dispatcher = new EntityAttributeDispatcher()
					for (s : dispatcher.getSuperRefinementTypes(entity)) {
						if (!((s as RefinementCommand).referencedType instanceof AtomicPattern) && dispatcher.getSuperTypeName(e.superEntity).equals(dispatcher.getName((s as RefinementCommand).referencedType as Entity))) {
							error("The type of entity you are trying to refine is not supported.",
								entity,
								EMSLPackage.Literals.ATOMIC_PATTERN__SUPER_REFINEMENT_TYPES)
						} else if ((s as RefinementCommand).referencedType instanceof AtomicPattern && dispatcher.getSuperTypeName(e.superEntity).equals(dispatcher.getName((s as RefinementCommand).referencedType as AtomicPattern))) {
							error("The type of entity you are trying to refine is not supported.",
								entity,
								EMSLPackage.Literals.ATOMIC_PATTERN__SUPER_REFINEMENT_TYPES)
						}
					}
				}
			}
			if (e.errorType == FlattenerErrorType.NO_COMMON_SUBTYPE_OF_PROPERTIES) {
				error("The types of the properties you are trying to refine are not compatible. The types " + 
					(e.property1 as ModelPropertyStatement).type.name + " and " + 
					(e.property2 as ModelPropertyStatement).type.name + " must be the same.", 
					e.property2, 
					EMSLPackage.Literals.MODEL_PROPERTY_STATEMENT__TYPE)			
			} else if (e.errorType == FlattenerErrorType.NON_RESOLVABLE_PROXY) {
				for (nb : new EntityAttributeDispatcher().getNodeBlocks(entity)) {
					if (nb.name.equals((e.relation.eContainer as ModelNodeBlock).name)) {
						error("A proxy target you defined could not be resolved.",
							nb,
							EMSLPackage.Literals.MODEL_NODE_BLOCK__NAME
						)
					}
				}	
			}
		}
	}
	
}
