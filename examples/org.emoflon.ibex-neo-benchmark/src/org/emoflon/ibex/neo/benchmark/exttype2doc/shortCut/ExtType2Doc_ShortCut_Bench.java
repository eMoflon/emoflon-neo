package org.emoflon.ibex.neo.benchmark.exttype2doc.shortCut;

import java.io.IOException;
import java.util.function.Function;

import org.eclipse.emf.ecore.resource.Resource;
import org.emoflon.delta.validation.InvalidDeltaException;
import org.emoflon.ibex.neo.benchmark.ModelAndDeltaGenerator;
import org.emoflon.ibex.neo.benchmark.SynchronizationBench;
import org.emoflon.ibex.neo.benchmark.util.BenchEntry;
import org.emoflon.neo.engine.modules.ilp.ILPBasedOperationalStrategy;

public class ExtType2Doc_ShortCut_Bench extends SynchronizationBench<ExtType2Doc_ShortCut_Params> {

	public ExtType2Doc_ShortCut_Bench(String projectName, String pathName) {
		super(projectName, pathName);
	}

//	@Override
//	protected SYNC initStub(TGGResourceHandler resourceHandler) throws IOException {
//		Function<IbexOptions, IbexOptions> ibexOptions = options -> {
//			options.resourceHandler(resourceHandler);
//			options.ilpSolver(SupportedILPSolver.Sat4J);
//			options.propagate.usePrecedenceGraph(true);
//			options.repair.useShortcutRules(true);
//			options.repair.advancedOverlapStrategies(false);
//			options.repair.relaxedSCPatternMatching(true);
//			options.repair.omitUnnecessaryContext(true);
//			options.repair.disableInjectivity(true);
//			return options;
//		};
//		return new SYNC_App(ibexOptions);
//	}

	@Override
	protected ModelAndDeltaGenerator<?, ?, ?, ?, ?, ExtType2Doc_ShortCut_Params> initModelAndDeltaGenerator(Resource s, Resource t, Resource c, Resource p,
			Resource d) {
		return null;
		//return new ExtType2Doc_ShortCut_MDGenerator(s, t, c, p, d);
	}

	@Override
	public ILPBasedOperationalStrategy initOpStrat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected BenchEntry applyDeltaAndRun(ILPBasedOperationalStrategy opStrat, ExtType2Doc_ShortCut_Params parameters,
			boolean saveTransformedModels) throws IOException, InvalidDeltaException {
		// TODO Auto-generated method stub
		return null;
	}

}
