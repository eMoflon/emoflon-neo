package org.emoflon.ibex.neo.benchmark.exttype2doc.concsync;

import java.io.IOException;
import java.util.Arrays;
import java.util.function.Function;

import org.eclipse.emf.ecore.resource.Resource;
import org.emoflon.ibex.neo.benchmark.IntegrationBench;
import org.emoflon.ibex.neo.benchmark.ModelAndDeltaGenerator;
import org.emoflon.ibex.tgg.operational.defaults.IbexOptions;
import org.emoflon.ibex.tgg.operational.strategies.integrate.FragmentProvider;
import org.emoflon.ibex.tgg.operational.strategies.integrate.INTEGRATE;
import org.emoflon.ibex.tgg.operational.strategies.integrate.conflicts.AttributeConflict;
import org.emoflon.ibex.tgg.operational.strategies.integrate.conflicts.CorrPreservationConflict;
import org.emoflon.ibex.tgg.operational.strategies.integrate.conflicts.DeletePreserveConflict;
import org.emoflon.ibex.tgg.operational.strategies.integrate.conflicts.resolution.util.CRSHelper;
import org.emoflon.ibex.tgg.operational.strategies.integrate.conflicts.resolution.util.ConflictResolver;
import org.emoflon.ibex.tgg.operational.strategies.integrate.pattern.IntegrationPattern;
import org.emoflon.ibex.tgg.operational.strategies.modules.TGGResourceHandler;
import org.emoflon.ibex.tgg.run.exttype2doc_concsync.INTEGRATE_App;
import org.emoflon.ibex.tgg.util.ilp.ILPFactory.SupportedILPSolver;

public class ExtType2Doc_ConcSync_Bench extends IntegrationBench<ExtType2Doc_ConcSync_Params> {
	
	protected int conflict_counter = 0;
	protected int conflict_solved_attr_counter = 0;
	protected int conflict_solved_delPres_counter = 0;
	protected int conflict_solved_move_counter = 0;

	public ExtType2Doc_ConcSync_Bench(String projectName) {
		super(projectName);
	}

	private final IntegrationPattern pattern = new IntegrationPattern(Arrays.asList( //
			FragmentProvider.APPLY_USER_DELTA //
			, FragmentProvider.REPAIR //
			, FragmentProvider.RESOLVE_CONFLICTS //
			, FragmentProvider.REPAIR //
			, FragmentProvider.RESOLVE_BROKEN_MATCHES //
			, FragmentProvider.TRANSLATE //
			, FragmentProvider.CLEAN_UP //
	));

	private final ConflictResolver crs = cc -> {
		conflict_counter++;
		CRSHelper.forEachResolve(cc, DeletePreserveConflict.class, s -> {
			s.crs_mergeAndPreserve();
			conflict_solved_delPres_counter++;
		});
		CRSHelper.forEachResolve(cc, CorrPreservationConflict.class, s -> {
			s.crs_preferSource();
			conflict_solved_move_counter++;
		});
		CRSHelper.forEachResolve(cc, AttributeConflict.class, s -> {
			s.crs_preferSource();
			conflict_solved_attr_counter++;
		});
	};

	@Override
	protected INTEGRATE initStub(TGGResourceHandler resourceHandler) throws IOException {
		Function<IbexOptions, IbexOptions> ibexOptions = options -> {
			options.resourceHandler(resourceHandler);
			options.ilpSolver(SupportedILPSolver.Sat4J);
			options.propagate.usePrecedenceGraph(true);
			options.repair.useShortcutRules(true);
			options.repair.advancedOverlapStrategies(false);
			options.repair.relaxedSCPatternMatching(true);
			options.repair.omitUnnecessaryContext(true);
			options.repair.disableInjectivity(true);
			options.integration.pattern(pattern);
			options.integration.conflictSolver(crs);
			return options;
		};

		return new INTEGRATE_App(ibexOptions);
	}

	@Override
	protected ModelAndDeltaGenerator<?, ?, ?, ?, ?, ExtType2Doc_ConcSync_Params> initModelAndDeltaGenerator(Resource s, Resource t, Resource c, Resource p,
			Resource d) {
		return new ExtType2Doc_ConcSync_MDGenerator(s, t, c, p, d);
	}

}
