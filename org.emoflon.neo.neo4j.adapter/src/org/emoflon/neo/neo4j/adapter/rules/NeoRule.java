package org.emoflon.neo.neo4j.adapter.rules;

import java.util.Collection;

import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPattern;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;

public abstract class NeoRule implements IRule<NeoMatch, NeoCoMatch> {
	protected boolean useSPOSemantics;
	protected NeoPattern contextPattern;
	protected IBuilder builder;
	protected NeoMask mask;
	protected NeoQueryData queryData;

	public NeoRule(Rule r, NeoPattern contextPattern, IBuilder builder, NeoMask mask, NeoQueryData neoQuery) {
		useSPOSemantics = false;
		this.contextPattern = contextPattern;
		this.builder = builder;
		this.queryData = neoQuery;
	}

	@Override
	public void useSPOSemantics(boolean spoSemantics) {
		this.useSPOSemantics = spoSemantics;
	}

	@Override
	public String getName() {
		return contextPattern.getName();
	}

	@Override
	public void setMatchInjectively(Boolean injective) {
		contextPattern.setMatchInjectively(injective);
	}

	@Override
	public Collection<NeoMatch> determineMatches() {
		return contextPattern.determineMatches();
	}

	@Override
	public Collection<NeoMatch> determineMatches(int limit) {
		return contextPattern.determineMatches(limit);
	}
}
