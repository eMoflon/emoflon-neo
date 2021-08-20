/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.classinhhier2db.run;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.classinhhier2db.API_Common;
import org.emoflon.neo.api.classinhhier2db.API_ClassInhHier2DB;
import org.emoflon.neo.api.classinhhier2db.tgg.API_ClassInhHier2DB_MI;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.modules.NeoGenerator;

import static org.emoflon.neo.api.classinhhier2db.run.ClassInhHier2DB_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.classinhhier2db.run.ClassInhHier2DB_GEN_Run.TRG_MODEL_NAME;
		
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.api.classinhhier2db.tgg.API_ClassInhHier2DB_GEN;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;
import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;
import org.emoflon.neo.engine.modules.updatepolicies.ModelIntegrationOperationalStrategy;
import org.emoflon.neo.engine.modules.startup.PrepareContextDeltaAttributes;
import org.emoflon.neo.engine.modules.ruleschedulers.MIRuleScheduler;
import org.emoflon.neo.engine.modules.matchreprocessors.MIReprocessor;

@SuppressWarnings("unused")
public class ClassInhHier2DB_MI_Run {
	protected static SupportedILPSolver solver = SupportedILPSolver.Gurobi;
	protected ModelIntegrationOperationalStrategy modelIntegration;
	protected static final Logger logger = Logger.getLogger(ClassInhHier2DB_MI_Run.class);
	protected String srcModelName;
	protected String trgModelName;
	
	public static void main(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new ClassInhHier2DB_MI_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		app.run();
	}
	
	public ClassInhHier2DB_MI_Run(String srcModelName, String trgModelName) {
		this.srcModelName = srcModelName;
		this.trgModelName = trgModelName;
	}
	
	public ClassInhHier2DB_MI_Run(String srcModelName, String trgModelName, SupportedILPSolver solver) {
		this(srcModelName, trgModelName);
		ClassInhHier2DB_MI_Run.solver = solver;
	}
	
	public void run() throws Exception {
		try (var builder = API_Common.createBuilder()) {
	
			var generator = createGenerator(builder);
	
			logger.info("Running generator...");
			generator.generate();
			logger.info("Generator terminated.");
		}
	}
	
	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var api = new API_ClassInhHier2DB(builder);
		var genAPI = new API_ClassInhHier2DB_GEN(builder);
		var miAPI = new API_ClassInhHier2DB_MI(builder);
		var genRules = genAPI.getAllRulesForClassInhHier2DB_GEN();
		var analyser = new TripleRuleAnalyser(new API_ClassInhHier2DB(builder).getTripleRulesOfClassInhHier2DB());
		modelIntegration = new ModelIntegrationOperationalStrategy(//
				solver, //
				builder, //
				genRules, //
				miAPI.getAllRulesForClassInhHier2DB_MI(), //
				api.getConstraintsOfClassInhHier2DB(), //
				srcModelName, //
				trgModelName//
		);
		
		return new NeoGenerator(//
				miAPI.getAllRulesForClassInhHier2DB_MI(), //
				new PrepareContextDeltaAttributes(builder, srcModelName, trgModelName), //
				new NoMoreMatchesTerminationCondition(), //
				new MIRuleScheduler(analyser), //
				modelIntegration, //
				new MIReprocessor(analyser), //
				modelIntegration, //
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator(srcModelName, trgModelName), //
				List.of(new LoremIpsumStringValueGenerator()));
	}
	
	public ModelIntegrationOperationalStrategy runModelIntegration() throws Exception {
		run();
		return modelIntegration;
	}
}
