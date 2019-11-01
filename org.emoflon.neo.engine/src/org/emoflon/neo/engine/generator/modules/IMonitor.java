package org.emoflon.neo.engine.generator.modules;

import org.emoflon.neo.engine.api.patterns.IMatch;
import org.emoflon.neo.engine.api.rules.ICoMatch;
import org.emoflon.neo.engine.generator.MatchContainer;

public interface IMonitor<M extends IMatch, C extends ICoMatch> {
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
	
	public void finishGeneration(MatchContainer<M, C> matchContainer);

	public void heartBeat(MatchContainer<M, C> matchContainer);
}
