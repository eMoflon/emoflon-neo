package org.emoflon.ibex.neo.benchmark;

import java.io.IOException;

import org.emoflon.delta.validation.InvalidDeltaException;
import org.emoflon.ibex.neo.benchmark.util.BenchEntry;
import org.emoflon.ibex.neo.benchmark.util.BenchParameters;
import org.emoflon.ibex.tgg.operational.strategies.sync.SYNC;

import delta.Delta;
import delta.DeltaContainer;

public abstract class SynchronizationBench<BP extends BenchParameters> extends IbexBench<SYNC, BP> {

	public SynchronizationBench(String projectName) {
		super(projectName);
	}

	@Override
	protected BenchEntry applyDeltaAndRun(SYNC opStrat, BP parameters, boolean saveTransformedModels) throws IOException, InvalidDeltaException {
		long tic = System.currentTimeMillis();
		opStrat.run();
		long toc = System.currentTimeMillis();
		double init = (double) (toc - tic) / 1000;
		
		DeltaContainer deltaContainer = (DeltaContainer) delta.getContents().get(0);
		for (Delta delta : deltaContainer.getDeltas())
			delta.apply();
		
		tic = System.currentTimeMillis();
		opStrat.run();
		toc = System.currentTimeMillis();
		double resolve = (double) (toc - tic) / 1000;
		
		if (saveTransformedModels)
			opStrat.saveModels();
		opStrat.terminate();

		return new BenchEntry(parameters.modelScale, parameters.numOfChanges, numOfElements, init, resolve);
	}

}
