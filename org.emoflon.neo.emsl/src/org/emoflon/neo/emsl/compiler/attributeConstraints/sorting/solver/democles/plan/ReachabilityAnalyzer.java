package org.emoflon.neo.emsl.compiler.attributeConstraints.sorting.solver.democles.plan;

import org.emoflon.neo.emsl.compiler.attributeConstraints.sorting.solver.democles.common.Adornment;

public interface ReachabilityAnalyzer {
	public boolean isReachable(Adornment adornment);
}
