/*
 * generated by Xtext 2.16.0
 */
package org.emoflon.neo.emsl.generator

import java.net.URI
import java.util.Arrays
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.CoreException
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.preferences.InstanceScope
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.ui.preferences.ScopedPreferenceStore
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.emoflon.neo.emsl.eMSL.ActionOperator
import org.emoflon.neo.emsl.eMSL.AtomicPattern
import org.emoflon.neo.emsl.eMSL.Constraint
import org.emoflon.neo.emsl.eMSL.EMSL_Spec
import org.emoflon.neo.emsl.eMSL.Entity
import org.emoflon.neo.emsl.eMSL.Metamodel
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock
import org.emoflon.neo.emsl.eMSL.Model
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement
import org.emoflon.neo.emsl.eMSL.Pattern
import org.emoflon.neo.emsl.eMSL.Rule
import org.emoflon.neo.emsl.refinement.EMSLFlattener
import org.emoflon.neo.emsl.util.ClasspathUtil
import org.emoflon.neo.emsl.util.EMSLUtil
import org.emoflon.neo.emsl.compiler.TGGCompiler
import org.emoflon.neo.emsl.eMSL.TripleGrammar
import org.eclipse.core.resources.ResourcesPlugin
import org.emoflon.neo.emsl.util.LogUtils
import org.emoflon.neo.emsl.util.ManifestFileUpdater
import org.apache.log4j.Logger

/**
 * Generates code from your model files on save.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#code-generation
 */
class EMSLGenerator extends AbstractGenerator {

	static final Logger logger = Logger.getLogger(EMSLGenerator)

	override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		val segments = resource.URI.trimFileExtension.segmentsList

		val apiName = "API_" + segments.last

		val apiPath = segments //
		.drop(3) // remove: resource/projectName/src/
		.take(segments.size - 4) // only take path up to EMSL file
		.join("/");
		var emslSpec = resource.contents.get(0) as EMSL_Spec
		
		val TGGCompiler compiler = new TGGCompiler(ResourcesPlugin.getWorkspace().getRoot().getProject(resource.URI.segment(1)))
		emslSpec.entities.filter[it instanceof TripleGrammar].map[it as TripleGrammar].forEach[compiler.compile(it)]

		fsa.generateFile("org/emoflon/neo/api/" + "API_Common.java", generateCommon())
		fsa.generateFile("org/emoflon/neo/api/" + apiPath + "/" + apiName + ".java",
			generateAPIFor(apiName, apiPath, emslSpec, resource))
	}

	override void afterGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		val segments = resource.URI.trimFileExtension.segmentsList
		val projectName = segments.get(1)
		val project = ResourcesPlugin.workspace.root.getProject(projectName)
		ClasspathUtil.makeSourceFolderIfNecessary(project.getFolder("src-gen"))

		try {
			new ManifestFileUpdater().processManifest(project, [ manifest |
				return ManifestFileUpdater.updateDependencies(manifest, Arrays.asList(
					// eNeo Deps
					"org.emoflon.neo.neo4j.adapter"
				))
			])
		} catch (CoreException e) {
			LogUtils.error(logger, e);
		}
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
			import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;
			
			public class API_Common {
				// Default values (might have to be changed)
				public static final String PLATFORM_PLUGIN_URI = "«getInstallLocation»";
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

	def generateAPIFor(String apiName, String apiPath, EMSL_Spec spec, Resource resource) {
		'''
			/** 
			 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
			 */
			package org.emoflon.neo.api«subPackagePath(apiPath)»;
			
			import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;
			import org.emoflon.neo.neo4j.adapter.NeoMatch;
			import org.emoflon.neo.neo4j.adapter.NeoCoMatch;
			import org.emoflon.neo.emsl.eMSL.EMSL_Spec;
			import org.emoflon.neo.emsl.eMSL.Model;
			import org.emoflon.neo.emsl.eMSL.Metamodel;
			import org.emoflon.neo.emsl.util.EMSLUtil;
			import org.emoflon.neo.engine.api.rules.IPattern;
			import org.emoflon.neo.engine.api.rules.IRule;
			import org.emoflon.neo.neo4j.adapter.NeoRule;
			import org.emoflon.neo.neo4j.adapter.NeoRuleAccess;
			import org.emoflon.neo.neo4j.adapter.patterns.NeoPattern;
			import org.emoflon.neo.neo4j.adapter.patterns.NeoPatternFactory;
			import org.emoflon.neo.emsl.eMSL.Pattern;
			import org.emoflon.neo.emsl.eMSL.Rule;
			import org.emoflon.neo.neo4j.adapter.NeoConstraint;
			import org.emoflon.neo.engine.api.constraints.IConstraint;
			import org.emoflon.neo.emsl.eMSL.Constraint;
			import org.neo4j.driver.v1.Value;
			import org.emoflon.neo.neo4j.adapter.patterns.NeoPatternAccess;
			import org.emoflon.neo.neo4j.adapter.NeoMask;
			import org.emoflon.neo.neo4j.adapter.NeoData;
			import java.util.HashMap;
			import java.util.Map;
			import java.util.Optional;
			
			@SuppressWarnings("unused")
			public class «apiName» {
				private EMSL_Spec spec;
				private NeoCoreBuilder builder;
			
				public «apiName»(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot){
					spec = (EMSL_Spec) EMSLUtil.loadSpecification("«resource.URI»", platformResourceURIRoot, platformPluginURIRoot);
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
				
				public class «accessClassName» extends NeoPatternAccess<«dataClassName»,«maskClassName»> {
					«FOR node : patternBody.nodeBlocks»
						public final String «node.name» = "«node.name»";
					«ENDFOR»
					
					@Override
					public NeoPattern matcher(){
						var p = (Pattern) spec.getEntities().get(«index»);
						return NeoPatternFactory.createNeoPattern(p, builder);
					}
					
					@Override
					public NeoPattern matcher(«maskClassName» mask) {
						var p = (Pattern) spec.getEntities().get(«index»);
						return NeoPatternFactory.createNeoPattern(p, builder, mask);
					}
					
					@Override
					public «dataClassName» data(NeoMatch m) {
						return new «dataClassName»(m);
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
				
					«maskClassMembers()»
				
					«maskMethods(patternBody.nodeBlocks, maskClassName)»
				
				}
			'''
		} catch (Exception e) {
			e.printStackTrace
			'''//FIXME Unable to generate API: «e.toString»  */ '''
		}
	}

	private def CharSequence maskClassMembers() '''
		private HashMap<String, Long> nodeMask = new HashMap<>();
		private HashMap<String, Object> attributeMask = new HashMap<>();
		
		@Override
		public Map<String, Long> getMaskedNodes() {
			return nodeMask;
		}
		
		@Override
		public Map<String, Object> getMaskedAttributes() {
			return attributeMask;
		}
		
	'''

	protected def CharSequence helperClasses(Iterable<ModelNodeBlock> nodeBlocks) '''
		«FOR node : nodeBlocks»
			«helperNodeClass(node)»
			
			«FOR rel : node.relations.filter[!EMSLUtil.isVariableLink(it)]»
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
					public «EMSLUtil.getJavaType(prop.type)» «prop.name»;
				«ENDFOR»
			
				public «relName.toFirstUpper»Rel(Value «relName») {
					«FOR prop : rel.types.flatMap[it.type.properties]»
						if(!«relName».get("«prop.name»").isNull())
							this.«prop.name» = «relName».get("«prop.name»").as«EMSLUtil.getJavaType(prop.type).toFirstUpper»();
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
				public «EMSLUtil.getJavaType(prop.type)» «prop.name»;
			«ENDFOR»
			
			public «node.name.toFirstUpper»Node(Value «node.name») {
				«FOR prop : allProperties(node.type)»
					if(!«node.name».get("«prop.name»").isNull())
						this.«prop.name» = «node.name».get("«prop.name»").as«EMSLUtil.getJavaType(prop.type).toFirstUpper»();
				«ENDFOR»
			}
		}
	'''

	protected def CharSequence constructor(String fileName, Iterable<ModelNodeBlock> nodeBlocks) '''
		public «fileName»(NeoMatch m) {
			var data = m.getData();
			«FOR node : nodeBlocks»
				var «node.name» = data.get("«node.name»");
				this.«node.name» = new «node.name.toFirstUpper»Node(«node.name»);
				«FOR rel : node.relations.filter[!EMSLUtil.isVariableLink(it)]»
					«val relName = EMSLUtil.relationNameConvention(//
						node.name,// 
						rel.allTypes,//
						rel.target.name,// 
						node.relations.indexOf(rel))»
					var «relName» = data.get("«relName»");
					this.«relName» = new «relName.toFirstUpper»Rel(«relName»);
				«ENDFOR»			
			«ENDFOR»
		}
		
	'''

	def CharSequence classMembers(Iterable<ModelNodeBlock> nodeBlocks) {
		'''
			«FOR node : nodeBlocks»
				public final «node.name.toFirstUpper»Node «node.name»;
				«FOR rel : node.relations.filter[!EMSLUtil.isVariableLink(it)]»
					«val relName = EMSLUtil.relationNameConvention(//
						node.name,// 
						rel.allTypes,//
						rel.target.name,// 
						node.relations.indexOf(rel))»
					public final «relName.toFirstUpper»Rel «relName»;
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

	dispatch def generateAccess(Rule r, int index) {
		if(r.abstract) return ""
		try {
			val rule = EMSLFlattener.flatten(r) as Rule;
			val rootName = namingConvention(rule.name)
			val dataClassName = rootName + "Data"
			val accessClassName = rootName + "Access"
			val maskClassName = rootName + "Mask"
			'''
				public «accessClassName» getRule_«rootName»() {
					return new «accessClassName»();
				}
				
				public class «accessClassName» extends NeoRuleAccess<«dataClassName»,«maskClassName»> {
					@Override
					public NeoRule rule(){
						var r = (Rule) spec.getEntities().get(«index»);
						return new NeoRule(r, builder);
					}
					
					@Override
					public NeoRule rule(«maskClassName» mask) {
						var r = (Rule) spec.getEntities().get(«index»);
						return new NeoRule(r, builder, mask);
					}
					
					@Override
					public «dataClassName» data(NeoMatch m) {
						return new «dataClassName»(m);
					}
					
					@Override
					public «maskClassName» mask() {
						return new «maskClassName»();
					}
				}
				
				public class «dataClassName» extends NeoData {
					«val blackAndGreenNodeBlocks = rule.nodeBlocks.filter[it.action === null || it.action.op !== ActionOperator.DELETE]»
					«val blackAndRedNodeBlocks = rule.nodeBlocks.filter[it.action === null || it.action.op == ActionOperator.DELETE]»
					«classMembers(blackAndGreenNodeBlocks)»
					
					«constructor(dataClassName, blackAndGreenNodeBlocks)»
					
					«helperClasses(blackAndGreenNodeBlocks)»
				}
				
				public class «maskClassName» extends NeoMask {
				
					«maskClassMembers()»
				
					«maskMethods(blackAndRedNodeBlocks, maskClassName)»
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
		'''
	}

	dispatch def generateAccess(Constraint c, int index) {
		'''
			public IConstraint getConstraint_«namingConvention(c.name)»() {
				var c = (Constraint) spec.getEntities().get(«index»);
				return new NeoConstraint(c, builder);
			}
		'''
	}

	def String namingConvention(String name) {
		name.toFirstUpper.replace(".", "_")
	}
}
