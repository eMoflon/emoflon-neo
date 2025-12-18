package ctorust.runtime;
import static org.emoflon.neo.api.ctorust.run.CtoRust_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.ctorust.run.CtoRust_GEN_Run.TRG_MODEL_NAME;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.ctorust.API_Common;
import org.emoflon.neo.api.ctorust.API_Models;
import org.emoflon.neo.api.ctorust.run.CtoRust_BWD_OPT_Run;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;

public class CtoRust_BWD_OPT_Run_App extends CtoRust_BWD_OPT_Run {
	public CtoRust_BWD_OPT_Run_App(String srcModelName, String trgModelName) {
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
			builder.exportEMSLEntityToNeo4j(new API_Models(builder).getModel_CtoRust_Target());
		}	
		
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new CtoRust_BWD_OPT_Run_App(SRC_MODEL_NAME, TRG_MODEL_NAME);
		app.run();
	}
}
