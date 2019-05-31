package org.emoflon.neo.neo4j.adapter

import java.util.Collection

class CypherPatternBuilder {
	
	def static String readQuery(Collection<NeoNode> nodes, boolean injective) {
		'''«matchQuery(nodes)»
		«withQuery(nodes,injective)»
		«returnQuery(nodes)»'''
	}
	
	def static String matchQuery(Collection<NeoNode> nodes) {
		'''MATCH «FOR n:nodes SEPARATOR ', '»
					«IF n.relations.size > 0 »
						«FOR r:n.relations SEPARATOR ', '»«sourceNode(n)»«directedRelation(r)»«targetNode(r)»«ENDFOR»
					«ELSE»
						(«n.varName»:«n.classType»«IF n.properties.size > 0»«FOR p:n.properties BEFORE ' {' SEPARATOR ',' AFTER '}'»«p.name»:«p.value»«ENDFOR»«ENDIF»)
					«ENDIF»
				«ENDFOR»'''	
	}
	def static String withQuery(Collection<NeoNode> nodes, boolean injective) {
		'''«IF injective && nodes.size > 1»«injectityBlock(nodes)»«ENDIF»'''
	}
	
	def static String injectityBlock(Collection<NeoNode> nodes) {
		var String ret = ''
		var boolean first = true
		
	    for (var i = 0 ; i < nodes.size ; i++) {
	      	for (var j = i+1 ; j < nodes.size ; j++) {
	      		if(nodes.get(i).classType == nodes.get(j).classType) {
	      			if(!first) {
	      				ret += " AND"
	      			} else {
	      				ret += "WHERE "
	      				first = false
	      			}
	      			ret += " NOT id(" + nodes.get(i).varName +")=id("+ nodes.get(j).varName +")"
	      		}
	    	}
	    }
	    return ret
	}
	def static String returnQuery(Collection<NeoNode> nodes) {
		'''RETURN «FOR n:nodes SEPARATOR ', '»id(«n.varName») AS «n.varName»«ENDFOR»'''	
	}
	
	def static String sourceNode (NeoNode n) {
		'''(«n.varName»:«n.classType»«properties(n.properties)»)'''
	}
	def static String targetNode(NeoRelation r) {
		'''(«r.toNodeVar»:«r.toNodeLabel»)'''
	}
	def static String directedRelation (NeoRelation r) {
		'''-[«r.relVarName»:«r.relType»«properties(r.properties)»]->'''
	}
	def static String properties(Collection<NeoProperty> props) {
		'''«IF props.size > 0»«FOR p:props BEFORE ' {' SEPARATOR ',' AFTER '}'»«p.name»:«p.value»«ENDFOR»«ENDIF»'''
	}
	
	
	
	
	
	
	/* +++ OLD SECTION */
	
	def static String createCypherQuery(Collection<NeoNode> nodes, Collection<NeoCondition> conditions, Collection<NeoRelation> relations, String pName) {
		val mnName = "matchingNode"
		val relName = "matches"
		cypherMatch(nodes, relations) + cypherConditions(conditions) + cypherCreate(nodes,mnName,relName,pName) + cypherReturn(mnName);
	}
	
	def static String createCypherValidQuery(Collection<NeoNode> nodes, Collection<NeoCondition> conditions, Collection<NeoRelation> relations, String pName) {
		val mnName = "matchingNode"
		cypherMatch(nodes, relations) + cypherConditions(conditions) + cypherReturn(mnName);
	}
	
	
	def static String cypherMatch(Collection<NeoNode> nodes, Collection<NeoRelation> relations) {

		'''MATCH  «cypherNodes(nodes)»«IF relations.size > 0 », «ENDIF»«cypherRelations(relations)»
		''' 
	}
	
	
	def static String cypherNodes(Collection<NeoNode> nodes) {
		'''«FOR n:nodes SEPARATOR ', '»«n.toString»«ENDFOR»'''
	}
	
	def static String cypherNode(String varName, String classType, Collection<NeoProperty> properties) {
		'''(«varName»:«classType»«IF properties.size > 0» «cypherProperties(properties)»«ENDIF»)'''
	}
	
	def static String cypherRelations(Collection<NeoRelation> relations) {
		'''«IF relations.size > 0»
		«FOR r:relations SEPARATOR', '»«r.toString»«ENDFOR»
		«ENDIF»'''
	}
	
	def static String cypherRelation(NeoNode node, NeoRelation relation, Collection<NeoProperty> properties) {
		'''(«node.varName»)-[«relation.relVarName»:«relation.relType»«IF properties.size > 0» «cypherProperties(properties)»«ENDIF»]->(«relation.toNodeVar»)'''
	}

	
	def static String cypherConditions(Collection<NeoCondition> conditions) {
		'''«IF conditions.size > 0»
		WHERE «FOR c:conditions SEPARATOR ' AND '»«c.toString»«ENDFOR»«ENDIF»'''
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
	
	def static String cypherPropertyClassTyped(String name, String value, String classVarName) {
		'''«classVarName».«name»: «value»'''
	}
	
	def static cypherCreate(Collection<NeoNode> nodes, String mnName, String relName, String pName) {
		'''CREATE(«mnName»:Match {uuid: randomUUID(), pattern: "«pName»"}), «cypherMatchNodeRelations(nodes, mnName, relName)» '''
	}
	
	def static cypherMatchNodeRelations(Collection<NeoNode> nodes, String mnName, String relName) {
		'''«FOR n:nodes SEPARATOR ', '»«cypherMatchNodeRelation(n,mnName,relName)»«ENDFOR»'''
	}
	
	def static cypherMatchNodeRelation(NeoNode node, String mnName, String relName) {
		'''(«mnName»)-[:matches_«node.varName»]->(«node.varName»)'''
	}
	
	def static String cypherReturn(String mnName) {
		'''
		RETURN «mnName».uuid as uuid'''
	}
	
	def static String createIsValidQuery(String uuid) {
		'''MATCH (n:Match {uuid: "«uuid»"}) RETURN count(n)'''
	}
	
	def static String createDestroyQuery(String uuid) {
		'''MATCH (n:Match {uuid: "«uuid»"}) DETACH DELETE n'''
	}
	
}