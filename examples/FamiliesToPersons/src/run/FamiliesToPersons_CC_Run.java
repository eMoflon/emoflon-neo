package run;

import java.util.List;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.familiestopersons.API_Common;
import org.emoflon.neo.api.familiestopersons.API_Schema;
import org.emoflon.neo.api.familiestopersons.tgg.API_FamiliesToPersons_CC;
import org.emoflon.neo.api.familiestopersons.tgg.API_FamiliesToPersons_GEN;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.matchreprocessors.CCReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.CCRuleScheduler;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.CorrCreationOperationalStrategy;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;

public class FamiliesToPersons_CC_Run {
	private static final Logger logger = Logger.getLogger(FamiliesToPersons_CC_Run.class);
	private static final SupportedILPSolver solver = SupportedILPSolver.Sat4J;

	private String srcModel;
	private String trgModel;
	private CorrCreationOperationalStrategy corrCreation;
	
	

	public FamiliesToPersons_CC_Run(String srcModel, String trgModel) {
		super();
		this.srcModel = srcModel;
		this.trgModel = trgModel;
	}

	/** arg0 = sourceModelName, arg1 = targetModelName
	 */
	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new FamiliesToPersons_CC_Run("Families", "Persons");
		app.run();
	}

	public void run() throws Exception {
		try (var builder = API_Common.createBuilder()) {

			var generator = createGenerator(builder);
			builder.deleteAllCorrs();
			
			logger.info("Start corr creation...");
			generator.generate();
		}
		
	
	}

	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var api = new API_Schema(builder);
		var genAPI = new API_FamiliesToPersons_GEN(builder);
		var ccAPI = new API_FamiliesToPersons_CC(builder);
		var genRules = genAPI.getAllRulesForFamiliesToPersons_GEN();
		var tripleRules = new API_Schema(builder).getTripleRulesOfFamiliesToPersons();
		var analyser = new TripleRuleAnalyser(tripleRules);
		
		corrCreation = new CorrCreationOperationalStrategy(//
				solver, //
				builder, //
				genRules, //
				ccAPI.getAllRulesForFamiliesToPersons_CC(), //
				api.getConstraintsOfFamiliesToPersons(), //
				srcModel, //
				trgModel//
		);

		return new NeoGenerator(//
				ccAPI.getAllRulesForFamiliesToPersons_CC(), //
				new NoOpStartup(), //
				new NoMoreMatchesTerminationCondition(), //
				new CCRuleScheduler(analyser), //
				corrCreation, //
				new CCReprocessor(analyser), //
				corrCreation,//
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator(srcModel, trgModel), //
				List.of(new LoremIpsumStringValueGenerator()));
	}

	public CorrCreationOperationalStrategy runCorrCreation(String srcModel, String trgModel) throws Exception {
		this.srcModel = srcModel;
		this.trgModel = trgModel;
		run();
		return corrCreation;
	}
}
