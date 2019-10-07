package org.emoflon.neo.engine.generator.modules;

public interface IMonitor {
	public void heartBeat();

	public void startRuleScheduling();
	public void finishRuleScheduling();
	
	public void startPatternMatching();
	public void finishPatternMatching();
	
	public void startMatchSelection();
	public void finishMatchSelection();
	
	public void startReprocessingMatches();
	public void finishReprocessingMatches();
	
	public void startRuleApplication();
	public void finishRuleApplication();
	
	public void finishGeneration();
}
