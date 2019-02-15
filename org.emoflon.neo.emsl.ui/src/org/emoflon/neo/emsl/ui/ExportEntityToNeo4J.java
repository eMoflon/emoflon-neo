package org.emoflon.neo.emsl.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.core.commands.ExpressionContext;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.XtextEditor;
import org.eclipse.xtext.ui.editor.outline.impl.EObjectNode;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.emsl.ui.util.ConsoleUtil;
import org.emoflon.neo.neo4j.adapter.NeoCoreBuilder;

@SuppressWarnings("restriction")
public class ExportEntityToNeo4J extends AbstractHandler {

	private static String uri = "bolt://localhost:7687";
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
			bootstrapNeoCoreIfNecessary(activePage, builder);
			ConsoleUtil.printDash(activePage);

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
		if (entity instanceof Metamodel) {
			var metamodel = (Metamodel) entity;
			ResourceSet rs = metamodel.eResource().getResourceSet();
			EcoreUtil.resolveAll(rs);

			ConsoleUtil.printInfo(activePage, "Trying to export " + metamodel.getName() + " as a metamodel...");

			var metamodels = new ArrayList<Metamodel>();
			rs.getAllContents().forEachRemaining(c -> {
				if (c instanceof Metamodel)
					metamodels.add((Metamodel) c);
			});

			var metamodelNames = metamodels.stream().map(Metamodel::getName).collect(Collectors.joining(","));
			ConsoleUtil.printInfo(activePage, "As a consequence, now trying to export metamodels: " + metamodelNames);
			var newMetamodels = builder.removeExisting(metamodels);

			for (Metamodel mm : metamodels) {
				if (!newMetamodels.contains(mm))
					ConsoleUtil.printInfo(activePage,
							"Skipping metamodel " + mm.getName() + " as it is already present.");
			}

			if (!newMetamodels.isEmpty())
				builder.exportMetamodelsToNeo4j(metamodels);
			ConsoleUtil.printInfo(activePage, "Done.");
		}
		if (entity instanceof Model) {
			var model = (Model) entity;
			ConsoleUtil.printInfo(activePage, "Trying to export " + model.getName() + " as a model...");
			builder.exportModelToNeo4j(model);
			ConsoleUtil.printInfo(activePage, "Done.");
		}
	}

	private void bootstrapNeoCoreIfNecessary(IWorkbenchPage activePage, NeoCoreBuilder builder) {
		if (builder.ecoreIsNotPresent()) {
			ConsoleUtil.printInfo(activePage, "Trying to bootstrap NeoCore...");
			builder.bootstrapNeoCore();
			ConsoleUtil.printInfo(activePage, "Done.");
		} else {
			ConsoleUtil.printInfo(activePage, "NeoCore is already present.");
		}
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
