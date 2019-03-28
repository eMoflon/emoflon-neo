package org.emoflon.neo.emf

import java.io.File
import org.apache.commons.io.FileUtils
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.ResourceSet

class EMFImporter {
	def String generateEMSLSpecification(ResourceSet rs) {
		'''
			«FOR r : rs.resources»
				«FOR p : r.contents.filter[o | o instanceof EPackage] SEPARATOR "\n"»
					metamodel «(p as EPackage).name» {
						// TODO [Deeksha]
					}
				«ENDFOR»
			«ENDFOR»
		'''
	}

	def saveEMSLSpecification(ResourceSet rs, File f) {
		FileUtils.writeStringToFile(f, generateEMSLSpecification(rs))
	}
}
