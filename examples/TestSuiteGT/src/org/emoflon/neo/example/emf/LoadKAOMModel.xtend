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
		register("kaom", new XMIResourceFactoryImpl())

		// Obtain resource set for loading
		val rs = ENeoUtil.createEMSLStandaloneResourceSet(".")

		// Load dependency for kaom metamodel
		rs.loadMetaModel("./models/annotations.ecore")

		// Load kaom metamodel
		rs.loadMetaModel("./models/kaom.ecore")

		// Load kaom model
		val aad1Resource = rs.loadModel("./models/aad1.kaom")

		// Ensure contents are non-trivial
		println(aad1Resource.contents)
		val rootEntity = aad1Resource.contents.get(0)
		// Do something with rootEntity...
		println(rootEntity.eContents.toList)

		// Push into Neo4j
		val importer = new Neo4jImporter()
		importer.importEMFModels(rs, "bolt://localhost:7687", "neo4j", "admin")
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
