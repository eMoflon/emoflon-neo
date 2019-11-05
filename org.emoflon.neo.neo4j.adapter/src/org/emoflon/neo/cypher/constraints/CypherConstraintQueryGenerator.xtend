package org.emoflon.neo.cypher.constraints

import static org.emoflon.neo.cypher.patterns.CypherPatternQueryGenerator.*

class CypherConstraintQueryGenerator {
	def static query(NeoConstraint constraint) {
		'''
			// Constraint: «constraint.name»
			
			// ------------- Subpatterns [begin]
			
			«FOR predicate : constraint.subPredicatePatterns SEPARATOR "\n"»				
				«matchPredicatePattern(predicate, constraint)»
			«ENDFOR»
			«FOR implication : constraint.subImplicationPatterns SEPARATOR "\n"»				
				«matchImplicationPattern(implication, constraint)»
			«ENDFOR»
			
			// ------------- Subpatterns [end]
			
			WHERE
				«constraint.logicalExprForWhere»
			
			RETURN DISTINCT
				TRUE
		'''
	}
}
