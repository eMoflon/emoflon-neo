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

		if (!selectedEntity.isPresent)
			return visualiseOverview(root)

		if (!selectedNodeBlock.isPresent)
			return visualiseEntity(selectedEntity.get)

		visualiseNodeBlock(selectedNodeBlock.get, true)
	}

	def visualiseNodeBlock(NodeBlock nb, boolean mainSelection) {
		if (nb.eContainer instanceof Metamodel)
			visualiseNodeBlockInMetamodel(nb, mainSelection)
		else if (nb.eContainer instanceof Model)
			visualiseNodeBlockInModel(nb, mainSelection)
		else if (nb.eContainer instanceof Pattern)
			visualiseNodeBlockInPattern(nb, mainSelection)
		else if (nb.eContainer instanceof Rule)
			visualiseNodeBlockInRule(nb.eContainer as Rule, nb, mainSelection)
		else if (nb.eContainer instanceof TripleRule)
			visualiseNodeBlockInTripleRule(nb.eContainer as TripleRule, nb, mainSelection)
	}

	def String visualiseOverview(EMSL_Spec root) {
		'''
			«FOR entity : root.entities»
				«IF entity instanceof Metamodel»
					package "Metamodel: «entity.name»" <<Rectangle>>  {
					  
					}
				«ENDIF»
				«IF entity instanceof Model»
					package "Model: «entity.name»" <<Rectangle>> {
					  
					}
				«ENDIF»
				«IF entity instanceof Pattern»
					package "Pattern: «entity.name»" <<Rectangle>> {
						
					}
				«ENDIF»
				«IF entity instanceof Rule»
					package "Rule: «entity.name»" <<Rectangle>> {
						
					}
				«ENDIF»
				«IF entity instanceof TripleRule»
					package "TripleRule: «entity.name»" <<Rectangle>> {
						
					}
				«ENDIF»
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
				«visualiseNodeBlockInRule(entity, nb, false)»
			«ENDFOR»
		'''
	}
	
	def dispatch String visualiseEntity(TripleRule entity) {
		'''
				«FOR nb : entity.nodeBlocks»
					«visualiseNodeBlockInTripleRule(entity, nb, false)»
				«ENDFOR»
		'''
	}

	private def labelForClass(NodeBlock nb) {
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
				«labelForObject(nb)» --> «labelForObject(link.value)» : «link.name»
			«ENDFOR»
			«FOR attr : nb.propertyStatements»
				«labelForObject(nb)» : «attr.name» = «attr.value»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as Model).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relationStatements»
					«IF incomingRef.value == nb && mainSelection»
						«labelForObject(incoming)» --> «labelForObject(nb)» : «incomingRef.name»
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
		'''
	}

	def String visualiseNodeBlockInMetamodel(NodeBlock nb, boolean mainSelection) {
		'''
			«IF nb.abstract»abstract«ENDIF» class «labelForClass(nb)» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR sup : nb.superTypes»
				«labelForClass(sup)» <|-- «labelForClass(nb)»
			«ENDFOR»
			«FOR ref : nb.relationStatements»
				«labelForClass(nb)» --> «labelForClass(ref.value)» : «ref.name»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as Metamodel).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relationStatements»
					«IF incomingRef.value == nb && mainSelection»
						«labelForClass(incoming)» --> «labelForClass(nb)» : «incomingRef.name»
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
			«FOR attr : nb.propertyStatements»
				«labelForClass(nb)» : «attr.name» : «attr.value»
			«ENDFOR»
		'''
	}
	
	def String visualiseNodeBlockInPattern(NodeBlock nb, boolean mainSelection) {
		'''
			class «labelForPatternComponent(nb)» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR link : nb.relationStatements»
				«labelForPatternComponent(nb)» --> «labelForPatternComponent(link.value)» : «link.name»
			«ENDFOR»
			«FOR attr : nb.propertyStatements»
				«labelForPatternComponent(nb)» : «attr.name» = «attr.value»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as Pattern).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relationStatements»
					«IF incomingRef.value == nb && mainSelection»
						«labelForPatternComponent(incoming)» --> «labelForPatternComponent(nb)» : «incomingRef.name»
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
		'''
	}
	
	def String visualiseNodeBlockInRule(Rule rule, NodeBlock nb, boolean mainSelection) {
		'''
			«IF nb.abstract»abstract«ENDIF»class «labelForRuleComponent(nb)» «IF mainSelection»<<Selection>>«ENDIF»
			«FOR link : nb.relationStatements»
				«IF link.action !== null»
					«labelForRuleComponent(nb)» -«IF link.action.op.toString === '++'»[#green]«ELSE»[#red]«ENDIF»-> «labelForRuleComponent(link.value)» : «IF link.action.op.toString === '++'»<color:green>++«ELSE»<color:red>--«ENDIF» «link.name»
				«ELSE»«labelForRuleComponent(nb)» --> «labelForRuleComponent(link.value)» : «IF (link.name !== null)»«link.name»«ELSE»?«ENDIF»
				«ENDIF»
			«ENDFOR»
			«FOR attr : nb.propertyStatements»
				«labelForRuleComponent(nb)» : «attr.name» = «attr.value»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as Rule).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relationStatements»
					«IF incomingRef.value == nb && mainSelection»
						«labelForRuleComponent(incoming)» --> «labelForRuleComponent(nb)» : «incomingRef.name»
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
					«labelForTripleRuleComponent(nb)» -«IF link.action.op.toString === '++'»[#green]«ELSE»[#red]«ENDIF»-> «labelForTripleRuleComponent(link.value)» : «IF link.action.op.toString === '++'»<color:green>++«ELSE»<color:red>--«ENDIF» «link.name»
				«ELSE»«labelForTripleRuleComponent(nb)» --> «labelForTripleRuleComponent(link.value)» : «IF (link.name !== null)»«link.name»«ELSE»?«ENDIF»
				«ENDIF»
			«ENDFOR»
			«FOR attr : nb.propertyStatements»
				«labelForTripleRuleComponent(nb)» : «attr.name» = «attr.value»
			«ENDFOR»
			«FOR incoming : (nb.eContainer as TripleRule).nodeBlocks.filter[n|n != nb]»
				«FOR incomingRef : incoming.relationStatements»
					«IF incomingRef.value == nb && mainSelection»
						«labelForTripleRuleComponent(incoming)» --> «labelForTripleRuleComponent(nb)» : «incomingRef.name»
					«ENDIF»
				«ENDFOR»
			«ENDFOR»
		'''
	}

	def Optional<NodeBlock> determineSelectedNodeBlock(ISelection selection, Entity entity) {
		if (selection instanceof TextSelection) {
			// For the TextSelection documents start with line 0.
			val selectionStart = selection.getStartLine() + 1;
			val selectionEnd = selection.getEndLine() + 1;

			for (nodeBlock : entity.nodeBlocks) {
				val object = NodeModelUtils.getNode(nodeBlock);
				if (selectionStart >= object.getStartLine() && selectionEnd <= object.getEndLine()) {
					return Optional.of(nodeBlock)
				}
			}
		}

		Optional.empty()
	}

	def dispatch getNodeBlocks(Metamodel entity) {
		entity.nodeBlocks
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
