package org.emoflon.neo.emsl.ui;

import java.util.List;
import java.util.Optional;

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
import org.emoflon.neo.emsl.ui.util.ConsoleUtil;
import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;

@SuppressWarnings("restriction")
public class ExportEntityToNeo4J extends AbstractHandler {

	private static String uri = "bolt://localhost:11007";
	private static String userName = "neo4j";
	private static String password = "test";
	private Optional<EObjectNode> eobNode = Optional.empty();
	private IWorkbenchPage activePage;
	private NeoCoreBuilder builder;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		activePage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();

		ConsoleUtil.printBar(activePage);
		ConsoleUtil.printInfo(activePage, "Trying to connect to your Neo4j database...");

		ConsoleUtil.printInfo(activePage, "Connection URI: " + uri);
		ConsoleUtil.printInfo(activePage, "User: " + userName);
		ConsoleUtil.printInfo(activePage, "Password: " + password);

		try {
			builder = new NeoCoreBuilder(uri, userName, password);

			ConsoleUtil.printInfo(activePage, "Great!  Seems to have worked.");
			ConsoleUtil.printDash(activePage);

			ConsoleUtil.printInfo(activePage, "Now performing export...");
			exportSelectedEMSLEntity(event, builder);

			builder.close();
		} catch (Exception e) {
			ConsoleUtil.printError(activePage, e.getMessage());
			ConsoleUtil.printInfo(activePage, "Sorry, something went wrong.");
			e.printStackTrace();
		}

		return null;
	}

	private void exportSelectedEMSLEntity(ExecutionEvent event, NeoCoreBuilder builder) throws ExecutionException {
		IEditorPart editorPart = HandlerUtil.getActiveEditorChecked(event);
		if (editorPart instanceof XtextEditor) {
			XtextEditor editor = (XtextEditor) editorPart;
			var emslEntity = editor.getDocument().readOnly(new IUnitOfWork<Optional<EObject>, XtextResource>() {
				@Override
				public Optional<EObject> exec(XtextResource state) throws Exception {
					return eobNode.map(n -> n.getEObject(state));
				}
			});

			emslEntity.ifPresent(this::exportEMSLEntityToNeo4j);
		}
	}

	private void exportEMSLEntityToNeo4j(EObject entity) {
		builder.exportEMSLEntityToNeo4j(entity, s -> ConsoleUtil.printInfo(activePage, s));
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
						eobNode.ifPresent(n -> setBaseEnabled(NeoCoreBuilder.canBeExported(n.getEClass())));
						return;
					}
				}
			}
		}

		setBaseEnabled(false);
	}
}
