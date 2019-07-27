/*
 * generated by Xtext 2.16.0
 */
package org.emoflon.neo.emsl.ui.contentassist

import java.util.HashSet
import org.eclipse.core.resources.IFile
import org.eclipse.core.resources.IResource
import org.eclipse.core.resources.IResourceVisitor
import org.eclipse.core.resources.ResourcesPlugin
import org.eclipse.core.runtime.CoreException
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.xtext.Assignment
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor
import org.emoflon.neo.emsl.eMSL.AtomicPattern
import org.emoflon.neo.emsl.eMSL.Pattern
import org.emoflon.neo.emsl.eMSL.RefinementCommand
import org.emoflon.neo.emsl.eMSL.SuperType
import org.emoflon.neo.emsl.refinement.EMSLFlattener
import org.emoflon.neo.emsl.util.EntityAttributeDispatcher

/**
 * See https://www.eclipse.org/Xtext/documentation/304_ide_concepts.html#content-assist
 * on how to customize the content assistant.
 */
class EMSLProposalProvider extends AbstractEMSLProposalProvider {
	
	override completeModelRelabelingCommand_OldLabel(
			EObject entity, Assignment assignment, 
  			ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		if ((entity as RefinementCommand).referencedType.eContainer instanceof Pattern) {
			for (nb : new EntityAttributeDispatcher().getNodeBlocks(EMSLFlattener.flatten(EcoreUtil.copy((entity as RefinementCommand).referencedType)))) {
				acceptor.accept(createCompletionProposal(nb.name, context))
				for (relation : nb.relations){
					if (relation.name !== null) {
						acceptor.accept(createCompletionProposal(relation.name, context))
					}
				}
			}
		} else {
			for (nb : new EntityAttributeDispatcher().getNodeBlocks(EMSLFlattener.flatten(EcoreUtil.copy((entity as RefinementCommand).referencedType)))) {
				acceptor.accept(createCompletionProposal(nb.name, context))
				for (relation : nb.relations){
					if (relation.name !== null) {
						acceptor.accept(createCompletionProposal(relation.name, context))
					}
				}
			}
		}
	}
	
	override completeModelRelationStatement_ProxyTarget(
			EObject entity, Assignment assignemnt,
			ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
			
		for (refinement : (entity.eContainer.eContainer as AtomicPattern).superRefinementTypes) {
			for (relabeling : refinement.relabeling) {
				acceptor.accept(createCompletionProposal(relabeling.newLabel, context))
			}
		}
	}
	
	override completeModelRelationStatement_Target(
			EObject entity, Assignment assignment,
			ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
			
		super.completeModelRelationStatement_Target(entity, assignment, context, acceptor)
		if (entity.eContainer.eContainer instanceof AtomicPattern) {
			for (refinement : new EntityAttributeDispatcher().getSuperRefinementTypes(entity.eContainer.eContainer as SuperType)) {
				for (relabeling : (refinement as RefinementCommand).relabeling) {
					acceptor.accept(createCompletionProposal("$" + relabeling.newLabel, context))
				}
			}
		} else {
			for (refinement : new EntityAttributeDispatcher().getSuperRefinementTypes(entity.eContainer.eContainer as SuperType)) {
				for (relabeling : (refinement as RefinementCommand).relabeling) {
					acceptor.accept(createCompletionProposal("$" + relabeling.newLabel, context))
				}
			}
		}
	}
	
	override completeModelNodeBlock_Name(
			EObject entity, Assignment assignment,
			ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		
		super.completeModelNodeBlock_Name(entity, assignment, context, acceptor)
		if (entity instanceof AtomicPattern) {
			for (nb : new EntityAttributeDispatcher().getNodeBlocks(EMSLFlattener.flatten(entity))) {
				acceptor.accept(createCompletionProposal(nb.name, context))
			}
		} else {
			for (nb : new EntityAttributeDispatcher().getNodeBlocks(EMSLFlattener.flatten(entity as SuperType))) {
				acceptor.accept(createCompletionProposal(nb.name, context))
			}
		}
	}
	
	override completeImportStatement_Value(
			EObject model, Assignment assignment, 
			ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		
		super.completeImportStatement_Value(model, assignment, context, acceptor)
		var prep = "\"platform:/resoure"
		val files = new HashSet<IResource>()
		var root = ResourcesPlugin.workspace.root
		root.accept(new IResourceVisitor() {
			override visit(IResource resource) throws CoreException {
				if (resource.type === IResource.FILE) {
					if (resource.name.contains(".msl")) {
						files.add(resource as IFile);
					}
				}
				return true;
			}
		})
		val filteredFiles = files.filter[f | !f.fullPath.toString.contains("/bin/")]
		for (f : filteredFiles) {
			acceptor.accept(createCompletionProposal(prep + f.fullPath.toString + "\"", context))
		}
	}	
}