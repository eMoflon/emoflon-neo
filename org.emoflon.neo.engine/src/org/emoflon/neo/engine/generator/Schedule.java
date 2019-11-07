package org.emoflon.neo.engine.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.emoflon.neo.engine.api.rules.IRule;

public class Schedule {

	public static final int UNLIMITED = -1;
	private static final String NODE_RANGE_PARAM = "nodeRange";
	private int limit;
	private Optional<IRule<?, ?>> rule;
	private NodeRange nodeRange;
	private INodeSampler sampler;
	private Map<String, Object> parameters = new HashMap<>();

	public Schedule(int limit, NodeRange nodeRange, Optional<IRule<?, ?>> rule, INodeSampler sampler) {
		this.limit = limit;
		this.nodeRange = nodeRange;
		this.rule = rule;
		this.sampler = sampler;
	}

	public Schedule(int limit, NodeRange nodeRange, IRule<?, ?> rule, INodeSampler sampler) {
		this(limit, nodeRange, Optional.of(rule), sampler);
	}

	public Schedule(int limit) {
		this(limit, new NodeRange(), Optional.empty(), (type, ruleName, nodeName) -> INodeSampler.EMPTY);
	}

	public static Schedule unlimited() {
		return new Schedule(Schedule.UNLIMITED);
	}

	public static Schedule once() {
		return new Schedule(1);
	}

	public int getLimit() {
		return limit;
	}

	public Map<String, Object> getParameters() {
		return parameters;
	}

	public String getParameterFor(String type, String nodeName) {
		return rule.map(r -> {
			var param = NODE_RANGE_PARAM + "_" + nodeName;
			parameters.put(param, nodeRange.sampleIDs(type, sampler.getSampleSizeFor(type, r.getName(), nodeName)));
			return param;
		}).orElseThrow();
	}

	public boolean hasRangeFor(String type, String nodeName) {
		return rule.map(r -> sampler.getSampleSizeFor(type, r.getName(), nodeName) > 0).orElse(false);
	}
}
