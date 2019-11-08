package org.emoflon.neo.engine.generator;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.emoflon.neo.engine.api.rules.IRule;

public class Schedule {

	public static final int UNLIMITED = -1;
	private static final String NODE_RANGE_PARAM = "nodeRange";
	private static final String REL_RANGE_PARAM = "relRange";
	private int limit;
	private Optional<IRule<?, ?>> rule;
	private ElementRange nodeRange;
	private ElementRange relRange;
	private INodeSampler nodeSampler;
	private IRelSampler relSampler;
	private Map<String, Object> parameters = new HashMap<>();

	public Schedule(int limit, ElementRange nodeRange, ElementRange relRange, Optional<IRule<?, ?>> rule, INodeSampler nodeSampler, IRelSampler relSampler) {
		this.limit = limit;
		this.nodeRange = nodeRange;
		this.relRange = relRange;
		this.rule = rule;
		this.nodeSampler = nodeSampler;
		this.relSampler = relSampler;
	}

	public Schedule(int limit, ElementRange nodeRange, ElementRange relRange, IRule<?, ?> rule, INodeSampler nodeSampler, IRelSampler relSampler) {
		this(limit, nodeRange, relRange, Optional.of(rule), nodeSampler, relSampler);
	}

	public Schedule(int limit) {
		this(limit, new ElementRange(), new ElementRange(), Optional.empty(), (type, ruleName, nodeName) -> INodeSampler.EMPTY, (type, ruleName) -> IRelSampler.EMPTY);
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

	public boolean isNodeRangeOred() {
		return false;
	}
	
	public boolean isRelRangeOred() {
		return true;
	}
	
	public Map<String, Object> getParameters() {
		return parameters;
	}

	public String getParameterForNode(String type, String nodeName) {
		return rule.map(r -> {
			var param = NODE_RANGE_PARAM + "_" + nodeName;
			parameters.put(param, nodeRange.sampleIDs(type, nodeSampler.getSampleSizeFor(type, r.getName(), nodeName)));
			return param;
		}).orElseThrow();
	}

	public boolean hasRangeForRel(String type) {
		return rule.map(r -> relSampler.getSampleSizeFor(type, r.getName()) > 0).orElse(false);
	}
	
	public String getParameterForRel(String type) {
		return rule.map(r -> {
			var param = REL_RANGE_PARAM + "_" + type;
			parameters.put(param, relRange.sampleIDs(type, relSampler.getSampleSizeFor(type, r.getName())));
			return param;
		}).orElseThrow();
	}

	public boolean hasRangeForNode(String type, String nodeName) {
		return rule.map(r -> nodeSampler.getSampleSizeFor(type, r.getName(), nodeName) > 0).orElse(false);
	}
		
}
