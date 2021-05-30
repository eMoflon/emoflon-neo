package run.emf.modelimport

import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.ResourceSet
import org.emoflon.neo.api.exttype2doc_concsync.API_Common
import org.emoflon.neo.api.exttype2doc_concsync.run.emf.modelimport.API_IbexToENeo
import org.emoflon.neo.emf.Neo4jImporter
import org.emoflon.neo.neocore.ENeoUtil
import run.ExtType2Doc_ConcSync_CO_Run
import org.apache.log4j.Logger

class EMFImportToENeo {
	
	static final Logger logger = Logger.getLogger(EMFImportToENeo)
	
	def static void main(String[] args) {
		try {
			loadModelsAndMetamodels("./emf/gen-models/presDel-scaled_n64_c1_H/", "src.xmi", "trg.xmi", "corr.xmi")
			val app = new ExtType2Doc_ConcSync_CO_Run()
			val result = app.runCheckOnly("src.xmi", "trg.xmi")
			logger.info("inconsistent elements: " + result.determineInconsistentElements)
		}
		catch(Exception e) {
			logger.info("Loading models and metamodels failed: " + e.message);
		}
	}

	private static def createResourceSet() {
		ENeoUtil.createEMSLStandaloneResourceSet(".")
	}

	private static def loadModel(ResourceSet rs, String uri, String label) {
		val resource = rs.getResource(URI.createURI(uri), true)
		resource.URI = URI.createURI(label)
	}

	private static def loadMetamodel(ResourceSet rs, String uri) {
		var resource = rs.getResource(URI.createURI(uri), true)
		val root = resource.contents.get(0) as EPackage
		resource.URI = URI.createURI(root.nsURI)
	}
	
	static def loadModelsAndMetamodels(String modelPath, String srcModel, String trgModel, String corrModel) {
		
		val builder = API_Common.createBuilder
		builder.clearDataBase
		
		val importer = new Neo4jImporter()

		val boltURL = "bolt://localhost:7687"
		val dbName = "neo4j"
		val passw = "test"

		val rs = createResourceSet()

		loadMetamodel(rs, "./emf/metamodels/ExtDocModel.ecore")
		loadMetamodel(rs, "./emf/metamodels/ExtTypeModel.ecore")
		loadMetamodel(rs, "./emf/metamodels/ExtType2Doc_ConcSync.ecore")

		loadModel(rs, modelPath + srcModel, srcModel)
		loadModel(rs, modelPath + trgModel, trgModel)
		loadModel(rs, modelPath + corrModel, corrModel)

		importer.importEMFModels(rs, boltURL, dbName, passw)

		val ruleAPI = new API_IbexToENeo(builder)

		for (rule : #{
			ruleAPI.rule_MigrateProject2DocContainer.rule,
			ruleAPI.rule_MigratePackage2Folder.rule,
			ruleAPI.rule_MigrateType2Doc.rule,
			ruleAPI.rule_MigrateMethod2Entry.rule,
			ruleAPI.rule_MigrateParam2Entry.rule,
			ruleAPI.rule_MigrateField2Entry.rule,
			ruleAPI.rule_MigrateJDoc2Annotation.rule
		}) {
			rule.applyAll(rule.determineMatches)
		}

		logger.info("Migrated all corrs")
	}
}
