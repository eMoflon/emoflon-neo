package org.emoflon.ibex.neo.benchmark;

import java.io.IOException;

import org.emoflon.delta.validation.InvalidDeltaException;
import org.emoflon.ibex.neo.benchmark.util.BenchEntry;
import org.emoflon.ibex.neo.benchmark.util.BenchParameters;
import org.emoflon.ibex.tgg.operational.strategies.integrate.INTEGRATE;

import delta.DeltaContainer;

public abstract class IntegrationBench<BP extends BenchParameters> extends IbexBench<INTEGRATE, BP> {

	public IntegrationBench(String projectName) {
		super(projectName);
	}

	@Override
	protected BenchEntry applyDeltaAndRun(INTEGRATE opStrat, BP parameters, boolean saveTransformedModels) throws IOException, InvalidDeltaException {
		long tic = System.currentTimeMillis();
		opStrat.run();
		long toc = System.currentTimeMillis();
		double init = (double) (toc - tic) / 1000;

		DeltaContainer deltaContainer = (DeltaContainer) delta.getContents().get(0);
		opStrat.applyDelta(deltaContainer);

		tic = System.currentTimeMillis();
		opStrat.integrate();
		toc = System.currentTimeMillis();
		double resolve = (double) (toc - tic) / 1000;

		if (saveTransformedModels)
			opStrat.saveModels();
		opStrat.terminate();

		return new BenchEntry(parameters.modelScale, parameters.numOfChanges, numOfElements, init, resolve);
	}

}
