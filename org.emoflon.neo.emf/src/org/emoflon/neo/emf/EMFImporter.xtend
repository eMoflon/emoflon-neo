package org.emoflon.neo.emf

import java.io.File

import org.apache.commons.io.FileUtils

import org.eclipse.emf.ecore.EPackage

import org.eclipse.emf.ecore.resource.ResourceSet

import org.eclipse.emf.ecore.EClass
import java.nio.charset.Charset
import org.eclipse.emf.ecore.EEnum

/**
 * Transforms EMF to EMSL
 * Currently only metamodels, and also a very limited set of features (just meant as a stub).
 */
class EMFImporter {

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
								«var enumerals = n as EEnum»
								«FOR literals : enumerals.ELiterals»
									«literals.name»
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

}
