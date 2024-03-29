package org.emoflon.neo.emsl.util;

public enum FlattenerErrorType {
	INFINITE_LOOP,
	NO_COMMON_SUBTYPE_OF_NODES,
	NO_COMMON_SUBTYPE_OF_PROPERTIES,
	REFINE_ENTITY_WITH_CONDITION,
	PROPS_WITH_DIFFERENT_VALUES,
	PROPS_WITH_DIFFERENT_OPERATORS,
	NON_RESOLVABLE_PROXY,
	NON_COMPLIANT_SUPER_ENTITY,
	PATH_LENGTHS_NONSENSE,
	NO_INTERSECTION_IN_MODEL_RELATION_STATEMENT_TYPE_LIST, 
	CONFLICTING_REFERENCES,
	ONLY_RED_AND_GREEN_NODES,
	ONLY_RED_AND_GREEN_EDGES,
	NON_RESOLVABLE_CORR_PROXY_SOURCE,
	NON_RESOLVABLE_CORR_PROXY_TARGET
}
