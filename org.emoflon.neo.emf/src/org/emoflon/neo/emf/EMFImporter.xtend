package org.emoflon.neo.emf

import java.io.File
import org.apache.commons.io.FileUtils
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.EClass

class EMFImporter {
	def String generateEMSLSpecification(ResourceSet rs) {
		'''
			import "platform:/plugin/org.emoflon.neo.neocore/model/NeoCore.msl"
			
			«FOR r : rs.resources»
				«FOR o : r.contents.filter[o | o instanceof EPackage] SEPARATOR "\n"»
					«var p = o as EPackage»
					metamodel «p.name» {
						«FOR c : p.EClassifiers.filter[c | c instanceof EClass] SEPARATOR "\n"»
							«c.name»:EClass {
								
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
