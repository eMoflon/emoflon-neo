/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.classinhhier2db.bin.run;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.classinhhier2db.API_Common;
import org.emoflon.neo.api.classinhhier2db.bin.API_Schema;
import org.emoflon.neo.api.classinhhier2db.bin.tgg.API_FamiliesToPersons_Constrained_GEN;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.modules.NeoGenerator;

import org.emoflon.neo.engine.generator.INodeSampler;
import org.emoflon.neo.engine.modules.cleanup.NoOpCleanup;
import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.TwoPhaseRuleSchedulerForGEN;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.CompositeTerminationConditionForGEN;
import org.emoflon.neo.engine.modules.terminationcondition.MaximalRuleApplicationsTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.TwoPhaseUpdatePolicyForGEN;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;
import java.util.List;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("unused")
public class FamiliesToPersons_Constrained_GEN_Run {
	public static final String SRC_MODEL_NAME = "FamiliesToPersons_Constrained_Source";
	public static final String TRG_MODEL_NAME = "FamiliesToPersons_Constrained_Target";
	protected static final Logger logger = Logger.getLogger(FamiliesToPersons_Constrained_GEN_Run.class);
	protected String srcModelName;
	protected String trgModelName;
	
	public static void main(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new FamiliesToPersons_Constrained_GEN_Run(SRC_MODEL_NAME, TRG_MODEL_NAME);
		app.run();
	}
	
	public FamiliesToPersons_Constrained_GEN_Run(String srcModelName, String trgModelName) {
		this.srcModelName = srcModelName;
		this.trgModelName = trgModelName;
	}
	
	
	public void run() throws Exception {
		try (var builder = API_Common.createBuilder()) {
			new API_Schema(builder).exportMetamodelsForFamiliesToPersons_Constrained();
	
			var generator = createGenerator(builder);
	
			logger.info("Running generator...");
			generator.generate();
			logger.info("Generator terminated.");
		}
	}
	
	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var allRules = new API_FamiliesToPersons_Constrained_GEN(builder).getAllRulesForFamiliesToPersons_Constrained_GEN();
		var maxRuleApps = new MaximalRuleApplicationsTerminationCondition(allRules, -1);
		
		INodeSampler sampler = (String type, String ruleName, String nodeName) -> {
			return INodeSampler.EMPTY;
		};
		
		return new NeoGenerator(//
				allRules, //
				new NoOpStartup(), //
				new CompositeTerminationConditionForGEN(2, TimeUnit.MINUTES, maxRuleApps), //
				new TwoPhaseRuleSchedulerForGEN(sampler), //
				new TwoPhaseUpdatePolicyForGEN(maxRuleApps), //
				new ParanoidNeoReprocessor(), //
				new NoOpCleanup(), //
				new HeartBeatAndReportMonitor(), //
				new ModelNameValueGenerator(srcModelName, trgModelName), //
				List.of(new LoremIpsumStringValueGenerator()));
	}
}
