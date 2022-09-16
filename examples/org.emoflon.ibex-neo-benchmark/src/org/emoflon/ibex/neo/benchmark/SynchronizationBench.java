package org.emoflon.ibex.neo.benchmark;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.emoflon.delta.validation.InvalidDeltaException;
import org.emoflon.ibex.neo.benchmark.util.BenchEntry;
import org.emoflon.ibex.neo.benchmark.util.BenchParameters;
import org.emoflon.neo.api.org.emoflon.benchmark.API_Common;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.engine.modules.NeoGenerator;
import org.emoflon.neo.engine.modules.ilp.ILPBasedOperationalStrategy;
import org.emoflon.neo.engine.modules.ilp.ILPFactory.SupportedILPSolver;
import org.emoflon.neo.engine.modules.updatepolicies.ModelIntegrationOperationalStrategy;

public abstract class SynchronizationBench<BP extends BenchParameters> extends NeoBench<ILPBasedOperationalStrategy, BP> {

	public SynchronizationBench(String projectName, String projectPath) {
		super(projectName, projectPath);
	}

	@Override
	protected BenchEntry applyDeltaAndRun(ILPBasedOperationalStrategy opStrat, BP parameters, SupportedILPSolver solver/*, boolean saveTransformedModels*/) throws IOException, InvalidDeltaException {
		long tic = System.currentTimeMillis();
		applyDelta(parameters);
		long toc = System.currentTimeMillis();
		double init = (double) (toc - tic) / 1000;

		tic = System.currentTimeMillis();
		run(solver);
		toc = System.currentTimeMillis();
		double resolve = (double) (toc - tic) / 1000;
		
		int ram = calcUsedRAM();

		return new BenchEntry<>(parameters, numOfElements, init, resolve, ram, 1.0);
	}
		
		protected static final Logger logger = Logger.getLogger(IntegrationBench.class);
		
		public void run(SupportedILPSolver solver) {
			try (var builder = API_Common.createBuilder()) {
		
				var generator = createGenerator(builder, solver);
		
				logger.info("Running generator...");
				generator.generate();
				logger.info("Generator terminated.");
			}
			catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public abstract NeoGenerator createGenerator(NeoCoreBuilder builder, SupportedILPSolver solver);

		abstract protected void applyDelta(BP parameters);
		
		@Override
		public abstract ModelIntegrationOperationalStrategy initOpStrat(NeoCoreBuilder builder, SupportedILPSolver solver);
	
		protected int calcUsedRAM() {
			Runtime.getRuntime().gc();
			return (int) ((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024));
		}
}
