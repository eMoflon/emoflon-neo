package org.emoflon.neo.neo4j.adapter

import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map

class CypherBuilder {
	Map<String, NodeCommand> nodesToMatch
	Map<String, EdgeCommand> edgesToMatch

	List<NodeCommand> nodesToCreate
	List<EdgeCommand> edgesToCreate

	Map<String, NodeCommand> nodesToMerge
	Map<String, EdgeCommand> edgesToMerge

	List<NodeCommand> nodesToReturn;

	new() {
		nodesToMatch = new HashMap
		edgesToMatch = new HashMap

		nodesToCreate = new ArrayList
		edgesToCreate = new ArrayList

		nodesToMerge = new HashMap
		edgesToMerge = new HashMap

		nodesToReturn = new ArrayList
	}

	def handleNode(Map<String, NodeCommand> map, List<NeoProperty> props, List<String> labels, NodeCommand type,
		NodeCommand container) {
		if(props === null)
			throw new IllegalArgumentException("Property list should not be null")
		
		val key = createKey(props, labels, type, container)
		if (map.containsKey(key))
			return map.get(key)
		else {
			val nc = new NodeCommand(props, labels, type, container)
			map.put(key, nc)
			return nc
		}
	}

	def matchNode(List<NeoProperty> props, List<String> labels, NodeCommand type, NodeCommand container) {
		handleNode(nodesToMatch, props, labels, type, container)
	}

	def String createKey(List<NeoProperty> props, List<String> labels, NodeCommand nc1, NodeCommand nc2) '''
		«props.join("-")»-«labels.join("-")»-«nc1»-«nc2»
	'''

	def handleEdge(Map<String, EdgeCommand> map, List<NeoProperty> props, String label, NodeCommand from,
		NodeCommand to) {
		if(props === null)
			throw new IllegalArgumentException("Property list should not be null")
		
		val key = createKey(props, List.of(label), from, to)
		if (map.containsKey(key))
			return map.get(key)
		else {
			val ec = new EdgeCommand(props, label, from, to)
			map.put(key, ec)
			return ec
		}
	}

	def matchEdge(List<NeoProperty> props, String label, NodeCommand from, NodeCommand to) {
		handleEdge(edgesToMatch, props, label, from, to)
	}

	def createNode(List<NeoProperty> props, List<String> labels, NodeCommand type, NodeCommand container) {
		val nc = new NodeCommand(props, labels, type, container)
		nodesToCreate.add(nc)
		return nc
	}

	def createEdge(List<NeoProperty> props, String label, NodeCommand from, NodeCommand to) {
		val ec = new EdgeCommand(props, label, from, to)
		edgesToCreate.add(ec)
		return ec
	}

	def mergeNode(List<NeoProperty> props, List<String> labels, NodeCommand type, NodeCommand container) {
		handleNode(nodesToMerge, props, labels, type, container)
	}

	def mergeEdge(List<NeoProperty> props, String label, NodeCommand from, NodeCommand to) {
		handleEdge(edgesToMerge, props, label, from, to)
	}

	def String buildCommand() {
		''' 
			«nodesToMatch.values.map[n| n.match()].join("\n")»
			«edgesToMatch.values.map[e| e.match()].join("\n")»
			«nodesToCreate.map[n| n.create()].join("\n")»
			«edgesToCreate.map[e| e.create()].join("\n")»
			«nodesToMerge.values.map[n| n.merge()].join("\n")»
			«edgesToMerge.values.map[e| e.merge()].join("\n")»
			«IF nodesToReturn.size > 0»
				RETURN «FOR nc : nodesToReturn SEPARATOR ","»(«nc.id»)«ENDFOR»
			«ENDIF»
		'''
	}

	def void returnWith(NodeCommand... ncs) {
		nodesToReturn.addAll(ncs)
	}
}
