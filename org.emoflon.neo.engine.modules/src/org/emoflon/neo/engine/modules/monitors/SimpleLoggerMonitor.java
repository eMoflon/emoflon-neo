package org.emoflon.neo.engine.modules.monitors;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.generator.modules.IMonitor;

public class SimpleLoggerMonitor implements IMonitor {

	private static final Logger logger = Logger.getLogger(SimpleLoggerMonitor.class);

	@Override
	public void startRuleScheduling() {
		logger.info("Started scheduling rules...");
	}

	@Override
	public void finishRuleScheduling() {
		logger.info("Finished scheduling rules.");
	}

	@Override
	public void startMatchSelection() {
		logger.info("Started match selection...");
	}

	@Override
	public void finishMatchSelection() {
		logger.info("Finihsed match selection.");
	}

	@Override
	public void startReprocessingMatches() {
		logger.info("Started reprocessing matches...");
	}

	@Override
	public void finishReprocessingMatches() {
		logger.info("Finihsed reprocessing matches.");
	}

}
