package org.emoflon.neo.neo4j.adapter

import java.util.ArrayList
import java.util.List

class CypherBuilder {
	List<NodeCommand> nodesToMatch
	List<EdgeCommand> edgesToMatch

	List<NodeCommand> nodesToCreate
	List<EdgeCommand> edgesToCreate

	List<NodeCommand> nodesToMerge
	List<EdgeCommand> edgesToMerge

	List<NodeCommand> nodesToReturn;

	new() {
		nodesToMatch = new ArrayList
		edgesToMatch = new ArrayList

		nodesToCreate = new ArrayList
		edgesToCreate = new ArrayList

		nodesToMerge = new ArrayList
		edgesToMerge = new ArrayList

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

	def mergeNode() {
		val nc = new NodeCommand()
		nodesToMerge.add(nc)
		return nc
	}

	def mergeEdge() {
		val ec = new EdgeCommand()
		edgesToMerge.add(ec)
		return ec
	}

	def String buildCommand() {
		println(nodesToMatch.size)
		println(nodesToMatch.toSet.size)

		''' 
			«nodesToMatch.toSet.map[n| n.match()].join("\n")»
			«edgesToMatch.toSet.map[e| e.match()].join("\n")»
			«nodesToCreate.map[n| n.create()].join("\n")»
			«edgesToCreate.map[e| e.create()].join("\n")»
			«nodesToMerge.map[n| n.merge()].join("\n")»
			«edgesToMerge.map[e| e.merge()].join("\n")»
			«IF nodesToReturn.size > 0»
				RETURN «FOR nc : nodesToReturn SEPARATOR ","»(«nc.matchId»)«ENDFOR»
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

	def createId() {
		"_" + id
	}
	
	def matchId() {
		("_" + hashCode).replace("-", "_")
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

	def getTo() {
		to
	}

	def getFrom() {
		from
	}

	def from(NodeCommand nc) {
		if (nc === null)
			throw new IllegalArgumentException("Source node command cannot be null!")
		from = nc
		return this
	}

	def to(NodeCommand nc) {
		if (nc === null)
			throw new IllegalArgumentException("Target node command cannot be null!")
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
			MATCH («from.matchId»)-[«matchId()»:«label» {«properties.join(", ")»}]->(«to.matchId»)
		'''
	}

	def create() {
		'''
			CREATE («from.createId»)-[«createId()»:«label» {«properties.join(", ")»}]->(«to.createId»)
		'''
	}

	def merge() {
		'''
			MERGE («from.createId»)-[«createId()»:«label» {«properties.join(", ")»}]->(«to.createId»)
		'''
	}

	override equals(Object other) {
		if (other instanceof EdgeCommand) {
			return label.equals(other.label) && from.equals(other.from) && to.equals(other.to) &&
				properties.equals(other.properties)
		} else
			return false
	}

	override hashCode() {
		return 13 * label?.hashCode + 17 * from?.hashCode + 23 * to?.hashCode + 29 * properties?.hashCode
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
			MATCH («matchId()»:«labels.join(":")» {«properties.join(", ")»})
			«typeOf?.match»
			«elOf?.match»
		'''
	}

	def create() {
		'''
			CREATE («createId()»:«labels.join(":")» {«properties.join(", ")»})
			«typeOf?.create»
			«elOf?.create»
		'''
	}

	def merge() {
		'''
			MERGE («createId()»:«labels.join(":")» {«properties.join(", ")»})
			«typeOf?.merge»
			«elOf?.merge»
		'''
	}

	override equals(Object other) {
		if (other === this)
			return true;

		if (other instanceof NodeCommand) {
			var type = typeOf?.to
			val model = elOf?.to

			if (this === type) {
				type = null
			}

			var areEqual = labels.equals(other.labels) && properties.equals(other.properties)

			if (type !== null)
				areEqual = areEqual && type.equals(other.typeOf?.to)

			if(model !== null)
				areEqual = areEqual && model.equals(other.elOf?.to)

			return areEqual
		} else
			return false
	}

	override hashCode() {
		var type = typeOf?.to
		val model = elOf?.to

		if (this === type) {
			type = null
		}

		return 13 * labels?.hashCode + 17 * type?.hashCode + 23 * model?.hashCode + 29 * properties?.hashCode
	}
}
