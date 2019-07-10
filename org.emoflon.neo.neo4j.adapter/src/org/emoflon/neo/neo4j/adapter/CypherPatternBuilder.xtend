package org.emoflon.neo.neo4j.adapter

import java.util.Collection

class CypherPatternBuilder {
	
	/*****************************
	 * Basic Cypher Strings
	 ****************************/
	 
	def static String sourceNode(NeoNode n) {
		'''(«n.varName»:«n.classType»«properties(n.properties)»)'''
	}

	def static String directedRelation(NeoRelation r) {
		'''-[«r.varName»:«r.relType»«properties(r.properties)»]->'''
	}
	
	def static String targetNode(NeoRelation r) {
		'''(«r.toNodeVar»:«r.toNodeLabel»)'''
	}

	def static String properties(Collection<NeoProperty> props) {
		'''«IF props.size > 0»«FOR p:props BEFORE ' {' SEPARATOR ',' AFTER '}'»«p.name»:«p.value»«ENDFOR»«ENDIF»'''
	}
	
	protected def static CharSequence queryNode(NeoNode n) '''
	(«n.varName»:«n.classType»«IF n.properties.size > 0»«FOR p:n.properties BEFORE ' {' SEPARATOR ',' AFTER '}'»«p.name»:«p.value»«ENDFOR»«ENDIF»)'''
	 

	/*****************************
	 * Standard Matching Functions
	 ****************************/
	 
	def static String readQuery(Collection<NeoNode> nodes, boolean injective) {
		'''
		«matchQuery(nodes)»
		«whereQuery(nodes,injective)»
		«returnQuery(nodes)»'''
	}
	def static String readQuery(Collection<NeoNode> nodes, boolean injective, int limit) {
		'''
		«readQuery(nodes,injective)» LIMIT «limit»'''
	}
	
	def static String getDataQuery(Collection<NeoNode> nodes, NeoMatch match, boolean injective){
		'''
		«matchQueryForData(nodes, match)»
		«withQueryForIsStillValid(nodes, match, injective)»
		«returnDataQuery(nodes)»'''
	}

	def static String matchQuery(Collection<NeoNode> nodes) {
		'''MATCH «FOR n : nodes SEPARATOR ', '»
			«IF n.relations.size > 0 »
				«FOR r:n.relations SEPARATOR ', '»«sourceNode(n)»«directedRelation(r)»«targetNode(r)»«ENDFOR»
			«ELSE»«queryNode(n)»
			«ENDIF»
		«ENDFOR»'''
	}

	def static String whereQuery(Collection<NeoNode> nodes, boolean injective) {
		'''«IF injective && nodes.size > 1»«injectiveBlock(nodes)»«ENDIF»'''
	}	

	def static String matchQueryForIsStillValid(Collection<NeoNode> nodes, NeoMatch match) {
		'''
		MATCH «FOR n : nodes SEPARATOR ', '»
				«IF n.relations.size == 0 || n.properties.size > 0»
					«queryNode(n)»«IF n.relations.size > 0», «ENDIF»
				«ENDIF»
				«FOR r : n.relations SEPARATOR ', '»
					(:«n.classType»)-[«r.varName»]->(:«r.toNodeLabel»)
				«ENDFOR»
			«ENDFOR»'''
	}
	
	def static String matchQueryForData(Collection<NeoNode> nodes, NeoMatch match) {
		'''
		MATCH «FOR n : nodes SEPARATOR ', '»
					«queryNode(n)»«IF n.relations.size > 0», «ENDIF»
				«FOR r : n.relations SEPARATOR ', '»
					(«n.varName»:«n.classType»)-[«r.varName»]->(«r.toNodeVar»:«r.toNodeLabel»)
				«ENDFOR»
			«ENDFOR»'''
	}

	def static String withQueryForIsStillValid(Collection<NeoNode> nodes, NeoMatch match, boolean injective) {
		'''
			«nodeIdBlock(nodes, match)»
		'''
	}

	def static String injectiveBlock(Collection<NeoNode> nodes) {
		var String ret = ''
		var boolean first = true

		for (var i = 0; i < nodes.size; i++) {
			for (var j = i + 1; j < nodes.size; j++) {
				if (nodes.get(i).classType == nodes.get(j).classType) {
					if (first)
						first = false
					else
						ret += "\nAND"

					ret += ''' NOT id(«nodes.get(i).varName») = id(«nodes.get(j).varName»)'''
				}
			}
		}

		'''«IF ret.length > 0»WHERE «ENDIF»«ret»'''
	}
	
	def static String returnQuery(Collection<NeoNode> nodes) {
		'''
		RETURN «FOR n : nodes SEPARATOR ',\n '»
				«IF n.relations.size == 0 || n.properties.size > 0»
					id(«n.varName») AS «n.varName»«IF n.relations.size > 0»,«ENDIF»
				«ENDIF»
				«FOR r : n.relations SEPARATOR ',\n  '»
					id(«r.varName») AS «r.varName»
				«ENDFOR»
			«ENDFOR»'''
	}
	
	def static String returnQuery(Collection<NeoNode> nodes, int limit) {
		'''«returnQuery(nodes)» LIMIT «limit»'''
	}
	
	
	/*****************************
	 * IsStillValid Functions
	 ****************************/
	
	def static String isStillValidQuery(Collection<NeoNode> nodes, NeoMatch match, boolean injective) {
		'''
		«isStillValid_matchQuery(nodes, match)»
		«isStillValid_whereQuery(nodes, match, injective)»
		«isStillValid_returnQuery()»'''
	}
	
	def static String returnDataQuery(Collection<NeoNode> nodes) {
		'''
		RETURN «FOR n : nodes SEPARATOR ',\n '»
					«n.varName» AS «n.varName»«IF n.relations.size > 0»,«ENDIF»
				«FOR r : n.relations SEPARATOR ',\n  '»
					«r.varName» AS «r.varName»
				«ENDFOR»
			«ENDFOR»'''
	}

	def static String returnQuery(Collection<NeoNode> nodes, Collection<NeoNode> nodes2, Collection<String> nodesMap) {
		'''RETURN «FOR n : nodesMap SEPARATOR ', '»id(«n») AS «n»«ENDFOR» LIMIT 1'''
	}
	
	def static String isStillValid_matchQuery(Collection<NeoNode> nodes, NeoMatch match) {
		'''
		MATCH «FOR n : nodes SEPARATOR ', '»
				«IF n.relations.size == 0 || n.properties.size > 0»
					«queryNode(n)»«IF n.relations.size > 0»", "«ENDIF»
				«ENDIF»
				«FOR r : n.relations SEPARATOR ', '»
					(:«n.classType»)-[«r.varName»]->(:«r.toNodeLabel»)
				«ENDFOR»
			«ENDFOR»'''
	}
	
	def static String isStillValid_whereQuery(Collection<NeoNode> nodes, NeoMatch match, boolean injective) {
		'''
			«nodeIdBlock(nodes, match)»
		'''
	}
	
	def static String nodeIdBlock(Collection<NeoNode> nodes, NeoMatch match) {
		'''
		WHERE «FOR n : nodes SEPARATOR " AND "»
				«IF n.relations.size == 0 || n.properties.size > 0»
					id(«n.varName») = «match.getIdForNode(n)»
					«IF n.relations.size > 0» AND«ENDIF»
				«ENDIF»
				«FOR r : n.relations SEPARATOR " AND "»
					id(«r.varName») = «match.getIdForRelation(r)»
				«ENDFOR»
			«ENDFOR»'''
	}
	
	
	def static String isStillValid_returnQuery() {
		'''RETURN TRUE'''
	}
	
	
	/*****************************
	 * Basic Constraint Functions
	 ****************************/
	
	def static String withQuery(Collection<NeoNode> nodes) {
		'''WITH «FOR n : nodes SEPARATOR ', '»«n.varName»«IF n.relations.size > 0»«FOR r: n.relations BEFORE ', ' SEPARATOR ', '»«r.varName»«ENDFOR»«ENDIF»«ENDFOR»'''
	}
	
	def static String constraint_ifThen_readQuery(Collection<NeoNode> nodes, Collection<NeoNode> nodes2, Collection<String> nodesMap,
		boolean injective) {
		'''
		«constraint_ifThen_matchQuery(nodes,nodes2,injective)»
		«constraint_withQuery(nodesMap)»
		WHERE «whereNegativeConditionQuery(nodes2)» // <<<<<<<<<<<<<<<<<<
		«constraint_returnQuery(nodesMap)»'''
	}
	
	def static String constraint_ifThen_matchQuery(Collection<NeoNode> nodes, Collection<NeoNode> nodes2, boolean injective) {
		'''«matchQuery(nodes)»
		«whereQuery(nodes,injective)»
		«withQuery(nodes)»
		OPTIONAL «matchQuery(nodes2)»
		«whereQuery(nodes2,injective)»
		'''
	}
	
	def static String constraint_withQuery(Collection<String> nodes) {
	 	''' WITH «FOR n:nodes SEPARATOR ', '»«n»«ENDFOR»'''
	}
	def static String constraint_returnQuery(Collection<String> nodes) {
		''' RETURN «FOR n : nodes SEPARATOR ', '»id(«n») AS «n»«ENDFOR» LIMIT 1'''
	}


	/*****************************
	 * Basic Condition Functions
	 ****************************/
	 
	 def static String wherePositiveConstraintQuery(int id) {
	 	'''m_«id» > 0'''
	 }
	 def static String whereNegativeConstraintQuery(int id) {
	 	'''m_«id» = 0'''
	 }
	 def static String whereImplicationConstraintQuery(int id) {
	 	'''(m_«id-1» = m_«id»)'''
	 }
	 
	 def static String wherePositiveConditionQuery(Collection<NeoNode> nodes) {
	 	'''«FOR n:nodes SEPARATOR ' AND '»«n.varName» IS NOT NULL«FOR r:n.relations BEFORE ' AND ' SEPARATOR ' AND '»«r.varName»  IS NOT NULL«ENDFOR»«ENDFOR»'''
	 }
	 def static String whereNegativeConditionQuery(Collection<NeoNode> nodes) {
	 	'''«FOR n:nodes SEPARATOR ' OR '»«n.varName» IS NULL«FOR r:n.relations BEFORE ' OR ' SEPARATOR ' OR '»«r.varName»  IS NULL«ENDFOR»«ENDFOR»'''
	 }
	 
	 def static String withCountQuery(Collection<NeoNode> nodes, int id) {
	 	
	 	var String ret = ''

		for (var i = 0; i < id; i++) {
			ret += "m_" + i + ", "
		}

		'''WITH «ret» count(«nodes.get(0).varName») as m_«id»'''
	 }

}
