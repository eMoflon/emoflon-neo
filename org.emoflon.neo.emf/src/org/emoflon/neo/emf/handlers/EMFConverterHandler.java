package org.emoflon.neo.emf.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.ui.handlers.HandlerUtil;
import org.emoflon.neo.emf.EMFImporter;

public class EMFConverterHandler extends AbstractHandler {

	private final static Logger logger = Logger.getLogger(EMFConverterHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		var selection = HandlerUtil.getCurrentStructuredSelection(event);
		var firstElement = selection.getFirstElement();
		if (firstElement instanceof IFile) {
			try {
				IFile file = (IFile) firstElement;
				if (file.getFileExtension().equals("ecore") || file.getFileExtension().equals("xmi")) {
					logger.info("Converting " + file + " to EMSL...");
					var mslContent = new EMFImporter().generateEMSLModel(loadFileIntoResourceSet(file));
					var path = file.getProjectRelativePath().removeFileExtension().addFileExtension("msl");
					var mslFile = file.getProject().getFile(path);
					try (var is = createInputStream(mslContent)) {
						mslFile.create(is, true, new NullProgressMonitor());
					}
				} else {
					logger.info("Only .ecore and .xmi files can be converted to EMSL.");
				}
			} catch (CoreException | IOException e) {
				e.printStackTrace();
				logger.error("Unable to perform conversion to EMSL: " + e);
			}
		} else {
			logger.info("Only .ecore and .xmi files can be converted to EMSL.");
		}

		return null;
	}

	private InputStream createInputStream(String mslContent) {
		return IOUtils.toInputStream(mslContent, Charset.defaultCharset());
	}

	private ResourceSet loadFileIntoResourceSet(IFile file) throws IOException {
		var rs = new ResourceSetImpl();
		var r = rs.createResource(URI.createFileURI(file.getLocation().toString()));
		r.load(null);
		return rs;
	}
}
