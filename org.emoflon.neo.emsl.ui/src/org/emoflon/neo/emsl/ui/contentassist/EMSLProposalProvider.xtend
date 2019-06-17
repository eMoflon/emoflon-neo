/*
 * generated by Xtext 2.16.0
 */
package org.emoflon.neo.emsl.ui.contentassist

import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.Assignment
import org.eclipse.xtext.ui.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ui.editor.contentassist.ICompletionProposalAcceptor
import org.emoflon.neo.emsl.EMSLFlattener
import java.util.ArrayList
import org.emoflon.neo.emsl.eMSL.Pattern
import org.eclipse.emf.ecore.util.EcoreUtil
import org.emoflon.neo.emsl.eMSL.RefinementCommand
import org.emoflon.neo.emsl.eMSL.AtomicPattern

/**
 * See https://www.eclipse.org/Xtext/documentation/304_ide_concepts.html#content-assist
 * on how to customize the content assistant.
 */
class EMSLProposalProvider extends AbstractEMSLProposalProvider {
	
	override completeModelRelabelingCommand_OldLabel(
		EObject entity, Assignment assignment, 
  		ContentAssistContext context, ICompletionProposalAcceptor acceptor) {
		
		var collection = new ArrayList<EObject> ()
		collection.addAll(((entity as RefinementCommand).referencedType.eContainer as Pattern).eAllContents.toSet.toList)
		collection.add((entity as RefinementCommand).referencedType.eContainer as Pattern)
		
		var collectionCopy = EcoreUtil.copyAll(collection)
		
		for (c : collectionCopy) {
			if (c instanceof Pattern) {
				for (nb : new EMSLFlattener().flattenPattern(c as Pattern, new ArrayList<String>()).body.nodeBlocks) {
					acceptor.accept(createCompletionProposal(nb.name, context))
				}
			}
		}
		collectionCopy.forEach[c | EcoreUtil.delete(c)]
	
	}
	
}