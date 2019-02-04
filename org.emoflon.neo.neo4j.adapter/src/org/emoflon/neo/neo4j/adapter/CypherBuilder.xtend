package org.emoflon.neo.neo4j.adapter

import java.util.ArrayList
import java.util.List

class CypherBuilder {
	List<NodeCommand> nodesToMatch
	List<EdgeCommand> edgesToMatch

	List<NodeCommand> nodesToCreate
	List<EdgeCommand> edgesToCreate

	List<NodeCommand> nodesToReturn;

	new() {
		nodesToMatch = new ArrayList
		edgesToMatch = new ArrayList

		nodesToCreate = new ArrayList
		edgesToCreate = new ArrayList

		nodesToReturn = new ArrayList
	}

	def matchNode() {
		val nc = new NodeCommand()
		nodesToMatch.add(nc)
		return nc
	}

	def matchEdge() {
		val ec = new EdgeCommand()
		edgesToMatch.add(ec)
		return ec
	}

	def createNode() {
		val nc = new NodeCommand()
		nodesToCreate.add(nc)
		return nc
	}

	def createEdge() {
		val ec = new EdgeCommand()
		edgesToCreate.add(ec)
		return ec
	}

	def String buildCommand() {
		''' 
			«nodesToMatch.map[n| n.match()].join("\n")»
			«edgesToMatch.map[e| e.match()].join("\n")»
			«nodesToCreate.map[n| n.create()].join("\n")»
			«edgesToCreate.map[e| e.create()].join("\n")»
			«IF nodesToReturn.size > 0»
				RETURN «FOR nc : nodesToReturn SEPARATOR ","»
						(«nc.id»)
				«ENDFOR»
			«ENDIF»
		'''
	}

	def void returnWith(NodeCommand... ncs) {
		nodesToReturn.addAll(ncs)
	}
}

abstract class ElementCommand {
	protected val List<String> properties = new ArrayList

	static int _id = 0
	val int id = _id++

	def id() {
		"_" + id
	}

	protected def addProperty(String prop, String value) {
		properties.add(prop + ":" + value)
	}

	protected def addStringProperty(String prop, String value) {
		properties.add(prop + ":" + "\"" + value + "\"")
	}
}

class EdgeCommand extends ElementCommand {
	var String label
	var NodeCommand from
	var NodeCommand to

	def from(NodeCommand nc) {
		from = nc
		return this
	}

	def to(NodeCommand nc) {
		to = nc
		return this
	}

	def withLabel(String label) {
		this.label = label
		return this
	}

	def withProperty(String prop, String value) {
		addProperty(prop, value)
		return this
	}

	def withStringProperty(String prop, String value) {
		addStringProperty(prop, value)
		return this
	}

	def match() {
		'''
			MATCH («from.id»)-[«id()»:«label» {«properties.join(", ")»}]->(«to.id»)
		'''
	}

	def create() {
		'''
			CREATE («from.id»)-[«id()»:«label» {«properties.join(", ")»}]->(«to.id»)
		'''
	}
}

class NodeCommand extends ElementCommand {
	val List<String> labels = new ArrayList

	var EdgeCommand typeOf

	var EdgeCommand elOf

	val String META_TYPE = "_type_"
	val String META_EL_OF = "_elementOf_"

	def withType(NodeCommand type) {
		typeOf = new EdgeCommand().withLabel(META_TYPE).from(this).to(type)
		return this
	}

	def elementOf(NodeCommand model) {
		elOf = new EdgeCommand().withLabel(META_EL_OF).from(this).to(model)
		return this
	}

	def withLabel(String label) {
		labels.add(label)
		return this
	}

	def withProperty(String prop, String value) {
		addProperty(prop, value)
		return this
	}

	def withStringProperty(String prop, String value) {
		addStringProperty(prop, value)
		return this
	}

	def match() {
		'''
			MATCH («id()»:«labels.join(":")» {«properties.join(", ")»})
			«typeOf?.create»
			«elOf?.create»
		'''
	}

	def create() {
		'''
			CREATE («id()»:«labels.join(":")» {«properties.join(", ")»})
			«typeOf?.create»
			«elOf?.create»
		'''
	}
}
