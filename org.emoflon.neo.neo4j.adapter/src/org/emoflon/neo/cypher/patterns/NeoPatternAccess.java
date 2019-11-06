package org.emoflon.neo.cypher.patterns;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

import org.emoflon.neo.cypher.common.NeoMask;
import org.emoflon.neo.engine.generator.Schedule;

public abstract class NeoPatternAccess<Data extends NeoData, Mask extends NeoMask> {
	public abstract NeoPattern pattern();
	
	public Collection<NeoMatch> determineMatches(Schedule schedule, Mask mask){
		return pattern().determineMatches(schedule, mask);
	}

	public Collection<NeoMatch> determineMatches(Mask mask){
		return determineMatches(Schedule.unlimited(), mask);
	}
	
	public Optional<NeoMatch> determineOneMatch(Mask mask) {
		return determineMatches(Schedule.once(), mask).stream().findAny();
	}
	
	public int countMatches(Mask mask) {
		return pattern().countMatches(mask);
	}
	
	public abstract Mask mask();

	public abstract Stream<Data> data(Collection<NeoMatch> m);
}
