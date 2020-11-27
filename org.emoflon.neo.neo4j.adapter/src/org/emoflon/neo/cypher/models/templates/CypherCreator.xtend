package org.emoflon.neo.cypher.models.templates

import java.util.ArrayList
import java.util.HashMap
import java.util.List
import java.util.Map
import org.neo4j.driver.Driver
import org.neo4j.driver.Session
import org.emoflon.neo.neocore.util.NeoCoreConstants

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

	def createNode(List< NeoProp> props, List<String> labels) {
		val nc = new  NodeCommand(props, labels)
		nodesToCreate.add(nc)
		return nc
	}

	def createNodeWithType(List< NeoProp> props, List<String> labels,  NodeCommand type) {
		val nc = createNode(props, labels)
		createEdge(NeoCoreConstants.META_TYPE, nc, type)
		return nc
	}

	def matchNodeWithContainer(List< NeoProp> props, List<String> labels,  String container) {
		val key = createKeyForNodeWithContainer(props, labels, container)
		if (nodesToMatch.containsKey(key))
			return nodesToMatch.get(key)
		else {
			val nc = new  NodeCommand(props, labels.subList(0, 1))
			nodesToMatch.put(key, nc)
			return nc
		}
	}

	private def String createKeyForNodeWithContainer(List< NeoProp> props, List<String> labels,  String container) {
		'''«props.join("-")»-«labels.join("-")»-«container»'''
	}

	private def String createKeyForEdge(List< NeoProp> props, String label,  NodeCommand from,  NodeCommand to) {
		'''«props.join("-")»-«label»-«from.name»-«to.name»'''
	}

	def createEdge(String label,  NodeCommand from,  NodeCommand to) {
		createEdgeWithProps(List.of, label, from, to)
	}

	def createEdgeWithProps(List< NeoProp> props, String label,  NodeCommand from,  NodeCommand to) {
		val key = createKeyForEdge(props, label, from, to)
		if (edgesToCreate.containsKey(key)) {
			return edgesToCreate.get(key)
		} else {
			val ec = new  EdgeCommand(props, label, from, to)
			edgesToCreate.put(key, ec)
			return ec
		}
	}

	def void run(Driver driver) {
		val session = driver.session
		val nodesToIds = new HashMap< NodeCommand, Number>
		matchNodesAndEdges(session, nodesToIds)
		createGreenNodesAccordingToLabels(session, nodesToIds)
		createGreenEdgesAccordingToLabels(session, nodesToIds)
		session.close
	}

	private def createGreenNodesAccordingToLabels(Session session, HashMap< NodeCommand, Number> nodesToIds) {
		val nodesGroupedByLabel = nodesToCreate.groupBy[n|n.labels.join(":")]
		for (nodesWithSameLabel : nodesGroupedByLabel.entrySet)
			createGreenNodesInBatches(session, nodesWithSameLabel.key, nodesWithSameLabel.value, nodesToIds)
	}

	private def createGreenEdgesAccordingToLabels(Session session, HashMap< NodeCommand, Number> nodesToIds) {
		val edgesGroupedByLabel = edgesToCreate.values.groupBy[e|e.label]
		for (edgesWithSameLabel : edgesGroupedByLabel.entrySet)
			createGreenEdgesInBatches(session, edgesWithSameLabel.key, edgesWithSameLabel.value, nodesToIds)
	}

	private def matchNodesAndEdges(Session session, HashMap< NodeCommand, Number> nodesToIds) {
		if (nodesToMatch.empty && edgesToMatch.empty)
			return

		val result = runCypherCommand(session, //
		''' 
			MATCH
			  «FOR n : nodesToMatch.values SEPARATOR ",\n"»«n.node»«ENDFOR»
			  «FOR e : edgesToMatch.values BEFORE "," SEPARATOR ",\n"»«e.edge»«ENDFOR»
			RETURN 
			  «FOR n : nodesToMatch.values SEPARATOR ",\n"»id(«n.name»)«ENDFOR»
		''')
		val record = result.next
		nodesToMatch.values.forEach[n|nodesToIds.put(n, record.get('''id(«n.name»)''').asNumber)]
	}

	private def createGreenNodesInBatches(Session session, String labels, List< NodeCommand> nodesWithSameLabel,
		HashMap< NodeCommand, Number> nodesToIds) {
		val itr = nodesWithSameLabel.iterator
		while (itr.hasNext) {
			val chosenNodes = itr.take(maxTransactionSizeNodes).toList
			val params = new HashMap<String, Object>
			val nodeParams = new ArrayList<HashMap<String, Object>>
			chosenNodes.forEach [ n |
				val nodeParam = new HashMap<String, Object>
				nodeParam.put("props", n.properties.toMap([p|p.key], [p|p.value]))
				nodeParam.put("name", n.name)
				nodeParams.add(nodeParam)
			]
			params.put("nodes", nodeParams)
			val result = runCypherCommand(session, //
			'''
				UNWIND $nodes AS node
				CREATE 
				  (n:«labels»)
				  SET n = node.props
				RETURN 
				  id(n)
			''', params)

			chosenNodes.forEach[n|nodesToIds.put(n, result.next.get("id(n)").asNumber)]
		}
	}

	private def createGreenEdgesInBatches(Session session, String label, List< EdgeCommand> edgesWithSameLabel,
		HashMap< NodeCommand, Number> nodesToIds) {
		val itr = edgesWithSameLabel.iterator
		while (itr.hasNext) {
			val edges = itr.take(maxTransactionSizeEdges).toList
			val params = new HashMap<String, Object>
			val edgeParams = new ArrayList<HashMap<String, Object>>
			edges.forEach [ e |
				if (!nodesToIds.containsKey(e.from) || !nodesToIds.containsKey(e.to))
					throw new IllegalStateException('''Found no ids for: " + «e.from» and «e.to»''')

				val edgeParam = new HashMap<String, Object>
				edgeParam.put("srcId", nodesToIds.get(e.from))
				edgeParam.put("trgId", nodesToIds.get(e.to))
				edgeParam.put("props", e.properties.toMap([p|p.key], [p|p.value]))
				edgeParams.add(edgeParam)
			]
			params.put("edges", edgeParams)
			runCypherCommand(session, //
			'''
				UNWIND $edges AS edge
				MATCH
				  (src),
				  (trg)
				WHERE
				  id(src) = edge.srcId AND
				  id(trg) = edge.trgId
				CREATE 
				  (src)-[e:«label»]->(trg)
				  SET e = edge.props
			''', params)
		}
	}
}
