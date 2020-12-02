package org.emoflon.neo.emf

import java.util.Set
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.ResourceSet
import org.emoflon.neo.api.org.emoflon.neo.emf.API_Common
import org.emoflon.neo.api.org.emoflon.neo.emf.API_RulesForImporter
import org.emoflon.neo.cypher.models.NeoCoreBuilder
import org.emoflon.neo.cypher.patterns.NeoMatch

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
		
		val api = new API_RulesForImporter(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED, false)
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