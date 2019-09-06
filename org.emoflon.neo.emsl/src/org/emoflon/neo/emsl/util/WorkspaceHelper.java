package org.emoflon.neo.emsl.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.osgi.framework.Bundle;
import org.osgi.framework.FrameworkUtil;

public class WorkspaceHelper {
	public final static String PATH_SEPARATOR = "/";

	/**
	 * Prints the stacktrace of the given {@link Throwable} to a string.
	 *
	 * If t is null, then the result is 'null'.
	 */
	public static String printStacktraceToString(final Throwable throwable) {
		if (null == throwable)
			return "null";

		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		throwable.printStackTrace(new PrintStream(stream));
		return new String(stream.toByteArray());
	}

	/**
	 * Creates a file at pathToFile with specified contents fileContent. All folders
	 * in the path are created if necessary.
	 *
	 * @param project     Project containing file to be created
	 * @param pathToFile  Project relative path to file to be created
	 * @param fileContent String content of file to be created
	 * @param monitor
	 * @throws CoreException
	 */
	public static void addAllFoldersAndFile(final IProject project, final IPath pathToFile, final String fileContent,
			final IProgressMonitor monitor) throws CoreException {
		final SubMonitor subMon = SubMonitor.convert(monitor, "Adding file " + pathToFile + " to project " + project,
				2);
		final IPath pathWithoutFileSegment = pathToFile.removeLastSegments(1);

		addAllFolders(project, pathWithoutFileSegment.toString(), subMon.split(1));

		addFile(project.getFile(pathToFile), fileContent, subMon.split(1));
	}

	/**
	 * Creates a folder denoted by the path inside the given project.
	 *
	 * @param project
	 * @param path    the path, separated with
	 *                {@link WorkspaceHelper#PATH_SEPARATOR}
	 * @param monitor
	 * @throws CoreException
	 */
	public static void addAllFolders(final IProject project, final String path, final IProgressMonitor monitor)
			throws CoreException {
		final String[] folders = path.split(PATH_SEPARATOR);
		final SubMonitor subMon = SubMonitor.convert(monitor, "Add folders", folders.length);
		StringBuilder currentFolder = new StringBuilder();
		for (String folder : folders) {
			currentFolder.append(PATH_SEPARATOR).append(folder);
			addFolder(project, currentFolder.toString(), subMon.split(1));
		}
	}

	/**
	 * Creates the given file (if not exists) and stores the given contents in it.
	 *
	 * If the file exists, its content is replaced with the given content.
	 *
	 * @param file
	 * @param contents
	 * @param monitor  the monitor that reports on the progress
	 * @throws CoreException
	 */
	private static void addFile(final IFile file, final String contents, final IProgressMonitor monitor)
			throws CoreException {
		final SubMonitor subMon = SubMonitor.convert(monitor, "Add file", 1);
		final ByteArrayInputStream source = new ByteArrayInputStream(contents.getBytes());
		if (file.exists()) {
			file.setContents(source, IFile.FORCE | IFile.KEEP_HISTORY, subMon.split(1));
		} else {
			file.create(source, true, subMon.split(1));
		}
	}

	/**
	 * Adds a new folder with name 'folderName' to project
	 *
	 * @param project    the project on which the folder will be added
	 * @param folderName name of the new folder
	 * @param monitor    a progress monitor, or null if progress reporting is not
	 *                   desired
	 * @return newly created folder
	 * @throws CoreException
	 */
	public static IFolder addFolder(final IProject project, final String folderName, final IProgressMonitor monitor)
			throws CoreException {
		final SubMonitor subMon = SubMonitor.convert(monitor, "", 1);

		final IFolder projFolder = project.getFolder(folderName);
		if (!projFolder.exists())
			projFolder.create(true, true, subMon.split(1));
		return projFolder;
	}

	/**
	 * Returns the symbolic name (aka. plugin ID) of the bundle containing the given
	 * class.
	 * 
	 * @param clazz the class whose bundle is searched
	 * @return the symbolic name or null if the class does not belong to a bundle
	 */
	public static String getPluginId(final Class<?> clazz) {
		final Bundle bundle = FrameworkUtil.getBundle(clazz);
		return bundle == null ? null : bundle.getSymbolicName();
	}
	
	/**
	 * Adds natureId to project
	 *
	 * @param project  Handle to existing project
	 * @param natureId ID of nature to be added
	 * @param monitor  a progress monitor, or null if progress reporting is not
	 *                 desired
	 * @throws CoreException if unable to add nature
	 */
	public static boolean addNature(IProject project, final String natureId, final IProgressMonitor monitor)
			throws CoreException {
		if (hasNature(project, natureId)) {
			return false;
		}
		
		final SubMonitor subMon = SubMonitor.convert(monitor, "Add nature to project", 2);

		IProjectDescription description = getDescriptionWithAddedNature(project, natureId, subMon.split(1));
		project.setDescription(description, subMon.split(1));
		
		return true;
	}
	
	/**
	 * Returns true if the given {@link IProject} has the given nature ID
	 * 
	 * @param project
	 *            the project
	 * @param natureId
	 *            the nature ID
	 * @return whether the project has the nature ID
	 */
	public static boolean hasNature(final IProject project, final String natureId) {
		try {
			return project.getNature(natureId) != null;
		} catch (final CoreException e) {
			return false;
		}
	}
	
	/**
	 * Adds and fills a file to project root, containing specified contents as a
	 * string
	 *
	 * If the file does not exist, it is created. If it exists, its contents are
	 * overwritten
	 *
	 * @param project
	 *            Name of project the file should be added to
	 * @param fileName
	 *            Name of file to add to project
	 * @param contents
	 *            What the file should contain as a String
	 * @param monitor
	 *            Monitor to indicate progress
	 * @throws CoreException
	 */
	public static void addFile(final IProject project, final String fileName, final String contents,
			final IProgressMonitor monitor) throws CoreException {
		final SubMonitor subMon = SubMonitor.convert(monitor, "", 1);
		IFile projectFile = project.getFile(fileName);
		ByteArrayInputStream source = new ByteArrayInputStream(contents.getBytes());
		if (projectFile.exists()) {
			projectFile.setContents(source, true, true, subMon.split(1));
		} else {
			projectFile.create(source, true, subMon.split(1));
		}
	}
	
	/**
	 * Returns the description of the given project with the given nature ID added
	 * to the project's list of natures
	 */
	public static IProjectDescription getDescriptionWithAddedNature(final IProject project, final String natureId,
			final IProgressMonitor monitor) throws CoreException {
		final SubMonitor subMon = SubMonitor.convert(monitor, "Create description with added natures", 1);

		final IProjectDescription description = project.getDescription();

		final List<String> natures = new ArrayList<>(Arrays.asList(description.getNatureIds()));

		if (!natures.contains(natureId)) {
			natures.add(natureId);
			description.setNatureIds(natures.toArray(new String[natures.size()]));
		}

		subMon.worked(1);

		return description;
	}
}
