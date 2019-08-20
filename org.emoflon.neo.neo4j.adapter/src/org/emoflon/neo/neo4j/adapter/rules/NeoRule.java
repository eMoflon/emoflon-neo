package org.emoflon.neo.neo4j.adapter.rules;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.emoflon.neo.emsl.eMSL.ActionOperator;
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.engine.api.rules.IRule;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPattern;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPatternFactory;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;
import org.emoflon.neo.neo4j.adapter.util.NeoUtil;

public class NeoRule implements IRule<NeoMatch, NeoCoMatch> {
	protected boolean useSPOSemantics;
	protected NeoPattern contextPattern;
	protected IBuilder builder;
	protected NeoMask mask;
	protected NeoQueryData queryData;

	protected List<ModelNodeBlock> blackNodes;
	protected List<ModelNodeBlock> redNodes;

	public NeoRule(Rule r, IBuilder builder, NeoMask mask, NeoQueryData neoQuery) {
		useSPOSemantics = false;
		this.builder = builder;
		this.queryData = neoQuery;

		var flatRule = NeoUtil.getFlattenedRule(r);
		var nodeBlocks = flatRule.getNodeBlocks();

		blackNodes = extractBlackNodes(nodeBlocks);
		redNodes = extractRedNodes(nodeBlocks);

		var redAndBlackNodes = new ArrayList<>(blackNodes);
		redAndBlackNodes.addAll(redNodes);

		contextPattern = NeoPatternFactory.createNeoPattern(flatRule.getName(), redAndBlackNodes,
				flatRule.getCondition(), builder, mask);
	}

	private List<ModelNodeBlock> extractRedNodes(List<ModelNodeBlock> nodeBlocks) {
		return nodeBlocks.stream()//
				.filter(nb -> nb.getAction() != null)//
				.filter(nb -> nb.getAction().getOp().equals(ActionOperator.DELETE))//
				.collect(Collectors.toList());
	}

	private List<ModelNodeBlock> extractBlackNodes(List<ModelNodeBlock> nodeBlocks) {
		return nodeBlocks.stream()//
				.filter(nb -> nb.getAction() == null)//
				.collect(Collectors.toList());
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

	@Override
	public Optional<NeoCoMatch> apply(NeoMatch match) {
		// TODO[Jannik]
		return Optional.empty();
	}
}
