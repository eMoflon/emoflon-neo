package org.emoflon.neo.emf

import java.io.File
import java.nio.charset.Charset
import java.util.HashMap
import org.apache.commons.io.FileUtils
import org.eclipse.emf.common.util.EList
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EAttribute
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EEnum
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.ResourceSet
import org.emoflon.neo.emsl.util.EMSLUtil

/**
 * Transforms EMF to EMSL
 * Currently only metamodels, and also a very limited set of features (just meant as a stub).
 */
class EMFImporter {
	
	HashMap<String , EEnum> metaModelEnum = new HashMap()	
		
	def String escapeKeyWords(String s){
		EMSLUtil.escapeKeyWords(s)
	}
		
	def String generateEMSLSpecification(ResourceSet rs) {
		'''
			«FOR r : rs.resources»
				«FOR p : r.contents.filter(EPackage) SEPARATOR "\n"»
					metamodel «p.name.escapeKeyWords» {
						«FOR eclass : p.EClassifiers.filter(EClass) SEPARATOR "\n"»
							«eclass.name.escapeKeyWords» «IF !eclass.ESuperTypes.empty»: «eclass.ESuperTypes.map[it.name].join(", ")» «ENDIF»{
								«FOR attr : eclass.EAttributes»
									.«attr.name.escapeKeyWords» : «attr.EType.name.escapeKeyWords»
								«ENDFOR»
								
								«FOR ref : eclass.EReferences»
									«IF ref.isContainment»
										<>-«ref.name.escapeKeyWords»(«ref.lowerBound»..«IF ref.upperBound==-1»*«ELSE»«ref.upperBound»«ENDIF»)->«ref.EType.name.escapeKeyWords»
									«ELSE»		
										-«ref.name.escapeKeyWords»(«ref.lowerBound»..«IF ref.upperBound==-1»*«ELSE»«ref.upperBound»«ENDIF»)->«ref.EType.name.escapeKeyWords»
									«ENDIF»
								«ENDFOR»
							}
						«ENDFOR»
						
						«FOR eenum : p.EClassifiers.filter(EEnum) SEPARATOR "\n"»
							enum «registerEnum(eenum).escapeKeyWords» {
								«FOR literal : eenum.ELiterals»
									«literal.name.escapeKeyWords»
								«ENDFOR»
							}
						«ENDFOR»
					}
				«ENDFOR»
			«ENDFOR»
		'''
	}
	
	def String registerEnum(EEnum eenum) {
		metaModelEnum.put(eenum.name, eenum)
		eenum.name
	}
	
	def String generateEMSLModels(ResourceSet rs) {
		var HashMap<EObject, String> objIDs = createObjectIDs(rs)
		'''
			«generateEMSLSpecification(rs)»
			
			«FOR r : rs.resources»
				«FOR rootOfModel : r.contents.filter[o | o instanceof EObject && !(o instanceof EPackage)] SEPARATOR "\n"»
					model «extractModelName(r.URI)» {
						«FOR modelElt : rootOfModel.eAllContents.toIterable + #{rootOfModel}»
							«objIDs.get(modelElt)» : «modelElt.eClass.name.escapeKeyWords» {
								«FOR attr : modelElt.eClass.EAllAttributes»
								«IF attr.EType instanceof EEnum»
									.«attr?.name.escapeKeyWords» : «getUserDefinedValue(attr, modelElt)»
								«ELSEIF modelElt.eGet(attr) !== null»
									.«attr?.name.escapeKeyWords» : «getBuiltInValue(attr, modelElt)»
								«ENDIF»
							«ENDFOR»
							
								«FOR ref : modelElt.eClass.EAllReferences»
								«IF modelElt.eGet(ref) !== null && modelElt.eGet(ref) instanceof EList»
									«FOR reference : (modelElt.eGet(ref) as EList<EObject>)»
										-«ref.name.escapeKeyWords» -> «objIDs.get(reference)»
									«ENDFOR»
								«ELSEIF modelElt.eGet(ref) !== null»
									-«ref.name.escapeKeyWords» -> «objIDs.get(modelElt.eGet(ref))»
								«ENDIF»
							«ENDFOR»
							}
						«ENDFOR»
					}
				«ENDFOR»				
			«ENDFOR»
		'''
	}
	
	def getBuiltInValue(EAttribute attr, EObject modelElt) {
		val value = modelElt.eGet(attr)
		if(attr.EType.name.escapeKeyWords=="EString" || attr.EType.name.escapeKeyWords=="EChar")
			'''"«value»"'''
		else 
			value
	}
	
	def getUserDefinedValue(EAttribute attr, EObject modelElt) {
		metaModelEnum.get(attr.EType.name)?.getEEnumLiteralByLiteral(modelElt.eGet(attr)?.toString)?.name.escapeKeyWords
	}
	
	def saveEMSLSpecification(ResourceSet rs, File f) {
		FileUtils.writeStringToFile(f, generateEMSLSpecification(rs), Charset.defaultCharset())
	}
	
	def String extractModelName(URI uri) {
		var filename = uri.segment(uri.segmentCount-1);
		var modelname = filename.substring(0,filename.length-4);
		return modelname;
	}
	
	def HashMap<EObject, String> createObjectIDs(ResourceSet rs) {
		val HashMap<EObject, String> objIDs = new HashMap()
		rs.allContents.filter(EObject).forEach[
			objIDs.put(it, "o" + objIDs.size)
		]
		
		return objIDs
	}

}

