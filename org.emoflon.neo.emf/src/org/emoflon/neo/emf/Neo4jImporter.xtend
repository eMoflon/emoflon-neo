package org.emoflon.neo.emf

import java.util.Set
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.Platform
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.emoflon.neo.api.org.emoflon.neo.emf.API_RulesForImporter
import org.emoflon.neo.cypher.models.NeoCoreBuilder
import org.emoflon.neo.cypher.patterns.NeoMatch
import org.emoflon.neo.emsl.eMSL.EMSL_Spec

class Neo4jImporter {
	def importEMFModels(ResourceSet rs, String boltURL, String dbName, String passw){
		val allMetamodels = rs.allContents.filter(
			EObject
		).map[
			eClass.EPackage
		].toSet
		
		importMetamodels(allMetamodels, boltURL, dbName, passw)
		
		// TODO:  Import models
	}
	
	def importMetamodels(Set<EPackage> packages, String boltURL, String dbName, String passw) {
		val builder = new NeoCoreBuilder(boltURL, dbName, passw);
		builder.bootstrapNeoCoreIfNecessary()
		
		val bundle = Platform.getBundle("org.emoflon.neo.emf")
		val url = FileLocator.resolve(bundle.getEntry("/src/RulesForImporter.msl"))
		
		var XtextResourceSet resourceSet = new XtextResourceSet
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE)
		var Resource resource = resourceSet.getResource(URI.createURI(url.toString), true)
		var EMSL_Spec spec = (resource.getContents().get(0) as EMSL_Spec)
		EcoreUtil.resolveAll(resourceSet)

		val api = new API_RulesForImporter(spec, builder)
		val match = api.rule_ImportMetamodel.rule.determineOneMatch()
		
		// Create as many copies of matches as necessary
		val matches = match.map[m |
			packages.map[
				val copy = new NeoMatch(m)
				// Set parameter for each match
				copy.addParameter("name", it.name)
				return copy
			].toList
		]
		
		// Apply all using rule
		matches.ifPresent[
			api.rule_ImportMetamodel.rule.applyAll(it)
		]
	}
	
}