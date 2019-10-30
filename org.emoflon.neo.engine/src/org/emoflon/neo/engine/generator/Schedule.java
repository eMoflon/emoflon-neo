package org.emoflon.neo.engine.generator;

import java.util.Map;

public class Schedule {

	private int limit;
	private NodeRange nodeRange;

	public Schedule(int limit, NodeRange nodeRange) {
		this.limit = limit;
		this.nodeRange = nodeRange;
	}

	public Schedule(int limit) {
		this(limit, new NodeRange());
	}

	public static Schedule unlimited() {
		return new Schedule(-1);
	}

	public static Schedule once() {
		return new Schedule(1);
	}

	public Map<String, Object> getParameters() {
		return nodeRange.getParameters();
	}

	public boolean hasRestrictiveRange() {
		return !nodeRange.getParameters().isEmpty();
	}

	public int getLimit() {
		return limit;
	}

	public boolean hasRangeFor(String type) {
		return nodeRange.hasRangeFor(type);
	}

	public String getParameterFor(String type) {
		return nodeRange.getParameterFor(type);
	}
}
