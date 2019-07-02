package org.emoflon.neo.neo4j.adapter

import java.util.Collection

class CypherPatternBuilder {

	def static String readQuery(Collection<NeoNode> nodes, boolean injective) {
		'''
		«matchQuery(nodes)»
		«withQuery(nodes,injective)»
		«returnQuery(nodes)»'''
	}

	def static String readQuery(Collection<NeoNode> nodes, Collection<NeoNode> nodes2, Collection<String> nodesMap,
		boolean injective) {
		'''
		«matchQuery(nodes,nodes2,nodesMap)»
		«withQuery(nodes,nodes2,nodesMap)»
		«returnQuery(nodes,nodes2,nodesMap)»'''
	}

	def static String isStillValidQuery(Collection<NeoNode> nodes, NeoMatch match, boolean injective) {
		'''
		«matchQueryForIsStillValid(nodes, match)»
		«withQueryForIsStillValid(nodes, match, injective)»
		«returnQueryForIsStillValid()»'''
	}

	def static String matchQuery(Collection<NeoNode> nodes) {
		'''MATCH «FOR n : nodes SEPARATOR ', '»
			«IF n.relations.size > 0 »
				«FOR r:n.relations SEPARATOR ', '»«sourceNode(n)»«directedRelation(r)»«targetNode(r)»«ENDFOR»
			«ELSE»«queryNode(n)»
			«ENDIF»
		«ENDFOR»'''
	}

	def static String matchQuery(Collection<NeoNode> nodes, Collection<NeoNode> nodes2, Collection<String> nodesMap) {
		'''«matchQuery(nodes)»
		OPTIONAL «matchQuery(nodes2)»
		'''
	}

	def static String withQuery(Collection<NeoNode> nodes) {
		'''WITH «FOR n : nodes SEPARATOR ', '»«n.varName»«ENDFOR»'''
	}

	def static String withQuery(Collection<NeoNode> nodes, Collection<NeoNode> nodes2, Collection<String> nodesMap) {
		'''WITH «FOR n : nodesMap SEPARATOR ', '»«n»«ENDFOR»
		WHERE «FOR n : nodesMap SEPARATOR 'OR '»«n» IS NULL «ENDFOR»'''
	}

	protected def static CharSequence queryNode(NeoNode n) '''
	(«n.varName»:«n.classType»«IF n.properties.size > 0»«FOR p:n.properties BEFORE ' {' SEPARATOR ',' AFTER '}'»«p.name»:«p.value»«ENDFOR»«ENDIF»)'''

	def static String matchQueryForIsStillValid(Collection<NeoNode> nodes, NeoMatch match) {
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

	def static String withQuery(Collection<NeoNode> nodes, boolean injective) {
		'''«IF injective && nodes.size > 1»«injectivityBlock(nodes)»«ENDIF»'''
	}

	def static String withQueryForIsStillValid(Collection<NeoNode> nodes, NeoMatch match, boolean injective) {
		'''
			«nodeIdBlock(nodes, match)»
		'''
	}

	def static String injectivityBlock(Collection<NeoNode> nodes) {
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
	
	def static String injectivityBlockCond(Collection<NeoNode> nodes) {
		var String ret = ''

		for (var i = 0; i < nodes.size; i++) {
			for (var j = i + 1; j < nodes.size; j++) {
				if (nodes.get(i).classType == nodes.get(j).classType) {
					ret += ''' AND (NOT id(«nodes.get(i).varName») = id(«nodes.get(j).varName») OR «nodes.get(i).varName» IS NULL OR «nodes.get(j).varName» IS NULL) '''
				}
			}
		}

		'''«ret»'''
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

	def static String returnQuery(Collection<NeoNode> nodes, Collection<NeoNode> nodes2, Collection<String> nodesMap) {
		'''RETURN «FOR n : nodesMap SEPARATOR ', '»id(«n») AS «n»«ENDFOR» LIMIT 1'''
	}

	def static String returnQueryForIsStillValid() {
		'''RETURN TRUE'''
	}

	def static String sourceNode(NeoNode n) {
		'''(«n.varName»:«n.classType»«properties(n.properties)»)'''
	}

	def static String targetNode(NeoRelation r) {
		'''(«r.toNodeVar»:«r.toNodeLabel»)'''
	}

	def static String directedRelation(NeoRelation r) {
		'''-[«r.varName»:«r.relType»«properties(r.properties)»]->'''
	}

	def static String properties(Collection<NeoProperty> props) {
		'''«IF props.size > 0»«FOR p:props BEFORE ' {' SEPARATOR ',' AFTER '}'»«p.name»:«p.value»«ENDFOR»«ENDIF»'''
	}

	def static String cypherNode(String varName, String classType, Collection<NeoProperty> properties) {
		'''(«varName»:«classType»«IF properties.size > 0» «cypherProperties(properties)»«ENDIF»)'''
	}

	def static String cypherCondition(String name, String op, Boolean opNeg, String value, String classVarName) {
		'''«IF opNeg»NOT «ENDIF»«classVarName».«name» «op» «value»'''
	}

	def static String cypherProperties(Collection<NeoProperty> properties) {
		'''«IF properties.size > 0»«FOR p:properties BEFORE'{' SEPARATOR', ' AFTER '}'»«p.toString»«ENDFOR»«ENDIF»'''
	}

	def static String cypherProperty(String name, String value) {
		'''«name»: «value»'''
	}
	
	
	/*
	 * Conditions
	 */
	 
	 def static String whereNegativeConstraintQuery(Collection<NeoNode> nodes) {
	 	'''«FOR n:nodes SEPARATOR ' OR '»«n.varName» IS NULL«FOR r:n.relations BEFORE ' OR ' SEPARATOR 'OR '»«r.varName»  IS NULL«ENDFOR»«ENDFOR»'''
	 }
	 def static String wherePositiveConstraintQuery(Collection<NeoNode> nodes) {
	 	'''«FOR n:nodes SEPARATOR ' AND '»«n.varName» IS NOT NULL«FOR r:n.relations BEFORE ' AND ' SEPARATOR ' AND '»«r.varName»  IS NOT NULL«ENDFOR»«ENDFOR»'''
	 }
	 
	 def static String withConstraintQuery(Collection<String> nodes) {
	 	'''WITH «FOR n:nodes SEPARATOR ', '»«n»«ENDFOR»'''
	 }
	 
	 def static String returnConstraintQuery(Collection<String> nodes) {
	 	'''RETURN «FOR n:nodes SEPARATOR ', '»id(«n») AS «n»«ENDFOR»'''
	 }
}
