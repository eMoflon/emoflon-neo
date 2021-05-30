package org.emoflon.ibex.neo.benchmark.exttype2doc.concsync;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;

import org.eclipse.emf.ecore.resource.Resource;
import org.emoflon.ibex.neo.benchmark.IntegrationBench;
import org.emoflon.ibex.neo.benchmark.ModelAndDeltaGenerator;

import org.emoflon.neo.api.exttype2doc_concsync.run.ExtType2Doc_ConcSync_MI_Run;
import org.emoflon.neo.engine.modules.ilp.ILPBasedOperationalStrategy;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;

public class ExtType2Doc_ConcSync_Bench extends IntegrationBench<ExtType2Doc_ConcSync_Params> {
	
	protected int conflict_counter = 0;
	protected int conflict_solved_attr_counter = 0;
	protected int conflict_solved_delPres_counter = 0;
	protected int conflict_solved_move_counter = 0;

	public ExtType2Doc_ConcSync_Bench(String projectName, String projectPath) {
		super(projectName, projectPath);
	}

//	private final ConflictResolver crs = cc -> {
//		conflict_counter++;
//		CRSHelper.forEachResolve(cc, DeletePreserveConflict.class, s -> {
//			s.crs_mergeAndPreserve();
//			conflict_solved_delPres_counter++;
//		});
//		CRSHelper.forEachResolve(cc, CorrPreservationConflict.class, s -> {
//			s.crs_preferSource();
//			conflict_solved_move_counter++;
//		});
//		CRSHelper.forEachResolve(cc, AttributeConflict.class, s -> {
//			s.crs_preferSource();
//			conflict_solved_attr_counter++;
//		});
//	};

//	@Override
//	protected ExtType2Doc_ConcSync_MI_Run initStub(TGGResourceHandler resourceHandler) throws IOException {
//		Function<IbexOptions, IbexOptions> ibexOptions = options -> {
//			options.resourceHandler(resourceHandler);
//			options.ilpSolver(SupportedILPSolver.Sat4J);
//			options.propagate.usePrecedenceGraph(true);
//			options.repair.useShortcutRules(true);
//			options.repair.advancedOverlapStrategies(false);
//			options.repair.relaxedSCPatternMatching(true);
//			options.repair.omitUnnecessaryContext(true);
//			options.repair.disableInjectivity(true);
//			options.integration.pattern(pattern);
//			options.integration.conflictSolver(crs);
//			return options;
//		};

//		return new ExtType2Doc_ConcSync_MI_Run(source.toString(), target.toString());
//	}

//	@Override
//	protected ModelAndDeltaGenerator<?, ?, ?, ?, ?, ExtType2Doc_ConcSync_Params> initModelAndDeltaGenerator(Resource s, Resource t, Resource c, Resource p,
//			Resource d) {
//		return new ExtType2Doc_ConcSync_MDGenerator(s, t, c, p, d);
//	}

	@Override
	public ILPBasedOperationalStrategy initOpStrat() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ModelAndDeltaGenerator<?, ?, ?, ?, ?, ExtType2Doc_ConcSync_Params> initModelAndDeltaGenerator(Resource s,
			Resource t, Resource c, Resource p, Resource d) {
		// TODO Auto-generated method stub
		return null;
	}

}
