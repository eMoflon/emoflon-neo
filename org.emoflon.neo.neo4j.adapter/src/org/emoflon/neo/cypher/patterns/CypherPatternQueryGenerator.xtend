package org.emoflon.neo.cypher.patterns

import org.emoflon.neo.cypher.common.NeoNode
import org.emoflon.neo.cypher.common.NeoProperty
import org.emoflon.neo.cypher.common.NeoRelation
import org.emoflon.neo.engine.api.patterns.IMask
import org.emoflon.neo.engine.generator.Schedule

import static org.emoflon.neo.cypher.patterns.NeoMatch.getIdParameter
import static org.emoflon.neo.cypher.patterns.NeoMatch.getMatchParameter
import static org.emoflon.neo.cypher.patterns.NeoMatch.getMatchesParameter
import static org.emoflon.neo.emsl.util.EMSLUtil.returnValueAsString

class CypherPatternQueryGenerator {
	def static query(NeoPattern pattern, Schedule schedule, IMask mask) {
		'''
			// Match query for: «pattern.name»
			«matchMainPattern(pattern, schedule, mask)»
			«matchSubPatterns(pattern)»
			
			WHERE
				«pattern.logicalExprForWhere»
			
			RETURN DISTINCT
				// All nodes
				«FOR node : pattern.nodes SEPARATOR ", "»
					id(«node.name») AS «node.name»
				«ENDFOR»
				
				// All relations (excluding paths)
				«FOR rel : pattern.relations.filter[!it.isPath] BEFORE "," SEPARATOR ", "»
					id(«rel.name») AS «rel.name»
				«ENDFOR»
			
			«IF schedule.limit > Schedule.UNLIMITED»
				LIMIT «schedule.limit»
			«ENDIF»
		'''
	}

	def static dataQuery(NeoPattern pattern) {
		'''
			// Data query for: «pattern.name»
			UNWIND $«matchesParameter» AS «matchParameter»
			
			«matchAllElements(pattern, false)»
			
			WHERE
				«FOR element : pattern.elements SEPARATOR " AND "»
					id(«element») = «matchParameter».«element»
				«ENDFOR»
			
			RETURN
				// All nodes
				«FOR node : pattern.nodes SEPARATOR ", "»
					«node.name» AS «node.name»
				«ENDFOR»
			
				// All relations (excluding paths)
				«FOR rel : pattern.relations.filter[!it.isPath] BEFORE "," SEPARATOR ", "»
					«rel.name» AS «rel.name»
				«ENDFOR»
		'''
	}

	private def static CharSequence matchMainPattern(NeoPattern pattern, Schedule schedule, IMask mask) {
		matchMainPattern(pattern, schedule, mask, false);
	}

	private def static CharSequence matchMainPattern(NeoPattern pattern, Schedule schedule, IMask mask,
		boolean passOnMatchParameter) {
		'''
			«matchAllElements(pattern, false)»
			
			WHERE
				// Injectivity
				«injectivityCheck(pattern.getInjectiveChecks())»
				
				// Attribute Conditions
				«FOR inequalityCheck : pattern.inequalityChecks BEFORE " AND " SEPARATOR " AND "»
					«inequalityCheck.element».«inequalityCheck.name» «inequalityCheck.operator» «inequalityCheck.value»
				«ENDFOR»
				
				// Masking
				«maskedAttributeEqualityChecks(pattern, mask)»
				«maskedNodesBlock(pattern.nodes, mask)»
				
				// Node Sampling
				«FOR node : pattern.nodes.filter[schedule.hasRangeFor(it.type, it.name)] BEFORE " AND " SEPARATOR " AND "»
					id(«node.name») in $«schedule.getParameterFor(node.type, node.name)»
				«ENDFOR»
			
			WITH
				«FOR name : pattern.elements SEPARATOR ", "»
					«name»
				«ENDFOR»
				«IF passOnMatchParameter»
					,«matchParameter»
				«ENDIF»
		'''
	}

	private def static maskedAttributeEqualityChecks(NeoPattern pattern, IMask mask) {
		var relevantEntries = mask.maskedAttributes.filter [ nodeAttr, value |
			pattern.elements.exists[it == nodeAttr.split("\\.").get(0)]
		]

		'''
			«FOR attrCheck : relevantEntries.entrySet BEFORE " AND " SEPARATOR " AND "»
				«attrCheck.key» = «returnValueAsString(attrCheck.value)»
			«ENDFOR»
		'''
	}

	private def static matchSubPatterns(NeoPattern pattern) {
		matchSubPatterns(pattern, false)
	}

	private def static matchSubPatterns(NeoPattern pattern, boolean passOnMatchParameter) {
		'''		
				
			«FOR predicate : pattern.subPredicatePatterns SEPARATOR "\n"»				
				«matchPredicatePattern(predicate, pattern, passOnMatchParameter)»
			«ENDFOR»
			«FOR implication : pattern.subImplicationPatterns SEPARATOR "\n"»				
				«matchImplicationPattern(implication, pattern, passOnMatchParameter)»
			«ENDFOR»
				
		'''
	}

	def static isStillValidQuery(NeoPattern pattern) {
		'''
			// Is-still-valid query for: «pattern.name»
			UNWIND $«matchesParameter» AS «matchParameter»
			
			«matchMainPattern(pattern, Schedule.unlimited, IMask.empty, true)»
			«matchSubPatterns(pattern, true)»
			
			WHERE
				«FOR element : pattern.elements SEPARATOR " AND " AFTER " AND "»
					id(«element») = «matchParameter».«element»
				«ENDFOR»
				«pattern.logicalExprForWhere»
			
			RETURN
				«matchParameter».«idParameter» AS «idParameter»
		'''
	}

	def static matchAllElements(NeoPattern pattern, boolean withMatchParams) {
		'''
			«IF !pattern.elements.isEmpty»
				MATCH
					// Match all nodes
					«FOR node : pattern.nodes SEPARATOR ", "»
						«matchNode(node, withMatchParams)»
					«ENDFOR»
				
					// Match all relations (including paths)
					«FOR rel : pattern.relations BEFORE "," SEPARATOR ", "»
						«matchRelation(rel, withMatchParams)»
					«ENDFOR»
			«ENDIF»
		'''
	}

	private def static injectivityCheck(Iterable<Pair<NeoNode, NeoNode>> injectiveChecks) {
		'''
			«IF injectiveChecks.empty»
				TRUE
			«ELSE»
				«FOR check : injectiveChecks SEPARATOR " AND "»
					NOT id(«check.key.name») = id(«check.value.name»)
				«ENDFOR»
			«ENDIF»
		'''
	}

	def static matchPredicatePattern(NeoPredicatePattern subPattern, NeoPattern pattern) {
		matchPredicatePattern(subPattern, pattern, false)
	}

	private def static matchPredicatePattern(NeoPredicatePattern subPattern, NeoPattern pattern,
		boolean passOnMatchParameter) {
		'''
			// Subpattern «subPattern.getIndex»: «subPattern.name»
			«optionalMatchOfBasicPattern(subPattern)»
			WHERE 
				«FOR match : subPattern.getBoundNodes.entrySet SEPARATOR " AND " AFTER " AND "»
					«match.key.name» = «match.value.name»
				«ENDFOR»
				«injectivityCheck(subPattern.injectiveChecks)»
				«FOR inequalityCheck : subPattern.inequalityChecks BEFORE " AND " SEPARATOR " AND "»
					«inequalityCheck.element».«inequalityCheck.name» «inequalityCheck.operator» «inequalityCheck.value»
				«ENDFOR»
			
			WITH
				«FOR name : pattern.elements SEPARATOR ", " AFTER ", "»
					«name»
				«ENDFOR»
				«FOR precedingSubPatterns : pattern.allSubPatterns.take(subPattern.getIndex) SEPARATOR ", " AFTER ", "»
					«precedingSubPatterns.logicVariable»
				«ENDFOR»
				count(«subPattern.nodes.get(0).name») «IF subPattern.positive»>«ELSE»=«ENDIF» 0 AS «subPattern.logicVariable»
				«IF passOnMatchParameter»
					,«matchParameter»
				«ENDIF»
		'''
	}

	private def static optionalMatchOfBasicPattern(NeoBasicPattern subPattern) {
		'''
			OPTIONAL MATCH 
				// Match all nodes
				«FOR node : subPattern.nodes SEPARATOR ", "»
					«matchNode(node, false)»
				«ENDFOR»
				
				// Match all relations
				«FOR rel : subPattern.relations BEFORE "," SEPARATOR ", "»
					«matchRelation(rel, false)»
				«ENDFOR»
		'''
	}

	def static matchImplicationPattern(NeoImplicationPattern subPattern, NeoPattern pattern) {
		matchImplicationPattern(subPattern, pattern, false)
	}

	private def static matchImplicationPattern(NeoImplicationPattern subPattern, NeoPattern pattern,
		boolean passOnMatchParameter) {
		'''
			// Subpattern «subPattern.index»: «subPattern.name»
			
			// Premise:
			«optionalMatchOfBasicPattern(subPattern.premise)»
			
			WHERE 
				«FOR match : subPattern.boundNodesInPremise.entrySet SEPARATOR " AND " AFTER " AND "»
					id(«match.key.name») = id(«match.value.name»)
				«ENDFOR»
				«injectivityCheck(subPattern.premise.injectiveChecks)»
				«FOR inequalityCheck : subPattern.premise.inequalityChecks BEFORE " AND " SEPARATOR " AND "»
					«inequalityCheck.element».«inequalityCheck.name» «inequalityCheck.operator» «inequalityCheck.value»
				«ENDFOR»
			
			WITH
				«FOR name : pattern.elements SEPARATOR ", " AFTER ", "»
					«name»
				«ENDFOR»
				«FOR name : subPattern.premise.elements SEPARATOR ", " AFTER ", "»
					«name»
				«ENDFOR»
				«FOR precedingSubPatterns : pattern.allSubPatterns.take(subPattern.index) SEPARATOR ", " AFTER ", "»
					«precedingSubPatterns.logicVariable»
				«ENDFOR»
				count(«subPattern.premise.nodes.get(0).name») = 0 AS «subPattern.logicVariable»_if
				«IF passOnMatchParameter»
					,«matchParameter»
				«ENDIF»
			
			// Conclusion:
			«optionalMatchOfBasicPattern(subPattern.conclusion)»
			
			WHERE 
				«FOR match : subPattern.boundNodesInConclusion.entrySet SEPARATOR " AND " AFTER " AND "»
					«match.key.name» = «match.value.name»
				«ENDFOR»
				«injectivityCheck(subPattern.conclusion.injectiveChecks)» AND
				«subPattern.conclusion.nodes.get(0).name» IS NOT NULL
				«FOR inequalityCheck : subPattern.conclusion.inequalityChecks BEFORE " AND " SEPARATOR " AND "»
					«inequalityCheck.element».«inequalityCheck.name» «inequalityCheck.operator» «inequalityCheck.value»
				«ENDFOR»
			
			WITH
				«FOR name : pattern.elements SEPARATOR ", " AFTER ", "»
					«name»
				«ENDFOR»
				«FOR precedingSubPatterns : pattern.allSubPatterns.take(subPattern.index) SEPARATOR ", " AFTER ", "»
					«precedingSubPatterns.logicVariable»
				«ENDFOR»
				«FOR name : subPattern.premise.elements SEPARATOR ", " AFTER ", "»
					«name»
				«ENDFOR»
				«FOR name : subPattern.conclusion.elements SEPARATOR ", " AFTER ", "»
					«name»
				«ENDFOR»
				«subPattern.logicVariable»_if OR count(«subPattern.conclusion.nodes.get(0).name») > 0 AS «subPattern.logicVariable»_then
				«IF passOnMatchParameter»
					,«matchParameter»
				«ENDIF»
				
			WITH
				reduce(result = TRUE, val IN collect(«subPattern.logicVariable»_then) | result AND val) AS «subPattern.logicVariable»
		'''
	}

	private def static matchNode(NeoNode node, boolean withMatchParams) {
		'''
			(«node.name»:«node.type»«matchProperties(node.getEqualityChecks, withMatchParams)»)
		'''
	}

	private def static matchProperties(Iterable<NeoProperty> properties, boolean withMatchParams) {
		'''«FOR prop : properties BEFORE " {" SEPARATOR ", " AFTER "}"»«prop.name»:«toParamValue(prop.value, withMatchParams)»«ENDFOR»'''
	}

	def static toParamValue(String propValue, boolean withMatchParams) {
		if (propValue.startsWith("$") && withMatchParams) {
			return propValue.replace("$", NeoMatch.matchParameter + ".")
		} else
			return propValue
	}

	private def static matchRelation(NeoRelation relation, boolean withMatchParams) {
		'''
			(«relation.srcNode.name»)-[«relation.name»:«relation.type»«matchProperties(relation.getEqualityChecks, withMatchParams)»]->(«relation.trgNode.name»)
		'''
	}

	private def static String maskedNodesBlock(Iterable<NeoNode> nodes, IMask mask) {
		var relevantEntries = mask.maskedNodes.filter [ node, id |
			nodes.map[it.name].exists[it == node]
		]

		'''
			«FOR entry : relevantEntries.entrySet BEFORE " AND " SEPARATOR " AND "»
				id(«entry.key») = «entry.value»
			«ENDFOR»
		'''
	}
}
