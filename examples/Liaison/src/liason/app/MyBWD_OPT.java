package liason.app;

import static org.emoflon.neo.api.liaison.run.RequirementsCoverage_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.liaison.run.RequirementsCoverage_GEN_Run.TRG_MODEL_NAME;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.liaison.API_Common;
import org.emoflon.neo.api.liaison.API_Data;
import org.emoflon.neo.api.liaison.API_ReqArchTraceability;
import org.emoflon.neo.api.liaison.run.RequirementsCoverage_BWD_OPT_Run;
import org.emoflon.neo.api.liaison.tgg.API_RequirementsCoverage_BWD_OPT;
import org.emoflon.neo.api.liaison.tgg.API_RequirementsCoverage_GEN;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.matchreprocessors.BWD_OPTReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.BWD_OPTRuleScheduler;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.BackwardTransformationOperationalStrategy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;

public class MyBWD_OPT extends RequirementsCoverage_BWD_OPT_Run {

	public MyBWD_OPT(String srcModelName, String trgModelName) {
		super(srcModelName, trgModelName);
	}

	public static void main(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new MyBWD_OPT(SRC_MODEL_NAME, TRG_MODEL_NAME);
		MyBWD_OPT.solver = SupportedILPSolver.Sat4J;

		try (var builder = API_Common.createBuilder()) {
			var data = new API_Data(builder);
			builder.clearDataBase();
			builder.exportEMSLEntityToNeo4j(data.getModel_RequirementsCoverage_Target());

			app.run();
		}
	}

	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var api = new API_ReqArchTraceability(builder);
		var genAPI = new API_RequirementsCoverage_GEN(builder);
		var bwd_optAPI = new API_RequirementsCoverage_BWD_OPT(builder);
		var genRules = genAPI.getAllRulesForRequirementsCoverage_GEN();
		var analyser = new TripleRuleAnalyser(
				new API_ReqArchTraceability(builder).getTripleRulesOfRequirementsCoverage());
		backwardTransformation = new BackwardTransformationOperationalStrategy(//
				solver, //
				builder, //
				genRules, //
				bwd_optAPI.getAllRulesForRequirementsCoverage_BWD_OPT(), //
				api.getConstraintsOfRequirementsCoverage(), //
				srcModelName, //
				trgModelName//
		);

		return new NeoGenerator(//
				bwd_optAPI.getAllRulesForRequirementsCoverage_BWD_OPT(), //
				new NoOpStartup(), //
				new NoMoreMatchesTerminationCondition() {
					@Override
					public boolean isReached(MatchContainer<NeoMatch, NeoCoMatch> matchContainer) {
						return super.isReached(matchContainer) || matchContainer.getNumberOfRuleApplications() > 1;
					}
				},
				new BWD_OPTRuleScheduler(analyser), //
				backwardTransformation, //
				new BWD_OPTReprocessor(analyser), //
				backwardTransformation, //
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator(srcModelName, trgModelName), //
				List.of(new LoremIpsumStringValueGenerator()));
	}

}
