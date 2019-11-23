package org.emoflon.neo.emsl.compiler

import org.emoflon.neo.emsl.eMSL.TripleGrammar

class TGGAppGenerator {
	TripleGrammar tgg
	
	new(TripleGrammar tgg) {
		this.tgg = tgg
	}
	
	def generateApp(Operation op, String appName) {
		'''
			package «tgg.name».run;
			
			import org.apache.log4j.Level;
			import org.apache.log4j.Logger;
			import org.emoflon.neo.api.API_Common;
			import org.emoflon.neo.api.API_«tgg.name»;
			import org.emoflon.neo.api.«tgg.name».API_«tgg.name»«op.nameExtension»;
			import org.emoflon.neo.cypher.models.NeoCoreBuilder;
			import org.emoflon.neo.engine.modules.NeoGenerator;
			
			«op.additionalImports(tgg.name)»
			
			public class «appName» {
				«op.additionalFields(tgg.name)»
				private static final Logger logger = Logger.getLogger(«appName».class);
				
				public static void main(String[] args) throws Exception {
					Logger.getRootLogger().setLevel(Level.INFO);
					var app = new «appName»();
					app.run();
				}
			
				public void run() throws Exception {
					try (var builder = API_Common.createBuilder()) {
						new API_«tgg.name»(builder).exportMetamodelsFor«tgg.name»();
				
						var generator = createGenerator(builder);
				
						logger.info("Start model generation...");
						generator.generate();
						logger.info("Generation done.");
					}
				}
				
				public NeoGenerator createGenerator(NeoCoreBuilder builder) {
					«op.createGeneratorMethodBody(tgg.name)»
				}
			
				«op.additionalMethods»
			}
		'''
	}
}
