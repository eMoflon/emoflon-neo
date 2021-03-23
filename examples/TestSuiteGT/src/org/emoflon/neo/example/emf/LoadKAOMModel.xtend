package org.emoflon.neo.example.emf

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl
import org.emoflon.neo.emf.Neo4jImporter
import org.emoflon.neo.neocore.ENeoUtil

import static org.junit.Assert.*

class LoadKAOMModel {
	def static void main(String[] args) {
		// Register factories for ecore and kaom
		register("ecore", new EcoreResourceFactoryImpl())
		register("xmi", new XMIResourceFactoryImpl())

		// Obtain resource set for loading
		val rs = ENeoUtil.createEMSLStandaloneResourceSet(".")

		// Load dependency for Company and IT metamodel
		rs.loadMetaModel("./resources/in/metamodel/CompanyLanguage.ecore")
		rs.loadMetaModel("./resources/in/metamodel/ITLanguage.ecore")

		// Load Company and IT model
		val companyResource = rs.loadModel("./resources/in/model/Company.xmi")
		val itResource = rs.loadModel("./resources/in/model/IT.xmi")

		// Ensure contents are non-trivial
		println(companyResource.contents)
		println(itResource.contents)
		val companyRootEntity = companyResource.contents.get(0)
		val itRootEntity = companyResource.contents.get(0)
		// Do something with rootEntity...
		println(companyRootEntity.eContents.toList)
		println(itRootEntity.eContents.toList)

		// Push into Neo4j
		val importer = new Neo4jImporter()
		importer.importEMFModels(rs, "bolt://localhost:7687", "neo4j", "test")
	}

	static def void register(String ext, Object factory) {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put(ext, factory)
	}

	static def Resource loadModel(ResourceSet rs, String pathToModel) {
		val uri = URI.createFileURI(pathToModel)
		val resource = rs.getResource(uri, true)
		assertTrue(!resource.contents.empty)
		return resource
	}

	static def void loadMetaModel(ResourceSet rs, String pathToModel) {
		val resource = loadModel(rs, pathToModel)
		val root = resource.contents.get(0) as EPackage
		resource.URI = URI.createURI(root.nsURI)
	}
}
