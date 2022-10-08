package liason.app;

import static org.emoflon.neo.api.liaison.run.RequirementsCoverage_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.liaison.run.RequirementsCoverage_GEN_Run.TRG_MODEL_NAME;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.liaison.API_Common;
import org.emoflon.neo.api.liaison.API_Delta;
import org.emoflon.neo.api.liaison.run.RequirementsCoverage_MI_Run;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;

public class MyMI extends RequirementsCoverage_MI_Run {
	
	public MyMI(String srcModelName, String trgModelName) {
		super(srcModelName, trgModelName);
	}

	public static void main(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new MyMI(SRC_MODEL_NAME, TRG_MODEL_NAME);
		MyMI.solver = SupportedILPSolver.Sat4J;

		try (var builder = API_Common.createBuilder()) {
			var delta = new API_Delta(builder);
			builder.clearDataBase();
			builder.exportEMSLEntityToNeo4j(delta.getModel_RequirementsCoverage_Source());
			builder.exportEMSLEntityToNeo4j(delta.getModel_RequirementsCoverage_Target());
			
			app.run();
		}
	}
}
