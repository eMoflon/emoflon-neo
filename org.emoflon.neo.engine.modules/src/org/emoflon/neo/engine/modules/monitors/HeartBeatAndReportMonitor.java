package org.emoflon.neo.engine.modules.monitors;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.generator.modules.IMonitor;

public class HeartBeatAndReportMonitor implements IMonitor {
	private static final Logger logger = Logger.getLogger(HeartBeatAndReportMonitor.class);

	private static final double interval = 5;
	private double heartBeats = 0;
	private Timer timerForHeartBeat = new Timer();

	private Timer timerForRuleScheduling = new Timer();
	private Timer timerForMatchSelection = new Timer();
	private Timer timerForPatternMatching = new Timer();
	private Timer timerForRuleApplication = new Timer();
	private Timer timerForMatchReprocessing = new Timer();

	private class Timer {
		private double start = 0;
		private double timeSpentInSeconds = 0;

		public void start() {
			start = System.currentTimeMillis();
		}

		public void stop() {
			timeSpentInSeconds += getTimeElapsedInSeconds();
		}

		public double getTimeElapsedInSeconds() {
			var now = System.currentTimeMillis();
			return (now - start)/1000.0;
		}
		
		public double getTimeSpentInSeconds() {
			return timeSpentInSeconds;
		}
	}

	@Override
	public void startRuleScheduling() {
		logger.debug("Started scheduling rules...");
		timerForRuleScheduling.start();
	}

	@Override
	public void finishRuleScheduling() {
		logger.debug("Finished scheduling rules.");
		timerForRuleScheduling.stop();
	}

	@Override
	public void startMatchSelection() {
		logger.debug("Started match selection...");
		timerForMatchSelection.start();
	}

	@Override
	public void finishMatchSelection() {
		logger.debug("Finished match selection.");
		timerForMatchSelection.stop();
	}

	@Override
	public void startPatternMatching() {
		logger.debug("Started pattern matching...");
		timerForPatternMatching.start();
	}

	@Override
	public void finishPatternMatching() {
		logger.debug("Finished pattern matching.");
		timerForPatternMatching.stop();
	}

	@Override
	public void startRuleApplication() {
		logger.debug("Started rule application...");
		timerForRuleApplication.start();
	}

	@Override
	public void finishRuleApplication() {
		logger.debug("Finished rule application.");
		timerForRuleApplication.stop();
	}

	@Override
	public void startReprocessingMatches() {
		logger.debug("Started reprocessing matches...");
		timerForMatchReprocessing.start();
	}

	@Override
	public void finishReprocessingMatches() {
		logger.debug("Finished reprocessing matches.");
		timerForMatchReprocessing.stop();
	}

	@Override
	public void heartBeat() {
		if(heartBeats == 0)
			timerForHeartBeat.start();
		
		heartBeats++;

		if (timerForHeartBeat.getTimeElapsedInSeconds() >= interval) {
			logger.info("Heartbeats per second: " + heartBeats / timerForHeartBeat.getTimeElapsedInSeconds());
			heartBeats = 0;
		}
	}

	@Override
	public void finishGeneration() {
		logger.debug("Finished generation.");
		
		synchronized (logger) {
			logger.info("");
			logger.info("********** Generation Report ************");
			logger.info("Rule scheduling took: " + timerForRuleScheduling.getTimeSpentInSeconds() + "s");
			logger.info("Match selection took: " + timerForMatchSelection.getTimeSpentInSeconds() + "s");
			logger.info("Pattern matching took: " + timerForPatternMatching.getTimeSpentInSeconds() + "s");
			logger.info("Rule application took: " + timerForRuleApplication.getTimeSpentInSeconds() + "s");
			logger.info("Match reprocessing took: " + timerForMatchReprocessing.getTimeSpentInSeconds() + "s");
			logger.info("********** Generation Report ************");
			logger.info("");
		}
 	}

}
