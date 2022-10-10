package liason.app;

import static org.emoflon.neo.api.liaison.run.RequirementsCoverage_GEN_Run.SRC_MODEL_NAME;
import static org.emoflon.neo.api.liaison.run.RequirementsCoverage_GEN_Run.TRG_MODEL_NAME;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.emoflon.neo.api.liaison.API_Common;
import org.emoflon.neo.api.liaison.API_Data;
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

		try (var builder = API_Common.createBuilder()) {
			var data = new API_Data(builder);
			builder.clearDataBase();
			builder.exportEMSLEntityToNeo4j(data.getModel_RequirementsCoverage_Source());
			builder.exportEMSLEntityToNeo4j(data.getModel_RequirementsCoverage_Target());
			var createCorr1 = data.getRule_CreateCorr1().rule();
			createCorr1.applyAll(createCorr1.determineMatches());
			var createCorr2 = data.getRule_CreateCorr2().rule();
			createCorr2.applyAll(createCorr2.determineMatches());			
		}
		
		app.run();
	}
}
