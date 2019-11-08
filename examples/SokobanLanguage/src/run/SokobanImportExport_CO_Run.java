package run;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.rules.SokobanTGGs.API_SokobanImportExport_CO;
import org.emoflon.neo.api.rules.SokobanTGGs.API_SokobanImportExport_GEN;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.matchreprocessors.NoOpReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.AllRulesAllMatchesScheduler;
import org.emoflon.neo.engine.modules.terminationcondition.OneShotTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.CheckOnlyOperationalStrategy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;

public class SokobanImportExport_CO_Run {
	private static final Logger logger = Logger.getLogger(SokobanImportExport_CO_Run.class);
	private static final SupportedILPSolver solver = SupportedILPSolver.Sat4J;

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new SokobanImportExport_CO_Run();
		app.runCheckOnly();
	}

	public boolean runCheckOnly() throws Exception {
		try (var builder = API_Common.createBuilder()) {
			var genAPI = new API_SokobanImportExport_GEN(builder);
			var coAPI = new API_SokobanImportExport_CO(builder);
			var checkOnly = new CheckOnlyOperationalStrategy(genAPI.getAllRulesForSokobanImportExport__GEN(),
					coAPI.getAllRulesForSokobanImportExport__CO(), getNegativeConstraints(builder));
			var generator = new NeoGenerator(//
					coAPI.getAllRulesForSokobanImportExport__CO(), //
					new OneShotTerminationCondition(), //
					new AllRulesAllMatchesScheduler(), //
					checkOnly, //
					new NoOpReprocessor(), //
					new HeartBeatAndReportMonitor(), //
					new ModelNameValueGenerator("TheSource", "TheTarget"), //
					List.of(new LoremIpsumStringValueGenerator()));

			logger.info("Start check only...");
			generator.generate();

			if (checkOnly.isConsistent(solver)) {
				logger.info("Your triple is consistent!");
				return true;
			} else {
				logger.info("Your triple is inconsistent!");
				var inconsistentElements = checkOnly.determineInconsistentElements(solver);
				logger.info(inconsistentElements.size() + " elements of your triple are inconsistent!");
				return false;
			}
		}
	}

	protected Collection<IConstraint> getNegativeConstraints(NeoCoreBuilder builder) {
		return List.of(//

		);
	}
}
