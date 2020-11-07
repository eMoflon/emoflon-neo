package run;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.emoflon.neo.api.facebooktoinstagram.API_Facebook;
import org.emoflon.neo.api.facebooktoinstagram.API_Instagram;
import org.emoflon.neo.api.facebooktoinstagram.API_Transformations;
import org.emoflon.neo.api.facebooktoinstagram.run.FacebookToInstagramGrammar_BWD_Run;
import org.emoflon.neo.api.facebooktoinstagram.run.FacebookToInstagramGrammar_FWD_Run;
import org.emoflon.neo.api.facebooktoinstagram.run.FacebookToInstagramGrammar_GEN_Run;
import org.emoflon.neo.api.facebooktoinstagram.tgg.API_FacebookToInstagramGrammar_BWD;
import org.emoflon.neo.api.facebooktoinstagram.tgg.API_FacebookToInstagramGrammar_FWD;
import org.emoflon.neo.api.facebooktoinstagram.tgg.API_FacebookToInstagramGrammar_GEN;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.generator.INodeSampler;
import org.emoflon.neo.engine.generator.modules.ICleanupModule;
import org.emoflon.neo.engine.generator.modules.IStartupModule;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.cleanup.RemoveTranslateAttributes;
import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.FixedNoOfMatchesRuleScheduler;
import org.emoflon.neo.engine.modules.ruleschedulers.TwoPhaseRuleSchedulerForGEN;
import org.emoflon.neo.engine.modules.startup.PrepareTranslateAttributes;
import org.emoflon.neo.engine.modules.terminationcondition.CompositeTerminationConditionForGEN;
import org.emoflon.neo.engine.modules.terminationcondition.MaximalRuleApplicationsTerminationCondition;
import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.AnySingleMatchUpdatePolicy;
import org.emoflon.neo.engine.modules.updatepolicies.TwoPhaseUpdatePolicyForGEN;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;


public class Benchmark {
	private static final Logger logger = Logger.getLogger(Benchmark.class);

	public static void main(String... pArgs) throws Throwable {
		var benchmarkSizes = new long[] { //
				100, //
				1000, //
				10000, //
				100000 //
		};

		var benchmarks = new LinkedHashMap<Long, long[]>();
		for (long size : benchmarkSizes)
			benchmarks.put(size, benchmark(size));

		logger.info("====================");
		logger.info("Final report:");
		final boolean[] firstReport = new boolean[] { true };
		benchmarks.forEach((size, result) -> {
			if (firstReport[0])
				firstReport[0] = false;
			else
				logger.info("--------------------");
			logger.info("Model size: " + size);
			logger.info("GEN duration: " + result[0]);
			logger.info("FWD duration: " + result[1]);
			logger.info("BWD duration: " + result[2]);
		});
		logger.info("====================");
	}

	public static long[] benchmark(long modelSize) throws Throwable {
		final String generatedSrcModelName = "FacebookToInstagram_benchmark_src";
		final String generatedTrgModelName = "FacebookToInstagram_benchmark_trg";
		final String derivedSrcModelName = "FacebookToInstagram_benchmark_src_derived";
		final String derivedTrgModelName = "FacebookToInstagram_benchmark_trg_derived";

		final long[] benchmarkTimers = new long[3];

		var gen = new FacebookToInstagramGrammar_GEN_Run(generatedSrcModelName, generatedTrgModelName) {
			public NeoGenerator createGenerator(NeoCoreBuilder builder) {
				var allRules = new API_FacebookToInstagramGrammar_GEN(builder)
						.getAllRulesForFacebookToInstagramGrammar_GEN();

				var maxRuleApps = new MaximalRuleApplicationsTerminationCondition(allRules, 500)//
						.setMax(API_Transformations.FacebookToInstagramGrammar__NetworkToNetworkIslandRule, 10)
						.setMax(API_Transformations.FacebookToInstagramGrammar__UserToUserIslandRule, 1000)
						.setMax(API_Transformations.FacebookToInstagramGrammar__UserNetworkBridgeRule, 1000)
						.setMax(API_Transformations.FacebookToInstagramGrammar__RequestFriendship, 10000)
						.setMax(API_Transformations.FacebookToInstagramGrammar__AcceptFriendship, 5000)
						.setMax(API_Transformations.FacebookToInstagramGrammar__IgnoreInterNetworkFollowers, 500);

				var startUp = new IStartupModule() {
					@Override
					public void startup() {
						logger.info("Starting GEN");
						benchmarkTimers[0] = System.currentTimeMillis();
					}

					@Override
					public String description() {
						return "Logging & timer";
					}
				};

				var cleanUp = new ICleanupModule() {
					@Override
					public String description() {
						return "Logging & timer";
					}

					@Override
					public void cleanup() {
						benchmarkTimers[0] = System.currentTimeMillis() - benchmarkTimers[0];
						logger.info("Finished GEN");
					}
				};

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
						startUp, //
						new CompositeTerminationConditionForGEN(builder, modelSize, maxRuleApps), //
						new TwoPhaseRuleSchedulerForGEN(sampler), //
						new TwoPhaseUpdatePolicyForGEN(maxRuleApps), //
						new ParanoidNeoReprocessor(), //
						cleanUp, //
						new HeartBeatAndReportMonitor(), //
						new ModelNameValueGenerator(generatedSrcModelName, generatedTrgModelName), //
						List.of(new LoremIpsumStringValueGenerator()));
			}
		};

		var fwd = new FacebookToInstagramGrammar_FWD_Run(generatedSrcModelName, derivedTrgModelName) {
			public NeoGenerator createGenerator(NeoCoreBuilder builder) {
				var startUp = new PrepareTranslateAttributes(builder, generatedSrcModelName) {
					public void startup() {
						logger.info("Starting FWD");
						benchmarkTimers[1] = System.currentTimeMillis();
						super.startup();
					}
				};

				var cleanUp = new RemoveTranslateAttributes(builder, generatedSrcModelName) {
					public void cleanup() {
						super.cleanup();
						benchmarkTimers[1] = System.currentTimeMillis() - benchmarkTimers[1];
						logger.info("Finished FWD");
					}
				};

				return new NeoGenerator(//
						new API_FacebookToInstagramGrammar_FWD(builder).getAllRulesForFacebookToInstagramGrammar_FWD(), //
						startUp, //
						new NoMoreMatchesTerminationCondition(), //
						new FixedNoOfMatchesRuleScheduler(1), //
						new AnySingleMatchUpdatePolicy(), //
						new ParanoidNeoReprocessor(), //
						cleanUp, //
						new HeartBeatAndReportMonitor(), //
						new ModelNameValueGenerator(generatedSrcModelName, derivedTrgModelName), //
						List.of(new LoremIpsumStringValueGenerator()));
			}
		};

		var bwd = new FacebookToInstagramGrammar_BWD_Run(derivedSrcModelName, generatedTrgModelName) {
			public NeoGenerator createGenerator(NeoCoreBuilder builder) {
				var startUp = new PrepareTranslateAttributes(builder, generatedTrgModelName) {
					public void startup() {
						logger.info("Starting BWD");
						benchmarkTimers[2] = System.currentTimeMillis();
						super.startup();
					}
				};

				var cleanUp = new RemoveTranslateAttributes(builder, generatedTrgModelName) {
					public void cleanup() {
						super.cleanup();
						benchmarkTimers[2] = System.currentTimeMillis() - benchmarkTimers[2];
						logger.info("Finished BWD");
						builder.clearDataBase();
						logger.info("Database cleared");
					}
				};

				return new NeoGenerator(//
						new API_FacebookToInstagramGrammar_BWD(builder).getAllRulesForFacebookToInstagramGrammar_BWD(), //
						startUp, //
						new NoMoreMatchesTerminationCondition(), //
						new FixedNoOfMatchesRuleScheduler(1), //
						new AnySingleMatchUpdatePolicy(), //
						new ParanoidNeoReprocessor(), //
						cleanUp, //
						new HeartBeatAndReportMonitor(), //
						new ModelNameValueGenerator(derivedSrcModelName, generatedTrgModelName), //
						List.of(new LoremIpsumStringValueGenerator()));
			}
		};

		logger.info("Running benchmark");
		gen.run();
		fwd.run();
		bwd.run();
		logger.info("Benchmark completed");
		return benchmarkTimers;
	}
}
