package org.emoflon.neo.engine.generator.modules;

public interface IMonitor {
	public void startRuleScheduling();
	public void finishRuleScheduling();
	
	public void startMatchSelection();
	public void finishMatchSelection();
	
	public void startReprocessingMatches();
	public void finishReprocessingMatches();
}
