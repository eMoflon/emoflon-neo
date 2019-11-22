package org.emoflon.neo.engine.modules.monitors;

import java.math.RoundingMode;
import java.text.DecimalFormat;

import org.apache.log4j.Logger;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.rules.NeoCoMatch;
import org.emoflon.neo.engine.generator.MatchContainer;
import org.emoflon.neo.engine.generator.modules.IMonitor;

public class HeartBeatAndReportMonitor implements IMonitor<NeoMatch, NeoCoMatch> {
	private static final Logger logger = Logger.getLogger(HeartBeatAndReportMonitor.class);

	private static final double interval = 5;
	private double heartBeats = 0;
	private double elements = 0;
	private double ruleApps = 0;
	private Timer timerForHeartBeat = new Timer("Heart beat");

	private Timer totalTimeSpent = new Timer("Total time");
	private Timer timerForStartup;
	private Timer timerForRuleScheduling = new Timer("Rule scheduling");
	private Timer timerForMatchSelection = new Timer("Match selection");
	private Timer timerForPatternMatching = new Timer("Pattern matching");
	private Timer timerForRuleApplication = new Timer("Rule application");
	private Timer timerForMatchReprocessing = new Timer("Match reprocessing");
	private Timer timerForCleanup;

	public HeartBeatAndReportMonitor() {
		totalTimeSpent.start();
	}

	private class Timer {
		private double start = 0;
		private double timeSpentInSeconds = 0;
		private String description;
		
		public Timer(String description) {
			this.description = description;
		}
		
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

		public String getDesc() {
			return description;
		}
	}

	private void startTimer(Timer timer) {
		logger.debug("Started " + timer.getDesc());
		timer.start();
	}
	
	private void stopTimer(Timer timer) {
		logger.debug("Finished " + timer.getDesc());
		timer.stop();
	}
	
	@Override
	public void startStartup(String startupDescription) {
		timerForStartup = new Timer(startupDescription);
		startTimer(timerForStartup);
	}


	@Override
	public void finishStartup() {
		stopTimer(timerForStartup);
	}

	@Override
	public void startRuleScheduling() {
		startTimer(timerForRuleScheduling);
	}

	@Override
	public void finishRuleScheduling() {
		stopTimer(timerForRuleScheduling);
	}

	@Override
	public void startMatchSelection() {
		startTimer(timerForMatchSelection);
	}

	@Override
	public void finishMatchSelection() {
		stopTimer(timerForMatchSelection);
	}

	@Override
	public void startPatternMatching() {
		startTimer(timerForPatternMatching);
	}

	@Override
	public void finishPatternMatching() {
		stopTimer(timerForPatternMatching);
	}

	@Override
	public void startRuleApplication() {
		startTimer(timerForRuleApplication);
	}

	@Override
	public void finishRuleApplication() {
		stopTimer(timerForRuleApplication);
	}

	@Override
	public void startReprocessingMatches() {
		startTimer(timerForMatchReprocessing);
	}

	@Override
	public void finishReprocessingMatches() {
		stopTimer(timerForMatchReprocessing);
	}

	@Override
	public void startCleanup(String cleanupDescription) {
		timerForCleanup = new Timer(cleanupDescription);
		startTimer(timerForCleanup);
	}

	@Override
	public void finishCleanup() {
		stopTimer(timerForCleanup);
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
					df.format((matchContainer.getNumberOfGeneratedElements() - elements)
							/ timerForHeartBeat.getTimeElapsedInSeconds()));
			logger.info("Applied rules/second: " + //
					df.format((matchContainer.getNumberOfRuleApplications() - ruleApps)
							/ timerForHeartBeat.getTimeElapsedInSeconds()));
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
			logger.info(totalTimeSpent.getDesc() + ": " + totalTimeSpent.getTimeElapsedInSeconds() + "s");
			logger.info(timerForStartup.getDesc() + ": " + timerForStartup.getTimeSpentInSeconds() + "s");
			logger.info(timerForRuleScheduling.getDesc() + ": " + timerForRuleScheduling.getTimeSpentInSeconds() + "s");
			logger.info(timerForMatchSelection.getDesc() + ": " + timerForMatchSelection.getTimeSpentInSeconds() + "s");
			logger.info(timerForPatternMatching.getDesc() + ": " + timerForPatternMatching.getTimeSpentInSeconds() + "s");
			logger.info(timerForRuleApplication.getDesc() + ": " + timerForRuleApplication.getTimeSpentInSeconds() + "s");
			logger.info(timerForMatchReprocessing.getDesc() + ": " + timerForMatchReprocessing.getTimeSpentInSeconds() + "s");
			logger.info(timerForCleanup.getDesc() + ": " + timerForCleanup.getTimeSpentInSeconds() + "s");
			logger.info("Total rules applied: " + matchContainer.getNumberOfRuleApplications());
			if (matchContainer.getNumberOfRuleApplications() > 0) {
				logger.info("Rules applied: ");
				matchContainer.getRuleApplications().entrySet().stream()//
						.forEach(entry -> logger.info(" =>  " + entry.getValue() + " @ " + entry.getKey()));
			}
			logger.info("Elements generated:  " + matchContainer.getNumberOfGeneratedElements());
			logger.info("********** Generation Report ************");
			logger.info("");
		}
	}

}
