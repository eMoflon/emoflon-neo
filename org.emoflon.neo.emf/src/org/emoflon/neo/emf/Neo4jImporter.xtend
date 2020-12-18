package org.emoflon.neo.emf

import java.util.ArrayList
import java.util.Collection
import java.util.HashMap
import java.util.HashSet
import java.util.List
import java.util.Map
import java.util.Set
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.Platform
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EEnum
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.EPackage
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.util.EContentsEList
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.emoflon.neo.api.org.emoflon.neo.emf.API_RulesForImporter
import org.emoflon.neo.cypher.models.NeoCoreBootstrapper
import org.emoflon.neo.cypher.models.NeoCoreBuilder
import org.emoflon.neo.cypher.models.templates.NeoProp
import org.emoflon.neo.cypher.models.templates.NodeCommand
import org.emoflon.neo.cypher.patterns.NeoMatch
import org.emoflon.neo.cypher.patterns.SyntheticNeoMatch
import org.emoflon.neo.cypher.rules.NeoCoMatch
import org.emoflon.neo.emsl.eMSL.EMSL_Spec
import org.emoflon.neo.neocore.util.NeoCoreConstants

import static org.emoflon.neo.neocore.util.NeoCoreConstants.NAME_PROP
import org.apache.log4j.Logger

class Neo4jImporter {
	static final Logger logger = Logger.getLogger(Neo4jImporter)
	
	def importEMFModels(ResourceSet rs, String boltURL, String dbName, String passw){
		val builder = new NeoCoreBuilder(boltURL, dbName, passw);
		builder.bootstrapNeoCoreIfNecessary()
		var spec = loadSpec()
		val api = new API_RulesForImporter(spec, builder)
		
		val allMetamodels = rs.allContents.filter(
			EObject
		).map[
			eClass.EPackage
		].toSet
		
		val allModels = rs.resources.filter[!it.URI.toString.endsWith(".ecore")]
		
		val metamodelsToImport = filterExistingMetamodels(builder, allMetamodels)
		val modelsToImport = filterExistingModels(builder, allModels)
		
		if(metamodelsToImport.empty && modelsToImport.empty){
			logger.info("All metamodels and metamodels are already present in the database.")
			return
		}
		
		val ecoreHandle = api.pattern_ECoreTypes
		val ecoreTypes = ecoreHandle.pattern.determineOneMatch.get
		val erefID = ecoreTypes.getElement(ecoreHandle._eref)
		val eclassID = ecoreTypes.getElement(ecoreHandle._eclass)
		
		logger.info("Importing metamodels: " + metamodelsToImport)
		if(!metamodelsToImport.empty)
			importMetamodels(api, metamodelsToImport, erefID)
		
		logger.info("Importing models: " + modelsToImport)
		if(!modelsToImport.empty){
			importModels(api, modelsToImport, eclassID) 
			importModelElements(builder, api, allModels)
		}
	}
	
	private def filterExistingMetamodels(NeoCoreBuilder builder, Iterable<EPackage> packages) {
		val result = builder.executeActionAsMatchTransaction[cb|
			var nc = cb.matchNode(List.of(), NeoCoreBootstrapper.LABELS_FOR_A_METAMODEL)
			cb.returnWith(nc)
		]
		
		val filteredPackages = new HashSet<EPackage>()
		filteredPackages.addAll(packages)
		result.forEachRemaining[mmNode|
			filteredPackages.removeIf[mm| mm.name == mmNode.get(0).get(NAME_PROP).asString()]
		]
				
		filteredPackages
	}
	
	private def filterExistingModels(NeoCoreBuilder builder, Iterable<Resource> resources) {
		val result = builder.executeActionAsMatchTransaction[cb|
			var nc = cb.matchNode(List.of(), NeoCoreBootstrapper.LABELS_FOR_A_MODEL)
			cb.returnWith(nc)
		]
		
		val filteredResources = new HashSet<Resource>()
		filteredResources.addAll(resources)
		result.forEachRemaining[mmNode|
			filteredResources.removeIf[rs| rs.URI.toString == mmNode.get(0).get(NAME_PROP).asString()]
		]
				
		filteredResources
	}
	
	private def importModelElements(NeoCoreBuilder builder, API_RulesForImporter api, Iterable<Resource> models){
		builder.executeActionAsCreateTransaction[cb|
			val objToCommand = new HashMap<EObject, NodeCommand>
			
			models.flatMap[
				it.allContents.toList
			].filter(
				EObject
			).forEach[
				val trg = cb.matchNode(computePropertiesForType(it.eClass), List.of("NeoCore__EClass"))
				val src = cb.createNode(computePropertiesForObject(it), computeLabelsForObjectOfType(it.eClass))
				cb.createEdge(NeoCoreConstants.META_TYPE, src, trg)
				objToCommand.put(it, src)
			]
			
			models.flatMap[
				it.allContents.toList
			].filter(
				EObject
			).forEach[src|				
				val contentsItr = src.eContents.iterator as EContentsEList.FeatureIterator<EObject>
				while(contentsItr.hasNext){
					val trg = contentsItr.next
					val labelOfRel = contentsItr.feature.name
					cb.createEdge(labelOfRel, objToCommand.get(src), objToCommand.get(trg))
 				}
 				
				val refsItr = src.eCrossReferences.iterator as EContentsEList.FeatureIterator<EObject>
 				while(refsItr.hasNext){
					val trg = refsItr.next
					val labelOfRel = refsItr.feature.name
					if(objToCommand.containsKey(src) && objToCommand.containsKey(trg))
						cb.createEdge(labelOfRel, objToCommand.get(src), objToCommand.get(trg))
 				}
			]
		]
	}
	
	def computePropertiesForType(EClass type) {
		List.of(new NeoProp("ename", type.name), new NeoProp("enamespace", type.EPackage.name))
	}
	
	def computePropertiesForObject(EObject object) {
		(object.eClass.EAllAttributes.filter[
			object.eGet(it) !== null
		].map[
			new NeoProp(it.name, object.eGet(it).toString)
		]
		+
		List.of(
			new NeoProp("ename", "o_" + object.hashCode), 
			new NeoProp("enamespace", object.eResource.URI.toString)
		)).toList
	}
	
	def computeLabelsForObjectOfType(EClass type) {
		(type.EAllSuperTypes + List.of(type)).map[
			'''«it.EPackage.name»__«it.name»'''
		].toList
	}
	
	private def importModels(API_RulesForImporter api, Iterable<Resource> models, long eclassID){
		createModels(api, models)
		connectModelsToMetamodels(api, models)
	}
	
	private def void connectModelsToMetamodels(API_RulesForImporter api, Iterable<Resource> models) {
		val handleConforms = api.rule_TypeModel
		
		models.forEach[m|
			m.allContents.map[
				it.eClass.EPackage
			].toSet.forEach[mm|
				val mask = handleConforms.mask
				mask.addParameter(handleConforms._param__modelName, m.URI.toString)
				mask.addParameter(handleConforms._param__metamodelName, mm.name)
				handleConforms.rule.apply(mask, mask)
			]
		]
	}
	
	private def Collection<NeoCoMatch> createModels(API_RulesForImporter api, Iterable<Resource> models) {
		val handle = api.rule_CreateModel
		val match = handle.rule.determineOneMatch.get
		
		val matches = models.map [
			val copy = new NeoMatch(match)
			copy.addParameter(handle._param__modelName, it.URI.toString)
			copy
		].toList
		
		handle.rule.applyAll(matches)
	}
	
	private def importMetamodels(API_RulesForImporter api, Set<EPackage> packages, long erefID) {
		createMetamodels(api, packages)
		val eClassToID = createEClasses(api, packages)
		createEnums(api, packages)
		createInheritances(api, eClassToID)
		createRelations(api, eClassToID, erefID)
		createAttributes(api, eClassToID)
	}
	
	private def createAttributes(API_RulesForImporter api, Map<EClass, Long> eClassToID) {		
		val handle = api.rule_CreateAttribute
		
		eClassToID.entrySet.forEach[entry|
			entry.key.EAttributes.forEach[
				val mask = handle.mask
				mask.addParameter(handle._param__attrName, it.name)
				mask.addParameter(handle._param__typeName, it.EAttributeType.name)
				mask.maskNode(handle._cls, entry.value)
				handle.rule.apply(mask, mask)
			]
		]
	}
	
	private def createRelations(API_RulesForImporter api, Map<EClass, Long> eClassToID, long erefID){
		val handle = api.rule_CreateRelation

		val matches = eClassToID.keySet.flatMap[src|
			src.EReferences.filter[ref|
				ref.name !== null &&
				eClassToID.containsKey(src) &&
				eClassToID.containsKey(ref.EReferenceType)
			].map[ref|
				val match = new SyntheticNeoMatch()
				match.addParameter(handle._param__refName, ref.name)
				match.setElement(
					eClassToID.get(src), handle._src
				).setElement(
					eClassToID.get(ref.EReferenceType), handle._trg
				).setElement(
					erefID, "____NeoCore__EReference"
				)
			]
		]
		
		val neoMatches = new ArrayList<NeoMatch>
		neoMatches.addAll(matches)
		handle.rule.applyAll(neoMatches)
	}
	
	private def createInheritances(API_RulesForImporter api, Map<EClass, Long> eClassToID) {
		val handle = api.rule_CreateInheritance 
		
		val matches = eClassToID.keySet.flatMap[sub|
			sub.ESuperTypes.map[sup |
				val match = new SyntheticNeoMatch()
				match.setElement(
					eClassToID.get(sub), handle._subClass
				).setElement(
					eClassToID.get(sup), handle._superClass
				)
			]
		].toList
		
		val neoMatches = new ArrayList<NeoMatch>
		neoMatches.addAll(matches)
		handle.rule.applyAll(neoMatches)
	}
	
	private def createEnums(API_RulesForImporter api, Set<EPackage> packages) {
		val handle = api.rule_CreateEnum
		val match = handle.rule.determineOneMatch.get
		val matchIDToEEnum = new HashMap<String, EEnum>
		
		val matches = packages.flatMap[
			it.EClassifiers.filter(
				EEnum
			).map[
				val copy = new NeoMatch(match)
				copy.addParameter(handle._param__name, it.name)
				copy.addParameter(handle._param__namespace, it.EPackage.name)
				matchIDToEEnum.put(copy.matchID, it)
				copy
			]
		].toList
		
		val eenumToID = new HashMap<EEnum, Long>
		val comatches = handle.rule.applyAll(matches)
		comatches.forEach[
			val eenum = matchIDToEEnum.get(it.matchID)
			eenumToID.put(eenum, it.getElement(handle._en))
		]
		
		val litHandle = api.rule_CreateEEnumLiteral
		packages.forEach[
			it.EClassifiers.filter(
				EEnum
			).forEach[en|
				en.ELiterals.forEach[lit|
					val mask = litHandle.mask
					mask.addParameter(litHandle._param__name, lit.name)
					mask.maskNode(litHandle._en, eenumToID.get(en))
					litHandle.rule.apply(mask, mask)
				]
			]
		]
	}
	
	private def createEClasses(API_RulesForImporter api, Set<EPackage> packages) {
		val handle = api.rule_CreateEClass
		val match = handle.rule.determineOneMatch()
		
		val matchIdToEClass = new HashMap<String, EClass>
		
		val matches = match.map[m |
			packages.flatMap[p|
				p.EClassifiers.filter(EClass)
			].map[
				val copy = new NeoMatch(m)
				copy.addParameter(handle._param__name, it.name)
				copy.addParameter(handle._param__namespace, it.EPackage.name)
				
				matchIdToEClass.put(copy.matchID, it)
				
				return copy
			].toList
		]
		
		val eClassToID = new HashMap<EClass, Long>
		
		// Apply all using rule
		matches.ifPresent[
			val coMatches = handle.rule.applyAll(it)
			coMatches.forEach[
				eClassToID.put(matchIdToEClass.get(it.matchID), it.getElement(handle._eclass))
			]
		]
		
		return eClassToID
	}
	
	private def void createMetamodels(API_RulesForImporter api, Set<EPackage> packages) {
		val handle = api.rule_CreateMetamodel
		
		// Create as many copies of matches as necessary
		packages.forEach[
			val mask = handle.mask
			mask.addParameter(handle._param__name, it.name)
			handle.apply(mask, mask)	
		]
	}
	
	private def EMSL_Spec loadSpec() {
		val bundle = Platform.getBundle("org.emoflon.neo.emf")
		val url = FileLocator.resolve(bundle.getEntry("/src/RulesForImporter.msl"))
		
		var XtextResourceSet resourceSet = new XtextResourceSet
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE)
		var Resource resource = resourceSet.getResource(URI.createURI(url.toString), true)
		var EMSL_Spec spec = (resource.getContents().get(0) as EMSL_Spec)
		EcoreUtil.resolveAll(resourceSet)
		spec
	}
	
}