package org.emoflon.neo.neo4j.adapter

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.List
import java.util.Map
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.Session

class CypherCreator extends CypherBuilder {
	// Use to split into chunks
	int maxTransactionSizeNodes
	int maxTransactionSizeEdges

	Map<String, EdgeCommand> edgesToMatch

	List<NodeCommand> nodesToCreate
	Map<String, EdgeCommand> edgesToCreate

	new(int maxTransactionSizeNodes, int maxTransactionSizeEdges) {
		this.maxTransactionSizeNodes = maxTransactionSizeNodes
		this.maxTransactionSizeEdges = maxTransactionSizeEdges

		edgesToMatch = new HashMap

		// Avoid creating exactly the same edge multiple times between the same nodes
		edgesToCreate = new HashMap

		// The "same" node can be created multiple times!
		nodesToCreate = new ArrayList
	}

	def createNode(List<NeoProp> props, List<String> labels) {
		val nc = new NodeCommand(props, labels)
		nodesToCreate.add(nc)
		return nc
	}

	def createNodeWithCont(List<NeoProp> props, List<String> labels, NodeCommand container) {
		val nc = createNode(props, labels)
		createEdge(List.of, NeoCoreBuilder.META_EL_OF, nc, container)
		return nc
	}

	def createNodeWithContAndType(List<NeoProp> props, List<String> labels, NodeCommand type, NodeCommand container) {
		val nc = createNodeWithCont(props, labels, container)
		createEdge(List.of, NeoCoreBuilder.META_TYPE, nc, type)
		return nc
	}

	def matchNodeWithContainer(List<NeoProp> props, List<String> labels, NodeCommand container) {
		if (!nodesToMatch.values.contains(container))
			throw new IllegalArgumentException("A node's container must be matched!")

		val key = createKeyForNodeWithContainer(props, labels, container)
		if (nodesToMatch.containsKey(key))
			return nodesToMatch.get(key)
		else {
			val nc = new NodeCommand(props, labels)
			nodesToMatch.put(key, nc)
			matchEdge(List.of, NeoCoreBuilder.META_EL_OF, nc, container)
			return nc
		}
	}

	def String createKeyForNodeWithContainer(List<NeoProp> props, List<String> labels, NodeCommand container) {
		'''«props.join("-")»-«labels.join("-")»-«container.name»'''
	}

	def matchEdge(List<NeoProp> props, String label, NodeCommand from, NodeCommand to) {
		if (props === null)
			throw new IllegalArgumentException("Property list should not be null")

		val key = createKeyForEdge(props, label, from, to)
		if (edgesToMatch.containsKey(key))
			return edgesToMatch.get(key)
		else {
			val ec = new EdgeCommand(props, label, from, to)
			edgesToMatch.put(key, ec)
			return ec
		}
	}

	def String createKeyForEdge(List<NeoProp> props, String label, NodeCommand from, NodeCommand to) {
		'''«props.join("-")»-«label»-«from.name»-«to.name»'''
	}

	def createEdge(List<NeoProp> props, String label, NodeCommand from, NodeCommand to) {
		val key = createKeyForEdge(props, label, from, to)
		if (edgesToCreate.containsKey(key)) {
			return edgesToCreate.get(key)
		} else {
			val ec = new EdgeCommand(props, label, from, to)
			edgesToCreate.put(key, ec)
			return ec
		}
	}

	def void run(Driver driver) {
		val session = driver.session
		val nodesToIds = new HashMap<NodeCommand, Number>
		createGreenNodesInBatches(session, nodesToIds)
		createGreenEdgesInBatches(session, nodesToIds)
		session.close
	}

	def createGreenNodesInBatches(Session session, HashMap<NodeCommand, Number> nodesToIds) {
		val itr = nodesToCreate.iterator
		while (itr.hasNext) {
			val chosenNodes = itr.take(maxTransactionSizeNodes).toList
			val result = runCypherCommand(session, //
			'''
				CREATE 
				  «chosenNodes.map[n| n.node()].join(",\n")»
				RETURN 
				  «FOR n : chosenNodes SEPARATOR ",\n"»id(«n.name»)«ENDFOR»
			''')

			val record = result.next
			chosenNodes.forEach[n|nodesToIds.put(n, record.get('''id(«n.name»)''').asNumber)]
		}
	}

	def createGreenEdgesInBatches(Session session, HashMap<NodeCommand, Number> nodesToIds) {
		val itr = edgesToCreate.values.iterator
		while (itr.hasNext) {
			val edges = itr.take(maxTransactionSizeEdges).toList
			runCypherCommand(session, //
			'''
				«matchRequiredExistingNodes(edges)»
				«matchRequiredCreatedNodes(edges, nodesToIds)»
				CREATE 
				  «edges.map[e| e.edge].join(",\n")»
			''')
		}
	}

	def matchRequiredCreatedNodes(List<EdgeCommand> edges, HashMap<NodeCommand, Number> nodesToIds) {
		val requiredNodes = new ArrayList
		edges.forEach [ e |
			requiredNodes.add(e.from)
			requiredNodes.add(e.to)
		]

		val commands = new HashSet<String>
		val where = new HashSet<String>
		for (rn : requiredNodes) {
			if (nodesToCreate.contains(rn)) {
				commands.add('''(«rn.name»)''')
				if (!nodesToIds.containsKey(rn))
					throw new IllegalArgumentException("Found no id for :" + rn.name)

				where.add('''id(«rn.name») = «nodesToIds.get(rn)»''')
			}
		}

		if (commands.size > 0)
			'''
				MATCH 
				  «commands.join(",\n")»
				WHERE 
				  «where.join(" AND\n")»
			'''
		else
			""
	}

	def matchRequiredExistingNodes(List<EdgeCommand> edges) {
		val requiredNodes = new ArrayList
		edges.forEach [ e |
			requiredNodes.add(e.from)
			requiredNodes.add(e.to)
		]

		val commands = new HashSet<String>
		for (rn : requiredNodes) {
			if (nodesToMatch.values.contains(rn))
				commands.add(rn.node)
		}

		if (commands.size > 0)
			'''
				MATCH 
				  «commands.join(",\n")»
			'''
		else
			""
	}

}
