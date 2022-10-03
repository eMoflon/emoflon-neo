package liason.app;

import static org.emoflon.neo.api.liaison.run.RequirementsCoverage_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.liaison.run.RequirementsCoverage_GEN_Run.TRG_MODEL_NAME;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.liaison.run.RequirementsCoverage_CO_Run;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;

public class MyCO extends RequirementsCoverage_CO_Run {

	public MyCO(String srcModelName, String trgModelName) {
		super(srcModelName, trgModelName);
	}

	public static void main(String[] args) throws Exception {
		Logger.getRootLogger().setLevel(Level.INFO);
		var app = new MyCO(SRC_MODEL_NAME, TRG_MODEL_NAME);
		MyCO.solver = SupportedILPSolver.Sat4J;
		app.run();
	}
}
