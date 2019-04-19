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
import org.emoflon.neo.emsl.eMSL.NodeBlock
import org.emoflon.neo.emsl.eMSL.Pattern
import org.emoflon.neo.emsl.eMSL.Rule
import org.emoflon.neo.emsl.eMSL.TripleRule
import org.emoflon.neo.emsl.eMSL.TripleGrammar
import org.emoflon.neo.emsl.eMSL.GraphGrammar
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock
import org.emoflon.neo.emsl.eMSL.RelationStatement
import org.emoflon.neo.emsl.eMSL.MetamodelRelationStatement

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
		val Optional<NodeBlock> selectedNodeBlock = selectedEntity.flatMap([e|determineSelectedNodeBlock(selection, e)])
		val Optional<MetamodelNodeBlock> selectedMetamodelNodeBlock = selectedEntity.flatMap([e|determineSelectedMetamodelNodeBlock(selection, e)])

		if (selectedMetamodelNodeBlock.isPresent)
			return visualiseNodeBlockInMetamodel(selectedMetamodelNodeBlock.get, true)
			
		if (!selectedEntity.isPresent)
			return visualiseOverview(root)

		if (!selectedNodeBlock.isPresent)
			return visualiseEntity(selectedEntity.get)

		visualiseNodeBlock(selectedNodeBlock.get, true)
	}

	def visualiseNodeBlock(NodeBlock nb, boolean mainSelection) {
		if (nb.eContainer instanceof Model)
			visualiseNodeBlockInModel(nb, mainSelection)
		else if (nb.eContainer instanceof Pattern)
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
					package "Pattern: «entity.name»" <<Rectangle>> «link(entity as Entity)» {
						
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
			«ENDFOR»
		'''
	}
	
	def String visualiseSuperTypesInPattern(Pattern entity) {
		'''
			«FOR st : entity.superTypes»
				«IF (st instanceof Pattern)»
					"Pattern: «entity.name»"--|>"Pattern: «st.name»"
				«ENDIF»
				«IF (st instanceof Rule)»
					"Pattern: "«entity.name»"--|>"Rule: «st.name»"
				«ENDIF»
				«IF (st instanceof Model)»
					"Pattern: «entity.name»"--|>"Model: «st.name»"
				«ENDIF»
				«IF (st instanceof Metamodel)»
					"Pattern: «entity.name»"--|>"Metamodel: «st.name»"
				«ENDIF»
				«IF (st instanceof TripleRule)»
					"Pattern: «entity.name»"--|>"TripleRule: «st.name»"
				«ENDIF»
				««« Maybe add more types
			«ENDFOR»
		'''
	}

	def String visualiseSuperTypesInRule(Rule entity) {
		'''
			«FOR st : entity.superTypes»
				«IF (st instanceof Pattern)»
					"Rule: «entity.name»"--|>"Pattern: «st.name»"
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
			«FOR st : entity.superTypes»
				«IF (st instanceof Pattern)»
					"TripleRule: «entity.name»"--|>"Pattern: «st.name»"
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
			«FOR nb : entity.nodeBlocks»
				«visualiseNodeBlockInPattern(nb, false)»
			«ENDFOR»
		'''
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
	
	def String visualiseTripleRuleNodeBlocks(TripleRule entity, NodeBlock nb, String type) {
		'''class "«entity.name».«nb.name»:«nb.type.name»" «IF nb.action !== null»<<GREEN>>«ENDIF» <<«type»>>
			«FOR link : nb.relationStatements»
				"«entity.name».«nb.name»:«nb.type.name»" -«IF (link.action !== null)»[#SpringGreen]«ENDIF»-> "«entity.name».«link.value.name»:«link.value.type.name»":"«IF link.relationName.name !== null»«link.relationName.name»«ELSE»?«ENDIF»"
			«ENDFOR»
		'''
	}
	
	private def labelForClass(MetamodelNodeBlock nb) {
		val entity = nb.eContainer as Metamodel
		'''"«entity?.name».«nb?.name»:«nb?.type?.name»"'''
	}

	private def labelForObject(NodeBlock nb) {
		val entity = nb.eContainer as Model
		'''"«entity?.name».«nb?.name»:«nb?.type?.name»"'''
	}
	
	private def labelForPatternComponent(NodeBlock nb) {
		val entity = nb.eContainer as Pattern
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
	
	private def labelForRuleComponent(NodeBlock nb) {
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
	
	private def labelForTripleRuleComponent(NodeBlock nb) {
		val entity = nb.eContainer as TripleRule
		'''"«entity.name».«nb.name» : «IF nb.type.name !== null »«nb.type.name»«ELSE»?«ENDIF»"'''
	}	
	
	def String visualiseNodeBlockInModel(NodeBlock nb, boolean mainSelection) {
		'''
			class «labelForObject(nb)» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR link : nb.relationStatements»
				«labelForObject(nb)» --> «IF link.value !== null»«labelForObject(link.value)»«ELSE»"?"«ENDIF» : «link.relationName.name»
			«ENDFOR»
			«FOR attr : nb.propertyStatements»
				«labelForObject(nb)» : «attr.propertyName.name» = «attr.value»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as Model).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relationStatements»
					«IF incomingRef.value == nb && mainSelection»
						«labelForObject(incoming)» --> «labelForObject(nb)» : «incomingRef.relationName.name»
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
			«FOR ref : nb.metamodelRelationStatements»
				«labelForClass(nb)» «IF ref.relationType == '<+>'»*«ENDIF»«IF ref.relationType == '<>'»o«ENDIF»--> «IF ref.constantLowerBound !== null»«visualiseMultiplicity(ref)»«ENDIF» «IF ref.value !== null»«labelForClass(ref.value)»«ELSE»"?"«ENDIF» : «ref.name»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as Metamodel).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.metamodelRelationStatements»
					«IF incomingRef.value == nb && mainSelection»
						«labelForClass(incoming)» --> «labelForClass(nb)» : «incomingRef.name»
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
			«FOR attr : nb.metamodelPropertyStatements»
				«labelForClass(nb)» : «attr.name» : «attr.value»
			«ENDFOR»
		'''
	}
	
	def String visualiseNodeBlockInPattern(NodeBlock nb, boolean mainSelection) {
		'''
			class «labelForPatternComponent(nb)» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR link : nb.relationStatements»
				«labelForPatternComponent(nb)» --> «labelForPatternComponent(link.value)» : «link.relationName.name»
			«ENDFOR»
			«FOR attr : nb.propertyStatements»
				«labelForPatternComponent(nb)» : «attr.propertyName.name» = «attr.value»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as Pattern).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relationStatements»
					«IF incomingRef.value == nb && mainSelection»
						«labelForPatternComponent(incoming)» --> «labelForPatternComponent(nb)» : «incomingRef.relationName.name»
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
		'''
	}
	
	def String visualiseNodeBlockInRule(NodeBlock nb, boolean mainSelection) {
		'''
			«IF nb.abstract»abstract«ENDIF»class «labelForRuleComponent(nb)» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR link : nb.relationStatements»
				«IF link.action !== null»
					«labelForRuleComponent(nb)» -«IF link.action.op.toString === '++'»[#SpringGreen]«ELSE»[#red]«ENDIF»-> «labelForRuleComponent(link.value)» : «link.relationName.name»
				«ELSE»«labelForRuleComponent(nb)» --> «labelForRuleComponent(link.value)» : «IF (link.relationName !== null)»«link.relationName.name»«ELSE»?«ENDIF»
				«ENDIF»
			«ENDFOR»
			«FOR attr : nb.propertyStatements»
				«labelForRuleComponent(nb)» : «attr.propertyName.name» = «attr.value»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as Rule).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relationStatements»
					«IF incomingRef.value == nb && mainSelection»
						«labelForRuleComponent(incoming)» --> «labelForRuleComponent(nb)» : «incomingRef.relationName.name»
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
		'''
	}
	
	def String visualiseNodeBlockInTripleRule(TripleRule rule, NodeBlock nb, boolean mainSelection) {
		'''
			class «labelForTripleRuleComponent(nb)» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR link : nb.relationStatements»
				«IF link.action !== null»
					«labelForTripleRuleComponent(nb)» -«IF link.action.op.toString === '++'»[#SpringGreen]«ELSE»[#red]«ENDIF»-> «labelForTripleRuleComponent(link.value)» : «link.relationName.name»
				«ELSE»«labelForTripleRuleComponent(nb)» --> «labelForTripleRuleComponent(link.value)» : «IF (link.relationName !== null)»«link.relationName»«ELSE»?«ENDIF»
				«ENDIF»
			«ENDFOR»
			«FOR attr : nb.propertyStatements»
				«labelForTripleRuleComponent(nb)» : «attr.propertyName.name» = «attr.value»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as TripleRule).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relationStatements»
					«IF incomingRef.value == nb && mainSelection»
						«labelForTripleRuleComponent(incoming)» --> «labelForTripleRuleComponent(nb)» : «incomingRef.relationName.name»
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
		'''"«link.constantLowerBound»«IF link.upperBound !== null»..«link.upperBound»«ENDIF»"'''
	}

	def Optional<NodeBlock> determineSelectedNodeBlock(ISelection selection, Entity entity) {
		if (selection instanceof TextSelection) {
			// For the TextSelection documents start with line 0.
			val selectionStart = selection.getStartLine() + 1;
			val selectionEnd = selection.getEndLine() + 1;
			if (!(entity instanceof GraphGrammar || entity instanceof TripleGrammar || entity instanceof Metamodel))
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
		entity.nodeBlocks
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
