package org.emoflon.neo.neo4j.adapter.templates

import java.util.ArrayList
import java.util.Collection
import org.emoflon.neo.neo4j.adapter.common.NeoNode
import org.emoflon.neo.neo4j.adapter.common.NeoRelation
import org.emoflon.neo.neo4j.adapter.common.NeoProperty
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask
import org.emoflon.neo.neo4j.adapter.patterns.EmptyMask
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch

class CypherPatternBuilder {

	/*****************************
	 * Basic Cypher Strings
	 ****************************/
	def static String sourceNode(NeoNode n) {
		'''(«n.varName»«FOR l : n.classTypes BEFORE ":" SEPARATOR ":"»«l»«ENDFOR»«properties(n.properties)»)'''
	}

	def static String directedRelation(NeoRelation r) {
		if(r.isPath())
			path(r)
		else
			'''-[«r.varName»:«r.relTypes.join("|")»«properties(r.properties)»]->'''
	}

	def static String path(NeoRelation r){
		'''-[«r.varName»:«r.relTypes.join("|")»*«r.lower»..«r.upper»«properties(r.properties)»]->'''
	}

	def static String targetNode(NeoRelation r) {
		'''(«r.toNodeVar»:«r.toNodeLabel»)'''
	}

	def static String properties(Collection<NeoProperty> props) {
		'''«IF props.size > 0»«FOR p:props BEFORE ' {' SEPARATOR ',' AFTER '}'»«p.name»:«p.value»«ENDFOR»«ENDIF»'''
	}

	protected def static CharSequence queryNode(NeoNode n) '''
	(«n.varName»«FOR l : n.classTypes BEFORE ":" SEPARATOR ":"»«l»«ENDFOR»«IF n.properties.size > 0»«FOR p:n.properties BEFORE ' {' SEPARATOR ',' AFTER '}'»«p.name»:«p.value»«ENDFOR»«ENDIF»)'''
	
	/*****************************
	 * Standard Matching Functions
	 ****************************/
	def static String readQuery(Collection<NeoNode> nodes, boolean injective, int limit, NeoMask mask) {
		'''
		«matchQuery(nodes)»
		«whereQuery(nodes, injective, mask)»
		«returnQuery(nodes)» «IF limit > 0»LIMIT «limit»«ENDIF»'''
	}
	
	def static String readQuery(Collection<NeoNode> nodes, boolean injective, NeoMask mask){
		readQuery(nodes, injective, 0, mask)
	}

	def static String readQuery_copyPaste(Collection<NeoNode> nodes, boolean injective) {
		'''
		«matchQuery(nodes)»
		«whereQuery(nodes,injective, new EmptyMask)»
		«returnQuery_copyPaste(nodes)»'''
	}

	def static String getDataQuery(Collection<NeoNode> nodes, NeoMatch match, boolean injective) {
		'''
		«matchQueryForData(nodes, match)»
		«isStillValid_whereQuery(nodes, match)»
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

	def static String whereQuery(Collection<NeoNode> nodes, boolean injective, NeoMask mask) {
		var injBlock = "";
		if(injective && nodes.size > 1){
			injBlock = injectiveBlock(nodes);
		}
		
		var maskBlock = maskBlock(nodes, mask)
						
		if(injBlock.length > 0 || maskBlock.length > 0)
			'''WHERE «injBlock» «maskBlock»'''
		else	
			''''''
	}

	private def static String maskBlock(Collection<NeoNode> nodes, NeoMask mask) {
		var relevantEntries = mask.maskedNodes.filter [ node, id |
			nodes.map[it.varName].exists[it == node]
		]

		'''«FOR entry : relevantEntries.entrySet SEPARATOR 'AND'»
				id(«entry.key») = «entry.value»«ENDFOR»'''
	}

	def static String matchQueryForIsStillValid(Collection<NeoNode> nodes, NeoMatch match) {
		'''
		MATCH «FOR n : nodes SEPARATOR ', '»
						«IF n.relations.size == 0 || n.properties.size > 0»
							«queryNode(n)»«IF n.relations.size > 0», «ENDIF»
						«ENDIF»
						«FOR r : n.relations SEPARATOR ', '»
							(«FOR l : n.classTypes BEFORE ":" SEPARATOR ":"»«l»«ENDFOR»)-[«r.varName»]->(:«r.toNodeLabel»)
						«ENDFOR»
		«ENDFOR»'''
	}

	def static String matchQueryForData(Collection<NeoNode> nodes, NeoMatch match) {
		'''
		MATCH «FOR n : nodes SEPARATOR ', '»
			«queryNode(n)»«IF n.relations.size > 0», «ENDIF»
			«FOR r : n.relations SEPARATOR ', '»
				(«n.varName»«FOR l : n.classTypes BEFORE ":" SEPARATOR ":"»«l»«ENDFOR»)«IF r.isPath»«directedRelation(r)»«ELSE»-[«r.varName»]->«ENDIF»(«r.toNodeVar»:«r.toNodeLabel»)
			«ENDFOR»
			«ENDFOR»'''
	}

	def static String injectiveBlock(Collection<NeoNode> nodes) {
		var pairsToCheck = new ArrayList<Pair<String, String>>()
		for (var i = 0; i < nodes.size; i++)
			for (var j = i + 1; j < nodes.size; j++)
				if (nodes.get(i).classTypes.equals(nodes.get(j).classTypes))
					pairsToCheck.add(Pair.of(nodes.get(i).varName, nodes.get(j).varName))

		'''«FOR pair : pairsToCheck SEPARATOR 'AND'»
				NOT id(«pair.key») = id(«pair.value»)«ENDFOR»'''
	}

	def static String returnQuery(Collection<NeoNode> nodes) {
		'''
		RETURN «FOR n : nodes SEPARATOR ',\n '»
			id(«n.varName») AS «n.varName»«IF removePaths(n.relations).size > 0», «ENDIF»
			«FOR r : removePaths(n.relations) SEPARATOR ', '»
				id(«r.varName») AS «r.varName»
			«ENDFOR»
			«ENDFOR»'''
	}
	
	private def static removePaths(Collection<NeoRelation> relations){
		relations.filter[r | !r.isPath]
	}

	def static String returnQuery_copyPaste(Collection<NeoNode> nodes) {
		'''
		RETURN «FOR n : nodes SEPARATOR ',\n '»
			«n.varName»«IF removePaths(n.relations).size > 0», «ENDIF»
			«FOR r : removePaths(n.relations) SEPARATOR ', '»
				«r.varName»
			«ENDFOR»
			«ENDFOR»'''
	}

	def static String returnQuery(Collection<NeoNode> nodes, int limit) {
		'''«returnQuery(nodes)» LIMIT «limit»'''
	}

	def static String returnQuery_copyPaste(Collection<NeoNode> nodes, int limit) {
		'''«returnQuery_copyPaste(nodes)» LIMIT «limit»'''
	}

	/*****************************
	 * IsStillValid Functions
	 ****************************/
	def static String isStillValidQuery(Collection<NeoNode> nodes, NeoMatch match, boolean injective) {
		'''
		«matchQuery(nodes)»
		«isStillValid_whereQuery(nodes, match)»
		«isStillValid_returnQuery()»'''
	}

	def static String returnDataQuery(Collection<NeoNode> nodes) {
		'''
		RETURN «FOR n : nodes SEPARATOR ',\n '»
			«n.varName» AS «n.varName»«IF removePaths(n.relations).size > 0»,«ENDIF»
			«FOR r : removePaths(n.relations) SEPARATOR ',\n  '»
				«r.varName» AS «r.varName»
			«ENDFOR»
			«ENDFOR»'''
	}

	def static String isStillValid_whereQuery(Collection<NeoNode> nodes, NeoMatch match) {
		'''
			WHERE «nodeIdBlock(nodes, match)»
		'''
	}

	def static String nodeIdBlock(Collection<NeoNode> nodes, NeoMatch match) {
		'''
		«FOR n : nodes SEPARATOR " AND "»
			id(«n.varName») = «match.getIdForNode(n)»
			«IF n.relations.size > 0»
				«FOR r : removePaths(n.relations) BEFORE " AND " SEPARATOR " AND "»
					id(«r.varName») = «match.getIdForRelation(r)»
				«ENDFOR»
			«ENDIF»
			«ENDFOR»'''
	}

	def static String isStillValid_returnQuery() {
		'''RETURN TRUE'''
	}

	/*****************************
	 * Basic Constraint Functions
	 ****************************/
	def static String constraintQuery(Collection<NeoNode> nodes, Collection<String> helperNodes, String matchCond,
		String whereCond, boolean injective, int limit, NeoMask mask) {

		'''«matchQuery(nodes)»
		«whereQuery(nodes, injective, mask)»
		«withQuery(nodes)»
		«matchCond»
		«constraint_withQuery(helperNodes)»
		WHERE «whereCond»
		«constraint_withQuery(helperNodes)»
		«IF limit>0»«returnQuery(nodes, limit)»«ELSE»«returnQuery(nodes)»«ENDIF»
		'''
	}

	def static String constraintQuery_copyPaste(Collection<NeoNode> nodes, Collection<String> helperNodes,
		String matchCond, String whereCond, boolean injective, int limit) {

		'''«matchQuery(nodes)»
		«whereQuery(nodes, injective, new EmptyMask)»
		«withQuery(nodes)»
		«matchCond»
		«constraint_withQuery(helperNodes)»
		WHERE «whereCond»
		«constraint_withQuery(helperNodes)»
		«IF limit>0»«returnQuery_copyPaste(nodes, limit)»«ELSE»«returnQuery_copyPaste(nodes)»«ENDIF»
		'''
	}

	def static String constraintQuery_isStillValid(Collection<NeoNode> nodes, Collection<String> helperNodes,
		String matchCond, String whereCond, boolean injective, NeoMatch match) {

		'''«matchQuery(nodes)»
		«isStillValid_whereQuery(nodes, match)»
		«withQuery(nodes)»
		«matchCond»
		«constraint_withQuery(helperNodes)»
		WHERE «whereCond»
		«constraint_withQuery(helperNodes)»
		RETURN TRUE
		'''
	}
	
	def static String constraintQuery_rule(Collection<String> helperNodes, String matchCond, String whereCond) {

		'''«matchCond»
		«constraint_withQuery(helperNodes)»
		WHERE «whereCond»
		«constraint_withQuery(helperNodes)»
		'''
	}
	
	def static String constraintQuery_ruleAdd(Collection<String> helperNodes, Collection<NeoNode> modelNodes, String matchCond, String whereCond) {

		'''«matchCond»
		«constraint_withQuery(helperNodes)»«FOR m:modelNodes BEFORE ", " SEPARATOR ", "»«m.varName»«ENDFOR»
		WHERE «whereCond»
		«constraint_withQuery(helperNodes)»«FOR m:modelNodes BEFORE ", " SEPARATOR ", "»«m.varName»«ENDFOR»
		'''
	}

	def static String constraintQuery_Satisfied(String optionalMatch, String whereClause) {
		'''«optionalMatch»
		WHERE «whereClause»
		RETURN TRUE'''
	}

	def static String constraint_matchQuery(Collection<NeoNode> nodes, boolean injective, int idForScope, NeoMask mask) {
		'''
			 OPTIONAL «matchQuery(nodes)»
			«whereQuery(nodes,injective, mask)»
			«withCountQuery(nodes, idForScope)»
		'''
	}

	def static String condition_matchQuery(Collection<NeoNode> nodes, boolean injective, NeoMask mask) {
		'''
			 OPTIONAL «matchQuery(nodes)»
			«whereQuery(nodes,injective, mask)»
		'''
	}

	def static String withQuery(Collection<NeoNode> nodes) {
		'''WITH «FOR n : nodes SEPARATOR ', '»«n.varName»«IF n.relations.size > 0»«FOR r: n.relations»«getRelationStringOnlyIfNotPath(r)»«ENDFOR»«ENDIF»«ENDFOR»'''
	}
	
	def static String getRelationStringOnlyIfNotPath(NeoRelation r) {
		if(r.isPath) {
			''''''
		} else {
			''', «r.varName»'''
		}
	}

	def static String constraint_ifThen_readQuery(Collection<NeoNode> nodes, Collection<NeoNode> nodes2,
		Collection<String> nodesMap, boolean injective, NeoMask mask) {
		'''
		«constraint_ifThen_matchQuery(nodes,nodes2,injective, mask)»
		«constraint_withQuery(nodesMap)»
		WHERE «whereNegativeConditionQuery_Nodes(nodes2)» 
		«constraint_returnQuery(nodesMap)»'''
	}
	
	def static String constraint_ifThen_readQuery_satisfy(Collection<NeoNode> nodesIf, Collection<NeoNode> nodesThen, Collection<String> nodesThenButNotIf, Collection<String> nodesMap,
        boolean injective, NeoMask mask) {
        '''
        «constraint_ifThen_matchQuery(nodesIf,nodesThen,injective, mask)»
        «constraint_withQuery(nodesMap)»
        WHERE «whereNegativeConditionQuery_String(nodesThenButNotIf)» 
        RETURN FALSE'''
    }

	def static String constraint_ifThen_matchQuery(Collection<NeoNode> nodes, Collection<NeoNode> nodes2,
		boolean injective, NeoMask mask) {
		'''«matchQuery(nodes)»
		«whereQuery(nodes,injective, mask)»
		«withQuery(nodes)»
		OPTIONAL «matchQuery(nodes2)»
		«whereQuery(nodes2, injective, mask)»
		'''
	}

	def static String constraint_withQuery(Collection<String> nodes) {
		''' WITH «FOR n : nodes SEPARATOR ', '»«n»«ENDFOR»'''
	}

	private def static String constraint_returnQuery(Collection<String> nodes) {
		''' RETURN «FOR n : nodes SEPARATOR ', '»id(«n») AS «n»«ENDFOR» LIMIT 1'''
	}

	/*****************************
	 * Basic Condition Functions
	 ****************************/
	def static String conditionQuery(Collection<NeoNode> nodes, String optionalMatches, String whereClause,
		Collection<String> helperNodes, boolean isNegated, int limit) {
		'''«matchQuery(nodes)»
	 	«withQuery(nodes)»
	 	«optionalMatches»
	 	«constraint_withQuery(helperNodes)»
	 	WHERE «IF isNegated»NOT(«ENDIF»«whereClause»«IF isNegated»)«ENDIF»
	 	«IF limit>0»«returnQuery(nodes,limit)»«ELSE»«returnQuery(nodes)»«ENDIF»'''
	}

	def static String conditionQuery_copyPaste(Collection<NeoNode> nodes, String optionalMatches, String whereClause,
		Collection<String> helperNodes, boolean isNegated, int limit) {
		'''«matchQuery(nodes)»
	 	«withQuery(nodes)»
	 	«optionalMatches»
	 	«constraint_withQuery(helperNodes)»
	 	WHERE «IF isNegated»NOT(«ENDIF»«whereClause»«IF isNegated»)«ENDIF»
	 	«IF limit>0»«returnQuery_copyPaste(nodes,limit)»«ELSE»«returnQuery_copyPaste(nodes)»«ENDIF»'''
	}

	def static String conditionQuery_isStillValid(Collection<NeoNode> nodes, String optionalMatches, String whereClause,
		Collection<String> helperNodes, boolean isNegated, NeoMatch match) {
		'''«matchQuery(nodes)»
	 	«isStillValid_whereQuery(nodes,match)»
	 	«withQuery(nodes)»
	 	«optionalMatches»
	 	«constraint_withQuery(helperNodes)»
	 	WHERE «IF isNegated»NOT(«ENDIF»«whereClause»«IF isNegated»)«ENDIF»
	 	RETURN TRUE'''
	}
	
	def static String whereNegativeConditionQuery_String(Collection<String> nodes) {
        '''«FOR n:nodes SEPARATOR ' OR '»«n» IS NULL«ENDFOR»'''
    }

	def static String wherePositiveConstraintQuery(int id) {
		'''m_«id» > 0'''
	}

	def static String whereNegativeConstraintQuery(int id) {
		'''m_«id» = 0'''
	}

	def static String whereImplicationConstraintQuery(int id) {
		'''(m_«id-1» = m_«id»)'''
	}

	def static String wherePositiveConditionQuery(Collection<String> elem) {
		'''«FOR e : elem SEPARATOR ' AND '»«e» IS NOT NULL«ENDFOR»'''
	}

	def static String whereNegativeConditionQuery(Collection<String> elem) {
		'''«FOR e : elem SEPARATOR ' OR '»«e» IS NULL«ENDFOR»'''
	}
	
	def static String whereNegativeConditionQuery_Nodes(Collection<NeoNode> nodes) {
        '''«FOR n : nodes SEPARATOR ' OR '»«n.varName» IS NULL«FOR r:n.relations BEFORE ' OR ' SEPARATOR ' OR '»«r.varName»  IS NULL«ENDFOR»«ENDFOR»'''
    }

	def static String withCountQuery(Collection<NeoNode> nodes, int id) {

		var String ret = ''

		for (var i = 0; i < id; i++) {
			ret += "m_" + i + ", "
		}

		'''WITH «ret» count(«nodes.get(0).varName») as m_«id»'''
	}
	
	/*****************************
	 * Basic Rule Functions
	 ****************************/
	 
	 
	 def static String ruleExecutionQuery(Collection<NeoNode> nodes, NeoMatch match, boolean spo, 
	 	Collection<NeoNode> nodesL, Collection<NeoNode> nodesR, Collection<NeoNode> nodesK, 
	 	Collection<NeoRelation> refL, Collection<NeoRelation> refR, Collection<NeoRelation> relK,
	 	Collection<NeoNode> modelNodes, Collection<NeoRelation> modelRel, String conditionQuery
	 ) {
	 	
	 	'''
	 	«matchQuery(nodes)»«ruleExecution_matchModelNodes(modelNodes)»
	 	«isStillValid_whereQuery(nodes, match)»
	 	«conditionQuery»
	 	«ruleExecution_deleteQuery(spo, nodesL, refL)»
	 	«ruleExecution_createQuery(nodesR,refR,modelNodes,modelRel)»
	 	«ruleExecution_returnQuery(nodesK,relK,nodesR,refR)»
	 	
	 	'''
	 	
	 }
	 
	 def static String ruleExecution_deleteQuery(boolean spo, Collection<NeoNode> nodesL, Collection<NeoRelation> refL) {
	 	'''«IF nodesL.size > 0 || refL.size > 0»«IF spo»DETACH «ENDIF»DELETE «FOR r: refL SEPARATOR ', '»«r.varName»«ENDFOR»
	 			«IF nodesL.size > 0 && refL.size > 0», «ENDIF»«FOR n: nodesL SEPARATOR ', '»«ENDFOR»«ENDIF»'''
	 }
	 
	 def static String ruleExecution_createQuery(Collection<NeoNode> nodesR, Collection<NeoRelation> refR, Collection<NeoNode> modelNode, Collection<NeoRelation> modelRel) {
	 	'''«IF nodesR.size > 0 || refR.size > 0»CREATE «FOR n: nodesR SEPARATOR ', '»«queryNode(n)»«ENDFOR»
	 	«IF nodesR.size > 0 && refR.size > 0», «ENDIF»«FOR r: refR SEPARATOR ', '»(«r.fromNode.varName»)«directedRelation(r)»(«r.toNodeVar»)«ENDFOR»«ENDIF»
	 	«IF modelRel.size > 0»«ruleExecution_createModelRel(modelRel)»«ENDIF»
	 	'''
	 }
	 
	 def static String ruleExecution_returnQuery(Collection<NeoNode> nodesK, Collection<NeoRelation> refK, Collection<NeoNode> nodesR, Collection<NeoRelation> refR) {
	 	'''RETURN «FOR n: nodesK SEPARATOR ', '»id(«n.varName») as «n.varName»«ENDFOR»
	 	«IF refK.size > 0 », «ENDIF»«FOR r: refK SEPARATOR ', '»id(«r.varName») as «r.varName»«ENDFOR»
	 	'''
	 }
	 
	 def static String ruleExecution_matchModelNodes(Collection<NeoNode> nodes) {
	 	'''«FOR n:nodes BEFORE ", " SEPARATOR ", "»«sourceNode(n)»«FOR r : n.relations»«directedRelation(r)»«targetNode(r)»«ENDFOR»«ENDFOR»'''
	 }
	 def static String ruleExecution_createModelRel(Collection<NeoRelation> rel) {
	 	'''«FOR r:rel BEFORE ", " SEPARATOR ", "»(«r.fromNodeVar»)-[:metaType]->(«r.toNodeVar»)«ENDFOR»'''
	 }

}
