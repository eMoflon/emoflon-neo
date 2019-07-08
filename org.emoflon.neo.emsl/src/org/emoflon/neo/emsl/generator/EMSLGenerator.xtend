/*
 * generated by Xtext 2.16.0
 */
package org.emoflon.neo.emsl.generator

import java.util.ArrayList
import org.eclipse.core.runtime.FileLocator
import org.eclipse.core.runtime.Platform
import org.eclipse.core.runtime.preferences.InstanceScope
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.ui.preferences.ScopedPreferenceStore
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.emoflon.neo.emsl.EMSLFlattener
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
import org.emoflon.neo.emsl.util.EMSLUtil

/**
 * Generates code from your model files on save.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#code-generation
 */
class EMSLGenerator extends AbstractGenerator {

	String UI_PLUGIN_ID = "org.emoflon.neo.emsl.ui"

	override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		val segments = resource.URI.trimFileExtension.segmentsList

		val apiName = "API_" + segments.last

		val apiPath = segments //
		.drop(3) // remove: resource/projectName/src/
		.take(segments.size - 4) // only take path up to EMSL file
		.join("/");
		val emslSpec = resource.contents.get(0) as EMSL_Spec

		fsa.generateFile("org/emoflon/neo/api/" + "API_Common.java", generateCommon())
		fsa.generateFile("org/emoflon/neo/api/" + apiPath + "/" + apiName + ".java",
			generateAPIFor(apiName, apiPath, emslSpec, resource))
	}

	def generateCommon() {
		val store = new ScopedPreferenceStore(InstanceScope.INSTANCE, UI_PLUGIN_ID)

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
		val fileURI = FileLocator.resolve(plugin.getEntry("/")).toURI.normalize;
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
			import org.emoflon.neo.neo4j.adapter.NeoPattern;
			import org.emoflon.neo.emsl.eMSL.Pattern;
			import org.emoflon.neo.emsl.eMSL.Rule;
			import org.emoflon.neo.neo4j.adapter.NeoConstraint;
			import org.emoflon.neo.engine.api.constraints.IConstraint;
			import org.emoflon.neo.emsl.eMSL.Constraint;
			import org.neo4j.driver.v1.Value;
			import org.emoflon.neo.neo4j.adapter.NeoAccess;
			
			
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
			val pattern = new EMSLFlattener().flattenEntity(p, new ArrayList<String>()) as Pattern;
			val patternBody = pattern.body
			val rootName = namingConvention(patternBody.name)
			val dataClassName = rootName + "Data"
			val accessClassName = rootName + "Access"
			'''
				public «accessClassName» getPattern_«rootName»() {
					return new «accessClassName»();
				}
				
				public class «accessClassName» extends NeoAccess {
					@Override
					public NeoPattern matcher(){
						var p = (Pattern) spec.getEntities().get(«index»);
						return new NeoPattern(p, builder);
					}
					
					public «dataClassName» data(NeoMatch m) {
						return new «dataClassName»(m);
					}
				}
				
				public class «dataClassName» {
					«classMembers(patternBody)»
					
					«constructor(dataClassName, patternBody)»
					
					«helperClasses(patternBody)»
				}
			'''
		} catch (Exception e) {
			e.printStackTrace
			'''//FIXME Unable to generate API: «e.toString»  */ '''
		}
	}

	protected def CharSequence helperClasses(AtomicPattern patternBody) '''
		«FOR node : patternBody.nodeBlocks»
			«helperNodeClass(node)»
			
			«FOR rel : node.relations»
				«helperRelClass(node, rel)»
			«ENDFOR»
		«ENDFOR»
	'''

	protected def CharSequence helperRelClass(ModelNodeBlock node, ModelRelationStatement rel) {
		val relName = EMSLUtil.relationNameConvention(node.name, rel.type.name, rel.target.name,
			node.relations.indexOf(rel))
		'''
			public class «relName.toFirstUpper»Rel {
				«FOR prop : rel.type.properties»
					public «EMSLUtil.getJavaType(prop.type)» «prop.name»;
				«ENDFOR»
			
				public «relName.toFirstUpper»Rel(Value «relName») {
				«FOR prop : rel.type.properties»
					if(!«relName».get("«prop.name»").isNull())
						this.«prop.name» = «relName».get("«prop.name»").as«EMSLUtil.getJavaType(prop.type).toFirstUpper»();
				«ENDFOR»
				}
			}
		'''
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

	protected def CharSequence constructor(String fileName, AtomicPattern patternBody) '''
		public «fileName»(NeoMatch m) {
			var data = m.getData();
			«FOR node : patternBody.nodeBlocks»
				var «node.name» = data.get("«node.name»");
				this.«node.name» = new «node.name.toFirstUpper»Node(«node.name»);
				«FOR rel : node.relations»
					«val relName = EMSLUtil.relationNameConvention(//
						node.name,// 
						rel.type.name,//
						rel.target.name,// 
						node.relations.indexOf(rel))»
					var «relName» = data.get("«relName»");
					this.«relName» = new «relName.toFirstUpper»Rel(«relName»);
				«ENDFOR»			
			«ENDFOR»
		}
		
	'''

	protected def CharSequence classMembers(AtomicPattern patternBody) {
		'''
			«FOR node : patternBody.nodeBlocks»
				public final «node.name.toFirstUpper»Node «node.name»;
				«FOR rel : node.relations»
					«val relName = EMSLUtil.relationNameConvention(//
						node.name,// 
						rel.type.name,//
						rel.target.name,// 
						node.relations.indexOf(rel))»
					public final «relName.toFirstUpper»Rel «relName»;
				«ENDFOR»
			«ENDFOR»
		'''
	}

	def allProperties(MetamodelNodeBlock nb) {
		EMSLUtil.thisAndAllSuperTypes(nb).flatMap[it.properties]
	}

	dispatch def generateAccess(Rule r, int index) {
		if(r.abstract) return ""
		'''
			public IRule<NeoMatch, NeoCoMatch> getRule_«namingConvention(r.name)»(){
				var r = (Rule) spec.getEntities().get(«index»);
				// TODO[Jannik] return new NeoRule(r, builder);
				return null;
			}
		'''
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
		if(m.abstract) return "// No API for abstract metamodels"
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
