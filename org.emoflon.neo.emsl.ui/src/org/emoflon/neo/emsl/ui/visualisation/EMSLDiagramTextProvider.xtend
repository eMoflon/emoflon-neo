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

		visualiseNodeBlock(selectedNodeBlock.get)
	}

	def visualiseNodeBlock(NodeBlock nb) {
		if (nb.eContainer instanceof Metamodel)
			visualiseNodeBlockInMetamodel(nb)
		else
			visualiseNodeBlockInModel(nb)
	}

	def String visualiseNodeBlockInModel(NodeBlock block) {
		// TODO
		'''
			center footer
				= Object: «block.name»
			end footer
		'''
	}

	def String visualiseOverview(EMSL_Spec root) {
		'''
			«FOR entity : root.entities»
				«IF entity instanceof Metamodel»
					package "«entity.name»" {
					  
					}
				«ENDIF»
				«IF entity instanceof Model»
					package "«entity.name»" <<Rectangle>> {
					  
					}
				«ENDIF»
			«ENDFOR»
		'''
	}

	def dispatch String visualiseEntity(Metamodel entity) {
		'''
			«FOR nb : entity.nodeBlocks»
				«visualiseNodeBlockInMetamodel(nb)»
			«ENDFOR»
		'''
	}

	private def labelForClass(NodeBlock nb) {
		val entity = nb.eContainer as Metamodel
		'''"«entity.name».«nb.name»:«nb.type.name»"'''
	}

	def dispatch String visualiseEntity(Model entity) {
		// TODO
		'''
			center footer
				= Model: «entity.name»
			end footer
		'''
	}

	def String visualiseNodeBlockInMetamodel(NodeBlock nb) {
		'''
			«IF nb.abstract»abstract«ENDIF» class «labelForClass(nb)»
			«FOR sup : nb.superTypes»
				«labelForClass(sup)» <|-- «labelForClass(nb)»
			«ENDFOR»
			«FOR ref : nb.relationStatements»
				«labelForClass(nb)» --> «labelForClass(ref.value)» : «ref.name»
			«ENDFOR»
			«FOR attr : nb.propertyStatements»
				«labelForClass(nb)» : «attr.name» : «attr.value»
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
			
			skinparam shadowing false
			skinparam StereotypeABackgroundColor White
			skinparam StereotypeCBackgroundColor White
			
			skinparam class {
				BorderColor Black
				BackgroundColor White
				ArrowColor Black
				StereotypeABackgroundColor White
				StereotypeCBackgroundColor White
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
