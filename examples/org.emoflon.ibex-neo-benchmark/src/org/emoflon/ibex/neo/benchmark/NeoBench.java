package org.emoflon.ibex.neo.benchmark;

import java.io.IOException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.emoflon.delta.validation.InvalidDeltaException;
import org.emoflon.ibex.neo.benchmark.util.BenchEntry;
import org.emoflon.ibex.neo.benchmark.util.BenchParameters;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.modules.ilp.*;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
//import org.emoflon.neo.api.org.emoflon.benchmark.org.emoflon.ibex.neo.benchmark.exttype2doc.concsync.API_ConflictGenerator;

import run.emf.modelimport.*;

public abstract class NeoBench<OpStrat extends ILPBasedOperationalStrategy, BenchParams extends BenchParameters> {

	protected final String projectName;
	protected final String projectPath;
	protected final String genModelFolder = "gen-models";

	protected final String filename_src = "src.xmi";
	protected final String filename_trg = "trg.xmi";
	protected final String filename_corr = "corr.xmi";
	protected final String filename_protocol = "/protocol.xmi";
	protected final String filename_delta = "/delta.xmi";

	protected final String workspacePath = "../";
	protected final URI base = URI.createPlatformResourceURI("/", true);
	protected ResourceSet rs;

	protected Resource source;
	protected Resource target;
	protected Resource corr;
	protected Resource protocol;
	protected Resource delta;

	protected int numOfElements = -1;
	protected NeoCoreBuilder builder;
	protected SupportedILPSolver solver = SupportedILPSolver.Gurobi;

	public NeoBench(String projectName, String projectPath) {
		this.projectName = projectName;
		this.projectPath = projectPath;
	}
	
	public abstract ILPBasedOperationalStrategy initOpStrat(NeoCoreBuilder builder, SupportedILPSolver solver);
	
	public BenchEntry genAndBench(BenchParams parameters) {
		genAndStore(parameters);
		return loadAndBench(parameters);
	}

	public void genAndStore(BenchParams parameters) {
			
			String modelPath = getModelPath(parameters);
			String metamodelPath = projectPath + "/metamodels/";
			
			EMFImportToENeo.loadModelsAndMetamodels(metamodelPath, modelPath, filename_src, filename_trg, filename_corr);
	}
	
	protected String getModelPath(BenchParams parameters) {
		return projectPath + "/" + genModelFolder + "/" + parameters.name + "_n" + parameters.modelScale + "_c" + parameters.numOfChanges + "_H/";
	}
	
	public BenchEntry loadAndBench(BenchParams parameters) {
		
		try {
			
			ILPBasedOperationalStrategy opStrat = initOpStrat(builder, solver);

			return applyDeltaAndRun(opStrat, parameters, solver);
		} catch (IOException | InvalidDeltaException e) {
			e.printStackTrace();
		}
		return null;
	}

	protected abstract BenchEntry applyDeltaAndRun(ILPBasedOperationalStrategy opStrat, BenchParams parameters, SupportedILPSolver solver/*, boolean saveTransformedModels*/)
			throws IOException, InvalidDeltaException;

}
