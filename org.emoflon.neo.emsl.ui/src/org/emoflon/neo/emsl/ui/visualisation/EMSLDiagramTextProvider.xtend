package org.emoflon.neo.emsl.ui.visualisation

import java.util.Optional
import net.sourceforge.plantuml.eclipse.utils.DiagramTextProvider
import org.eclipse.jface.text.TextSelection
import org.eclipse.jface.viewers.ISelection
import org.eclipse.ui.IEditorPart
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.ui.editor.XtextEditor
import org.emoflon.neo.emsl.eMSL.EMSL_Spec
import org.emoflon.neo.emsl.eMSL.Entity
import org.emoflon.neo.emsl.eMSL.Metamodel
import org.emoflon.neo.emsl.eMSL.Model
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock
import org.emoflon.neo.emsl.eMSL.Pattern
import org.emoflon.neo.emsl.eMSL.Rule
import org.emoflon.neo.emsl.eMSL.TripleRule
import org.emoflon.neo.emsl.eMSL.TripleGrammar
import org.emoflon.neo.emsl.eMSL.GraphGrammar
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock
import org.emoflon.neo.emsl.eMSL.MetamodelRelationStatement
import org.emoflon.neo.emsl.eMSL.UserDefinedType
import org.emoflon.neo.emsl.eMSL.BuiltInType
import org.emoflon.neo.emsl.eMSL.RelationKind
import org.emoflon.neo.emsl.eMSL.AtomicPattern
import org.emoflon.neo.emsl.eMSL.Constraint
import org.emoflon.neo.emsl.eMSL.ConstraintBody
import org.emoflon.neo.emsl.eMSL.NegativeConstraint
import org.emoflon.neo.emsl.eMSL.PositiveConstraint
import org.emoflon.neo.emsl.eMSL.Implication
import org.emoflon.neo.emsl.eMSL.ConstraintReference

class EMSLDiagramTextProvider implements DiagramTextProvider {
	static final int MAX_SIZE = 500

	override String getDiagramText(IEditorPart editor, ISelection selection) {
		var Optional<String> diagram = Optional.empty()
		try {
			var String d = getDiagramBody(editor, selection)
			if (d === null || d.split("\n").length > MAX_SIZE)
				diagram = Optional.of(tooBigDiagram())
			else
				diagram = Optional.of(d)
		} catch (Exception e) {
			e.printStackTrace()
		}

		return wrapInTags(diagram.orElse(errorDiagram()))
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
			title I'm having problems visualising the current selection (check your console).
		'''
	}

	def String tooBigDiagram() {
		'''
			title This diagram would be so big, trying to render it would fry your Eclipse instance
		'''
	}

	def String getDiagramBody(IEditorPart editor, ISelection selection) {
		val EMSL_Spec root = getRoot(editor) as EMSL_Spec
		val Optional<Entity> selectedEntity = determineSelectedEntity(selection, root)
		val Optional<ModelNodeBlock> selectedNodeBlock = selectedEntity.flatMap([e|determineSelectedNodeBlock(selection, e)])
		val Optional<MetamodelNodeBlock> selectedMetamodelNodeBlock = selectedEntity.flatMap([e|determineSelectedMetamodelNodeBlock(selection, e)])
		
		if (selectedEntity.isPresent && selectedEntity.get instanceof org.emoflon.neo.emsl.eMSL.Enum)
			return visualiseEnumItems(selectedEntity.get as org.emoflon.neo.emsl.eMSL.Enum)
		
		if (selectedMetamodelNodeBlock.isPresent)
			return visualiseNodeBlockInMetamodel(selectedMetamodelNodeBlock.get, true)
			
		if (!selectedEntity.isPresent)
			return visualiseOverview(root)

		if (!selectedNodeBlock.isPresent)
			return visualiseEntity(selectedEntity.get)
		
		visualiseNodeBlock(selectedNodeBlock.get, true)
	}

	def visualiseNodeBlock(ModelNodeBlock nb, boolean mainSelection) {
		if (nb.eContainer instanceof Model)
			visualiseNodeBlockInModel(nb, mainSelection)
		else if (nb.eContainer instanceof AtomicPattern)
			visualiseNodeBlockInPattern(nb, mainSelection)
		else if (nb.eContainer instanceof Rule)
			visualiseNodeBlockInRule(nb, mainSelection)
		else if (nb.eContainer instanceof TripleRule)
			visualiseNodeBlockInTripleRule(nb.eContainer as TripleRule, nb, mainSelection)
		else if (nb.eContainer instanceof TripleGrammar) {
			// TODO [Maximilian]
		}
		else if (nb.eContainer instanceof GraphGrammar)
			visualiseNodeBlockInRule(nb, mainSelection)
	}

	def String visualiseOverview(EMSL_Spec root) {
		'''
			left to right direction
			«FOR entity : root.entities»
				«IF entity instanceof Metamodel»
					package "Metamodel: «entity.name»" <<Rectangle>> «link(entity as Entity)» {
						
					}
				«ENDIF»
				«IF entity instanceof Model»
					package "Model: «entity.name»" <<Rectangle>> «link(entity as Entity)» {
						
					}
				«ENDIF»
				«IF entity instanceof Pattern»
					package "Pattern: «entity.body.name»" <<Rectangle>> «link(entity as Entity)» {
						
					}
					«visualiseSuperTypesInPattern(entity)»
				«ENDIF»
				«IF entity instanceof Rule»
					package "Rule: «entity.name»" <<Rectangle>> «link(entity as Entity)» {
						
					}
					«visualiseSuperTypesInRule(entity)»
				«ENDIF»
				«IF entity instanceof TripleRule»
					package "TripleRule: «entity.name»" <<Rectangle>> «link(entity as Entity)» {
						
					}
					«visualiseSuperTypesInTripleRule(entity)»
				«ENDIF»
				«IF entity instanceof TripleGrammar»
					package "TripleGrammar: «entity.name»" <<Rectangle>> «link(entity as Entity)» {
						
					}
				«ENDIF»
				«IF entity instanceof GraphGrammar»
					package "GraphGrammar: «entity.name»" <<Rectangle>> «link(entity as Entity)» {
						
					}
				«ENDIF»
				«IF entity instanceof org.emoflon.neo.emsl.eMSL.Enum»
					package "Enum: «entity.name»" <<Rectangle>> «link(entity as Entity)» {
						
					}
				«ENDIF»
			«ENDFOR»
		'''
	}
	
	def String visualiseSuperTypesInPattern(Pattern entity) {
		'''
			«FOR st : entity.body.superRefinementTypes»
				«IF (st instanceof Pattern)»
					"Pattern: «entity.body.name»"--|>"Pattern: «st.body.name»"
				«ENDIF»
				«IF (st instanceof Rule)»
					"Pattern: "«entity.body.name»"--|>"Rule: «st.name»"
				«ENDIF»
				«IF (st instanceof Model)»
					"Pattern: «entity.body.name»"--|>"Model: «st.name»"
				«ENDIF»
				«IF (st instanceof Metamodel)»
					"Pattern: «entity.body.name»"--|>"Metamodel: «st.name»"
				«ENDIF»
				«IF (st instanceof TripleRule)»
					"Pattern: «entity.body.name»"--|>"TripleRule: «st.name»"
				«ENDIF»
				««« Maybe add more types
			«ENDFOR»
		'''
	}

	def String visualiseSuperTypesInRule(Rule entity) {
		'''
			«FOR st : entity.superRefinementTypes»
				«IF (st instanceof Pattern)»
					"Rule: «entity.name»"--|>"Pattern: «st.body.name»"
				«ENDIF»
				«IF (st instanceof Rule)»
					"Rule: «entity.name»"--|>"Rule: «st.name»"
				«ENDIF»
				«IF (st instanceof Model)»
					"Rule: «entity.name»"--|>"Model: «st.name»"
				«ENDIF»
				«IF (st instanceof Metamodel)»
					"Rule: «entity.name»"--|>"Metamodel: «st.name»"
				«ENDIF»
				«IF (st instanceof TripleRule)»
					"Rule: «entity.name»"--|>"TripleRule: «st.name»"
				«ENDIF»
				««« Maybe add more types
			«ENDFOR»
		'''
	}
	
	def String visualiseSuperTypesInTripleRule(TripleRule entity) {
		'''
			«FOR st : entity.superRefinementTypes»
				«IF (st instanceof Pattern)»
					"TripleRule: «entity.name»"--|>"Pattern: «st.body.name»"
				«ENDIF»
				«IF (st instanceof Rule)»
					"TripleRule: "«entity.name»"--|>"Rule: «st.name»"
				«ENDIF»
				«IF (st instanceof Model)»
					"TripleRule: «entity.name»"--|>"Model: «st.name»"
				«ENDIF»
				«IF (st instanceof Metamodel)»
					"TripleRule: «entity.name»"--|>"Metamodel: «st.name»"
				«ENDIF»
				«IF (st instanceof TripleRule)»
					"TripleRule: «entity.name»"--|>"TripleRule: «st.name»"
				«ENDIF»
				««« Maybe add more types
			«ENDFOR»
		'''
	}

	def dispatch String visualiseEntity(Metamodel entity) {
		'''
			«FOR nb : entity.nodeBlocks»
				«visualiseNodeBlockInMetamodel(nb, false)»
			«ENDFOR»
		'''
	}
	
	def dispatch String visualiseEntity(Model entity) {
		'''
			«FOR nb : entity.nodeBlocks»
				«visualiseNodeBlockInModel(nb, false)»
			«ENDFOR»
		'''
	}
	
	def dispatch String visualiseEntity(Pattern entity) {
		'''
			«FOR nb : entity.body.nodeBlocks»
				«visualiseNodeBlockInPattern(nb, false)»
			«ENDFOR»
			«IF entity.condition !== null »
				legend bottom
					«getConditionString(entity)»
				endlegend
			«ENDIF»
		'''
	}
	
	def String getConditionString(Pattern entity) {
		var text = ""
		
		// return the String for simple Constraints
		if (entity.condition instanceof NegativeConstraint || entity.condition instanceof PositiveConstraint || entity.condition instanceof Implication)
			text += getSimpleConstraintString(entity.condition as ConstraintBody)
			
		// return the String for ConstraintReference
		if (entity.condition instanceof ConstraintReference)
			text += getConstraintReferenceString((entity.condition as ConstraintReference))
		
		
		return text
	}
	
	def String getConstraintReferenceString(ConstraintReference constraint) {
		var text = ""
		if (constraint.reference.body instanceof NegativeConstraint || constraint.reference.body instanceof PositiveConstraint || constraint.reference.body instanceof Implication)
			text += getSimpleConstraintString(constraint.reference.body)
		// OrBody
		else  {
			if (constraint.reference.body !== null) {
				var count = constraint.reference.body.children.size - 1
				for (c : constraint.reference.body.children) {
					text += getOrBodyString(c)
					if (count > 0)
						text += " **||** "
					count--
				}
			}
		}
		return text
	}
	
	def String getOrBodyString(ConstraintBody constraintBody) {
		var text = ""
		
		var count = constraintBody.children.size - 1
		for (c : constraintBody.children) {
			text += getAndBodyString(c)
			if (count > 0)
				text += " **&&** "
			count--
		}
		
		return text
	}
	
	def String getAndBodyString(ConstraintBody constraintBody) {
		var text = ""
		
		if ((constraintBody instanceof ConstraintReference))
			text += getConstraintReferenceString(constraintBody)
		
		var count = constraintBody.children.size - 1
		if (constraintBody.children.size > 1)
			text += " ( "
		for (c : constraintBody.children) {
			text += getPrimaryString(c)
			if (count > 0)
				text += " **||** "
			count--
		}
		if (constraintBody.children.size > 1)
			text += " ) "
		
		return text
	}
	
	def String getPrimaryString(ConstraintBody constraintBody) {
		var text = ""
		
		if (constraintBody.children.get(0) instanceof ConstraintReference) {
			text += getConstraintReferenceString((constraintBody.children.get(0) as ConstraintReference))
		}
		else {
			text += " ( "
			text += getOrBodyString(constraintBody)
			text += " ) "
		}
		
		return text
	}
	
	def String getSimpleConstraintString(ConstraintBody constraintBody) {
		if (constraintBody instanceof NegativeConstraint)
			return '''**forbid** «(constraintBody as NegativeConstraint).pattern.name»'''
		else if (constraintBody instanceof PositiveConstraint)
			return '''**enforce** «(constraintBody as PositiveConstraint).pattern.name»'''
		else if (constraintBody instanceof Implication)
			return '''***if*** «(constraintBody as Implication).premise.name» **then** «(constraintBody as Implication).conclusion.name»'''
	}
		
	def dispatch String visualiseEntity(Rule entity) {
		'''
			«FOR nb : entity.nodeBlocks»
				«visualiseNodeBlockInRule(nb, false)»
			«ENDFOR»
		'''
	}
	
	def dispatch String visualiseEntity(TripleRule entity) {
		'''
			together {
				«FOR snb : entity.srcNodeBlocks»
					«visualiseTripleRuleNodeBlocks(entity, snb, "SRC")»
				«ENDFOR»
				
				«FOR tnb : entity.trgNodeBlocks»
					«visualiseTripleRuleNodeBlocks(entity, tnb, "TRG")»
				«ENDFOR»

				«FOR corr : entity.correspondences»
					"«entity.name».«corr.source.name»:«corr.source.type.name»" ...«IF corr.action !== null»[#SpringGreen]«ENDIF»"«entity.name».«corr.target.name»:«corr.target.type.name»": :«corr.type.name»
				«ENDFOR»
			}
		'''
	}
	
	def dispatch String visualiseEntity(TripleGrammar entity) {
		'''
			together Source {
				«FOR mm : entity.srcMetamodels»
					class "«entity.name».«mm.name»"
				«ENDFOR»
			}
			
			together Target {
				«FOR mm : entity.trgMetamodels»
					class "«entity.name».«mm.name»"
				«ENDFOR»
			}
		'''
	}
	
	def dispatch String visualiseEntity(GraphGrammar entity) {
		'''
			«FOR r : entity.rules»
				class "«entity.name».«r.name»"
			«ENDFOR»
		'''
	}
	
	def dispatch String visualiseEntity(Constraint entity) {
		''''''
	}
	
	def dispatch String visualiseEntity(org.emoflon.neo.emsl.eMSL.Enum entity) {
		'''
			«FOR item : entity.literals»
				class "«entity.name»"
			«ENDFOR»
		'''
	}
	
	def String visualiseEnumItems(org.emoflon.neo.emsl.eMSL.Enum entity) {
		'''
			«FOR item : entity.literals»
				class "«entity.name».«item.name»"
			«ENDFOR»
		'''
	}
	
	def String visualiseTripleRuleNodeBlocks(TripleRule entity, ModelNodeBlock nb, String type) {
		'''class "«entity.name».«nb.name»:«nb.type.name»" «IF nb.action !== null»<<GREEN>>«ENDIF» <<«type»>>
			«FOR link : nb.relations»
				"«entity.name».«nb.name»:«nb.type.name»" -«IF (link.action !== null)»[#SpringGreen]«ENDIF»-> "«entity.name».«link.target.name»:«link.target.type.name»":"«IF link.type.name !== null»«link.type.name»«ELSE»?«ENDIF»"
			«ENDFOR»
		'''
	}
	
	private def labelForClass(MetamodelNodeBlock nb) {
		val entity = nb.eContainer as Metamodel
		'''"«entity?.name».«nb?.name»:EClass"'''
	}

	private def labelForObject(ModelNodeBlock nb) {
		val entity = nb.eContainer as Model
		'''"«entity?.name».«nb?.name»:«nb?.type?.name»"'''
	}
	
	private def labelForPatternComponent(ModelNodeBlock nb) {
		val entity = nb.eContainer as AtomicPattern
		if (entity !== null) {
			if (entity.name === null)
				entity.name = "?"
			if (nb.name === null)
				nb.name = "?"
			if (nb.type.name === null)
				nb.type.name = "?"
				
			'''"«entity.name».«nb.name» : «nb.type.name»"'''	
		}
		else
			'''"?"'''
	}
	
	private def labelForRuleComponent(ModelNodeBlock nb) {
		val entity = nb?.eContainer as Rule
		if (entity !== null) {
			if (entity.name === null)
				entity.name = "?"
			if (nb.name === null)
				nb.name = "?"
			if (nb.type.name === null)
				nb.type.name = "?"
				
			'''"«entity.name».«nb.name» : «nb.type.name»"'''	
		}
		else
			'''"?"'''
	}
	
	private def labelForTripleRuleComponent(ModelNodeBlock nb) {
		val entity = nb.eContainer as TripleRule
		'''"«entity.name».«nb.name» : «IF nb.type.name !== null »«nb.type.name»«ELSE»?«ENDIF»"'''
	}	
	
	def String visualiseNodeBlockInModel(ModelNodeBlock nb, boolean mainSelection) {
		'''
			class «labelForObject(nb)» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR link : nb.relations»
				«labelForObject(nb)» --> «IF link.target !== null»«labelForObject(link.target)»«ELSE»"?"«ENDIF» : «IF (link.type.name !== null && link.type !== null)»«link.type.name»«ELSE»?«ENDIF»
			«ENDFOR»
			«FOR attr : nb.properties»
				«labelForObject(nb)» : «attr.type.name» = «attr.value»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as Model).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relations»
					«IF incomingRef.target == nb && mainSelection»
						«labelForObject(incoming)» --> «labelForObject(nb)» : «IF (incomingRef.type.name !== null && incomingRef.type !== null)»«incomingRef.type.name»«ELSE»?«ENDIF»
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
		'''
	}

	def String visualiseNodeBlockInMetamodel(MetamodelNodeBlock nb, boolean mainSelection) {
		'''
			«IF nb.abstract»abstract«ENDIF» class «labelForClass(nb)» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR sup : nb.superTypes»
				«labelForClass(sup)» <|-- «labelForClass(nb)»
			«ENDFOR»
			«FOR ref : nb.relations»
				«labelForClass(nb)» «IF ref.kind == RelationKind.KOMPOSITION»*«ENDIF»«IF ref.kind == RelationKind.AGGREGATION»o«ENDIF»--> «IF ref.lower !== null»«visualiseMultiplicity(ref)»«ENDIF» «IF ref.target !== null»«labelForClass(ref.target)»«ELSE»"?"«ENDIF» : «IF ref.name !== null»«ref.name»«ELSE»?«ENDIF»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as Metamodel).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relations»
					«IF incomingRef.target == nb && mainSelection»
						«labelForClass(incoming)» «IF incomingRef.kind == RelationKind.KOMPOSITION»*«ENDIF»«IF incomingRef.kind == RelationKind.AGGREGATION»o«ENDIF»--> «IF incomingRef.lower !== null»«visualiseMultiplicity(incomingRef)»«ENDIF» «labelForClass(nb)» : «IF (incomingRef.name !== null)»«incomingRef.name»«ELSE»?«ENDIF»
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
			«FOR attr : nb.properties»
				«labelForClass(nb)» : «attr.name» : «IF (attr.type instanceof UserDefinedType)»«((attr.type as UserDefinedType).reference.eContainer as org.emoflon.neo.emsl.eMSL.Enum).name».«(attr.type as UserDefinedType).reference.name»«ELSE»«(attr.type as BuiltInType).reference.toString»«ENDIF»
			«ENDFOR»
		'''
	}
	
	def String visualiseNodeBlockInPattern(ModelNodeBlock nb, boolean mainSelection) {
		'''
			class «labelForPatternComponent(nb)» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR link : nb.relations»
				«labelForPatternComponent(nb)» --> «labelForPatternComponent(link.target)» : «IF (link.type.name !== null && link.type !== null)»«link.type.name»«ELSE»?«ENDIF»
			«ENDFOR»
			«FOR attr : nb.properties»
				«labelForPatternComponent(nb)» : «attr.type.name» = «attr.value»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as AtomicPattern).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relations»
					«IF incomingRef.target == nb && mainSelection»
						«labelForPatternComponent(incoming)» --> «labelForPatternComponent(nb)» : «IF (incomingRef.type.name !== null && incomingRef.type !== null)»«incomingRef.type.name»«ELSE»?«ENDIF»
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
			«IF (nb.eContainer.eContainer as Pattern).condition !== null »
				legend bottom
					«getConditionString((nb.eContainer.eContainer as Pattern))»
				endlegend
			«ENDIF»
		'''
	}
	
	def String visualiseNodeBlockInRule(ModelNodeBlock nb, boolean mainSelection) {
		'''
			class «labelForRuleComponent(nb)» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR link : nb.relations»
				«IF link.action !== null»
					«labelForRuleComponent(nb)» -«IF link.action.op.toString === '++'»[#SpringGreen]«ELSE»[#red]«ENDIF»-> «labelForRuleComponent(link.target)» : «IF (link.type.name !== null && link.type !== null)»«link.type.name»«ELSE»?«ENDIF»
				«ELSE»«labelForRuleComponent(nb)» --> «labelForRuleComponent(link.target)» : «IF (link.type !== null)»«link.type.name»«ELSE»?«ENDIF»
				«ENDIF»
			«ENDFOR»
			«FOR attr : nb.properties»
				«labelForRuleComponent(nb)» : «attr.type.name» = «attr.value»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as Rule).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relations»
					«IF incomingRef.target == nb && mainSelection»
						«labelForRuleComponent(incoming)» --> «labelForRuleComponent(nb)» : «IF (incomingRef.type.name !== null && incomingRef.type !== null)»«incomingRef.type.name»«ELSE»?«ENDIF»
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
		'''
	}
	
	def String visualiseNodeBlockInTripleRule(TripleRule rule, ModelNodeBlock nb, boolean mainSelection) {
		'''
			class «labelForTripleRuleComponent(nb)» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR link : nb.relations»
				«IF link.action !== null»
					«labelForTripleRuleComponent(nb)» -«IF link.action.op.toString === '++'»[#SpringGreen]«ELSE»[#red]«ENDIF»-> «labelForTripleRuleComponent(link.target)» : «IF (link.type.name !== null && link.type !== null)»«link.type.name»«ELSE»?«ENDIF»
				«ELSE»«labelForTripleRuleComponent(nb)» --> «labelForTripleRuleComponent(link.target)» : «IF (link.type !== null)»«link.type.name»«ELSE»?«ENDIF»
				«ENDIF»
			«ENDFOR»
			«FOR attr : nb.properties»
				«labelForTripleRuleComponent(nb)» : «attr.type.name» = «attr.value»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as TripleRule).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relations»
					«IF incomingRef.target == nb && mainSelection»
						«labelForTripleRuleComponent(incoming)» --> «labelForTripleRuleComponent(nb)» : «IF (incomingRef.type.name !== null && incomingRef.type !== null)»«incomingRef.type.name»«ELSE»?«ENDIF»
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
		'''
	}
	
	def String visualiseMetamodelsOfTripleGrammar(TripleGrammar tg, boolean mainSelection) {
		
	}
	
	def String visualiseRulesOfGraphGrammar(GraphGrammar gg, boolean mainSelection) {
		// TODO [Maximilian]
	}
	
	def visualiseMultiplicity(MetamodelRelationStatement link) {
		'''"«link.lower»«IF link.upper !== null»..«link.upper»«ENDIF»"'''
	}

	def Optional<ModelNodeBlock> determineSelectedNodeBlock(ISelection selection, Entity entity) {
		if (selection instanceof TextSelection) {
			// For the TextSelection documents start with line 0.
			val selectionStart = selection.getStartLine() + 1;
			val selectionEnd = selection.getEndLine() + 1;
			if (!(entity instanceof GraphGrammar || entity instanceof TripleGrammar || entity instanceof Metamodel || entity instanceof org.emoflon.neo.emsl.eMSL.Enum || entity instanceof Constraint))
			for (nodeBlock : entity.nodeBlocks) {
				val object = NodeModelUtils.getNode(nodeBlock);
				if (selectionStart >= object.getStartLine() && selectionEnd <= object.getEndLine()) {
					return Optional.of(nodeBlock)
				}
			}
		}

		Optional.empty()
	}
	
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

	def dispatch getNodeBlocks(Model entity) {
		entity.nodeBlocks
	}
	
	def dispatch getNodeBlocks(Pattern entity) {
		entity.body.nodeBlocks
	}
	
	def dispatch getNodeBlocks(Rule entity) {
		entity.nodeBlocks
	}
	
	def dispatch getNodeBlocks(TripleRule entity) {
		val nodeBlocks = entity.srcNodeBlocks
		nodeBlocks.addAll(entity.trgNodeBlocks)
		return nodeBlocks
	}
	
	private static def link(Entity pattern) {
		val resource = pattern.eResource
		val uri = resource.URI + '#' + resource.getURIFragment(pattern)
		'''[[«uri»]]'''
	}
	
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
		getRoot(editor) instanceof EMSL_Spec
	}

	def getRoot(IEditorPart editor) {
		if (editor instanceof XtextEditor) {
			return editor.document.readOnly([res|res.contents.get(0)])
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
				BackgroundColor White
				ArrowColor Black
				BackgroundColor<<Selection>> PapayaWhip
			}
			
			skinparam package {
				BackgroundColor GhostWhite
				BorderColor LightSlateGray
				Fontcolor LightSlateGray
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
		'''
	}
}
