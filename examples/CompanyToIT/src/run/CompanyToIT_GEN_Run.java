package run;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_CompanyToIT;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_GEN;
import org.emoflon.neo.cypher.rules.NeoRule;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.engine.generator.INodeSampler;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.TwoPhaseRuleSchedulerForGEN;
import org.emoflon.neo.engine.modules.terminationcondition.CompositeTerminationConditionForGEN;
import org.emoflon.neo.engine.modules.terminationcondition.MaximalRuleApplicationsTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.TwoPhaseUpdatePolicyForGEN;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;
import org.emoflon.neo.api.metamodels.API_Company;

public class CompanyToIT_GEN_Run {
	private static final Logger logger = Logger.getLogger(CompanyToIT_GEN_Run.class);

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new CompanyToIT_GEN_Run();
		app.runGenerator();
	}

	public void runGenerator() throws FlattenerException, Exception {
		try (var builder = API_Common.createBuilder()) {
			var api = new API_CompanyToIT(builder);
			api.exportMetamodelsForCompanyToIT();

			var genAPI = new API_CompanyToIT_GEN(builder);
			var generator = createGenerator(genAPI);

			logger.info("Start model generation...");
			generator.generate();
			logger.info("Generation done.");
		}
	}

	protected NeoGenerator createGenerator(API_CompanyToIT_GEN genAPI) {
		Collection<NeoRule> allRules = genAPI.getAllRulesForCompanyToIT__GEN();

		var maxRuleApps = new MaximalRuleApplicationsTerminationCondition(allRules, -1);

		INodeSampler sampler = (String type, String ruleName, String nodeName) -> {
			switch (type) {
			case API_Company.Company__Company:
				return 1;
			default:
				return INodeSampler.EMPTY;
			}
		};

		return new NeoGenerator(//
				allRules, //
				new CompositeTerminationConditionForGEN(5, TimeUnit.MINUTES, maxRuleApps), //
				new TwoPhaseRuleSchedulerForGEN(sampler), //
				new TwoPhaseUpdatePolicyForGEN(maxRuleApps), //
				new ParanoidNeoReprocessor(), //
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator("TheSource", "TheTarget"), //
				List.of(new LoremIpsumStringValueGenerator()));
	}
}
