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

//FIXME: Handle schedule.ranges
class CypherPatternQueryGenerator {
	def static query(NeoPattern pattern, Schedule schedule, IMask mask) {
		'''
			// Match query for: «pattern.name»
			«matchMainPattern(pattern, mask)»
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
			
			«matchAllElements(pattern)»
			
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
			
			LIMIT 1
		'''
	}

	def static CharSequence matchMainPattern(NeoPattern pattern, IMask mask) {
		matchMainPattern(pattern, mask, false);
	}

	def static CharSequence matchMainPattern(NeoPattern pattern, IMask mask, boolean passOnMatchParameter) {
		'''
			«matchAllElements(pattern)»
			
			WHERE
				«injectivityCheck(pattern.getInjectiveChecks())»
				«FOR inequalityCheck : pattern.inequalityChecks BEFORE " AND " SEPARATOR " AND "»
					«inequalityCheck.element».«inequalityCheck.name» «inequalityCheck.operator» «inequalityCheck.value»
				«ENDFOR»
				
				«maskedAttributeEqualityChecks(pattern, mask)»
				
				«maskedNodesBlock(pattern.nodes, mask)»
			
			WITH
				«FOR name : pattern.elements SEPARATOR ", "»
					«name»
				«ENDFOR»
				«IF passOnMatchParameter»
					,«matchParameter»
				«ENDIF»
		'''
	}
	
	def static maskedAttributeEqualityChecks(NeoPattern pattern, IMask mask){
		var relevantEntries = mask.maskedAttributes.filter [ nodeAttr, value |
			pattern.elements.exists[it == nodeAttr.split("\\.").get(0)]
		]
		
		'''
			«FOR attrCheck : relevantEntries.entrySet BEFORE " AND " SEPARATOR " AND "»
				«attrCheck.key» = «returnValueAsString(attrCheck.value)»
			«ENDFOR»
		'''
	}

	def static matchSubPatterns(NeoPattern pattern) {
		matchSubPatterns(pattern, false)
	}

	def static matchSubPatterns(NeoPattern pattern, boolean passOnMatchParameter) {
		'''
			// ------------- Subpatterns [begin]
			
			«FOR predicate : pattern.subPredicatePatterns SEPARATOR "\n"»				
				«matchPredicatePattern(predicate, pattern, passOnMatchParameter)»
			«ENDFOR»
			«FOR implication : pattern.subImplicationPatterns SEPARATOR "\n"»				
				«matchImplicationPattern(implication, pattern, passOnMatchParameter)»
			«ENDFOR»
			
			// ------------- Subpatterns [end]
				
		'''
	}

	def static isStillValidQuery(NeoPattern pattern) {
		'''
			// Is-still-valid query for: «pattern.name»
			UNWIND $«matchesParameter» AS «matchParameter»
			
			«matchMainPattern(pattern, IMask.empty, true)»
			«matchSubPatterns(pattern, true)»
			
			WHERE
				«FOR element : pattern.elements SEPARATOR " AND " AFTER " AND "»
					id(«element») = «matchParameter».«element»
				«ENDFOR»
				«pattern.logicalExprForWhere»
			
			RETURN
				«matchParameter».«idParameter» AS «idParameter»
			
			LIMIT 1
		'''
	}

	def static matchAllElements(NeoPattern pattern) {
		'''
			«IF !pattern.elements.isEmpty»
				MATCH
					// Match all nodes
					«FOR node : pattern.nodes SEPARATOR ", "»
						«matchNode(node)»
					«ENDFOR»
				
					// Match all relations (including paths)
					«FOR rel : pattern.relations BEFORE "," SEPARATOR ", "»
						«matchRelation(rel)»
					«ENDFOR»
			«ENDIF»
		'''
	}

	def static injectivityCheck(Iterable<Pair<NeoNode, NeoNode>> injectiveChecks) {
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

	def static matchPredicatePattern(NeoPredicatePattern subPattern, NeoPattern pattern, boolean passOnMatchParameter) {
		'''
			// Subpattern «subPattern.getIndex»: «subPattern.name»
			«optionalMatchOfBasicPattern(subPattern)»
			WHERE 
				«FOR match : subPattern.getBoundNodes.entrySet SEPARATOR " AND " AFTER " AND "»
					«match.key.name» = «match.value.name»
				«ENDFOR»
				«injectivityCheck(subPattern.injectiveChecks)»
			
			WITH
				«FOR name : pattern.elements SEPARATOR ", " AFTER ", "»
					«name»
				«ENDFOR»
				«FOR precedingSubPatterns : pattern.allSubPatterns.take(subPattern.getIndex) SEPARATOR ", " AFTER ", "»
					«precedingSubPatterns.logicVariable»
				«ENDFOR»
				count («subPattern.nodes.get(0).name») «IF subPattern.positive»>«ELSE»=«ENDIF» 0 AS «subPattern.logicVariable»
				«IF passOnMatchParameter»
					,«matchParameter»
				«ENDIF»
		'''
	}

	def static optionalMatchOfBasicPattern(NeoBasicPattern subPattern) {
		'''
			OPTIONAL MATCH 
				// Match all nodes
				«FOR node : subPattern.nodes SEPARATOR ", "»
					«matchNode(node)»
				«ENDFOR»
				
				// Match all relations
				«FOR rel : subPattern.relations BEFORE "," SEPARATOR ", "»
					«matchRelation(rel)»
				«ENDFOR»
		'''
	}

	def static matchImplicationPattern(NeoImplicationPattern subPattern, NeoPattern pattern) {
		matchImplicationPattern(subPattern, pattern, false)
	}

	def static matchImplicationPattern(NeoImplicationPattern subPattern, NeoPattern pattern,
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
				count(«subPattern.premise.nodes.get(0).name») > 0 AS «subPattern.logicVariable»_if
				«IF passOnMatchParameter»
					,«matchParameter»
				«ENDIF»
			
			// Conclusion:
			«optionalMatchOfBasicPattern(subPattern.conclusion)»
			
			WHERE 
				«FOR match : subPattern.boundNodesInConclusion.entrySet SEPARATOR " AND " AFTER " AND "»
					«match.key.name» = «match.value.name»
				«ENDFOR»
				«injectivityCheck(subPattern.conclusion.injectiveChecks)»
			
			WITH
				«FOR name : pattern.elements SEPARATOR ", " AFTER ", "»
					«name»
				«ENDFOR»
				«FOR precedingSubPatterns : pattern.allSubPatterns.take(subPattern.index) SEPARATOR ", " AFTER ", "»
					«precedingSubPatterns.logicVariable»
				«ENDFOR»
				«subPattern.logicVariable»_if AND count(«subPattern.conclusion.nodes.get(0).name») = 0 AS «subPattern.logicVariable»
				«IF passOnMatchParameter»
					,«matchParameter»
				«ENDIF»
		'''
	}

	private def static matchNode(NeoNode node) {
		'''
			(«node.name»:«node.type»«matchProperties(node.getEqualityChecks)»)
		'''
	}

	private def static matchProperties(Iterable<NeoProperty> properties) {
		'''«FOR prop : properties BEFORE " {" SEPARATOR ", " AFTER "}"»«prop.name»:«prop.value»«ENDFOR»'''
	}

	private def static matchRelation(NeoRelation relation) {
		'''
			(«relation.srcNode.name»)-[«relation.name»:«relation.type»«matchProperties(relation.getEqualityChecks)»]->(«relation.trgNode.name»)
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
