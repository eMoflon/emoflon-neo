package org.emoflon.neo.emsl.ui;

import java.util.List;
import java.util.Optional;

import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.outline.impl.EObjectNode;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.emsl.eMSL.Entity;
import org.emoflon.neo.emsl.ui.internal.EmslActivator;
import org.emoflon.neo.emsl.ui.util.ENeoConsole;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.emsl.util.FlattenerException;

public class ExportEntityToNeo4J extends AbstractHandler {
	private Optional<EObjectNode> eobNode = Optional.empty();
	private NeoCoreBuilder builder;

	private static final Logger logger = Logger.getLogger(ExportEntityToNeo4J.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchPage activePage = HandlerUtil.getActiveWorkbenchWindow(event).getActivePage();
		ENeoConsole.setActivePage(activePage);

		logger.info("Trying to connect to your Neo4j database...");

		String uri = EmslActivator.getInstance().getPreferenceStore().getString(EMSLUtil.P_URI);
		String userName = EmslActivator.getInstance().getPreferenceStore().getString(EMSLUtil.P_USER);
		String password = EmslActivator.getInstance().getPreferenceStore().getString(EMSLUtil.P_PASSWORD);

		logger.info("Connection URI: " + uri);
		logger.info("User: " + userName);
		logger.info("Password: " + password);

		try {
			builder = new NeoCoreBuilder(uri, userName, password);

			logger.info("Great!  Seems to have worked.");

			logger.info("Now performing export...");
			exportSelectedEMSLEntity(event, builder);
			builder.close();
		} catch (Exception e) {
			logger.error(e);
			logger.info("Sorry, something went wrong.");
			e.printStackTrace();
		}

		return null;
	}

	private void exportSelectedEMSLEntity(ExecutionEvent event, NeoCoreBuilder builder)
			throws ExecutionException, FlattenerException {
		IEditorPart editorPart = HandlerUtil.getActiveEditorChecked(event);
		if (editorPart instanceof XtextEditor) {
			XtextEditor editor = (XtextEditor) editorPart;
			var emslEntity = editor.getDocument().readOnly(new IUnitOfWork<Optional<Entity>, XtextResource>() {
				@Override
				public Optional<Entity> exec(XtextResource state) throws Exception {
					return eobNode.map(n -> {
						var o = n.getEObject(state);
						if (o instanceof Entity)
							return (Entity) o;
						else
							return null;
					});
				}
			});

			if (emslEntity.isPresent())
				exportEMSLEntityToNeo4j(emslEntity.get());
		}
	}

	private void exportEMSLEntityToNeo4j(Entity entity) throws FlattenerException {
		builder.exportEMSLEntityToNeo4j(entity);
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
