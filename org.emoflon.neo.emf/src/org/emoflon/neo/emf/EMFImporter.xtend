package org.emoflon.neo.emf

import java.io.File

import org.apache.commons.io.FileUtils

import org.eclipse.emf.ecore.EPackage

import org.eclipse.emf.ecore.resource.ResourceSet

import org.eclipse.emf.ecore.EClass
import java.nio.charset.Charset
import org.eclipse.emf.ecore.EEnum
import org.eclipse.emf.ecore.EObject
import com.google.common.collect.Multimap
import com.google.common.collect.ArrayListMultimap
import java.util.HashMap

/**
 * Transforms EMF to EMSL
 * Currently only metamodels, and also a very limited set of features (just meant as a stub).
 */
class EMFImporter {
	
	HashMap<String , EEnum> metaModelEnum = new HashMap()
	
	def String generateEMSLSpecification(ResourceSet rs) {
		'''
			«FOR r : rs.resources»
				«FOR o : r.contents.filter[o | o instanceof EPackage] SEPARATOR "\n"»
					«var p = o as EPackage»
					metamodel «p.name» {
						«FOR c : p.EClassifiers.filter[c | c instanceof EClass] SEPARATOR "\n"»
							«var eclass = c as EClass»
							«var esupClass = ""»
							«FOR esuper: eclass.getESuperTypes()»«var esupname = esuper.name»«{esupClass = esupClass+esupname+", ";""}»«ENDFOR»
							«c.name» «IF esupClass != ""»: «esupClass.substring(0,esupClass.length-2)» «ENDIF»{
								«FOR attr : eclass.EAttributes»
									.«attr.name» : «attr.EType.name»
								«ENDFOR»
								«IF !eclass.EAttributes.isEmpty && !eclass.EReferences.isEmpty»
								
								«ENDIF»
								«FOR ref : eclass.EReferences»
									«IF ref.isContainment»
										<>-«ref.name»(«ref.lowerBound»..«IF ref.upperBound==-1»*«ELSE»«ref.upperBound»«ENDIF»)->«ref.EType.name»
									«ELSE»		
										-«ref.name»(«ref.lowerBound»..«IF ref.upperBound==-1»*«ELSE»«ref.upperBound»«ENDIF»)->«ref.EType.name»
									«ENDIF»
								«ENDFOR»
							}
							«ENDFOR»
						
						«FOR n : p.EClassifiers.filter[n | n instanceof EEnum] SEPARATOR "\n"»
						enum «n.name» {
							«var enumliterals = n as EEnum»
							«FOR literals : enumliterals.ELiterals»«{metaModelEnum.put(enumliterals.name,enumliterals);""}»
							«literals.name»
							«ENDFOR»
						}
						«ENDFOR»
					}
				«ENDFOR»
			«ENDFOR»
		'''
	}
	
	def String generateEMSLModel(ResourceSet rs) {
		var modelname = ""
		var objectCount = 1
		val Multimap<String, String> objName = ArrayListMultimap.create()
		val HashMap<String, String> objCreate = new HashMap()
		'''
			«generateEMSLSpecification(rs)»
			
			«FOR r : rs.resources»
				«{var filename = r.URI.segment(r.URI.segmentCount-1); modelname = filename.substring(0,filename.length-4);""}»
				«FOR o : r.contents.filter[o | o instanceof EObject && !(o instanceof EPackage)] SEPARATOR "\n"»
					«var p = o as EObject»
					model «modelname» {
						«FOR c : p.eContents»
							o«objectCount++»: «c.eClass.name» {«{objName.put(c.eClass.name,"o"+(objectCount-1));""}»
								«FOR attr : c.eClass.EAttributes»
									«IF attr.EType instanceof EEnum»«var attEnum = attr.EType as EEnum»
									.«attr.name» : «metaModelEnum.get(attEnum.name).getEEnumLiteralByLiteral(c.eGet(attr).toString).name»
									«ELSE»
									.«attr.name» : «IF attr.EType.name=="EString" || attr.EType.name=="EChar"»"«c.eGet(attr)»"«ELSE»«c.eGet(attr)»«ENDIF»
									«ENDIF»
								«ENDFOR»
								«IF !c.eClass.EAttributes.isEmpty && !c.eClass.EReferences.isEmpty»

								«ENDIF»
								«FOR ref : c.eClass.EReferences»
									-«ref.name» ->«IF !objName.containsKey(ref.EType.name)» o«objectCount++»«{objCreate.put(ref.EType.name,"o"+(objectCount-1));""}»«ELSE» «objName.get(ref.EType.name).findFirst[true]»«{objName.remove(ref.EType.name,objName.get(ref.EType.name).findFirst[true]);""}»«ENDIF»
								«ENDFOR»
							}
						«ENDFOR»
						o«objectCount++»: «p.eClass.name» {
						«FOR attr : p.eClass.EAttributes»
						«IF attr.EType instanceof EEnum»«var attEnum = attr.EType as EEnum»
							.«attr.name» : «metaModelEnum.get(attEnum.name).getEEnumLiteralByLiteral(p.eGet(attr).toString).name»
						«ELSE»
							.«attr.name» : «IF attr.EType.name=="EString" || attr.EType.name=="EChar"»"«p.eGet(attr)»"«ELSE»«p.eGet(attr)»«ENDIF»
						«ENDIF»
						«ENDFOR»
							«IF !p.eClass.EAttributes.isEmpty && !p.eClass.EReferences.isEmpty»
								
							«ENDIF»
							«FOR ref : p.eClass.EReferences»
								-«ref.name» ->«IF !objName.containsKey(ref.EType.name)» o«objectCount++»«{objCreate.put(ref.EType.name,"o"+(objectCount-1));""}»«ELSE» «objName.get(ref.EType.name).findFirst[true]»«{objName.remove(ref.EType.name,objName.get(ref.EType.name).findFirst[true]);""}»«ENDIF»
							«ENDFOR»							
						}
						«FOR obj : objCreate.entrySet»
							«obj.value»: «obj.key»{ 
								
							}
						«ENDFOR»
					}
				«ENDFOR»				
			«ENDFOR»
		'''
	}
	
	def saveEMSLSpecification(ResourceSet rs, File f) {
		FileUtils.writeStringToFile(f, generateEMSLSpecification(rs), Charset.defaultCharset())
	}

}
