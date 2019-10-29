package org.emoflon.neo.neo4j.adapter.templates

import java.util.ArrayList
import java.util.Collection
import java.util.HashMap
import org.emoflon.neo.emsl.util.EMSLUtil
import org.emoflon.neo.neo4j.adapter.common.NeoNode
import org.emoflon.neo.neo4j.adapter.common.NeoProperty
import org.emoflon.neo.neo4j.adapter.common.NeoRelation
import org.emoflon.neo.neo4j.adapter.patterns.EmptyMask
import org.emoflon.neo.neo4j.adapter.patterns.NeoAttributeExpression
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask

class CypherPatternBuilder {

	/*****************************
	 * Basic Cypher Strings
	 ****************************/
	private def static String sourceNode(NeoNode n) {
		'''(«n.varName»:«n.primaryLabel»«properties(n.properties)»)'''
	}

	private def static String directedRelation(NeoRelation r) {
		if(r.isPath())
			path(r)
		else
			'''-[«r.varName»:«r.relTypes.join("|")»«properties(r.properties)»]->'''
	}

	private def static String path(NeoRelation r){
		'''-[«r.varName»:«r.relTypes.join("|")»*«r.lower»..«r.upper»«properties(r.properties)»]->'''
	}

	private def static String targetNode(NeoRelation r) {
		'''(«r.toNodeVar»:«r.toNodeLabel»)'''
	}

	private def static String properties(Collection<NeoProperty> props) {
		'''«IF props.size > 0»«FOR p:props BEFORE ' {' SEPARATOR ',' AFTER '}'»«p.name»:«p.value»«ENDFOR»«ENDIF»'''
	}

	protected def static CharSequence queryNode(NeoNode n) '''
	(«n.varName»:«n.primaryLabel»«IF n.properties.size > 0»«FOR p:n.properties BEFORE ' {' SEPARATOR ',' AFTER '}'»«p.name»:«p.value»«ENDFOR»«ENDIF»)'''
	
	protected def static CharSequence createNode(NeoNode n) 
	'''
		(«n.varName»
			«FOR l : n.getLabels BEFORE ":" SEPARATOR ":"»«l»«ENDFOR»
			«IF n.properties.size > 0»
				«FOR p:n.properties BEFORE ' {' SEPARATOR ',' AFTER '}'»
					«p.name»:«p.value»
				«ENDFOR»
			«ENDIF»
		)
	'''
	
	
	/*****************************
	 * Standard Matching Functions
	 ****************************/
	
	def static String readQuery(Collection<NeoNode> nodes, Collection<NeoAttributeExpression> attr, boolean injective, int limit, NeoMask mask) {
		'''
		«IF nodes.size>0»«matchQuery(nodes)»
		«whereQuery(nodes, attr, injective, mask)»
		«returnQuery(nodes)» «IF limit > 0»LIMIT «limit»«ENDIF»
		«ELSE»
		RETURN TRUE
		«ENDIF»'''
	}
	
	

	def static String readQuery_copyPaste(Collection<NeoNode> nodes, Collection<NeoAttributeExpression> attr, boolean injective) {
		'''
		«matchQuery(nodes)»
		«whereQuery(nodes, attr, injective, new EmptyMask)»
		«returnQuery_copyPaste(nodes)»'''
	}

	
	
	def static String getDataQuery(Collection<NeoNode> nodes, Collection<NeoAttributeExpression> attr, boolean injective) {
		'''
		«unwindQuery»
		«matchQueryForData(nodes)»
		«isStillValid_whereQuery(nodes, attr)»
		«returnDataQuery(nodes)»'''
	}

	private def static String matchQuery(Collection<NeoNode> nodes) {
		'''MATCH «FOR n : nodes SEPARATOR ', '»
			«IF n.relations.size > 0 »
				«FOR r:n.relations SEPARATOR ', '»«sourceNode(n)»«directedRelation(r)»«targetNode(r)»«ENDFOR»
			«ELSE»«queryNode(n)»
			«ENDIF»
		«ENDFOR»'''
	}
	
	private def static String whereQuery(Collection<NeoNode> nodes, Collection<NeoAttributeExpression> attr, boolean injective, NeoMask mask) {
		var out = "";
		if(injective && nodes.size > 1){
			out += injectiveBlock(nodes);
		}
		var maskBlock = maskBlock(nodes, mask)
		if(out.length > 0 && maskBlock.length > 0)
			out += " AND "
		out += maskBlock;	
		
		var attrBlock = attributeExpressionQuery(attr);
		if(out.length > 0 && attrBlock.length > 0)
			out += " AND ";
		out += attrBlock;
						
		if(out.length > 0)
			'''WHERE «out»'''
		else	
			''''''
	}
	
	
	
	private def static attributeExpressionQuery(Collection<NeoAttributeExpression> attr) {
		'''«FOR a:attr SEPARATOR " AND "»«a.varName».«a.attrKey» «a.opString» «a.attrValue»«ENDFOR»'''
	}
	
	private def static attributeExpressionQueryList(Collection<NeoAttributeExpression> attr) {
		'''«FOR a:attr SEPARATOR ", "»«a.varName».«a.attrKey» «a.opString» «a.attrValue»«ENDFOR»'''
	}
	
	private def static String whereQuery(Collection<NeoNode> nodes, Collection<NeoAttributeExpression> attr, boolean injective, NeoMask mask, 
		HashMap<String,String> equalElem, Collection<String> injElem) {
		var out = "";
		if(injective) {
			out += injectiveNodes(injElem);			
		}
		
		var maskBlock = maskBlock(nodes, mask)
		if(out.length > 0 && maskBlock.length > 0)
			out += " AND "
		out += maskBlock;
		
		var equalCond = whereEqualElementsConditionQuery(equalElem);
		if(out.length > 0 && equalCond.length > 0)
			out += " AND "
		out += equalCond;
		
		var attrBlock = attributeExpressionQuery(attr);
		if(out.length > 0 && attrBlock.length > 0)
			out += " AND ";
		out += attrBlock;
						
		if(out.length>0)
			'''WHERE 
				«out»
			'''
		else	
			''''''
	}
	
	//FIXME[Anjorin] Get rid of this
	private def static String injectiveNodes(Collection<String> injElem) {
		'''«FOR k:injElem SEPARATOR " AND "»NOT id(«k.split("<>").get(0)») = id(«k.split("<>").get(1)»)«ENDFOR»'''
	}

	private def static String maskBlock(Collection<NeoNode> nodes, NeoMask mask) {
		var relevantEntries = mask.maskedNodes.filter [ node, id |
			nodes.map[it.varName].exists[it == node]
		]

		'''«FOR entry : relevantEntries.entrySet SEPARATOR 'AND'»
				id(«entry.key») = «entry.value»«ENDFOR»'''
	}

	

	private def static String matchQueryForData(Collection<NeoNode> nodes) {
		'''
		MATCH «FOR n : nodes SEPARATOR ', '»
			«queryNode(n)»«IF n.relations.size > 0», «ENDIF»
			«FOR r : n.relations SEPARATOR ', '»
				(«n.varName»:«n.primaryLabel»)«IF r.isPath»«directedRelation(r)»«ELSE»-[«r.varName»]->«ENDIF»(«r.toNodeVar»:«r.toNodeLabel»)
			«ENDFOR»
			«ENDFOR»'''
	}

	private def static ArrayList<Pair<String,String>> injectiveElem(Collection<NeoNode> nodes) {
		var pairsToCheck = new ArrayList<Pair<String, String>>()
		for (var i = 0; i < nodes.size; i++) {
			for (var j = i + 1; j < nodes.size; j++) {
				var classTypesI = nodes.get(i).getLabels;
				var classTypesJ = nodes.get(j).getLabels;
				
				if(classTypesJ.contains(nodes.get(i).primaryLabel) ||
				   classTypesI.contains(nodes.get(j).primaryLabel))				
					pairsToCheck.add(Pair.of(nodes.get(i).varName, nodes.get(j).varName))
			}
		}
		
		return pairsToCheck;
	}

	private def static String injectiveCond(ArrayList<Pair<String,String>> pairsToCheck) {
		'''«FOR p: pairsToCheck SEPARATOR " AND "»NOT id(«p.key») = id(«p.value»)«ENDFOR»'''
	}
	
	private def static String injectiveBlock(Collection<NeoNode> nodes) {
		var pairsToCheck = new ArrayList<Pair<String,String>>(injectiveElem(nodes));
		return injectiveCond(pairsToCheck);
	}
	
	private def static String returnQuery(Collection<NeoNode> nodes) {
		'''
		RETURN DISTINCT «FOR n : nodes SEPARATOR ',\n '»
			id(«n.varName») AS «n.varName»«IF removePaths(n.relations).size > 0», «ENDIF»
			«FOR r : removePaths(n.relations) SEPARATOR ', '»
				id(«r.varName») AS «r.varName»
			«ENDFOR»
			«ENDFOR»'''
	}
	
	private def static removePaths(Collection<NeoRelation> relations){
		relations.filter[r | !r.isPath]
	}

	private def static String returnQuery_copyPaste(Collection<NeoNode> nodes) {
		'''
		RETURN DISTINCT «FOR n : nodes SEPARATOR ',\n '»
			«n.varName»«IF removePaths(n.relations).size > 0», «ENDIF»
			«FOR r : removePaths(n.relations) SEPARATOR ', '»
				«r.varName»
			«ENDFOR»
			«ENDFOR»'''
	}

	private def static String returnQuery(Collection<NeoNode> nodes, int limit) {
		'''«returnQuery(nodes)» LIMIT «limit»'''
	}

	private def static String returnQuery_copyPaste(Collection<NeoNode> nodes, int limit) {
		'''«returnQuery_copyPaste(nodes)» LIMIT «limit»'''
	}
	
	private def static String unwindQuery() {
		'''UNWIND $matches AS «EMSLUtil.PARAM_NAME_FOR_MATCH»'''
	}

	/*****************************
	 * IsStillValid Functions
	 ****************************/
	def static String isStillValidQuery(Collection<NeoNode> nodes, Collection<NeoAttributeExpression> attr, boolean injective) {
		'''
		«unwindQuery()»
		«matchQuery(nodes)»
		«isStillValid_whereQuery(nodes, attr)»
		«isStillValid_returnQuery()»'''
	}

	private def static String returnDataQuery(Collection<NeoNode> nodes) {
		'''
		RETURN «FOR n : nodes SEPARATOR ',\n '»
			«n.varName» AS «n.varName»«IF removePaths(n.relations).size > 0»,«ENDIF»
			«FOR r : removePaths(n.relations) SEPARATOR ',\n  '»
				«r.varName» AS «r.varName»
			«ENDFOR»
			«ENDFOR»'''
	}
	
	private def static String isStillValid_whereQuery(Collection<NeoNode> nodes, Collection<NeoAttributeExpression> attr) {
		'''
			WHERE «nodeIdBlock(nodes)»«IF attr.size > 0» AND «ENDIF»«attributeExpressionQuery(attr)»
		'''
	}
	
	private def static String nodeIdBlock(Collection<NeoNode> nodes) {
		'''
		«FOR n : nodes SEPARATOR " AND "»
			id(«n.varName») = «EMSLUtil.PARAM_NAME_FOR_MATCH».«n.varName»
			«IF n.relations.size > 0»
				«FOR r : removePaths(n.relations) BEFORE " AND " SEPARATOR " AND "»
					id(«r.varName») = «EMSLUtil.PARAM_NAME_FOR_MATCH».«r.varName»
				«ENDFOR»
			«ENDIF»
			«ENDFOR»'''
	}
	
	private def static String isStillValid_returnQuery() {
		'''RETURN «EMSLUtil.PARAM_NAME_FOR_MATCH».match_id as match_id'''
	}

	/*****************************
	 * Basic Constraint Functions
	 ****************************/
	def static String constraintQuery(Collection<NeoNode> nodes, Collection<String> helperNodes, String matchCond,
		String whereCond, Collection<NeoAttributeExpression> attr, boolean injective, int limit, NeoMask mask) {

		'''«IF nodes.size>0»«matchQuery(nodes)»
		«whereQuery(nodes, attr, injective, mask)»
		«withQuery(nodes)»«ENDIF»
		«matchCond»
		«constraint_withQuery(helperNodes)»
		WHERE «whereCond»
		«constraint_withQuery(helperNodes)»
		«IF nodes.size>0»«IF limit>0»«returnQuery(nodes, limit)»«ELSE»«returnQuery(nodes)»«ENDIF»
		«ELSE»RETURN TRUE«ENDIF»
		'''
	}

	def static String constraintQuery_copyPaste(Collection<NeoNode> nodes, Collection<String> helperNodes,
		String matchCond, String whereCond, Collection<NeoAttributeExpression> attr, boolean injective, int limit) {

		'''«IF nodes.size>0»«matchQuery(nodes)»
		«whereQuery(nodes, attr, injective, new EmptyMask)»
		«withQuery(nodes)»«ENDIF»
		«matchCond»
		«constraint_withQuery(helperNodes)»
		WHERE «whereCond»
		«constraint_withQuery(helperNodes)»
		«IF nodes.size>0»«IF limit>0»«returnQuery_copyPaste(nodes, limit)»«ELSE»«returnQuery_copyPaste(nodes)»«ENDIF»
		«ELSE»RETURN TRUE«ENDIF»
		'''
	}
	
	def static String constraintQuery_isStillValid(Collection<NeoNode> nodes, Collection<String> helperNodes,
		String matchCond, String whereCond, Collection<NeoAttributeExpression> attr, boolean injective) {

		'''«unwindQuery()»
		«IF nodes.size>0»«matchQuery(nodes)»
		«isStillValid_whereQuery(nodes, attr)»
		«withQuery(nodes)», «EMSLUtil.PARAM_NAME_FOR_MATCH»«ENDIF»
		«matchCond»
		«constraint_withQuery(helperNodes)»
		WHERE «whereCond»
		«constraint_withQuery(helperNodes)»
		«isStillValid_returnQuery()»
		'''
	}

	def static String constraintQuery_Satisfied(String optionalMatch, String whereClause) {
		'''«optionalMatch»
		WHERE «whereClause»
		RETURN TRUE'''
	}

	def static String constraint_matchQuery(Collection<NeoNode> nodes, Collection<NeoAttributeExpression> attr, boolean injective, int idForScope, NeoMask mask) {
		'''
			 OPTIONAL «matchQuery(nodes)»
			«whereQuery(nodes,attr,injective, mask)»
			«withCountQuery(nodes, idForScope)»
		'''
	}

	def static String condition_matchQuery(Collection<NeoNode> nodes, Collection<NeoAttributeExpression> attr, boolean injective, NeoMask mask, 
		HashMap<String, String> equalElem, Collection<String> injElem) {
		'''
			 OPTIONAL «matchQuery(nodes)»
			«whereQuery(nodes, attr, injective, mask, equalElem, injElem)»
		'''
	}

	private def static String withQuery(Collection<NeoNode> nodes) {
		'''WITH «FOR n : nodes SEPARATOR ', '»«n.varName»«IF n.relations.size > 0»«FOR r: n.relations»«getRelationStringOnlyIfNotPath(r)»«ENDFOR»«ENDIF»«ENDFOR»'''
	}
	
	private def static String getRelationStringOnlyIfNotPath(NeoRelation r) {
		if(r.isPath) {
			''''''
		} else {
			''', «r.varName»'''
		}
	}
	
	def static String constraint_ifThen_readQuery_satisfy(
			Collection<NeoNode> nodesIf,
			Collection<NeoNode> nodesThen,
			Collection<String> nodesThenButNotIf,
			Collection<String> nodesMap,
        	Collection<NeoAttributeExpression> attr,
        	Collection<NeoAttributeExpression> attrOpt, 
        	HashMap<String,String> equalElem, 
        	Collection<String> injectiveElem, 
        	boolean injective, 
        	NeoMask mask
        ) {
        '''
        «constraint_ifThen_matchQuery(
        	nodesIf, 
        	nodesThen, 
        	attr, 
        	attrOpt, 
        	injective, 
        	mask, 
        	equalElem, 
        	injectiveElem
        )»
        «constraint_withQuery(nodesMap)»
        WHERE 
        	«whereNegativeConditionQuery_String(nodesThenButNotIf)»
        RETURN FALSE
        '''
    }

	private def static String constraint_ifThen_matchQuery(
			Collection<NeoNode> nodesIf, 
			Collection<NeoNode> nodesThen,
			Collection<NeoAttributeExpression> attr, 
			Collection<NeoAttributeExpression> attrOpt, 
			boolean injective, 
			NeoMask mask, 
			HashMap<String,String> equalElem, 
			Collection<String> injectiveElem
		) {
		'''
		«matchQuery(nodesIf)»
		«whereQuery(nodesIf, attr, injective, mask)»
		«withQuery(nodesIf)»
		OPTIONAL «matchQuery(nodesThen)»
		«whereQuery(nodesThen, attrOpt, injective, mask, equalElem, injectiveElem)»
		'''
	}

	private def static String constraint_withQuery(Collection<String> nodes) {
		''' WITH «FOR n : nodes SEPARATOR ', '»«n»«ENDFOR»'''
	}

	

	/*****************************
	 * Basic Condition Functions
	 ****************************/
	def static String conditionQuery(Collection<NeoNode> nodes, String optionalMatches, String whereClause,
		Collection<String> helperNodes, boolean isNegated, Collection<NeoAttributeExpression> attr, boolean injective, int limit, NeoMask mask) {
		'''«IF nodes.size>0»«matchQuery(nodes)»
		«whereQuery(nodes, attr, injective, mask)»
	 	«withQuery(nodes)»«ENDIF»
	 	«optionalMatches»
	 	«constraint_withQuery(helperNodes)»
	 	WHERE «IF isNegated»NOT(«ENDIF»«whereClause»«IF isNegated»)«ENDIF»
	 	«IF nodes.size>0»«IF limit>0»«returnQuery(nodes,limit)»«ELSE»«returnQuery(nodes)»«ENDIF»
	 	«ELSE»RETURN TRUE«ENDIF»'''
	}

	def static String conditionQuery_copyPaste(Collection<NeoNode> nodes, String optionalMatches, String whereClause,
		Collection<String> helperNodes, boolean isNegated, Collection<NeoAttributeExpression> attr, boolean injective, int limit) {
		'''«IF nodes.size>0»«matchQuery(nodes)»
		«whereQuery(nodes, attr, injective, new EmptyMask)»
	 	«withQuery(nodes)»«ENDIF»
	 	«optionalMatches»
	 	«constraint_withQuery(helperNodes)»
	 	WHERE «IF isNegated»NOT(«ENDIF»«whereClause»«IF isNegated»)«ENDIF»
	 	«IF nodes.size>0»«IF limit>0»«returnQuery_copyPaste(nodes,limit)»«ELSE»«returnQuery_copyPaste(nodes)»«ENDIF»
	 	«ELSE»RETURN TRUE«ENDIF»'''
	}
	
	def static String conditionQuery_isStillValid(Collection<NeoNode> nodes, String optionalMatches, String whereClause,
		Collection<String> helperNodes, Collection<NeoAttributeExpression> attr, boolean isNegated) {
		'''
		«unwindQuery()»
		«IF nodes.size>0»«matchQuery(nodes)»
		«org.emoflon.neo.neo4j.adapter.templates.CypherPatternBuilder.isStillValid_whereQuery(nodes,attr)»
		«withQuery(nodes)»«ENDIF», «EMSLUtil.PARAM_NAME_FOR_MATCH»
		«optionalMatches»
		«constraint_withQuery(helperNodes)», «EMSLUtil.PARAM_NAME_FOR_MATCH»
		WHERE «IF isNegated»NOT(«ENDIF»«whereClause»«IF isNegated»)«ENDIF»
		«isStillValid_returnQuery()»'''
	}
	
	private def static String whereNegativeConditionQuery_String(Collection<String> nodes) {
        '''«FOR n:nodes SEPARATOR ' OR '»«n» IS NULL«ENDFOR»'''
    }

	def static String wherePositiveConstraintQuery(int id) {
		'''m_«id» > 0'''
	}

	def static String whereNegativeConstraintQuery(int id) {
		'''m_«id» = 0'''
	}

	

	def static String wherePositiveConditionQuery(Collection<String> elem) {
		'''«FOR e : elem SEPARATOR ' AND '»«e» IS NOT NULL«ENDFOR»'''
	}

	def static String whereNegativeConditionQuery(Collection<String> elem) {
		'''«FOR e : elem SEPARATOR ' OR '»«e» IS NULL«ENDFOR»'''
	}
	
	
    
    private def static String whereEqualElementsConditionQuery(HashMap<String,String> elem) {
        '''«FOR e: elem.keySet SEPARATOR " AND "»«e» = «elem.get(e)»«ENDFOR»'''
    }

	private def static String withCountQuery(Collection<NeoNode> nodes, int id) {

		var String ret = ''

		for (var i = 0; i < id; i++) {
			ret += "m_" + i + ", "
		}

		'''WITH «ret» count(«nodes.get(0).varName») as m_«id»'''
	}
	
	/*****************************
	 * Basic Rule Functions
	 ****************************/
	 
	 def static String ruleExecutionQuery(Collection<NeoNode> nodes, boolean spo, 
	 	Collection<NeoNode> nodesL, Collection<NeoNode> nodesR, Collection<NeoNode> nodesK, 
	 	Collection<NeoRelation> refL, Collection<NeoRelation> refR, Collection<NeoRelation> relK,
	 	Collection<NeoAttributeExpression> attrExpr, Collection<NeoAttributeExpression> attrAsgn) {
	 	'''
	 	«unwindQuery()»
	 	«IF nodes.size>0»
	 		«matchQuery(nodes)»
	 		«isStillValid_whereQuery(nodes, attrExpr)»
	 	«ENDIF»
	 	«ruleExecution_deleteQuery(spo, nodesL, refL)»
	 	«ruleExecution_createQuery(nodesR,refR)»
	 	«ruleExecution_setQuery(attrAsgn)»
	 	«ruleExecution_returnQuery(nodesK,relK,nodesR,refR)»«IF nodesK.size>0 || relK.size>0 || nodesR.size>0 || refR.size>0»,«ENDIF» «EMSLUtil.PARAM_NAME_FOR_MATCH».match_id as match_id
	 	'''
	 }
	 
	 private def static String ruleExecution_deleteQuery(boolean spo, Collection<NeoNode> nodesL, Collection<NeoRelation> refL) {
	 	'''«IF nodesL.size > 0 || refL.size > 0»«IF spo»DETACH «ENDIF»DELETE «FOR r: refL SEPARATOR ', '»«r.varName»«ENDFOR»
	 			«IF nodesL.size > 0 && refL.size > 0», «ENDIF»«FOR n: nodesL SEPARATOR ', '»«n.varName»«ENDFOR»«ENDIF»'''
	 }
	 
	 private def static String ruleExecution_createQuery(Collection<NeoNode> nodesR, Collection<NeoRelation> refR) {
	 	'''«IF nodesR.size > 0 || refR.size > 0»CREATE «FOR n: nodesR SEPARATOR ', '»«createNode(n)»«ENDFOR»
	 	«IF nodesR.size > 0 && refR.size > 0», «ENDIF»«FOR r: refR SEPARATOR ', '»(«r.fromNode.varName»)«directedRelation(r)»(«r.toNodeVar»)«ENDFOR»«ENDIF»
	 	'''
	 }
	 
	 private def static String ruleExecution_setQuery(Collection<NeoAttributeExpression> assignments) {
	 	if(assignments.size>0)
	 	'''SET «attributeExpressionQueryList(assignments)»'''
	 	else
	 	''''''
	 }
	 
	 private def static String ruleExecution_returnQuery(Collection<NeoNode> nodesK, Collection<NeoRelation> refK, Collection<NeoNode> nodesR, Collection<NeoRelation> refR) {
	 	var nodes = new ArrayList<NeoNode>(nodesK);
	 	nodes.addAll(nodesR);
	 	var ref = new ArrayList<NeoRelation>(refK);
	 	ref.addAll(refR);
	 	
	 	'''RETURN «ruleExecution_returnQueryNodes(nodes)»
	 	«ruleExecution_returnQueryRelation(ref)»'''
	 	
	 }
	 
	 private def static String ruleExecution_returnQueryNodes(Collection<NeoNode> nodes) {
	 	'''«FOR n: nodes SEPARATOR ', '»id(«n.varName») as «n.varName»«ENDFOR»'''
	 }
	 
	 
	 private def static String ruleExecution_returnQueryRelation(Collection<NeoRelation> rel) {
	 	var out = "";
	 	for(r:rel) {
	 		if(!r.isPath) {
	 			out += ", id(" + r.varName + ") as " + r.varName;
	 		}
	 	}
	 	return out;
	 }
}
