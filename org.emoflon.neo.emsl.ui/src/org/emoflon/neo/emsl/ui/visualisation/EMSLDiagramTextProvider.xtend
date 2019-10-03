package org.emoflon.neo.emsl.ui.visualisation

import java.util.ArrayList
import java.util.HashMap
import java.util.HashSet
import java.util.List
import java.util.Optional
import net.sourceforge.plantuml.eclipse.utils.DiagramTextProvider
import org.eclipse.jface.text.TextSelection
import org.eclipse.jface.viewers.ISelection
import org.eclipse.ui.IEditorPart
import org.eclipse.xtext.EcoreUtil2
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.ui.editor.XtextEditor
import org.emoflon.neo.emsl.eMSL.ActionOperator
import org.emoflon.neo.emsl.eMSL.AndBody
import org.emoflon.neo.emsl.eMSL.AtomicPattern
import org.emoflon.neo.emsl.eMSL.AttributeExpression
import org.emoflon.neo.emsl.eMSL.BinaryExpression
import org.emoflon.neo.emsl.eMSL.BuiltInType
import org.emoflon.neo.emsl.eMSL.Constraint
import org.emoflon.neo.emsl.eMSL.ConstraintBody
import org.emoflon.neo.emsl.eMSL.ConstraintReference
import org.emoflon.neo.emsl.eMSL.EMSL_Spec
import org.emoflon.neo.emsl.eMSL.Entity
import org.emoflon.neo.emsl.eMSL.Enum
import org.emoflon.neo.emsl.eMSL.EnumValue
import org.emoflon.neo.emsl.eMSL.GraphGrammar
import org.emoflon.neo.emsl.eMSL.Implication
import org.emoflon.neo.emsl.eMSL.LinkAttributeExpTarget
import org.emoflon.neo.emsl.eMSL.Metamodel
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock
import org.emoflon.neo.emsl.eMSL.MetamodelRelationStatement
import org.emoflon.neo.emsl.eMSL.Model
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock
import org.emoflon.neo.emsl.eMSL.NegativeConstraint
import org.emoflon.neo.emsl.eMSL.NodeAttributeExpTarget
import org.emoflon.neo.emsl.eMSL.OrBody
import org.emoflon.neo.emsl.eMSL.Pattern
import org.emoflon.neo.emsl.eMSL.PositiveConstraint
import org.emoflon.neo.emsl.eMSL.PrimitiveBoolean
import org.emoflon.neo.emsl.eMSL.PrimitiveInt
import org.emoflon.neo.emsl.eMSL.PrimitiveString
import org.emoflon.neo.emsl.eMSL.RefinementCommand
import org.emoflon.neo.emsl.eMSL.RelationKind
import org.emoflon.neo.emsl.eMSL.Rule
import org.emoflon.neo.emsl.eMSL.SourceNAC
import org.emoflon.neo.emsl.eMSL.SuperType
import org.emoflon.neo.emsl.eMSL.TripleGrammar
import org.emoflon.neo.emsl.eMSL.TripleRule
import org.emoflon.neo.emsl.eMSL.UserDefinedType
import org.emoflon.neo.emsl.refinement.EMSLFlattener
import org.emoflon.neo.emsl.ui.util.ConstraintTraversalHelper
import org.emoflon.neo.emsl.util.EntityAttributeDispatcher
import org.emoflon.neo.emsl.util.FlattenerException
import org.emoflon.neo.emsl.eMSL.PrimitiveDouble
import org.apache.commons.lang3.StringUtils
import org.emoflon.neo.emsl.eMSL.TargetNAC

class EMSLDiagramTextProvider implements DiagramTextProvider {
	static final int MAX_SIZE = 500

	override String getDiagramText(IEditorPart editor, ISelection selection) {
		var Optional<String> diagram = Optional.empty()
		try {
			var String d = getDiagramBody(editor, selection)
			if(d === null)
				diagram = Optional.of(errorDiagram)
			if (d.split("\n").length > MAX_SIZE)
				diagram = Optional.of(tooBigDiagram)
			else
				diagram = Optional.of(d)
		} catch (FlattenerException e) {
			e.printStackTrace()
			return wrapInTags('''title The entity could not be flattened.''')
		} catch (Exception e) {
			e.printStackTrace()
		}

		return wrapInTags(diagram.orElse(errorDiagram))
	}

	def String wrapInTags(String body) {
		'''
			@startuml
			«plantUMLPreamble»
			«body»
			@enduml
		'''
	}

	def String emptyDiagram() {
		'''
			title Choose an element that can be visualised
		'''
	}

	def String errorDiagram() {
		'''
			title I'm having problems visualising the current selection (check your editor and console).
		'''
	}

	def String tooBigDiagram() {
		'''
			title This diagram would be so big, trying to render it would fry your Eclipse instance
		'''
	}

	def String getDiagramBody(IEditorPart editor, ISelection selection) {
		val EMSL_Spec root = getRoot(editor).get() as EMSL_Spec
		val Optional<Entity> selectedEntity = determineSelectedEntity(selection, root)
		val Optional<ModelNodeBlock> selectedNodeBlock = selectedEntity.flatMap([ e |
			determineSelectedNodeBlock(selection, e)
		])
		val Optional<MetamodelNodeBlock> selectedMetamodelNodeBlock = selectedEntity.flatMap([ e |
			determineSelectedMetamodelNodeBlock(selection, e)
		])

		if (selectedEntity.isPresent && selectedEntity.get instanceof Enum)
			return visualiseEnumLiterals(selectedEntity.get as Enum)

		if (selectedMetamodelNodeBlock.isPresent)
			return visualiseNodeBlockInMetamodel(selectedMetamodelNodeBlock.get, true)

		if (!selectedEntity.isPresent)
			return visualiseOverview(root)

		if (!selectedNodeBlock.isPresent)
			return visualiseEntity(selectedEntity.get, true)

		visualiseNodeBlock(selectedNodeBlock.get, true)
	}

	/**
	 * Returns the diagram text for the different cases of NodeBlocks appearing.
	 */
	def visualiseNodeBlock(ModelNodeBlock nb, boolean mainSelection) {
		if (nb.eContainer instanceof Model)
			visualiseNodeBlockInModel(nb, mainSelection)
		else if (nb.eContainer instanceof AtomicPattern)
			return visualiseNodeBlockInPattern(nb, mainSelection) +
				visualiseCondition(nb.eContainer.eContainer as Pattern)
		else if (nb.eContainer instanceof Rule)
			return visualiseNodeBlockInRule(nb, mainSelection) + visualiseCondition(nb.eContainer as Rule)
		else if (nb.eContainer instanceof TripleRule)
			visualiseNodeBlockInTripleRule(nb.eContainer as TripleRule, nb, mainSelection)
		else if (nb.eContainer instanceof GraphGrammar)
			visualiseNodeBlockInRule(nb, mainSelection)
	}

	/**
	 * Returns the diagram text for the overview of the current file, i.e. when no entity is selected.
	 */
	def String visualiseOverview(EMSL_Spec root) {
		val sortedEntities = sortEntities(root.entities)
		'''
			left to right direction
			«FOR type : sortedEntities.keySet»
			«IF !sortedEntities.get(type).empty»
			package «type» {
				«FOR entity : sortedEntities.get(type)»
					«IF entity instanceof Metamodel»
						rectangle "Metamodel: «entity.name»" as Metamodels«entity.name»  <<Rectangle>> «link(entity)»
					«ENDIF»
					«IF entity instanceof Model»
						rectangle "Model: «IF entity.abstract»//«ENDIF»«entity.name»«IF entity.abstract»//«ENDIF»" as Models«entity.name»  «IF entity.abstract»<<Abstract>>«ENDIF» <<Rectangle>> «link(entity)»
						«referenceInstantiatedMetamodel(entity as Model)»
					«ENDIF»
					«IF entity instanceof Pattern»
						rectangle "Pattern: «IF entity.body.abstract»//«ENDIF»«entity.body.name»«IF entity.body.abstract»//«ENDIF»" as Patterns«entity.name»  «IF entity.body.abstract»<<Abstract>>«ENDIF» <<Rectangle>> «link(entity)»
					«ENDIF»
					«IF entity instanceof Rule»
						rectangle "Rule: «IF entity.abstract»//«ENDIF»«entity.name»«IF entity.abstract»//«ENDIF»" as Rules«entity.name»  «IF entity.abstract»<<Abstract>>«ENDIF» <<Rectangle>> «link(entity)»
					«ENDIF»
					«IF entity instanceof TripleRule»
						rectangle "TripleRule: «IF entity.abstract»//«ENDIF»«entity.name»«IF entity.abstract»//«ENDIF»" as TripleRules«entity.name»  «IF entity.abstract»<<Abstract>>«ENDIF» <<Rectangle>> «link(entity)»
					«ENDIF»
					«IF entity instanceof TripleGrammar»
						rectangle "TripleGrammar: «IF entity.abstract»//«ENDIF»«entity.name»«IF entity.abstract»//«ENDIF»" as TripleGrammars«entity.name»  «IF entity.abstract»<<Abstract>>«ENDIF» <<Rectangle>> «link(entity)»
					«ENDIF»
					«IF entity instanceof GraphGrammar»
						rectangle "GraphGrammar: «IF entity.abstract»//«ENDIF»«entity.name»«IF entity.abstract»//«ENDIF»" as GraphGrammars«entity.name»  «IF entity.abstract»<<Abstract>>«ENDIF» <<Rectangle>> «link(entity)»
					«ENDIF»
					«IF entity instanceof Constraint»
						rectangle "Constraint: «IF entity.abstract»//«ENDIF»«entity.name»«IF entity.abstract»//«ENDIF»" as Constraint«entity.name»  «IF entity.abstract»<<Abstract>>«ENDIF» <<Rectangle>> «link(entity)»
					«ENDIF»
				«ENDFOR»
			}
			«ENDIF»
			«ENDFOR»
			«FOR entity : root.entities»
				«IF entity instanceof SuperType»
					«visualiseSuperTypesInEntity(entity)»
				«ENDIF»
				«IF entity instanceof Pattern»
					«visualiseSuperTypesInEntity(entity)»
				«ENDIF»
			«ENDFOR»
		'''
	}

	/*-------------------------------------------------*/
	/*------------------- Models ----------------------*/
	/*-------------------------------------------------*/
	/**
	 * Returns the diagram text for a Model.
	 */
	def dispatch String visualiseEntity(Model entity, boolean mainSelection) {
		var entityCopy = EMSLFlattener.flatten(entity)
		'''
			package "«IF entity.abstract»//«ENDIF»«(entityCopy as Model).name»«IF entity.abstract»//«ENDIF»"«IF mainSelection» <<Selection>> «ENDIF»{
			«FOR nb : entityCopy.nodeBlocks»
				«visualiseNodeBlockInModel(nb, false)»
			«ENDFOR»
			}
		'''
	}

	/**
	 * Returns the diagram text for a NodeBlock in a Model.
	 */
	def String visualiseNodeBlockInModel(ModelNodeBlock nodeBlock, boolean mainSelection) {
		var node = nodeBlock
		for (n : (new EntityAttributeDispatcher().getNodeBlocks((EMSLFlattener.flatten(nodeBlock.eContainer as SuperType))))) {
			if (nodeBlock.name.equals(n.name))
				node = n
		}
		val nb = node
		
		var sizeOfTypeList = 0
		var sizeOfIncomingRefTypeList = 0
		'''
			class «labelForObject(nb)» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR link : nb.relations»«{sizeOfTypeList = link.types.size - 1;""}»
				«labelForObject(nb)» --> «IF link.target !== null»«labelForObject(link.target)»«ELSE»"?"«ENDIF» : "«FOR t : link.types»«IF (t.type as MetamodelRelationStatement).name !== null && t.type !== null»«(t.type as MetamodelRelationStatement).name»«ELSE»?«ENDIF»«IF sizeOfTypeList > 0» | «ENDIF»«{sizeOfTypeList = sizeOfTypeList - 1;""}»«ENDFOR»«IF (link.lower !== null && link.upper !== null)»(«link.lower»..«link.upper»)«ENDIF»"
			«ENDFOR»
			«FOR attr : nb.properties»
				«labelForObject(nb)» : «IF attr?.type?.name !== null»«attr.type.name»«ELSE»?«ENDIF» = «IF attr?.value !== null»«printValue(attr.value)»«ELSE»?«ENDIF»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as Model).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relations»«{sizeOfIncomingRefTypeList = incomingRef.types.size - 1;""}»
					«IF incomingRef.target == nb && mainSelection»
						«labelForObject(incoming)» --> «labelForObject(nb)» : "«FOR t : incomingRef.types»«IF (t.type as MetamodelRelationStatement).name !== null && t.type !== null»«(t.type as MetamodelRelationStatement).name»«ELSE»?«ENDIF»«IF sizeOfIncomingRefTypeList > 0» | «ENDIF»«{sizeOfIncomingRefTypeList = sizeOfIncomingRefTypeList - 1;""}»«ENDFOR»«IF (incomingRef.lower !== null && incomingRef.upper !== null)»(«incomingRef.lower»..«incomingRef.upper»)«ENDIF»"
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
		'''
	}
	
	dispatch def printValue(Object o){
		'''?'''
	}

	dispatch def String printValue(BinaryExpression value){
		'''«printValue(value?.left)» «value?.op» «printValue(value?.right)»'''
	}

	dispatch def printValue(AttributeExpression value) {
		'''«value?.node?.name».«printTarget(value?.target)»'''
	}

	dispatch def printTarget(NodeAttributeExpTarget target) {
		'''«IF target !== null && target.attribute !== null»«target.attribute.name»«ELSE»?«ENDIF»'''
	}

	dispatch def printTarget(LinkAttributeExpTarget target) {
		'''-"«IF target !== null»«target.link.name»"->.«target.attribute.name»«ELSE»?«ENDIF»'''
	}

	dispatch def printValue(EnumValue value) {
		'''«value?.literal?.name»'''
	}

	dispatch def printValue(PrimitiveInt value) {
		'''«value.literal»'''
	}

	dispatch def printValue(PrimitiveString value) {
		if (value.eContainer.eContainer instanceof ModelNodeBlock)
			'''«StringUtils.abbreviate(value.literal, (value.eContainer.eContainer as ModelNodeBlock).name.length * 2 + 3)»'''
		else if (value.eContainer.eContainer.eContainer instanceof ModelNodeBlock) {
			'''«StringUtils.abbreviate(value.literal, (value.eContainer.eContainer.eContainer as ModelNodeBlock).name.length * 2 + 3)»'''
		}
	}

	dispatch def printValue(PrimitiveBoolean value) {
		'''«value.^true»'''
	}
	
	dispatch def printValue(PrimitiveDouble value) {
		'''«value.literal»'''
	}

	/**
	 * Returns the diagram text for the name of an object in a Model.
	 */
	private def labelForObject(ModelNodeBlock nb) {
		val entity = nb?.eContainer as Model
		'''"«IF entity !== null && entity.abstract»//«ENDIF»«IF entity?.name !== null»«entity?.name»«ELSE»?«ENDIF»«IF entity !== null && entity.abstract»//«ENDIF».«IF nb?.name !== null»«nb?.name»«ELSE»?«ENDIF»:«IF nb?.type?.name !== null»«nb?.type?.name»«ELSE»?«ENDIF»"'''
	}
	
	/**
	 * Returns the diagram text for the reference of a Model to the Metamodel it instantiates, i.e. which types the NodeBlocks of the Model use.
	 */
	private def referenceInstantiatedMetamodel(Model model) {
		var root = EcoreUtil2.getRootContainer(model)
		var allMetamodels = EcoreUtil2.getAllContentsOfType(root, Metamodel)
		if (!model.nodeBlocks.isEmpty) {
			for (nb : model.nodeBlocks) {
				for (i : allMetamodels) {
					if (i.nodeBlocks.contains(nb.type))
						return '''Models«model.name» --> Metamodels«i.name»'''
				}
			}
		}
		else {
			''''''
		}
	}
	
	/*-------------------------------------------------*/
	/*----------------- Metamodels --------------------*/
	/*-------------------------------------------------*/
	/**
	 * Returns the diagram text for a Metamodel.
	 */
	def dispatch String visualiseEntity(Metamodel mm, boolean mainSelection) {
		'''
			package "«(mm as Metamodel).name»"«IF mainSelection» <<Selection>> «ENDIF»{
			«FOR nb : mm.nodeBlocks»
				«visualiseNodeBlockInMetamodel(nb, false)»
			«ENDFOR»
			«FOR e : mm.enums»
				«visualiseEnumInMetamodel(e, false)»
			«ENDFOR»
			}
		'''
	}

	/**
	 * Returns the diagram text for a NodeBlock in a Metamodel.
	 */
	def String visualiseNodeBlockInMetamodel(MetamodelNodeBlock nb, boolean mainSelection) {
		'''
			«IF nb.abstract»abstract«ENDIF» class «labelForClass(nb)» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR sup : nb.superTypes»
				«labelForClass(sup)» <|-- «labelForClass(nb)»
			«ENDFOR»
			«FOR ref : nb.relations»
				«labelForClass(nb)» «IF ref.kind == RelationKind.COMPOSITION»*«ENDIF»«IF ref.kind == RelationKind.AGGREGATION»o«ENDIF»--> «IF ref.lower !== null»«visualiseMultiplicity(ref)»«ENDIF» «IF ref.target !== null»«labelForClass(ref.target)»«ELSE»"?"«ENDIF» : «IF ref.name !== null»«ref.name»«ELSE»?«ENDIF»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as Metamodel).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relations»
					«IF incomingRef.target == nb && mainSelection»
						«labelForClass(incoming)» «IF incomingRef.kind == RelationKind.COMPOSITION»*«ENDIF»«IF incomingRef.kind == RelationKind.AGGREGATION»o«ENDIF»--> «IF incomingRef.lower !== null»«visualiseMultiplicity(incomingRef)»«ENDIF» «labelForClass(nb)» : «IF (incomingRef.name !== null)»«incomingRef.name»«ELSE»?«ENDIF»
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
			«FOR attr : nb.properties»
				«labelForClass(nb)» : «attr.name» : «IF (attr.type instanceof UserDefinedType)»«((attr.type as UserDefinedType).reference).name»«ELSE»«(attr.type as BuiltInType).reference.toString»«ENDIF»
			«ENDFOR»
		'''
	}
	
	def String visualiseEnumInMetamodel(Enum e, boolean mainSelection) {
		'''
			class "«(e.eContainer as Metamodel).name».«e.name»" «IF mainSelection»<<Selection>>«ENDIF» {
				«FOR literal : e.literals»
					«literal.name»
				«ENDFOR»
			}
		'''
	}

	/**
	 * Returns the diagram text for the name of a class in a Metamodel.
	 */
	private def labelForClass(MetamodelNodeBlock nb) {
		val entity = nb?.eContainer as Metamodel
		'''"«entity?.name».«nb?.name»"'''
	}

	/**
	 * Returns the diagram text for multiplicities in a Metamodel.
	 */
	def visualiseMultiplicity(MetamodelRelationStatement link) {
		'''"«link.lower»«IF link.upper !== null»..«link.upper»«ENDIF»"'''
	}

	/**
	 * Returns the diagram text for an Enum.
	 */
	def dispatch String visualiseEntity(Enum entity, boolean mainSelection) {
		'''
			«FOR item : entity.literals»
				class "«entity.name»"
			«ENDFOR»
		'''
	}

	/**
	 * Returns the diagram text for the EnumLiterals.
	 */
	def String visualiseEnumLiterals(Enum entity) {
		'''
			«FOR literal : entity.literals»
				class "«entity.name».«literal.name»"
			«ENDFOR»
		'''
	}

	/*-------------------------------------------------*/
	/*------------------ Patterns ---------------------*/
	/*-------------------------------------------------*/
	/**
	 * Returns the diagram text for a Pattern.
	 */
	def dispatch String visualiseEntity(Pattern entity, boolean mainSelection) {
		var entityCopy = EMSLFlattener.flattenPattern(entity)
		'''
			package «IF entity.body.abstract»//«ENDIF»«entityCopy.body.name»«IF entity.body.abstract»//«ENDIF» «IF mainSelection» <<Selection>> «ENDIF»{
			«FOR nb : new EntityAttributeDispatcher().getNodeBlocks(entityCopy.body)»
				«visualiseNodeBlockInPattern2(entityCopy, nb, false)»
			«ENDFOR»
			}
			«IF entityCopy.condition !== null »
				legend bottom
					«IF entityCopy.condition instanceof ConstraintReference && (entityCopy.condition as ConstraintReference).negated»**!**(«ENDIF»«getConditionString(entityCopy)»«IF entityCopy.condition instanceof ConstraintReference && (entityCopy.condition as ConstraintReference).negated»)«ENDIF»
				endlegend
				«visualiseCondition(entityCopy)»
			«ENDIF»
		'''
	}

	/**
	 * Returns the diagram text for a NodeBlock in a Pattern.
	 */
	def String visualiseNodeBlockInPattern(ModelNodeBlock nodeBlock, boolean mainSelection) {
		var node = nodeBlock
		for (n : (new EntityAttributeDispatcher().getNodeBlocks((EMSLFlattener.flatten(nodeBlock.eContainer as SuperType))))) {
			if (nodeBlock.name.equals(n.name))
				node = n
		}
		val nb = node
		
		'''
			class «labelForPatternComponent(nb)» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR link : nb.relations»
				«labelForPatternComponent(nb)» --> «labelForPatternComponent(link.target)» : "«IF link.name !== null»«link.name»«ELSE»«link.types.get(0)?.type?.name»«ENDIF»«IF (link.lower !== null && link.upper !== null)»(«link.lower»..«link.upper»)«ENDIF»"
			«ENDFOR»
			«FOR attr : nb.properties»
				«labelForPatternComponent(nb)» : «attr.type.name» «attr.op.toString» «printValue(attr.value)»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as AtomicPattern).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relations»
					«IF incomingRef.target == nb && mainSelection»
						«labelForPatternComponent(incoming)» --> «labelForPatternComponent(nb)» : "«IF incomingRef.name !== null»«incomingRef.name»«ELSE»«incomingRef.types.get(0)?.type?.name»«ENDIF»«IF (incomingRef.lower !== null && incomingRef.upper !== null)»(«incomingRef.lower»..«incomingRef.upper»)«ENDIF»"
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
			«IF (nb.eContainer.eContainer as Pattern)?.condition !== null »
				legend bottom
					«getConditionString((nb.eContainer.eContainer as Pattern))»
				endlegend
			«ENDIF»
		'''
	}
	
	/**
	 * Returns the diagram text for a NodeBlock in a Pattern.
	 */
	def String visualiseNodeBlockInPattern2(Pattern entity, ModelNodeBlock nodeBlock, boolean mainSelection) {
		var node = nodeBlock
		for (n : new EntityAttributeDispatcher().getPatternNodeBlocks(entity)) {
			if (nodeBlock.name.equals(n.name))
				node = n
		}
		val nb = node
		
		'''
			class «labelForPatternComponent(nb)» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR link : nb.relations»
				«labelForPatternComponent(nb)» --> «labelForPatternComponent(link.target)» : "«IF link.name !== null»«link.name»«ELSE»«link.types.get(0)?.type?.name»«ENDIF»«IF (link.lower !== null && link.upper !== null)»(«link.lower»..«link.upper»)«ENDIF»"
			«ENDFOR»
			«FOR attr : nb.properties»
				«labelForPatternComponent(nb)» : «attr.type.name» «attr.op.toString» «printValue(attr.value)»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as AtomicPattern).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relations»
					«IF incomingRef.target == nb && mainSelection»
						«labelForPatternComponent(incoming)» --> «labelForPatternComponent(nb)» : "«IF incomingRef.name !== null»«incomingRef.name»«ELSE»«incomingRef.types.get(0)?.type?.name»«ENDIF»«IF (incomingRef.lower !== null && incomingRef.upper !== null)»(«incomingRef.lower»..«incomingRef.upper»)«ENDIF»"
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
			«IF (nb.eContainer.eContainer as Pattern)?.condition !== null »
				legend bottom
					«getConditionString((nb.eContainer.eContainer as Pattern))»
				endlegend
			«ENDIF»
		'''
	}

	/**
	 * Returns the diagram text for the name of an object in a Pattern.
	 */
	private def labelForPatternComponent(ModelNodeBlock nb) {
		var entityName = "?"
		var nbName = "?"
		var nbTypeName = "?"
		if (nb !== null && nb.eContainer !== null) {
			val entity = nb.eContainer as AtomicPattern
			if (entity.name !== null)
				entityName = entity.name
			if (nb.name !== null)
				nbName = nb.name
			if (nb.type !== null && nb.type.name !== null)
				nbTypeName = nb.type.name

			'''"«IF (nb.eContainer as AtomicPattern).abstract»//«ENDIF»«entityName»«IF (nb.eContainer as AtomicPattern).abstract»//«ENDIF».«nbName»:«nbTypeName»"'''
		} else
			'''"?"'''
	}	

	/*-------------------------------------------------*/
	/*------------------- Rules -----------------------*/
	/*-------------------------------------------------*/
	/**
	 * Returns the diagram text for a Rule.
	 */
	def dispatch String visualiseEntity(Rule entity, boolean mainSelection) {
		var entityCopy = EMSLFlattener.flatten(entity) as Rule
		'''
			package «IF entity.abstract»//«ENDIF»«(entityCopy as Rule).name»«IF entity.abstract»//«ENDIF»«IF mainSelection» <<Selection>> «ENDIF»{
			«FOR nb : new EntityAttributeDispatcher().getNodeBlocks(entityCopy)»
				«visualiseNodeBlockInRule2(entityCopy as Rule, nb, false)»
			«ENDFOR»
			}
			«IF (entityCopy as Rule).condition !== null»
				legend bottom
					«IF entityCopy.condition instanceof ConstraintReference && (entityCopy.condition as ConstraintReference).negated»**!**(«ENDIF»«getConditionString(entityCopy)»«IF entityCopy.condition instanceof ConstraintReference && (entityCopy.condition as ConstraintReference).negated»)«ENDIF»
				endlegend
				«visualiseCondition(entityCopy)»
			«ENDIF»
		'''
	}

	/**
	 * Returns the diagram text for a NodeBlock in a Rule.
	 */
	def String visualiseNodeBlockInRule(ModelNodeBlock nodeBlock, boolean mainSelection) {
		var node = nodeBlock
		for (n : (new EntityAttributeDispatcher().getNodeBlocks((EMSLFlattener.flatten(nodeBlock.eContainer as SuperType))))) {
			if (nodeBlock.name.equals(n.name))
				node = n
		}
		val nb = node
		
		'''
			class «labelForRuleComponent(nb)» «IF nb.action !== null && nb.action.op == ActionOperator.CREATE»<<GREEN>>«ENDIF»«IF nb.action !== null && nb.action.op == ActionOperator.DELETE»<<RED>>«ENDIF» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR link : nb.relations»
				«IF link.action !== null»
					«labelForRuleComponent(nb)» -«IF link.action.op === ActionOperator.CREATE»[#SpringGreen]«ELSE»[#red]«ENDIF»-> «labelForRuleComponent(link.target)» : "«IF link.name !== null»«link.name»«ELSE»«link.types.get(0)?.type?.name?.toString»«ENDIF»«IF (link.lower !== null && link.upper !== null)»(«link.lower»..«link.upper»)«ENDIF»"
				«ELSE»«labelForRuleComponent(nb)» --> «labelForRuleComponent(link.target)» : "«IF link.name !== null»«link.name»«ELSE»«link.types.get(0)?.type?.name?.toString»«ENDIF»«IF (link.lower !== null && link.upper !== null)»(«link.lower»..«link.upper»)«ENDIF»"
				«ENDIF»
				class «labelForRuleComponent(link.target)» «IF link.target.action !== null && link.target.action.op == ActionOperator.CREATE»<<GREEN>>«ENDIF»«IF link.target.action !== null && link.target.action.op == ActionOperator.DELETE»<<RED>>«ENDIF»
			«ENDFOR»
			«FOR attr : nb.properties»
				«labelForRuleComponent(nb)» : «attr.type.name» «attr.op.toString» «printValue(attr.value)»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as Rule).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relations»
					«IF incomingRef.target == nb && mainSelection»
						class «labelForRuleComponent(incoming)» «IF incoming.action !== null && incoming.action.op == ActionOperator.CREATE»<<GREEN>>«ENDIF»«IF incoming.action !== null && incoming.action.op == ActionOperator.DELETE»<<RED>>«ENDIF»
						«labelForRuleComponent(incoming)» -«IF incomingRef.action !== null &&  incomingRef.action.op === ActionOperator.CREATE»[#SpringGreen]«ENDIF»«IF incomingRef.action !== null && incomingRef.action.op === ActionOperator.DELETE»[#red]«ENDIF»-> «labelForRuleComponent(nb)» : "«IF incomingRef.name !== null»«incomingRef.name»«ELSE»«incomingRef.types.get(0)?.type?.name?.toString»«ENDIF»«IF (incomingRef.lower !== null && incomingRef.upper !== null)»(«incomingRef.lower»..«incomingRef.upper»)«ENDIF»"
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
			«IF (nb.eContainer as Rule).condition !== null »
				legend bottom
					«getConditionString((nb.eContainer as Rule))»
				endlegend
			«ENDIF»
		'''
	}
	
	/**
	 * Returns the diagram text for a NodeBlock in a Rule.
	 */
	def String visualiseNodeBlockInRule2(Rule entity, ModelNodeBlock nodeBlock, boolean mainSelection) {
		var node = nodeBlock
		for (n : (new EntityAttributeDispatcher().getNodeBlocks((EMSLFlattener.flatten(entity))))) {
			if (nodeBlock.name.equals(n.name))
				node = n
		}
		val nb = node
		
		'''
			class «labelForRuleComponent(nb)» «IF nb.action !== null && nb.action.op == ActionOperator.CREATE»<<GREEN>>«ENDIF»«IF nb.action !== null && nb.action.op == ActionOperator.DELETE»<<RED>>«ENDIF» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR link : nb.relations»
				«IF link.action !== null»
					«labelForRuleComponent(nb)» -«IF link.action.op === ActionOperator.CREATE»[#SpringGreen]«ELSE»[#red]«ENDIF»-> «labelForRuleComponent(link.target)» : "«IF link.name !== null»«link.name»«ELSE»«link.types.get(0)?.type?.name?.toString»«ENDIF»«IF (link.lower !== null && link.upper !== null)»(«link.lower»..«link.upper»)«ENDIF»"
				«ELSE»«labelForRuleComponent(nb)» --> «labelForRuleComponent(link.target)» : "«IF link.name !== null»«link.name»«ELSE»«link.types.get(0)?.type?.name?.toString»«ENDIF»«IF (link.lower !== null && link.upper !== null)»(«link.lower»..«link.upper»)«ENDIF»"
				«ENDIF»
				class «labelForRuleComponent(link.target)» «IF link.target.action !== null && link.target.action.op == ActionOperator.CREATE»<<GREEN>>«ENDIF»«IF link.target.action !== null && link.target.action.op == ActionOperator.DELETE»<<RED>>«ENDIF»
			«ENDFOR»
			«FOR attr : nb.properties»
				«labelForRuleComponent(nb)» : «attr.type.name» «attr.op.toString» «printValue(attr.value)»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as Rule).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relations»
					«IF incomingRef.target == nb && mainSelection»
						class «labelForRuleComponent(incoming)» «IF incoming.action !== null && incoming.action.op == ActionOperator.CREATE»<<GREEN>>«ENDIF»«IF incoming.action !== null && incoming.action.op == ActionOperator.DELETE»<<RED>>«ENDIF»
						«labelForRuleComponent(incoming)» -«IF incomingRef.action !== null &&  incomingRef.action.op === ActionOperator.CREATE»[#SpringGreen]«ENDIF»«IF incomingRef.action !== null && incomingRef.action.op === ActionOperator.DELETE»[#red]«ENDIF»-> «labelForRuleComponent(nb)» : "«IF incomingRef.name !== null»«incomingRef.name»«ELSE»«incomingRef.types.get(0)?.type?.name?.toString»«ENDIF»«IF (incomingRef.lower !== null && incomingRef.upper !== null)»(«incomingRef.lower»..«incomingRef.upper»)«ENDIF»"
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
			«IF (nb.eContainer as Rule).condition !== null »
				legend bottom
					«getConditionString((nb.eContainer as Rule))»
				endlegend
			«ENDIF»
		'''
	}

	/**
	 * Returns the diagram text for the name of an object in a Rule.
	 */
	private def labelForRuleComponent(ModelNodeBlock nb) {
		val entity = nb?.eContainer as Rule
		var entityName = "?"
		var nbName = "?"
		var nbTypeName = "?"
		
		if (entity !== null) {
			if (entity?.name !== null)
				entityName = entity.name
			if (nb?.name !== null)
				nbName = nb.name
			if (nb?.type?.name !== null)
				nbTypeName = nb.type.name

			'''"«IF entity.abstract»//«ENDIF»«entityName»«IF entity.abstract»//«ENDIF».«nbName»:«nbTypeName»"'''
		} else
			'''"?"'''
	}

	/**
	 * Returns the diagram text for a GraphGrammar.
	 */
	def dispatch String visualiseEntity(GraphGrammar entity, boolean mainSelection) {
		'''
			«FOR r : entity.rules»
				class "«IF entity.abstract»//«ENDIF»«entity.name»«IF entity.abstract»//«ENDIF».«r.name»" «link(r)»
			«ENDFOR»
		'''
	}

	/*------------------------------------------*/
	/*-------- Constraints & Conditions --------*/
	/*------------------------------------------*/
	/**
	 * Returns the diagram text for a Constraint.
	 */
	def dispatch String visualiseEntity(Constraint entity, boolean mainSelection) {
		'''
			legend bottom
				«IF entity.abstract»//«ENDIF»«getConditionString(entity)»«IF entity.abstract»//«ENDIF»
			endlegend
			«visualiseCondition(entity)»
		'''
	}

	/**
	 * Returns the diagram text for a condition of a Rule or Pattern.
	 */
	def String visualiseCondition(Entity entity) {
		var conditionPattern = new ConstraintTraversalHelper().getConstraintPattern(entity)
		var copiesOfConditionPatterns = new HashSet()
		for (p : conditionPattern) {
			if (p.name.equals(entity.name) && entity instanceof Pattern) {
				throw new Exception("Using a pattern as its own condition is not allowed.")
			} else {
				var copiedPattern = EMSLFlattener.flattenToPattern(p)
				copiedPattern.condition = EcoreUtil2.copy((p.eContainer as Pattern).condition)
				copiesOfConditionPatterns.add(copiedPattern)
			}
		}
		'''
			«FOR c : copiesOfConditionPatterns»
				«visualiseEntity(c, false)»
			«ENDFOR»
			«IF entity instanceof Rule || entity instanceof Pattern»
				«FOR nb : entity.nodeBlocksOfEntity»
					«FOR p : copiesOfConditionPatterns»
						«FOR otherNB : (p as Pattern).body.nodeBlocks»
							«IF otherNB.name.equals(nb.name)»«IF (entity instanceof Rule)»«labelForRuleComponent(nb)»«ELSE»«labelForPatternComponent(nb)»«ENDIF»#-[#DarkRed]-#«labelForPatternComponent(otherNB)»«ENDIF»
						«ENDFOR»
					«ENDFOR»
				«ENDFOR»
			«ENDIF»
			«IF entity instanceof Constraint && (entity as Constraint).body instanceof Implication»
				«createLinksForConstraintPatterns(conditionPattern)»
			«ENDIF»
		'''
	}
	
	/**
	 * Returns the diagram text for the links between objects with the same name in the patterns of a constraint.
	 */
	def String createLinksForConstraintPatterns(ArrayList<AtomicPattern> patterns) {
		var text = ""
		for (p : patterns) {
			for (AtomicPattern other : patterns) {
				if (!p.name.equals(other.name)) {
					for (ModelNodeBlock nb : p.nodeBlocks) {
						for (ModelNodeBlock otherNB : other.nodeBlocks) {
							if (otherNB.name.equals(nb.name)) {
								// create link if not already created
								if (!text.contains(labelForPatternComponent(nb) + "#-[#DarkRed]-#" + labelForPatternComponent(otherNB)) &&
									!text.contains(labelForPatternComponent(otherNB) + "#-[#DarkRed]-#" + labelForPatternComponent(nb))
								)
									text += labelForPatternComponent(nb) + "#-[#DarkRed]-#" + labelForPatternComponent(otherNB) + "\n"
							}
						}
					}
				}
			}
		}
		return text			
	}

	/**
	 * Returns the diagram text for the condition of a Rule or Pattern.
	 */
	def String getConditionString(Entity entity) {
		var builder = new StringBuilder();
		if (entity instanceof Rule) {
			// return for atomicConstraints
			if ((entity as Rule).condition instanceof NegativeConstraint)
				return '''**forbid** «((entity as Rule).condition as NegativeConstraint).pattern.name» '''
			else if ((entity as Rule).condition instanceof PositiveConstraint)
				return '''**enforce** «((entity as Rule).condition as PositiveConstraint).pattern.name» '''
			else if ((entity as Rule).condition instanceof Implication) {
				return '''**if** «((entity as Rule).condition as Implication).premise.name»  **then** «((entity as Rule).condition as Implication).conclusion.name» '''
			} 	
			// return the String for ConstraintReference
			else if (entity.condition instanceof ConstraintReference) {
				if ((entity.condition as ConstraintReference).reference.body instanceof NegativeConstraint
						|| (entity.condition as ConstraintReference).reference.body instanceof PositiveConstraint
						|| (entity.condition as ConstraintReference).reference.body instanceof Implication) {
					builder.append(getAtomicConstraintString((entity.condition as ConstraintReference).reference.body))
				} else if ((entity.condition as ConstraintReference).reference.body instanceof OrBody) {
					builder.append(getOrBodyString((entity.condition as ConstraintReference).reference.body))
				}
			}
		} else if (entity instanceof Pattern) {
			// return for atomicConstraints
			if ((entity as Pattern).condition instanceof NegativeConstraint)
				return '''**forbid** «((entity as Pattern)?.condition as NegativeConstraint)?.pattern?.name» '''
			else if ((entity as Pattern).condition instanceof PositiveConstraint)
				return '''**enforce** «((entity as Pattern)?.condition as PositiveConstraint)?.pattern?.name» '''
			else if ((entity as Pattern).condition instanceof Implication) {
				return '''**if** «((entity as Pattern)?.condition as Implication)?.premise?.name»  **then** «((entity as Pattern)?.condition as Implication)?.conclusion?.name» '''
			} 
			// return the String for ConstraintReference
			else if (entity.condition instanceof ConstraintReference) {
				if ((entity.condition as ConstraintReference).reference.body instanceof NegativeConstraint
						|| (entity.condition as ConstraintReference).reference.body instanceof PositiveConstraint
						|| (entity.condition as ConstraintReference).reference.body instanceof Implication) {
					builder.append(getAtomicConstraintString((entity.condition as ConstraintReference).reference.body))
				} else if ((entity.condition as ConstraintReference).reference.body instanceof OrBody) {
					builder.append(getOrBodyString((entity.condition as ConstraintReference).reference.body))
				}
			}
		} else if (entity instanceof Constraint) {
			// return for atomicConstraints
			if ((entity as Constraint).body instanceof NegativeConstraint)
				return '''**forbid** «((entity as Constraint).body as NegativeConstraint).pattern.name» '''
			else if ((entity as Constraint).body instanceof PositiveConstraint)
				return '''**enforce** «((entity as Constraint).body as PositiveConstraint).pattern.name» '''
			else if ((entity as Constraint).body instanceof Implication) {
				return '''**if** «((entity as Constraint).body as Implication).premise.name»  **then** «((entity as Constraint).body as Implication).conclusion.name» '''
			} 
			// return for OrBody
			else if ((entity as Constraint).body instanceof OrBody) {
				builder.append(getOrBodyString((entity as Constraint).body))
			}
		}
		return builder.toString
	}

	/**
	 * Returns the diagram text for an OrBody in a recursive Constraint definition.
	 */
	def String getOrBodyString(ConstraintBody constraintBody) {
		var builder = new StringBuilder()
		var count = constraintBody.children.size - 1
		for (c : constraintBody.children) {
			if (c instanceof AndBody) {
				builder.append(getAndBodyString(c))
				if (count > 0)
					builder.append(" **||** ")
				count--
			}
		}
		return builder.toString
	}

	/**
	 * Returns the diagram text for an AndBody in a recursive Constraint definition.
	 */
	def String getAndBodyString(ConstraintBody constraintBody) {
		var builder = new StringBuilder()
		var count = constraintBody.children.size - 1
		for (c : constraintBody.children) {
			if (c instanceof ConstraintReference) {
				if (c.negated)
					builder.append("**!**(")
				if ((c as ConstraintReference).reference.body instanceof NegativeConstraint 
						|| (c as ConstraintReference).reference.body instanceof PositiveConstraint 
						|| (c as ConstraintReference).reference.body instanceof Implication) {
					builder.append(getAtomicConstraintString(c))
				} else if ((c as ConstraintReference).reference.body instanceof OrBody) {
					builder.append(getOrBodyString(c.reference.body))
				}
				if (c.negated)
					builder.append(")")
			} else if (c instanceof OrBody) {
				builder.append(" (" + getOrBodyString(c) + ") ")
			}
			if (count > 0)
				builder.append(" **&&** ")
			count --
		}
		return builder.toString
	}

	/**
	 * Returns the diagram text for an AtomicConstraint.
	 */
	def String getAtomicConstraintString(ConstraintBody constraintBody) {
		if (constraintBody instanceof ConstraintReference) {
			if (constraintBody.reference.body instanceof NegativeConstraint)
				return '''**forbid** «(constraintBody.reference.body as NegativeConstraint).pattern.name» '''
			else if (constraintBody.reference.body instanceof PositiveConstraint)
				return '''**enforce** «(constraintBody.reference.body as PositiveConstraint).pattern.name» '''
			else if (constraintBody.reference.body instanceof Implication)
				return '''**if** «(constraintBody.reference.body as Implication).premise.name» **then** «(constraintBody.reference.body as Implication).conclusion.name» '''
		} if (constraintBody instanceof NegativeConstraint)
				return '''**forbid** «(constraintBody as NegativeConstraint).pattern.name» '''
		else if (constraintBody instanceof PositiveConstraint)
			return '''**enforce** «(constraintBody as PositiveConstraint).pattern.name» '''
		else if (constraintBody instanceof Implication)
			return '''**if** «constraintBody.premise.name» **then** «constraintBody.conclusion.name» '''
	}
	
	def List<? extends ConstraintBody> getChildren(ConstraintBody body){
		return ConstraintTraversalHelper.getChildren(body)
	}

	/*-------------------------------------------------*/
	/*---------------- Triple Rules -------------------*/
	/*-------------------------------------------------*/
	/**
	 * Returns the diagram text for a TripleRule.
	 */
	def dispatch String visualiseEntity(TripleRule entity, boolean mainSelection) {
		var entityCopy = EMSLFlattener.flatten(entity) as TripleRule
		'''
			package «IF entity.abstract»//«ENDIF»«entityCopy.name»«IF entity.abstract»//«ENDIF» «IF mainSelection» <<Selection>> «ENDIF» {
				«FOR snb : entityCopy.srcNodeBlocks»
					«visualiseTripleRuleNodeBlocks(entityCopy, snb, "SRC")»
				«ENDFOR»
				
				«FOR tnb : entityCopy.trgNodeBlocks»
					«visualiseTripleRuleNodeBlocks(entityCopy, tnb, "TRG")»
				«ENDFOR»
			
				«FOR corr : entityCopy.correspondences»
				"«IF entityCopy.abstract»//«ENDIF»«entityCopy.name»«IF entityCopy.abstract»//«ENDIF».«corr.source.name»:«corr.source.type.name»" ...«IF corr.action !== null»[#SpringGreen]«ENDIF»"«IF entityCopy.abstract»//«ENDIF»«entityCopy.name»«IF entityCopy.abstract»//«ENDIF».«corr.target.name»:«corr.target.type.name»": :«corr.type.name»
				«ENDFOR»
			}
			«IF entityCopy.nacs.size > 0»
				«visualiseTripleRuleNACs(entityCopy)»
			«ENDIF»
		'''
	}

	/**
	 * Returns the diagram text for the NodeBlocks of a TripleRule.
	 */
	def String visualiseTripleRuleNodeBlocks(TripleRule entity, ModelNodeBlock nb, String type) {
		var sizeOfTypeList = 0
		'''class "«IF entity.abstract»//«ENDIF»«entity.name»«IF entity.abstract»//«ENDIF».«nb.name»:«nb.type.name»" «IF nb.action !== null»<<GREEN>>«ENDIF» <<«type»>>
			«FOR link : nb.relations»«{sizeOfTypeList = link.types.size - 1;""}»
				class «labelForTripleRuleComponent(link.target)» «IF link.target.action !== null && link.target.action.op == ActionOperator.CREATE»<<GREEN>>«ENDIF»«IF link.target.action !== null && link.target.action.op == ActionOperator.DELETE»<<RED>>«ENDIF» «IF entity.srcNodeBlocks.contains(link.target)»<<SRC>>«ELSE»<<TRG>>«ENDIF»
				"«IF entity.abstract»//«ENDIF»«entity.name»«IF entity.abstract»//«ENDIF».«nb.name»:«nb.type.name»" -«IF (link.action !== null)»[#SpringGreen]«ENDIF»-> "«IF entity.abstract»//«ENDIF»«entity.name»«IF entity.abstract»//«ENDIF».«link.target.name»:«link.target.type.name»":"«FOR t : link.types»«IF (t.type as MetamodelRelationStatement).name !== null && t.type !== null»«(t.type as MetamodelRelationStatement).name»«ELSE»?«ENDIF»«IF sizeOfTypeList > 0» | «ENDIF»«{sizeOfTypeList = sizeOfTypeList - 1;""}»«ENDFOR»«IF (link.lower !== null && link.upper !== null)»(«link.lower»..«link.upper»)«ENDIF»"
			«ENDFOR»
			«FOR attr : nb.properties»
				«labelForTripleRuleComponent(nb)» : «attr.type.name» «attr.op.toString» «printValue(attr.value)»
			«ENDFOR»
		'''
	}

	/**
	 * Returns the diagram text for a NodeBlock in a TripleRule.
	 */
	def String visualiseNodeBlockInTripleRule(TripleRule rule, ModelNodeBlock nodeBlock, boolean mainSelection) {
		var node = nodeBlock
		val entityCopy = EMSLFlattener.flatten(nodeBlock.eContainer as SuperType)
		for (n : (entityCopy as TripleRule).srcNodeBlocks) {
			if (nodeBlock.name.equals(n.name) && (nodeBlock.eContainer as TripleRule).srcNodeBlocks.contains(nodeBlock))
				node = n
		}
		for (n : (entityCopy as TripleRule).trgNodeBlocks) {
			if (nodeBlock.name.equals(n.name) && (nodeBlock.eContainer as TripleRule).trgNodeBlocks.contains(nodeBlock))
				node = n
		}
		val nb = node	
		'''
			class «labelForTripleRuleComponent(nb)» «IF nb.action !== null && nb.action.op == ActionOperator.CREATE»<<GREEN>>«ENDIF»«IF nb.action !== null && nb.action.op == ActionOperator.DELETE»<<RED>>«ENDIF» «IF mainSelection»<<Selection>>«ENDIF»«IF rule.srcNodeBlocks.contains(nb)»<<SRC>>«ELSE»<<TRG>>«ENDIF»
			«FOR link : nb.relations»
				class «labelForTripleRuleComponent(link.target)» «IF link.target.action !== null && link.target.action.op == ActionOperator.CREATE»<<GREEN>>«ENDIF»«IF link.target.action !== null && link.target.action.op == ActionOperator.DELETE»<<RED>>«ENDIF»«IF (entityCopy as TripleRule).srcNodeBlocks.contains(link.target)»<<SRC>>«ELSE»<<TRG>>«ENDIF»
				«IF link.action !== null»
					«labelForTripleRuleComponent(nb)» -«IF link.action.op.toString === '++'»[#SpringGreen]«ELSE»[#red]«ENDIF»-> «labelForTripleRuleComponent(link.target)» : "«IF link.name !== null»«link.name»«ELSE»«link.types.get(0)?.type?.name?.toString»«ENDIF»«IF (link.lower !== null && link.upper !== null)»(«link.lower»..«link.upper»)«ENDIF»"
				«ELSE»«labelForTripleRuleComponent(nb)» --> «labelForTripleRuleComponent(link.target)» : "«IF link.name !== null»«link.name»«ELSE»«link.types.get(0)?.type?.name?.toString»«ENDIF»«IF (link.lower !== null && link.upper !== null)»(«link.lower»..«link.upper»)«ENDIF»"
				«ENDIF»
			«ENDFOR»
			«FOR attr : nb.properties»
				«labelForTripleRuleComponent(nb)» : «attr.type.name» «attr.op.toString» «printValue(attr.value)»
			«ENDFOR»
			«FOR incoming : (entityCopy as TripleRule).srcNodeBlocks.filter[n|n != nb]»
				«incomingRefHelperForTripleRules(entityCopy as TripleRule, nb, incoming, mainSelection)»
			«ENDFOR»
			«FOR incoming : (entityCopy as TripleRule).trgNodeBlocks.filter[n|n != nb]»
				«incomingRefHelperForTripleRules(entityCopy as TripleRule, nb, incoming, mainSelection)»
			«ENDFOR»
		'''
	}
	
	private def incomingRefHelperForTripleRules(TripleRule entity, ModelNodeBlock nb, ModelNodeBlock incoming, boolean mainSelection) {
		'''«FOR incomingRef : incoming.relations»
			«IF incomingRef.target == nb && mainSelection»
				class «labelForTripleRuleComponent(incoming)» «IF incoming.action !== null && incoming.action.op == ActionOperator.CREATE»<<GREEN>>«ENDIF»«IF incoming.action !== null && incoming.action.op == ActionOperator.DELETE»<<RED>>«ENDIF»«IF (entity as TripleRule).srcNodeBlocks.contains(incoming)»<<SRC>>«ELSE»<<TRG>>«ENDIF»
				«labelForTripleRuleComponent(incoming)» -«IF incomingRef.action !== null && incomingRef.action.op === ActionOperator.CREATE»[#SpringGreen]«ENDIF»«IF incomingRef.action !== null && incomingRef.action.op === ActionOperator.DELETE»[#red]«ENDIF»-> «labelForTripleRuleComponent(nb)» : "«IF incomingRef.name !== null»«incomingRef.name»«ELSE»«incomingRef.types.get(0)?.type?.name?.toString»«ENDIF»«IF (incomingRef.lower !== null && incomingRef.upper !== null)»(«incomingRef.lower»..«incomingRef.upper»)«ENDIF»"
			«ENDIF»
		«ENDFOR»'''
	}

	/**
	 * Returns the diagram text for the name of an object in a TripleRule.
	 */
	private def labelForTripleRuleComponent(ModelNodeBlock nb) {
		val entity = nb.eContainer as TripleRule
		'''"«IF entity.abstract»//«ENDIF»«entity.name»«IF entity.abstract»//«ENDIF».«nb.name»:«IF nb.type.name !== null »«nb.type.name»«ELSE»?«ENDIF»"'''
	}

	/**
	 * Returns the diagram text for the NACs of a given TripleRule.
	 */
	def String visualiseTripleRuleNACs(TripleRule entity) {
		var count = entity.nacs.size
		'''
			«FOR c : entity.nacs»
				«IF c.pattern.eContainer !== null»
					«visualiseEntity(c.pattern.eContainer as Pattern, false)»
					«FOR nb : c.pattern.nodeBlocks»
						«IF c instanceof SourceNAC»
							«FOR other : entity.srcNodeBlocks»
								«IF other.name.equals(nb.name)»«labelForTripleRuleComponent(other)»#-[#DarkRed]-#«labelForPatternComponent(nb)»«ENDIF»
							«ENDFOR»
						«ENDIF»
						«IF c instanceof TargetNAC»
							«FOR other : entity.trgNodeBlocks»
								«IF other.name.equals(nb.name)»«labelForTripleRuleComponent(other)»#-[#DarkRed]-#«labelForPatternComponent(nb)»«ENDIF»
							«ENDFOR»
						«ENDIF»
					«ENDFOR»
				«ENDIF»
			«ENDFOR»
			
			legend bottom
				**forbid** «FOR c : entity.nacs»«IF c instanceof SourceNAC»src(«ELSE»trg(«ENDIF»«c.pattern.name»)«{count--; ""}»«IF count > 0» && «ENDIF»«ENDFOR»
			endlegend
		'''
	}

	/**
	 * Returns the diagram text for a TripleGrammar.
	 */
	def dispatch String visualiseEntity(TripleGrammar entity, boolean mainSelection) {
		'''
			set namespaceSeparator none
			
			left to right direction
		
			package "Source Metamodels" {
				«FOR mm : entity.srcMetamodels»
					class "«IF entity.abstract»//«ENDIF»«mm.name»«IF entity.abstract»//«ENDIF»" «link(mm)»
				«ENDFOR»
			}
			
			package "Target Metamodels" {
				«FOR mm : entity.trgMetamodels»
					class "«IF entity.abstract»//«ENDIF»«mm.name»«IF entity.abstract»//«ENDIF»" «link(mm)»
				«ENDFOR»
			}
			«IF !entity.correspondences.empty»
			package Correspondences {
				«FOR c : entity.correspondences»
					class "«(c.source.eContainer as Entity).name».«c.source.name»"
					class "«(c.target?.eContainer as Entity).name».«c.target?.name»"
					"«(c.source.eContainer as Entity).name».«c.source.name»" .. "«(c.target.eContainer as Entity).name».«c.target.name»" : «c.name»
				«ENDFOR»
			}
			«ENDIF»
			«IF !entity.rules.empty»
			package Rules {
				«FOR r : entity.rules»
					class "«r.name»" «link(r)»
				«ENDFOR»
			}
			«ENDIF»
		'''
	}

	/*----------------------------------*/
	/*--------- Get NodeBlocks ---------*/
	/*----------------------------------*/
	/**
	 * Returns all NodeBlocks of a Model.
	 */
	def dispatch getNodeBlocks(Model entity) {
		entity.nodeBlocks
	}

	/**
	 * Returns all NodeBlocks of a Pattern.
	 */
	def getPatternNodeBlocks(Pattern entity) {
		entity.body.nodeBlocks
	}
	
	def dispatch getNodeBlocks(AtomicPattern p){
		p.nodeBlocks
	}

	/**
	 * Returns all NodeBlocks of a Rule.
	 */
	def dispatch getNodeBlocks(Rule entity) {
		entity.nodeBlocks
	}
	
	def getNodeBlocksOfEntity(Entity e){
		if(e instanceof Pattern)
			getPatternNodeBlocks(e)
		else
			getNodeBlocks(e as SuperType)
	}
	
	
	/*-----------------------------------------*/
	/*------ Get SuperRefinementTypes ---------*/
	/*-----------------------------------------*/

	def dispatch List<RefinementCommand> getSuperRefinementTypes(Model entity) {
		entity.superRefinementTypes
	}
	
	def dispatch getSuperRefinementTypes(Pattern entity) {
		entity.body.superRefinementTypes
	}
	
	def dispatch getSuperRefinementTypes(Rule entity) {
		entity.superRefinementTypes
	}
	
	def dispatch getSuperRefinementTypes(TripleRule entity) {
		entity.superRefinementTypes
	}
	
	def dispatch getSuperRefinementTypes(Constraint entity) {
		return newArrayList
	}
	
	def dispatch getSuperRefinementTypes(TripleGrammar entity) {
		return newArrayList
	}
	
	def dispatch getSuperRefinementTypes(GraphGrammar entity) {
		return newArrayList
	}
	
	/*------------------------------*/
	/*---------- Get Names ---------*/
	/*------------------------------*/

	def dispatch getName(Model entity) {
		entity.name
	}
	
	def dispatch getName(Metamodel entity) {
		entity.name
	}
	
	def dispatch getName(Pattern entity) {
		entity.body.name
	}
	
	def dispatch getName(Rule entity) {
		entity.name
	}
	
	def dispatch getName(TripleRule entity) {
		entity.name
	}
	
	def dispatch getName(Constraint entity) {
		entity.name
	}
	
	def dispatch getName(GraphGrammar entity) {
		entity.name
	}
	
	def dispatch getName(TripleGrammar entity) {
		entity.name
	}
	
	


	/*------------------------------*/
	/*------------ Misc ------------*/
	/*------------------------------*/
	
	/**
	 * Returns the diagram text for all SuperTypes of a Pattern with inheritance arrows.
	 */
	def String visualiseSuperTypesInEntity(Entity entity) {
		var superTypes = sortEntities(entity.superRefinementTypes.map[st | 
			if (!(st.referencedType instanceof AtomicPattern))
				st.referencedType as Entity
			else
				st.referencedType.eContainer as Entity
		])
		'''
			«FOR type : superTypes.keySet»
				«FOR e : superTypes.get(type)»
					«type»«entity.name» --|> «type»«(e as Entity).name»
				«ENDFOR»
			«ENDFOR»
		'''
	}
	
	/**
	 * Helper method to sort the given list of entities into a hashMap.
	 * The result is a list mapped to the typename of the entities that are
	 * contained in the list.
	 */
	def HashMap<String, HashSet<Entity>> sortEntities(List<Entity> entities) {
		var superTypes = new HashMap<String, HashSet<Entity>>()
		superTypes.put("Patterns", new HashSet())
		superTypes.put("Rules", new HashSet())
		superTypes.put("Models", new HashSet())
		superTypes.put("TripleRules", new HashSet())
		superTypes.put("TripleGrammars", new HashSet())
		superTypes.put("GraphGrammars", new HashSet())
		superTypes.put("Metamodels", new HashSet())
		superTypes.put("Constraints", new HashSet())

		for (st : entities) {
			if (st instanceof Pattern &&
					!superTypes.get("Patterns").contains(
						(st as Pattern).body)) {
					superTypes.get("Patterns").add(st as Entity)
			} else if (st instanceof Rule &&
					!superTypes.get("Rules").contains(
						(st as Rule))) {
				superTypes.get("Rules").add(st as Entity)
			} else if (st instanceof Model &&
					!superTypes.get("Models").contains(
						(st as Model))) {
				superTypes.get("Models").add(st as Entity)
			} else if (st instanceof TripleRule &&
					!superTypes.get("TripleRules").contains(
						(st as TripleRule))) {
				superTypes.get("TripleRules").add(st as Entity)
			} else if (st instanceof TripleGrammar &&
					!superTypes.get("TripleGrammars").contains(
						(st as TripleGrammar))) {
				superTypes.get("TripleGrammars").add(st as Entity)
			} else if (st instanceof GraphGrammar &&
					!superTypes.get("GraphGrammars").contains(
						(st as GraphGrammar))) {
				superTypes.get("GraphGrammars").add(st as Entity)
			} else if (st instanceof Metamodel &&
					!superTypes.get("Metamodels").contains(
						(st as Metamodel))) {
				superTypes.get("Metamodels").add(st as Entity)
			}  else if (st instanceof Constraint &&
					!superTypes.get("Constraints").contains(
						(st as Constraint))) {
				superTypes.get("Constraints").add(st as Entity)
			}
		}
		return superTypes
	}
	
	/**
	 * Returns the diagram text for a link to the given entity to make it clickable.
	 */
	private static def link(Entity entity) {
		val resource = entity.eResource
		val uri = resource.URI + '#' + resource.getURIFragment(entity)
		'''[[«uri»]]'''
	}

	/**
	 * Returns the ModelNodeBlock that is currently selected in the editor.
	 */
	def Optional<ModelNodeBlock> determineSelectedNodeBlock(ISelection selection, Entity entity) {
		if (selection instanceof TextSelection) {
			// For the TextSelection documents start with line 0.
			val selectionStart = selection.getStartLine() + 1;
			val selectionEnd = selection.getEndLine() + 1;
			if (!(entity instanceof GraphGrammar || entity instanceof TripleGrammar || entity instanceof Metamodel ||
				entity instanceof Enum || entity instanceof Constraint)) {
				if (entity instanceof TripleRule) {
					for (srcNodeBlock : entity.srcNodeBlocks) {
						val srcObject = NodeModelUtils.getNode(srcNodeBlock);
						if (srcObject !== null && selectionStart >= srcObject.getStartLine() && selectionEnd <= srcObject.getEndLine()) {
							return Optional.of(srcNodeBlock)
						}
					}
					for (trgNodeBlock : entity.trgNodeBlocks) {
						val trgObject = NodeModelUtils.getNode(trgNodeBlock);
						if (trgObject !== null && selectionStart >= trgObject.getStartLine() && selectionEnd <= trgObject.getEndLine()) {
							return Optional.of(trgNodeBlock)
						}
					}
				} else {
					for (nodeBlock : entity.nodeBlocksOfEntity) {
						val object = NodeModelUtils.getNode(nodeBlock);
						if (object !== null && selectionStart >= object.getStartLine() && selectionEnd <= object.getEndLine()) {
							return Optional.of(nodeBlock)
						}
					}
				}
			}
		}

		Optional.empty()
	}

	/**
	 * Returns the MetamodelNodeBlock that is currently selected in the editor.
	 */
	def Optional<MetamodelNodeBlock> determineSelectedMetamodelNodeBlock(ISelection selection, Entity entity) {
		if (selection instanceof TextSelection) {
			// For the TextSelection documents start with line 0.
			val selectionStart = selection.getStartLine() + 1;
			val selectionEnd = selection.getEndLine() + 1;
			if (entity instanceof Metamodel)
				for (nodeBlock : entity.nodeBlocks) {
					val object = NodeModelUtils.getNode(nodeBlock);
					if (selectionStart >= object.getStartLine() && selectionEnd <= object.getEndLine()) {
						return Optional.of(nodeBlock)
					}
				}
		}

		Optional.empty()
	}

	/**
	 * Returns the Entity that is currently selected in the editor.
	 */
	def Optional<Entity> determineSelectedEntity(ISelection selection, EMSL_Spec root) {
		if (selection instanceof TextSelection) {
			// For the TextSelection documents start with line 0.
			val selectionStart = selection.getStartLine() + 1;
			val selectionEnd = selection.getEndLine() + 1;

			for (entity : root.entities) {
				val object = NodeModelUtils.getNode(entity);
				if (selectionStart >= object.getStartLine() && selectionEnd <= object.getEndLine()) {
					return Optional.of(entity);
				}
			}
		}

		Optional.empty()
	}

	override boolean supportsSelection(ISelection selection) {
		return true
	}

	override boolean supportsEditor(IEditorPart editor) {
		getRoot(editor).map([it instanceof EMSL_Spec]).orElse(false)
	}

	def getRoot(IEditorPart editor) {
		if (editor instanceof XtextEditor) {
			return editor.document.readOnly([res|
				if(!res.contents.empty) 
					return Optional.of(res.contents.get(0))
				else
					return Optional.empty
			])
		} else {
			return Optional.empty
		}
	}

	def plantUMLPreamble() {
		'''
			hide empty members
			hide circle
			hide stereotypes
			
			skinparam shadowing false
			
			skinparam class {
				BorderColor Black
				BorderColor<<GREEN>> SpringGreen
				BorderColor<<RED>> Red
				BackgroundColor White
				ArrowColor Black
				BackgroundColor<<Selection>> AliceBlue
				BackgroundColor<<SRC>> LightYellow
				BackgroundColor<<TRG>> MistyRose
			}
			
			skinparam package {
				BorderColor LightSlateGray
				Fontcolor LightSlateGray
				BackgroundColor<<Selection>> AliceBlue
			}
			
			skinparam object {
				BorderColor Black
				BackgroundColor White
				ArrowColor Black
			}
			
			skinparam note {
				BorderColor Black
				BackgroundColor White
				ArrowColor Black
			}			
			
			skinparam rectangle {
				packageStyle rectangle
				BackgroundColor<<Rectangle>> White
				BackgroundColor<<Abstract>> LavenderBlush
			}
		'''
	}
}
