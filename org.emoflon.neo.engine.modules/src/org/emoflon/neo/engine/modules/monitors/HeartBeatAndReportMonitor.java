package org.emoflon.neo.engine.modules.monitors;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.apache.log4j.Logger;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMonitor;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class HeartBeatAndReportMonitor implements IMonitor<NeoMatch, NeoCoMatch> {
	private static final Logger logger = Logger.getLogger(HeartBeatAndReportMonitor.class);

	private static final double interval = 5;
	private double heartBeats = 0;
	private double elements = 0;
	private double ruleApps = 0;
	private Timer timerForHeartBeat = new Timer();

	private Timer totalTimeSpent = new Timer();
	private Timer timerForRuleScheduling = new Timer();
	private Timer timerForMatchSelection = new Timer();
	private Timer timerForPatternMatching = new Timer();
	private Timer timerForRuleApplication = new Timer();
	private Timer timerForMatchReprocessing = new Timer();

	public HeartBeatAndReportMonitor() {
		totalTimeSpent.start();
	}

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
			return (now - start) / 1000.0;
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
	public void heartBeat(MatchContainer<NeoMatch, NeoCoMatch> matchContainer) {
		if (heartBeats == 0)
			timerForHeartBeat.start();

		heartBeats++;

		if (timerForHeartBeat.getTimeElapsedInSeconds() >= interval) {
			DecimalFormat df = new DecimalFormat("#.##");
			df.setRoundingMode(RoundingMode.CEILING);
			logger.info("*********");
			logger.info("Heartbeats/second: " + //
					df.format(heartBeats / timerForHeartBeat.getTimeElapsedInSeconds()));
			logger.info("Generated elements/second: " + //
					df.format((matchContainer.getNumberOfGeneratedElements() - elements) / timerForHeartBeat.getTimeElapsedInSeconds()));
			logger.info("Applied rules/second: " + //
					df.format((matchContainer.getNumberOfRuleApplications() - ruleApps) / timerForHeartBeat.getTimeElapsedInSeconds()));
			logger.info("*********");
			
			heartBeats = 0;
			elements = matchContainer.getNumberOfGeneratedElements();
			ruleApps = matchContainer.getNumberOfRuleApplications();
		}
	}

	@Override
	public void finishGeneration(MatchContainer<NeoMatch, NeoCoMatch> matchContainer) {
		logger.debug("Finished generation.");

		synchronized (logger) {
			logger.info("");
			logger.info("********** Generation Report ************");
			logger.info("Total time spent: " + totalTimeSpent.getTimeElapsedInSeconds() + "s");
			logger.info("Rule scheduling took: " + timerForRuleScheduling.getTimeSpentInSeconds() + "s");
			logger.info("Match selection took: " + timerForMatchSelection.getTimeSpentInSeconds() + "s");
			logger.info("Pattern matching took: " + timerForPatternMatching.getTimeSpentInSeconds() + "s");
			logger.info("Rule application took: " + timerForRuleApplication.getTimeSpentInSeconds() + "s");
			logger.info("Match reprocessing took: " + timerForMatchReprocessing.getTimeSpentInSeconds() + "s");
			logger.info("Rules applied: ");
			matchContainer.getRuleApplications().entrySet().stream()//
					.forEach(entry -> logger.info(" =>  " + entry.getValue() + " @ " + entry.getKey()));
			logger.info("Elements generated:  " + matchContainer.getNumberOfGeneratedElements());
			logger.info("Total rules applied: " + matchContainer.getNumberOfRuleApplications());
			logger.info("********** Generation Report ************");
			logger.info("");
		}
	}

}
