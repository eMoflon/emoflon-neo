package org.emoflon.neo.emsl.compiler;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.emoflon.neo.emsl.EMSLFlattener;
import org.emoflon.neo.emsl.eMSL.TripleGrammar;

public class TGGCompiler {

	public final static String PATH_SEPARATOR = "/";
	public final static String FILE_ENDING = ".msl";

	private IProject project;
	private EMSLFlattener flattener;

	public TGGCompiler(IProject pProject) {
		project = pProject;
		flattener = new EMSLFlattener();
	}

	public void compile(TripleGrammar pTGG) {
		try {
			createRulesFile("", "TestFile", TGGFilesGenerator.generateTGGFile(pTGG));
		} catch (CoreException pCE) {
			System.err.println("Failed to create test-file");
		}
	}

	public void createRulesFile(String pPath, String pFileName, String pContents) throws CoreException {
		IPath pathToFile = new Path(pPath + PATH_SEPARATOR + pFileName + FILE_ENDING);
		IFile file = project.getFile(pathToFile);

		file.delete(true, null);

		final String[] folders = pathToFile.removeLastSegments(1).toString().split(PATH_SEPARATOR);
		StringBuilder currentFolder = new StringBuilder();
		for (String folder : folders) {
			currentFolder.append(PATH_SEPARATOR).append(folder);
			final IFolder projFolder = project.getFolder(currentFolder.toString());
			if (!projFolder.exists())
				projFolder.create(true, true, null);
		}

		file.create(new ByteArrayInputStream(pContents.getBytes()), true, null);
	}
}
