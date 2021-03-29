package run;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.facebooktoinstagram_constrained.API_Common;
import org.emoflon.neo.api.facebooktoinstagram_constrained.API_Instagram;
import org.emoflon.neo.api.facebooktoinstagram_constrained.API_Transformations;
import org.emoflon.neo.api.facebooktoinstagram_constrained.tgg.API_FacebookToInstagram_Constrained_GEN;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.engine.generator.INodeSampler;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.cleanup.NoOpCleanup;
import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.TwoPhaseRuleSchedulerForGEN;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.CompositeTerminationConditionForGEN;
import org.emoflon.neo.engine.modules.terminationcondition.MaximalRuleApplicationsTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.RandomSingleMatchUpdatePolicy;
import org.emoflon.neo.engine.modules.updatepolicies.TwoPhaseUpdatePolicyForGEN;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;

public class FacebookToInstagram_Constrained_GEN_Run {
	private static final Logger logger = Logger.getLogger(FacebookToInstagram_Constrained_GEN_Run.class);

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.DEBUG);
		Logger.getLogger(RandomSingleMatchUpdatePolicy.class).setLevel(Level.INFO);

		var app = new FacebookToInstagram_Constrained_GEN_Run();

		for (int i = 0; i < 50; i++) {
			app.runGenerator();
		}
	}

	public void runGenerator() throws FlattenerException, Exception {
		try (var builder = API_Common.createBuilder()) {
			var api = new API_Transformations(builder);
			api.exportMetamodelsForFacebookToInstagram_Constrained();

			var genAPI = new API_FacebookToInstagram_Constrained_GEN(builder);
			var generator = createGenerator(api, genAPI, builder);

			logger.info("Start model generation...");
			generator.generate();
			logger.info("Generation done.");
		}
	}

	protected NeoGenerator createGenerator(API_Transformations api, API_FacebookToInstagram_Constrained_GEN genAPI,
			NeoCoreBuilder builder) {
		var allRules = genAPI.getAllRulesForFacebookToInstagram_Constrained_GEN();

		var maxRuleApps = new MaximalRuleApplicationsTerminationCondition(allRules, -1)
				.setMax(API_Transformations.FacebookToInstagram_Constrained__NetworkToNetwork, 5)
				.setMax(API_Transformations.FacebookToInstagram_Constrained__UserToUser, 5000)
				.setMax(API_Transformations.FacebookToInstagram_Constrained__RequestFriendship, 50000)
				.setMax(API_Transformations.FacebookToInstagram_Constrained__AcceptFriendship, 30000);

		INodeSampler sampler = (String type, String ruleName, String nodeName) -> {
			switch (ruleName) {
			case API_Transformations.FacebookToInstagram_Constrained__UserToUser:
				switch (nodeName) {
				case API_Transformations.FacebookToInstagram_Constrained__UserToUser__fn:
					return 1;
				default:
					return INodeSampler.EMPTY;
				}
			case API_Transformations.FacebookToInstagram_Constrained__RequestFriendship:
				switch (type) {
				case API_Instagram.InstagramLanguage__User:
					return 1;
				default:
					return INodeSampler.EMPTY;
				}
			case API_Transformations.FacebookToInstagram_Constrained__AcceptFriendship:
				switch (nodeName) {
				case API_Transformations.FacebookToInstagram_Constrained__AcceptFriendship__iu1:
					return 1;
				default:
					return INodeSampler.EMPTY;
				}
			default:
				return INodeSampler.EMPTY;
			}
		};

		return new NeoGenerator(//
				allRules, //
				new NoOpStartup(), //
				new CompositeTerminationConditionForGEN(1, TimeUnit.HOURS, maxRuleApps), //
				new TwoPhaseRuleSchedulerForGEN(sampler), //
				new TwoPhaseUpdatePolicyForGEN(maxRuleApps), //
				new ParanoidNeoReprocessor(), //
				new NoOpCleanup(), //
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator("Facebook", "Instagram"), //
				List.of(new LoremIpsumStringValueGenerator()));
	}
}
