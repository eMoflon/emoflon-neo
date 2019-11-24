package org.emoflon.neo.emf.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.ui.handlers.HandlerUtil;
import org.emoflon.neo.emf.EMFImporter;

public class EMFConverterHandler extends AbstractHandler {

	private final static Logger logger = Logger.getLogger(EMFConverterHandler.class);

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		var selection = HandlerUtil.getCurrentStructuredSelection(event);
		List<?> selected = selection.toList();

		if (!selected.stream().allMatch(this::isOfRelevantType)) {
			logger.info("Only .ecore and .xmi files can be converted to EMSL.");
		} else {
			var files = selected.stream().map(IFile.class::cast).collect(Collectors.toList());
			try {
				logger.info("Converting " + files + " to EMSL...");
				var file = files.get(0);
				var resourceSet = loadFilesIntoResourceSet(files);
				var mslContent = new EMFImporter().generateEMSLModel(resourceSet);
				var path = file.getProjectRelativePath().removeFileExtension().addFileExtension("msl");
				var mslFile = file.getProject().getFile(path);
				try (var is = createInputStream(mslContent)) {
					mslFile.create(is, true, new NullProgressMonitor());
				} catch (CoreException | IOException e) {
					throw e;
				}
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("Unable to perform conversion to EMSL: " + e);
			}
		}

		return null;
	}

	private boolean isOfRelevantType(Object file) {
		if (file instanceof IFile) {
			var ifile = (IFile) file;
			return ifile.getFileExtension().equals("xmi") || ifile.getFileExtension().equals("ecore");
		}

		return false;
	}

	private InputStream createInputStream(String mslContent) {
		return IOUtils.toInputStream(mslContent, Charset.defaultCharset());
	}

	private ResourceSet loadFilesIntoResourceSet(List<IFile> files) throws IOException {
		var rs = new ResourceSetImpl();
		Collections.sort(files, (x,y) -> x.getFileExtension().compareTo(y.getFileExtension()));
		for (var file : files) {
			var r = rs.createResource(URI.createFileURI(file.getLocation().toString()));
			r.load(null);
			if (file.getFileExtension().equals("ecore")) {
				var pack = r.getContents().get(0);
				if (pack instanceof EPackage) {
					var uri = ((EPackage) pack).getNsURI();
					r.setURI(URI.createURI(uri));
					rs.getPackageRegistry().put(uri, pack);
				}
			}
		}

		return rs;
	}
}
