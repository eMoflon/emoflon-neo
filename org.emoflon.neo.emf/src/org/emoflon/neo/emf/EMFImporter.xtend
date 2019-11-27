package org.emoflon.neo.emf

import java.io.File
import java.nio.charset.Charset
import java.util.HashMap
import org.apache.commons.io.FileUtils
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EEnum
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.ResourceSet

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
		var HashMap<EObject, String> objCreate = new HashMap()
		'''
			«generateEMSLSpecification(rs)»
			
			«FOR r : rs.resources»
				«FOR o : r.contents.filter[o | o instanceof EObject && !(o instanceof EPackage)] SEPARATOR "\n"»
					«var p = o as EObject»
					model «extractModelName(r.URI)» {
						«{objCreate = storeObjectsCreated(p as EObject);""}»
						«objCreate.get(p)» : «p.eClass.name» {
							«FOR attr : p.eClass.EAllAttributes»
								«IF attr.EType instanceof EEnum»«var attEnum = attr.EType as EEnum»
								.«attr.name» : «metaModelEnum.get(attEnum.name).getEEnumLiteralByLiteral(p.eGet(attr).toString).name»
								«ELSEIF p.eGet(attr)!== null»
								.«attr.name» : «IF attr.EType.name=="EString" || attr.EType.name=="EChar"»"«p.eGet(attr)»"«ELSE»«p.eGet(attr)»«ENDIF»
								«ENDIF»
							«ENDFOR»
							«IF !p.eClass.EAttributes.isEmpty && !p.eClass.EReferences.isEmpty»
							
							«ENDIF»
							«FOR ref : p.eClass.EAllReferences»
								«IF p.eGet(ref)!== null && p.eGet(ref) instanceof EList»
									«FOR reference : (p.eGet(ref) as EList<EObject>)»
										-«ref.name» -> «objCreate.get(reference)»
									«ENDFOR»
								«ELSEIF p.eGet(ref)!== null»
									-«ref.name» -> «objCreate.get(p.eGet(ref))»
								«ENDIF»
							«ENDFOR»
						}
						
						«FOR c: p.eAllContents.toIterable»
							«objCreate.get(c)» : «c.eClass.name» {
								«FOR attr : c.eClass.EAllAttributes»
									«IF attr.EType instanceof EEnum»«var attEnum = attr.EType as EEnum»
									.«attr.name» : «metaModelEnum.get(attEnum.name).getEEnumLiteralByLiteral(c.eGet(attr).toString).name»
									«ELSEIF c.eGet(attr)!== null»
									.«attr.name» : «IF attr.EType.name=="EString" || attr.EType.name=="EChar"»"«c.eGet(attr)»"«ELSE»«c.eGet(attr)»«ENDIF»
									«ENDIF»
								«ENDFOR»
								«IF !c.eClass.EAttributes.isEmpty && !c.eClass.EReferences.isEmpty»

								«ENDIF»
								«FOR ref : c.eClass.EAllReferences»
									«IF c.eGet(ref)!== null && c.eGet(ref) instanceof EList»
										«FOR reference : (c.eGet(ref) as EList<EObject>)»
											-«ref.name» -> «objCreate.get(reference)»
										«ENDFOR»
									«ELSEIF c.eGet(ref)!== null»
										-«ref.name» -> «objCreate.get(c.eGet(ref))»
									«ENDIF»
								«ENDFOR»
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
	
	def String extractModelName(URI uri) {
		var filename = uri.segment(uri.segmentCount-1);
		var modelname = filename.substring(0,filename.length-4);
		return modelname;
	}
	
	def HashMap<EObject, String> storeObjectsCreated(EObject p) {
		var HashMap<EObject, String> objects = new HashMap()
		var objectCount = 1
		objects.put(p,"o"+objectCount++)
		for(c: p.eAllContents.toIterable) {
			objects.put(c,"o"+objectCount++)
		}
		return objects
	}

}

