package ctorust.runtime;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.ctorust.API_Common;
import org.emoflon.neo.api.ctorust.API_CtoRust_Function;
import org.emoflon.neo.api.ctorust.API_Models;
import org.emoflon.neo.api.ctorust.run.CtoRust_CC_Run;
import org.emoflon.neo.api.ctorust.tgg.API_CtoRust_CC;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.modules.NeoGenerator;

import static org.emoflon.neo.api.ctorust.run.CtoRust_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.ctorust.run.CtoRust_GEN_Run.TRG_MODEL_NAME;
		
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.emoflon.neo.engine.api.constraints.IConstraint;
import org.emoflon.neo.api.ctorust.tgg.API_CtoRust_GEN;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.monitors.HeartBeatAndReportMonitor;
import org.emoflon.neo.engine.modules.startup.NoOpStartup;
import org.emoflon.neo.engine.modules.terminationcondition.NoMoreMatchesTerminationCondition;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.ModelNameValueGenerator;
import org.emoflon.neo.engine.modules.analysis.TripleRuleAnalyser;
import org.emoflon.neo.engine.modules.matchreprocessors.CCReprocessor;
import org.emoflon.neo.engine.modules.ruleschedulers.CCRuleScheduler;
import org.emoflon.neo.engine.modules.updatepolicies.CorrCreationOperationalStrategy;

@SuppressWarnings("unused")
public class CtoRust_CC_Run_App extends CtoRust_CC_Run {
	public CtoRust_CC_Run_App(String srcModelName, String trgModelName) {
		super(srcModelName, trgModelName, SupportedILPSolver.Sat4J);
	}
	
	public NeoGenerator createGenerator(NeoCoreBuilder builder) {
		var gen = super.createGenerator(builder);
		gen.getAttrConstrContainer().addCreator("cEnumToRustEnum", () -> new CEnumToRustEnum());
		return gen;
	}
	
	public static void main(String[] args) throws Exception {
		try (var builder = API_Common.createBuilder()) {
			builder.clearDataBase();
			builder.exportEMSLEntityToNeo4j(new API_Models(builder).getModel_CtoRust_Source());
			builder.exportEMSLEntityToNeo4j(new API_Models(builder).getModel_CtoRust_Target());
		}	
		
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new CtoRust_CC_Run_App(SRC_MODEL_NAME, TRG_MODEL_NAME);
		app.run();
	}
}
