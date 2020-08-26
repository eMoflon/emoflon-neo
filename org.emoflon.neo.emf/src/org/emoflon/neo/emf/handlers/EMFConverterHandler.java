package org.emoflon.neo.emf.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.handlers.HandlerUtil;
import org.emoflon.neo.emf.EMFImporter;

public class EMFConverterHandler extends AbstractHandler {

	private final static Logger logger = Logger.getLogger(EMFConverterHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		var selection = HandlerUtil.getCurrentStructuredSelection(event);
		var shell = HandlerUtil.getActiveEditor(event).getSite().getShell();
		
		var resourceSet = extractResourceSet(selection);
		resourceSet.ifPresent(rs -> {
			EcoreUtil.resolveAll(rs);
			var mslContent = new EMFImporter().generateEMSLModels(rs);
			var mslFile = chooseFileToCreate(shell);
			try (var is = createInputStream(mslContent)) {
				mslFile.create(is, true, new NullProgressMonitor());
			} catch (CoreException | IOException e) {
				logger.error("Unable to perform conversion to EMSL: " + e);
			}
		});

		return null;
	}

	private IFile chooseFileToCreate(Shell parentShell) {
		var saveAsDialog = new SaveAsDialog(parentShell);
		saveAsDialog.open();
		var path = saveAsDialog.getResult();
		return ResourcesPlugin.getWorkspace().getRoot().getFile(path);
	}

	private Optional<ResourceSet> extractResourceSet(IStructuredSelection selection) {
		var someObject = selection.getFirstElement();
		if(someObject instanceof EObject) {
			return Optional.of(((EObject) someObject).eResource().getResourceSet());
		} else if(someObject instanceof Resource) {
			return Optional.of(((Resource) someObject).getResourceSet());
		} else
			return Optional.empty();
	}

	private InputStream createInputStream(String mslContent) {
		return IOUtils.toInputStream(mslContent, Charset.defaultCharset());
	}
}
