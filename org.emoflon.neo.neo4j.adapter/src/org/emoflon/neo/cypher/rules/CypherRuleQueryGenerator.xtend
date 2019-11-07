package org.emoflon.neo.cypher.rules

import org.emoflon.neo.cypher.common.NeoNode
import org.emoflon.neo.cypher.common.NeoRelation
import org.emoflon.neo.engine.api.patterns.IMask

import static org.emoflon.neo.cypher.patterns.CypherPatternQueryGenerator.matchAllElements
import static org.emoflon.neo.cypher.patterns.CypherPatternQueryGenerator.toParamValue
import static org.emoflon.neo.cypher.patterns.NeoMatch.getIdParameter
import static org.emoflon.neo.cypher.patterns.NeoMatch.getMatchParameter
import static org.emoflon.neo.cypher.patterns.NeoMatch.getMatchesParameter
import static org.emoflon.neo.emsl.util.EMSLUtil.returnValueAsString

class CypherRuleQueryGenerator {
	def static query(NeoRule rule, IMask mask) {
		'''
			// Execute rule query for: «rule.name» 
			UNWIND $«matchesParameter» AS «matchParameter»
			
			«matchAllElements(rule.getPrecondition(), true)»
			
			WHERE
				«bindElementsUsingMatch(rule)»
			
			«IF !rule.deletedElts.isEmpty»
				«IF rule.SPOSemantics»DETACH «ENDIF»DELETE
					«FOR element : rule.deletedElts SEPARATOR ","»
						(«element»)
					«ENDFOR»
			«ENDIF»
			
			«IF !rule.createdNodes.isEmpty || !rule.createdEdges.isEmpty»
				CREATE
					«FOR node : rule.createdNodes.values SEPARATOR "," AFTER rule.createdEdges.empty? "" : ","»
						«createNode(node)»
					«ENDFOR»
					«FOR rel : rule.createdEdges.values() SEPARATOR ","»
						«createRelation(rel)»
					«ENDFOR»
			«ENDIF»
			«IF !rule.attributeAssignments.isEmpty»
				SET
					«FOR maskedAssignment : mask.maskedAttributes.entrySet SEPARATOR "," AFTER rule.attributeAssignments.isEmpty? "":","»
						«maskedAssignment.key» = «toParamValue(returnValueAsString(maskedAssignment.value), true)»
					«ENDFOR»
					«FOR assg : rule.attributeAssignments SEPARATOR ","»
						«assg.element».«assg.name» = «toParamValue(assg.value, true)»
					«ENDFOR»
			«ENDIF»
			
			RETURN
				// Created elements
				«FOR element : rule.createdElts SEPARATOR "," AFTER ","»
					id(«element») AS «element»
				«ENDFOR»
				
				// Preserved elements
				«FOR element : rule.contextElts.reject[rule.deletedElts.contains(it)] SEPARATOR "," AFTER ","»
					id(«element») AS «element»
				«ENDFOR»
				
				«matchParameter».«idParameter» AS «idParameter»
		'''
	}

	private def static createRelation(NeoRelation relation) {
		'''
			(«relation.srcNode.name»)-[«relation.name»:«relation.type»]->(«relation.trgNode.name»)
		'''
	}

	private def static createNode(NeoNode node) {
		'''
			(«node.name» «FOR l : node.getLabels BEFORE ":" SEPARATOR ":"»«l»«ENDFOR»)
		'''
	}

	private def static bindElementsUsingMatch(NeoRule rule) {
		'''
			«IF rule.contextElts.isEmpty»
				TRUE
			«ELSE»
				«FOR element : rule.contextElts SEPARATOR " AND "»
					id(«element») = «matchParameter».«element»
				«ENDFOR»
			«ENDIF»
		'''
	}
}
