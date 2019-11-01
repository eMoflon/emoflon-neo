package org.emoflon.neo.emsl.ui;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.outline.impl.EObjectNode;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.Rule;
import org.emoflon.neo.emsl.ui.util.ENeoConsole;
import org.emoflon.neo.neo4j.adapter.constraints.NeoConstraintFactory;
import org.emoflon.neo.neo4j.adapter.patterns.NeoPatternFactory;
import org.emoflon.neo.neo4j.adapter.rules.NeoRuleFactory;

@SuppressWarnings("restriction")
public class CreateCypherQuery extends AbstractHandler {
	private Optional<EObjectNode> eobNode = Optional.empty();
	private static final Logger logger = Logger.getLogger(CreateCypherQuery.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPage activePage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		ENeoConsole.setActivePage(activePage);

		try {
			createQueryFromEMSLEntity(event);
		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.info("Sorry, something went wrong.");
			e.printStackTrace();
		}

		return null;
	}

	private void createQueryFromEMSLEntity(ExecutionEvent event) throws ExecutionException {
		IEditorPart editorPart = HandlerUtil.getActiveEditorChecked(event);
		if (editorPart instanceof XtextEditor) {
			XtextEditor editor = (XtextEditor) editorPart;
			logger.debug("Extracting selection from editor");
			var emslEntity = editor.getDocument().readOnly(new IUnitOfWork<Optional<EObject>, XtextResource>() {
				@Override
				public Optional<EObject> exec(XtextResource state) throws Exception {
					return eobNode.map(n -> n.getEObject(state));
				}
			});
			logger.debug("Extracted: " + emslEntity);
			emslEntity.ifPresent(this::createCypherQueryFromSelection);
		}
	}

	private void createCypherQueryFromSelection(EObject selection) {
		if (selection instanceof AtomicPattern) {
			var pattern = (Pattern) (((AtomicPattern) selection).eContainer());
			var neoPattern = NeoPatternFactory.createNeoPattern(pattern);
			copyStringToClipboard(neoPattern.getQuery());

		} else if (selection instanceof Constraint) {
			var constraint = NeoConstraintFactory.createNeoConstraint((Constraint) selection);
			copyStringToClipboard(constraint.getQuery());
		} else if (selection instanceof Rule) {
			var rule = NeoRuleFactory.createNeoRule((Rule) selection);
			copyStringToClipboard(rule.getQuery());
		} else
			throw new IllegalArgumentException("This type of selection cannot be exported: " + selection);
	}

	@Override
	public void setEnabled(Object evaluationContext) {
		if (evaluationContext instanceof ExpressionContext) {
			ExpressionContext context = (ExpressionContext) evaluationContext;
			Object sel = context.getDefaultVariable();
			if (sel instanceof List<?>) {
				List<?> nodes = (List<?>) sel;
				if (nodes.size() == 1) {
					Object o = nodes.get(0);
					if (o instanceof EObjectNode) {
						eobNode = Optional.of((EObjectNode) o);
						eobNode.ifPresent(n -> setBaseEnabled(NeoCoreBuilder.canBeCopiedToClipboard(n.getEClass())));
						return;
					}
				}
			}
		}

		setBaseEnabled(false);
	}

	private static void copyStringToClipboard(String query) {
		var toolkit = Toolkit.getDefaultToolkit();
		var clipboard = toolkit.getSystemClipboard();
		var strSel = new StringSelection(query);
		clipboard.setContents(strSel, strSel);
		logger.info("Cyper query is now on your clipboard");
	}
}
