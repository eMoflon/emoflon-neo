/*
 * generated by Xtext 2.16.0
 */
package org.emoflon.neo.emsl.generator

import java.net.URI
import java.util.ArrayList
import java.util.List
import java.util.function.Predicate
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.preferences.InstanceScope
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.ui.preferences.ScopedPreferenceStore
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.emoflon.neo.emsl.compiler.RefreshFilesJob
import org.emoflon.neo.emsl.compiler.TGGCompiler
import org.emoflon.neo.emsl.eMSL.ActionOperator
import org.emoflon.neo.emsl.eMSL.AtomicPattern
import org.emoflon.neo.emsl.eMSL.Constraint
import org.emoflon.neo.emsl.eMSL.EMSL_Spec
import org.emoflon.neo.emsl.eMSL.Entity
import org.emoflon.neo.emsl.eMSL.GraphGrammar
import org.emoflon.neo.emsl.eMSL.Metamodel
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock
import org.emoflon.neo.emsl.eMSL.Model
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement
import org.emoflon.neo.emsl.eMSL.Pattern
import org.emoflon.neo.emsl.eMSL.Rule
import org.emoflon.neo.emsl.eMSL.TripleGrammar
import org.emoflon.neo.emsl.eMSL.TripleRule
import org.emoflon.neo.emsl.refinement.EMSLFlattener
import org.emoflon.neo.emsl.util.ClasspathUtil
import org.emoflon.neo.emsl.util.EMSLUtil

/**
 * Generates code from your model files on save.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#code-generation
 */
class EMSLGenerator extends AbstractGenerator {
	public static final String TGG_GEN_FOLDER = "tgg-gen"
	public static final String SRC_GEN_Folder = "src-gen"
	public static final String API_ROOT = "org/emoflon/neo/api/"
	EMSL_Spec emslSpec
	List<String> derivedGTFiles = new ArrayList
	boolean cleanedUp

	override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {				
		if(resource.contents.isEmpty) return

		val apiPath = getAPIPath(resource)
		val apiName = getAPIName(resource)
		val apiFile = getAPIFileName(resource)

		emslSpec = resource.contents.get(0) as EMSL_Spec
		emslSpec.entities.filter[it instanceof TripleGrammar].map[it as TripleGrammar].forEach [
			derivedGTFiles.addAll(new TGGCompiler(it, apiPath, apiName).compileAll(fsa))
		]

		fsa.generateFile(API_ROOT + "API_Common.java", generateCommon())
		fsa.generateFile(API_ROOT + apiPath + "/" + apiFile + ".java",
			generateAPIFor(apiFile, apiPath, emslSpec, resource))
			
		cleanedUp = true
	}

	def getAPIName(Resource resource) {
		val segments = resource.URI.trimFileExtension.segmentsList
		return segments.last
	}

	def getAPIFileName(Resource resource) {
		return "API_" + getAPIName(resource)
	}

	def getAPIPath(Resource resource) {
		val segments = resource.URI.trimFileExtension.segmentsList

		// Always remove:  resource/projectName
		var prefixSegments = 2
		// If the msl file is nested any deeper, also remove first folder (typically src)
		if (segments.size > 3)
			prefixSegments++

		val apiPath = segments //
		.drop(prefixSegments).take(segments.size - (prefixSegments + 1)) // only take path up to EMSL file
		.join("/");

		return apiPath
	}

	override void afterGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		if (cleanedUp) {
			val segments = resource.URI.trimFileExtension.segmentsList
			val projectName = segments.get(1)
			val project = ResourcesPlugin.workspace.root.getProject(projectName)
			val srcFolder = project.getFolder(SRC_GEN_Folder)
			val tggFolder = project.getFolder(TGG_GEN_FOLDER)

			ClasspathUtil.setUpAsJavaProject(project)
			ClasspathUtil.setUpAsPluginProject(project)
			ClasspathUtil.setUpAsXtextProject(project)
			ClasspathUtil.addDependencies(project, List.of("org.emoflon.neo.neo4j.adapter"))
			ClasspathUtil.addDependencies(project, List.of("org.emoflon.neo.engine.modules"))
			ClasspathUtil.addDependencies(project, List.of("org.eclipse.xtext"))
			ClasspathUtil.addDependencies(project, List.of("org.apache.commons.logging"))
			ClasspathUtil.addDependencies(project, List.of("org.apache.log4j"))
			ClasspathUtil.addDependencies(project, List.of("org.apache.commons.lang"))
			ClasspathUtil.makeSourceFolderIfNecessary(srcFolder)
			ClasspathUtil.makeSourceFolderIfNecessary(tggFolder)

			createRefreshJob(derivedGTFiles.map[project.getFile(it)])
		}

		cleanedUp = false
		derivedGTFiles.clear
	}
	
	private def void createRefreshJob(List<IFile> generatedFiles){
		if(!generatedFiles.isEmpty)
			new RefreshFilesJob(generatedFiles).schedule()
	}

	def generateCommon() {
		val store = new ScopedPreferenceStore(InstanceScope.INSTANCE, EMSLUtil.UI_PLUGIN_ID)

		val uri = store.getString(EMSLUtil.P_URI);
		val userName = store.getString(EMSLUtil.P_USER);
		val password = store.getString(EMSLUtil.P_PASSWORD);

		'''
			/** 
			 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
			 */
			package org.emoflon.neo.api;			
			import org.emoflon.neo.cypher.models.*;
			
			public class API_Common {
				// Default values (might have to be changed)
				public static final String PLATFORM_PLUGIN_URI = "«getInstallLocation»";
				public static final String NEOCORE_URI_INSTALLED = "«getNeoCoreURIInstalled»";
				public static final String PLATFORM_RESOURCE_URI = "../";
			
				public static NeoCoreBuilder createBuilder() {
					return new NeoCoreBuilder("«uri»", "«userName»", "«password»");
				}
			}
		'''
	}

	private def getInstallLocation() {
		val plugin = Platform.getBundle("org.emoflon.neo.neocore");
		val fileURL = FileLocator.resolve(plugin.getEntry("/")).toString
		val fileURI = new URI(fileURL.replace(" ", "%20")).normalize
		val segments = fileURI.path.split("/")
		val path = segments.take(segments.length - 1)
		path.join("/") + "/"
	}
	
	private def getNeoCoreURIInstalled(){
		val plugin = Platform.getBundle("org.emoflon.neo.neocore");
		val fileURL = FileLocator.resolve(plugin.getEntry("/model/NeoCore.msl")).toString
		val fileURI = new URI(fileURL.replace(" ", "%20")).normalize
		return fileURI.path
	}

	def generateAPIFor(String apiName, String apiPath, EMSL_Spec spec, Resource resource) {
		'''
			/** 
			 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
			 */
			package org.emoflon.neo.api«subPackagePath(apiPath)»;
			
			import org.emoflon.neo.cypher.common.*;
			import org.emoflon.neo.cypher.factories.*;
			import org.emoflon.neo.cypher.models.*;
			import org.emoflon.neo.cypher.patterns.*;
			import org.emoflon.neo.cypher.rules.*;
			import org.emoflon.neo.engine.api.patterns.*;
			import org.emoflon.neo.engine.api.constraints.*;
			import org.emoflon.neo.engine.api.rules.*;
			import org.emoflon.neo.emsl.eMSL.*;
			import org.emoflon.neo.emsl.util.*;
			import org.neo4j.driver.v1.Value;
			import org.neo4j.driver.v1.Record;
			import org.eclipse.emf.common.util.URI;
			import org.emoflon.neo.api.API_Common;
			import java.util.Collection;
			import java.util.HashSet;
			import java.util.HashMap;
			import java.util.Map;
			import java.util.stream.Stream;
			import java.util.Optional;
			import java.time.LocalDate;
			
			@SuppressWarnings("unused")
			public class «apiName» {
				private EMSL_Spec spec;
				private NeoCoreBuilder builder;
			
				/** Use this constructor for default values */
				public «apiName»(NeoCoreBuilder builder) {
					this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
				}
			
				/** Use this constructor to configure values for loading EMSL files */
				public «apiName»(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
					spec = (EMSL_Spec) EMSLUtil.loadSpecification("«resource.URI»", platformResourceURIRoot, platformPluginURIRoot, neocoreURI);
					this.builder = builder;
				}
			
				«FOR e : spec.entities SEPARATOR "\n"»
					//:~> «resource.URI»#«resource.getURIFragment(e)»
					«generateAccess(e, spec.entities.indexOf(e))»
				«ENDFOR»
			}
		'''
	}

	def subPackagePath(String apiPath) {
		if (apiPath.length > 0)
			"." + apiPath.replace("/", ".")
		else
			""
	}

	dispatch def generateAccess(Entity e, int index) {
		''''''
	}

	dispatch def generateAccess(Pattern p, int index) {
		if(p.body.abstract) return ""
		try {
			val patternBody = EMSLFlattener.flatten(p.body) as AtomicPattern
			val rootName = namingConvention(patternBody.name)
			val dataClassName = rootName + "Data"
			val accessClassName = rootName + "Access"
			val maskClassName = rootName + "Mask"
			'''
				public «accessClassName» getPattern_«rootName»() {
					return new «accessClassName»();
				}
				
				public class «accessClassName» extends NeoPatternAccess<«dataClassName», «maskClassName»> {
					«FOR node : patternBody.nodeBlocks»
						public final String _«node.name» = "«node.name»";
					«ENDFOR»
					
					@Override
					public NeoPattern pattern(){
						var p = (Pattern) spec.getEntities().get(«index»);
						return NeoPatternFactory.createNeoPattern(p, builder);
					}
					
					@Override
					public Stream<«dataClassName»> data(Collection<NeoMatch> matches) {
						var data = NeoMatch.getData(matches);
						return data.stream().map(d -> new «dataClassName»(d));
					}
					
					@Override
					public «maskClassName» mask() {
						return new «maskClassName»();
					}
				}
				
				public class «dataClassName» extends NeoData {
					«classMembers(patternBody.nodeBlocks)»
					
					«constructor(dataClassName, patternBody.nodeBlocks)»
					
					«helperClasses(patternBody.nodeBlocks)»
				}
				
				public class «maskClassName» extends NeoMask {
					«maskMethods(patternBody.nodeBlocks, maskClassName)»
				
				}
			'''
		} catch (Exception e) {
			e.printStackTrace
			'''//FIXME Unable to generate API: «e.toString»  */ '''
		}
	}

	protected def CharSequence helperClasses(Iterable<ModelNodeBlock> nodeBlocks) {
		helperClasses(nodeBlocks, [true], [true])
	}

	protected def CharSequence helperClasses(Iterable<ModelNodeBlock> nodeBlocks, Predicate<ModelNodeBlock> nodeFilter,
		Predicate<ModelRelationStatement> edgeFilter) '''
		«FOR node : nodeBlocks.filter(nodeFilter)»
			«helperNodeClass(node)»
			
			«FOR rel : node.relations.filter(edgeFilter).filter[!EMSLUtil.isVariableLink(it)]»
				«helperRelClass(node, rel)»
			«ENDFOR»
		«ENDFOR»
	'''

	protected def CharSequence helperRelClass(ModelNodeBlock node, ModelRelationStatement rel) {
		val relName = EMSLUtil.relationNameConvention(node.name, rel.allTypes, rel.target.name,
			node.relations.indexOf(rel))
		'''
			public class «relName.toFirstUpper»Rel {
				«FOR prop : rel.types.flatMap[it.type.properties]»
					public «EMSLUtil.getJavaType(prop.type)» _«prop.name»;
				«ENDFOR»
			
				public «relName.toFirstUpper»Rel(Value _«relName») {
					«FOR prop : rel.types.flatMap[it.type.properties]»
						if(!_«relName».get("«prop.name»").isNull())
							this._«prop.name» = _«relName».get("«prop.name»").as«EMSLUtil.getJavaType(prop.type).toFirstUpper»();
					«ENDFOR»
				}
			}
		'''
	}

	def getAllTypes(ModelRelationStatement rel) {
		EMSLUtil.getAllTypes(rel)
	}

	protected def CharSequence helperNodeClass(ModelNodeBlock node) '''
		public class «node.name.toFirstUpper»Node {
			«FOR prop : allProperties(node.type)»
				public «EMSLUtil.getJavaType(prop.type)» _«prop.name»;
			«ENDFOR»
			
			public «node.name.toFirstUpper»Node(Value _«node.name») {
				«FOR prop : allProperties(node.type)»
					if(!_«node.name».get("«prop.name»").isNull())
						this._«prop.name» = _«node.name».get("«prop.name»").as«EMSLUtil.getJavaType(prop.type).toFirstUpper»();
				«ENDFOR»
			}
		}
	'''

	protected def CharSequence constructor(String fileName, Iterable<ModelNodeBlock> nodeBlocks) {
		constructor(fileName, nodeBlocks, [true], [true])
	}

	protected def CharSequence constructor(String fileName, Iterable<ModelNodeBlock> nodeBlocks,
		Predicate<ModelNodeBlock> nodeFilter, Predicate<ModelRelationStatement> edgeFilter) '''
		public «fileName»(Record data) {
			«FOR node : nodeBlocks.filter(nodeFilter)»
				var _«node.name» = data.get("«node.name»");
				this._«node.name» = new «node.name.toFirstUpper»Node(_«node.name»);
				«FOR rel : node.relations.filter(edgeFilter).filter[!EMSLUtil.isVariableLink(it)]»
					«val relName = EMSLUtil.relationNameConvention(//
						node.name,// 
						rel.allTypes,//
						rel.target.name,// 
						node.relations.indexOf(rel))»
					var _«relName» = data.get("«relName»");
					this._«relName» = new «relName.toFirstUpper»Rel(_«relName»);
				«ENDFOR»			
			«ENDFOR»
		}
		
	'''

	def CharSequence classMembers(Iterable<ModelNodeBlock> nodeBlocks) {
		classMembers(nodeBlocks, [true], [true])
	}

	def CharSequence classMembers(Iterable<ModelNodeBlock> nodeBlocks, Predicate<ModelNodeBlock> nodeFilter,
		Predicate<ModelRelationStatement> edgeFilter) {
		'''
			«FOR node : nodeBlocks.filter(nodeFilter)»
				public final «node.name.toFirstUpper»Node _«node.name»;
				«FOR rel : node.relations.filter(edgeFilter).filter[!EMSLUtil.isVariableLink(it)]»
					«val relName = EMSLUtil.relationNameConvention(//
						node.name,// 
						rel.allTypes,//
						rel.target.name,// 
						node.relations.indexOf(rel))»
					public final «relName.toFirstUpper»Rel _«relName»;
				«ENDFOR»
			«ENDFOR»
		'''
	}

	def CharSequence maskMethods(Iterable<ModelNodeBlock> nodeBlocks, String maskClassName) {
		'''
			«FOR node : nodeBlocks»
				public «maskClassName» set«node.name.toFirstUpper»(Long value) {
					nodeMask.put("«node.name»", value);
					return this;
				}
				«FOR prop : allProperties(node.type)»
					public «maskClassName» set«node.name.toFirstUpper»«prop.name.toFirstUpper»(«EMSLUtil.getJavaType(prop.type)» value) {
						attributeMask.put("«node.name».«prop.name»", value);
						return this;
					}
				«ENDFOR»
				«FOR rel : node.relations.filter[!EMSLUtil.isVariableLink(it)]»
					«FOR prop : rel.types.flatMap[it.type.properties]»
						«val relName = EMSLUtil.relationNameConvention(//
											node.name,// 
											rel.allTypes,//
											rel.target.name,// 
											node.relations.indexOf(rel))»
						public «maskClassName» set«relName.toFirstUpper»«prop.name.toFirstUpper»(«EMSLUtil.getJavaType(prop.type)» value) {
							attributeMask.put("«relName».«prop.name»", value);
							return this;
						}
					«ENDFOR»
				«ENDFOR»
			«ENDFOR»
		'''
	}

	def allProperties(MetamodelNodeBlock nb) {
		EMSLUtil.thisAndAllSuperTypes(nb).flatMap[it.properties]
	}

	dispatch def generateAccess(GraphGrammar gg, int index) {
		if(gg.abstract) return ""
		try {
			'''
				public Collection<NeoRule> getAllRulesFor«namingConvention(gg.name)»() {
					Collection<NeoRule> rules = new HashSet<>();
					
					«FOR rule : gg.rules»
						«FOR access : emslSpec.entities.filter[it instanceof Rule && (it as Rule).name.equals(rule.name)]»
							rules.add(getRule_«namingConvention((access as Rule).name)»().rule());
						«ENDFOR»
					«ENDFOR»
					return rules;
				}
				
				public Collection<Rule> getAllEMSLRulesFor«namingConvention(gg.name)»(){
					var rules = new HashSet<Rule>();
					«FOR r : emslSpec.entities.filter[it instanceof Rule]»
						rules.add((Rule) spec.getEntities().get(«emslSpec.entities.indexOf(r)»));
					«ENDFOR»
					return rules;
				}
			'''
		} catch (Exception e) {
			e.printStackTrace
			'''//FIXME Unable to generate API: «e.toString»  */ '''
		}
	}

	dispatch def generateAccess(TripleGrammar tgg, int index) {
		if(tgg.abstract) return ""
		try {
			val rootName = namingConvention(tgg.name)
			'''
				public void exportMetamodelsFor«rootName»() throws FlattenerException {
					«FOR mm : tgg.srcMetamodels + tgg.trgMetamodels»
						«val apiPath = API_ROOT + getAPIPath(mm.eResource) + "/" + getAPIFileName(mm.eResource)»
						«val apiFQN = apiPath.replace("/", ".").replace("..", ".")»
						«val mmName = namingConvention(mm.name)»
						{
							var api = new «apiFQN»(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
							builder.exportEMSLEntityToNeo4j(api.getMetamodel_«mmName»());
						}
					«ENDFOR»
				}
				
				public Collection<TripleRule> getTripleRulesOf«rootName»(){
					var rules = new HashSet<TripleRule>();
					var rs = spec.eResource().getResourceSet();
					«FOR tr : tgg.rules»
					{
						var uri = "«EcoreUtil.getURI(tr)»";
						rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
					}
					«ENDFOR»
					return rules;
				}
			'''
		} catch (Exception e) {
			e.printStackTrace
			'''//FIXME Unable to generate API: «e.toString»  */ '''
		}
	}

	dispatch def generateAccess(TripleRule tr, int index) {
		if (tr.abstract)
			return ""
		else
			'''
				public static final String «tr.type.name»__«tr.name» = "«tr.name»";
				«FOR node : tr.srcNodeBlocks+tr.trgNodeBlocks»
					public static final String «tr.type.name»__«tr.name»__«node.name» = "«node.name»";
				«ENDFOR»
			'''
	}

	dispatch def generateAccess(Rule r, int index) {
		if(r.abstract) return ""
		try {
			val rule = EMSLFlattener.flatten(r) as Rule;
			val rootName = namingConvention(rule.name)
			val dataClassName = rootName + "Data"
			val codataClassName = rootName + "CoData"
			val accessClassName = rootName + "Access"
			val maskClassName = rootName + "Mask"
			'''
				public «accessClassName» getRule_«rootName»() {
					return new «accessClassName»();
				}
				
				public class «accessClassName» extends NeoRuleCoAccess<«dataClassName», «codataClassName», «maskClassName»> {
					«FOR node : rule.nodeBlocks»
						public final String _«node.name» = "«node.name»";
					«ENDFOR»
					
					@Override
					public NeoRule rule(){
						var r = (Rule) spec.getEntities().get(«index»);
						return NeoRuleFactory.createNeoRule(r, builder);
					}
					
					@Override
					public Stream<«dataClassName»> data(Collection<NeoMatch> matches) {
						var data = NeoMatch.getData(matches);
						return data.stream().map(d -> new «dataClassName»(d));
					}
						
					@Override
					public Stream<«codataClassName»> codata(Collection<NeoCoMatch> matches) {
						var data = NeoMatch.getData(matches);
						return data.stream().map(d -> new «codataClassName»(d));
					}
					
					@Override
					public «maskClassName» mask() {
						return new «maskClassName»();
					}
				}
				
				public class «dataClassName» extends NeoData {
					«val blackAndRedNodes = [ModelNodeBlock n | n.action === null || n.action.op == ActionOperator.DELETE]»
					«val blackAndRedEdges = [ModelRelationStatement e | e.action === null || e.action.op == ActionOperator.DELETE]»
					«classMembers(rule.nodeBlocks, blackAndRedNodes, blackAndRedEdges)»
					
					«constructor(dataClassName, rule.nodeBlocks, blackAndRedNodes, blackAndRedEdges)»
					
					«helperClasses(rule.nodeBlocks, blackAndRedNodes, blackAndRedEdges)»
				}
				
				public class «codataClassName» extends NeoData {
					«val blackAndGreenNodes = [ModelNodeBlock n | n.action === null || n.action.op == ActionOperator.CREATE]»
					«val blackAndGreenEdges = [ModelRelationStatement e | e.action === null || e.action.op == ActionOperator.CREATE]»
					«classMembers(rule.nodeBlocks, blackAndGreenNodes, blackAndGreenEdges)»
				
					«constructor(codataClassName, rule.nodeBlocks, blackAndGreenNodes, blackAndGreenEdges)»
				
					«helperClasses(rule.nodeBlocks, blackAndGreenNodes, blackAndGreenEdges)»
				}
				
				public class «maskClassName» extends NeoMask {
					«maskMethods(rule.nodeBlocks, maskClassName)»
				}
			'''
		} catch (Exception e) {
			e.printStackTrace
			'''//FIXME Unable to generate API: «e.toString»  */ '''
		}
	}

	dispatch def generateAccess(Model m, int index) {
		if(m.abstract) return "// No API for abstract models"
		'''
			public Model getModel_«namingConvention(m.name)»(){
				return (Model) spec.getEntities().get(«index»);
			}
		'''
	}

	dispatch def generateAccess(Metamodel m, int index) {
		'''
			public Metamodel getMetamodel_«namingConvention(m.name)»(){
				return (Metamodel) spec.getEntities().get(«index»);
			}
			
			«FOR type : m.nodeBlocks»
				public static final String «m.name»__«type.name» = "«m.name»__«type.name»";
			«ENDFOR»
		'''
	}

	dispatch def generateAccess(Constraint c, int index) {
		'''
			public IConstraint getConstraint_«namingConvention(c.name)»() {
				var c = (Constraint) spec.getEntities().get(«index»);
				return NeoConstraintFactory.createNeoConstraint(c, builder);
			}
		'''
	}

	def String namingConvention(String name) {
		name.toFirstUpper.replace(".", "_")
	}
}
