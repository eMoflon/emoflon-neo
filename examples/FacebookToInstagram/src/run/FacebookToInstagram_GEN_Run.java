package run;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_Facebook;
import org.emoflon.neo.api.API_Instagram;
import org.emoflon.neo.api.API_Transformations;
import org.emoflon.neo.api.Transformations.API_FacebookToInstagramGrammar_GEN;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.engine.generator.INodeSampler;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.TwoPhaseRuleSchedulerForGEN;
import org.emoflon.neo.engine.modules.terminationcondition.CompositeTerminationConditionForGEN;
import org.emoflon.neo.engine.modules.terminationcondition.MaximalRuleApplicationsTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.RandomSingleMatchUpdatePolicy;
import org.emoflon.neo.engine.modules.updatepolicies.TwoPhaseUpdatePolicyForGEN;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;

public class FacebookToInstagram_GEN_Run {
	private static final Logger logger = Logger.getLogger(FacebookToInstagram_GEN_Run.class);

	public static void main(String[] pArgs) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		Logger.getLogger(RandomSingleMatchUpdatePolicy.class).setLevel(Level.INFO);

		var app = new FacebookToInstagram_GEN_Run();

		for (int i = 0; i < 50; i++) {
			app.runGenerator();
		}
	}

	public void runGenerator() throws FlattenerException, Exception {
		try (var builder = API_Common.createBuilder()) {
			var api = new API_Transformations(builder);
			api.exportMetamodelsForFacebookToInstagramGrammar();

			var genAPI = new API_FacebookToInstagramGrammar_GEN(builder);
			var generator = createGenerator(api, genAPI, builder);

			logger.info("Start model generation...");
			generator.generate();
			logger.info("Generation done.");
		}
	}

	protected NeoGenerator createGenerator(API_Transformations api, API_FacebookToInstagramGrammar_GEN genAPI,
			NeoCoreBuilder builder) {
		var allRules = genAPI.getAllRulesForFacebookToInstagramGrammar__GEN();

		var maxRuleApps = new MaximalRuleApplicationsTerminationCondition(allRules, 500)//
				.setMax(API_Transformations.FacebookToInstagramGrammar__NetworkToNetworkIslandRule, 10)
				.setMax(API_Transformations.FacebookToInstagramGrammar__UserToUserIslandRule, 1000)
				.setMax(API_Transformations.FacebookToInstagramGrammar__UserNetworkBridgeRule, 1000)
				.setMax(API_Transformations.FacebookToInstagramGrammar__RequestFriendship, 10000)
				.setMax(API_Transformations.FacebookToInstagramGrammar__AcceptFriendship, 5000)
				.setMax(API_Transformations.FacebookToInstagramGrammar__IgnoreInterNetworkFollowers, 500);

		INodeSampler sampler = (String type, String ruleName, String nodeName) -> {
			switch (ruleName) {
			case API_Transformations.FacebookToInstagramGrammar__RequestFriendship:
				return 1;
			case API_Transformations.FacebookToInstagramGrammar__AcceptFriendship:
				switch (nodeName) {
				case API_Transformations.FacebookToInstagramGrammar__AcceptFriendship__iu:
					return 1;
				default:
					return -1;
				}
			case API_Transformations.FacebookToInstagramGrammar__IgnoreInterNetworkFollowers:
				switch (type) {
				case API_Instagram.InstagramLanguage__User:
					return 1;
				default:
					return -1;
				}
			default:
				switch (type) {
				case API_Facebook.FacebookLanguage__User:
				case API_Facebook.FacebookLanguage__Network:
					return 1;
				default:
					return -1;
				}
			}
		};

		return new NeoGenerator(//
				allRules, //
				new CompositeTerminationConditionForGEN(1, TimeUnit.HOURS, maxRuleApps), //
				new TwoPhaseRuleSchedulerForGEN(sampler), //
				new TwoPhaseUpdatePolicyForGEN(maxRuleApps), //
				new ParanoidNeoReprocessor(), //
				new HeartBeatAndReportMonitor(), //
				List.of(new LoremIpsumStringValueGenerator()));
	}
}
