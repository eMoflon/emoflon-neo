package org.emoflon.neo.emf

import java.io.File

import org.apache.commons.io.FileUtils

import org.eclipse.emf.ecore.EPackage

import org.eclipse.emf.ecore.resource.ResourceSet

import org.eclipse.emf.ecore.EClass

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
							«c.name» {
							«var eclass = c as EClass»
								«FOR attr : eclass.EAttributes»
									.«attr.name»: «attr.EType.name»
								«ENDFOR»
								«IF !eclass.EAttributes.isEmpty && !eclass.EReferences.isEmpty»
								
								«ENDIF»
								«FOR ref : eclass.EReferences»
									-«ref.name»(«ref.lowerBound»..«IF ref.upperBound==-1»*«ELSE»«ref.upperBound»«ENDIF»)->«ref.EType.name»
								«ENDFOR»
							}
						«ENDFOR»
					}
				«ENDFOR»
			«ENDFOR»
		'''
	}

	def saveEMSLSpecification(ResourceSet rs, File f) {
		FileUtils.writeStringToFile(f, generateEMSLSpecification(rs))
	}

}
