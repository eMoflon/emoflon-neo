/*
 * generated by Xtext 2.16.0
 */
package org.emoflon.neo.emsl.generator

import org.eclipse.core.runtime.preferences.InstanceScope
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.ui.preferences.ScopedPreferenceStore
import org.eclipse.xtext.generator.AbstractGenerator
import org.eclipse.xtext.generator.IFileSystemAccess2
import org.eclipse.xtext.generator.IGeneratorContext
import org.emoflon.neo.emsl.eMSL.Constraint
import org.emoflon.neo.emsl.eMSL.EMSL_Spec
import org.emoflon.neo.emsl.eMSL.Entity
import org.emoflon.neo.emsl.eMSL.Metamodel
import org.emoflon.neo.emsl.eMSL.Model
import org.emoflon.neo.emsl.eMSL.Pattern
import org.emoflon.neo.emsl.util.EMSLUtil

/**
 * Generates code from your model files on save.
 * 
 * See https://www.eclipse.org/Xtext/documentation/303_runtime_concepts.html#code-generation
 */
class EMSLGenerator extends AbstractGenerator {

	String UI_PLUGIN_ID = "org.emoflon.neo.emsl.ui"

	override void doGenerate(Resource resource, IFileSystemAccess2 fsa, IGeneratorContext context) {
		var apiName = "API_" + resource.URI.trimFileExtension.segmentsList //
		.drop(2) //
		.map[s|s.toFirstUpper] //
		.join("_");
		var emslSpec = resource.contents.get(0) as EMSL_Spec

		fsa.generateFile("org/emoflon/neo/api/" + "API_Common.java", generateCommon())
		fsa.generateFile("org/emoflon/neo/api/" + apiName + ".java", generateAPIFor(apiName, emslSpec, resource))
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
			
				public static NeoCoreBuilder createBuilder() {
					return new NeoCoreBuilder("«uri»", "«userName»", "«password»");
				}
			}
		'''
	}

	def generateAPIFor(String apiName, EMSL_Spec spec, Resource resource) {
		'''
			/** 
			 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
			 */
			package org.emoflon.neo.api;
			
			import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;
			import org.emoflon.neo.emsl.eMSL.EMSL_Spec;
			import org.emoflon.neo.emsl.eMSL.Model;
			import org.emoflon.neo.emsl.eMSL.Metamodel;
			import org.emoflon.neo.emsl.util.EMSLUtil;
			import org.emoflon.neo.engine.api.rules.IPattern;
			import org.emoflon.neo.neo4j.adapter.NeoPattern;
			import org.emoflon.neo.emsl.eMSL.Pattern;
			import org.emoflon.neo.neo4j.adapter.NeoConstraint;
			import org.emoflon.neo.engine.api.constraints.IConstraint;
			import org.emoflon.neo.emsl.eMSL.Constraint;
			
			@SuppressWarnings("unused")
			public class «apiName» {
				private EMSL_Spec spec;
				private NeoCoreBuilder builder;
			
				public «apiName»(NeoCoreBuilder builder, String platformURIRoot){
					spec = (EMSL_Spec) EMSLUtil.loadSpecification("«resource.URI»", platformURIRoot);
					this.builder = builder;
				}
				
				public «apiName»(NeoCoreBuilder builder){
					this(builder, "../");
				}
			
				«FOR e : spec.entities SEPARATOR "\n"»
					//:~> «resource.URI»#«resource.getURIFragment(e)»
					«generateAccess(e, spec.entities.indexOf(e))»
				«ENDFOR»
			}
		'''
	}

	dispatch def generateAccess(Entity e, int index) {
		''''''
	}

	dispatch def generateAccess(Pattern p, int index) {
		'''
			public IPattern getPattern_«namingConvention(p.body.name)»(){
				var p = (Pattern) spec.getEntities().get(«index»);
				return new NeoPattern(p, builder);
			}
		'''
	}

	dispatch def generateAccess(Model m, int index) {
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
	
	dispatch def generateAccess(Constraint c, int index){
		'''
			public IConstraint getConstraint_«namingConvention(c.name)»() {
				var c = (Constraint) spec.getEntities().get(«index»);
				return new NeoConstraint(c, builder);
			}
		'''
	}
	
	def String namingConvention(String name){
		name.toFirstUpper.replace(".", "_")
	}
}
