package liason.app;

import static org.emoflon.neo.api.liaison.run.RequirementsCoverage_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.liaison.run.RequirementsCoverage_GEN_Run.TRG_MODEL_NAME;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.liaison.API_Common;
import org.emoflon.neo.api.liaison.API_Delta;
import org.emoflon.neo.api.liaison.API_ReqArchTraceability;
import org.emoflon.neo.api.liaison.run.RequirementsCoverage_MI_Run;
import org.emoflon.neo.api.liaison.tgg.API_RequirementsCoverage_GEN;
import org.emoflon.neo.api.liaison.tgg.API_RequirementsCoverage_MI;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.matchreprocessors.MIReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.MIRuleScheduler;
import org.emoflon.neo.engine.modules.startup.PrepareContextDeltaAttributes;
import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.ModelIntegrationOperationalStrategy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;

public class MyMI extends RequirementsCoverage_MI_Run {
	
	public MyMI(String srcModelName, String trgModelName) {
		super(srcModelName, trgModelName);
	}

	public static void main(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new MyMI(SRC_MODEL_NAME, TRG_MODEL_NAME);
		MyMI.solver = SupportedILPSolver.Sat4J;

		try (var builder = API_Common.createBuilder()) {
			var delta = new API_Delta(builder);
			builder.clearDataBase();
			builder.exportEMSLEntityToNeo4j(delta.getModel_RequirementsCoverage_Source());
			builder.exportEMSLEntityToNeo4j(delta.getModel_RequirementsCoverage_Target());
			
			app.run();
		}
	}
	
	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var api = new API_ReqArchTraceability(builder);
		var genAPI = new API_RequirementsCoverage_GEN(builder);
		var miAPI = new API_RequirementsCoverage_MI(builder);
		var genRules = genAPI.getAllRulesForRequirementsCoverage_GEN();
		var analyser = new TripleRuleAnalyser(new API_ReqArchTraceability(builder).getTripleRulesOfRequirementsCoverage());
				
		modelIntegration = new ModelIntegrationOperationalStrategy(//
				solver, //
				builder, //
				genRules, //
				miAPI.getAllRulesForRequirementsCoverage_MI(), //
				api.getConstraintsOfRequirementsCoverage(), //
				srcModelName, //
				trgModelName//
		);
		
		return new NeoGenerator(//
				miAPI.getAllRulesForRequirementsCoverage_MI(), //
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
}
