package org.emoflon.neo.emsl.compiler.attributeConstraints.sorting.solver.democles.common;

import java.util.List;

public interface SearchPlanAlgorithm<C extends Combiner<C, O>, O extends OperationRuntime> {
	public C generateSearchPlan(C combiner, List<O> operations, Adornment inputAdornment);
}
