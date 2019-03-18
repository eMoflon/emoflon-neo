package org.emoflon.neo.neo4j.adapter

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.List
import java.util.Map
import org.neo4j.driver.v1.Driver
import org.neo4j.driver.v1.Session

class CypherCreator extends CypherBuilder {
	public static final String PENDING_LABEL = "PENDING"
	public static final String TEMP_ID_PROP = "_id"

	// Use to split into chunks
	int maxTransactionSize

	Map<String, EdgeCommand> edgesToMatch

	List<NodeCommand> nodesToCreate
	Map<String, EdgeCommand> edgesToCreate

	new(int maxTransactionSize) {
		this.maxTransactionSize = maxTransactionSize

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
		'''«props.join("-")»-«labels.join("-")»-«container.id»'''
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
		'''«props.join("-")»-«label»-«from.id»-«to.id»'''
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
		createIndex(session)
		createGreenNodesInBatches(session)
		createGreenEdgesInBatches(session)
		deleteIds(session)
		dropIndex(session)
		session.close
	}

	def createIndex(Session session) {
		runCypherCommand(session, '''CREATE INDEX ON :«PENDING_LABEL»(«TEMP_ID_PROP»)''')
	}

	def waitForIndex(Session session) {
		runCypherCommand(session, '''CALL db.awaitIndexes''')
	}

	def createGreenNodesInBatches(Session session) {
		val itr = nodesToCreate.iterator
		while (itr.hasNext)
			runCypherCommand(session, '''CREATE «itr.take(maxTransactionSize).map[n| n.node()].join(",")»''')
	}

	def createGreenEdgesInBatches(Session session) {
		val itr = edgesToCreate.values.iterator
		while (itr.hasNext) {
			val edges = itr.take(maxTransactionSize).toList
			runCypherCommand(session, '''
			«matchRequiredNodes(edges)»
			CREATE «edges.map[e| e.edge].join(",")»''')
		}
	}

	def matchRequiredNodes(List<EdgeCommand> edges) {
		val requiredNodes = new ArrayList
		edges.forEach [ e |
			requiredNodes.add(e.from)
			requiredNodes.add(e.to)
		]

		val commands = new HashSet<String>
		for (rn : requiredNodes) {
			if (nodesToMatch.values.contains(rn))
				commands.add(rn.node)
			else
				commands.add('''(«rn.id»:«CypherCreator.PENDING_LABEL» {«CypherCreator.TEMP_ID_PROP»:"«rn.id»"})''')
		}

		if (commands.size > 0)
			'''MATCH «commands.join(", ")»'''
		else
			""
	}

	def deleteIds(Session session) {
		runCypherCommand(
			session,
			'''
			MATCH (n:«PENDING_LABEL»)
			REMOVE n.«TEMP_ID_PROP»
			REMOVE n:«PENDING_LABEL»'''
		)
	}

	def dropIndex(Session session) {
		runCypherCommand(session, '''DROP INDEX ON :«PENDING_LABEL»(«TEMP_ID_PROP»)''')
	}
}
