package run;

import java.util.LinkedHashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.emoflon.neo.api.javatodocsle.metamodels.API_SimpleJavaSLE;
import org.emoflon.neo.api.javatodocsle.tgg.API_JavaToDocSLE_BWD;
import org.emoflon.neo.api.javatodocsle.tgg.API_JavaToDocSLE_FWD;
import org.emoflon.neo.api.javatodocsle.tgg.API_JavaToDocSLE_GEN;
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
import org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_GEN_Run;
import org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_FWD_Run;
import org.emoflon.neo.api.javatodocsle.run.JavaToDocSLE_BWD_Run;

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
		final String generatedSrcModelName = "JavaToDocSLE_benchmark_src";
		final String generatedTrgModelName = "JavaToDocSLE_benchmark_trg";
		final String derivedSrcModelName = "JavaToDocSLE_benchmark_src_derived";
		final String derivedTrgModelName = "JavaToDocSLE_benchmark_trg_derived";

		final long[] benchmarkTimers = new long[3];

		var gen = new JavaToDocSLE_GEN_Run(generatedSrcModelName, generatedTrgModelName) {
			public NeoGenerator createGenerator(NeoCoreBuilder builder) {
				var allRules = new API_JavaToDocSLE_GEN(builder).getAllRulesForJavaToDocSLE_GEN();
				var maxRuleApps = new MaximalRuleApplicationsTerminationCondition(allRules, -1);

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
					switch (type) {
					case API_SimpleJavaSLE.SimpleJavaSLE__Clazz:
						return 1;
					default:
						return INodeSampler.EMPTY;
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

		var fwd = new JavaToDocSLE_FWD_Run(generatedSrcModelName, derivedTrgModelName) {
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
						new API_JavaToDocSLE_FWD(builder).getAllRulesForJavaToDocSLE_FWD(), //
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

		var bwd = new JavaToDocSLE_BWD_Run(derivedSrcModelName, generatedTrgModelName) {
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
						new API_JavaToDocSLE_BWD(builder).getAllRulesForJavaToDocSLE_BWD(), //
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
