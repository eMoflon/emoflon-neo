package org.emoflon.neo.emsl.util;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

/**
 * Utility class for manipulating and analyzing the classpath
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public final class ClasspathUtil {

	/**
	 * Adds the given folder to the given project's classpath
	 * 
	 * @param folder the folder to be manipulated
	 * @throws CoreException if analyzing the classpath fails
	 */
	public static void makeSourceFolder(final IFolder folder) throws CoreException {
		try {
			final IJavaProject javaProject = JavaCore.create(folder.getProject());
			final IClasspathEntry[] oldEntries = javaProject.getRawClasspath();
			final IClasspathEntry[] newEntries = new IClasspathEntry[oldEntries.length + 1];
			System.arraycopy(oldEntries, 0, newEntries, 0, oldEntries.length);
			newEntries[oldEntries.length] = JavaCore.newSourceEntry(folder.getFullPath());
			javaProject.setRawClasspath(newEntries, null);
		} catch (final JavaModelException e) {
			throw new CoreException(new Status(IStatus.ERROR, EMSLUtil.PLUGIN_ID,
					String.format("%s happended while analyzing classpath: %s", e.getClass(), e.getMessage())));
		}
	}

	/**
	 * Invokes {@link makeSourceFolder} if
	 * {@link RepositoryBuilder#isSourceFolder(IFolder)} returns false for the given
	 * folder
	 * 
	 * @param folder the folder to be converted to a source folder
	 * @throws CoreException if analyzing the classpath fails
	 */
	public static void makeSourceFolderIfNecessary(final IFolder folder) throws CoreException {
		if (!ClasspathUtil.isSourceFolder(folder)) {
			makeSourceFolder(folder);
		}
	}

	/**
	 * Returns true if the given folder's full path is present in the folder's
	 * project's classpath
	 * 
	 * @param folder the folder to be checked
	 * @throws CoreException if analyzing the classpath fails
	 * @see IJavaProject#getRawClasspath()
	 */
	public static boolean isSourceFolder(final IFolder folder) throws CoreException {
		final IJavaProject javaProject = JavaCore.create(folder.getProject());
		try {
			for (final IClasspathEntry entry : javaProject.getRawClasspath()) {
				if (entry.getPath().equals(folder.getFullPath()))
					return true;
			}
		} catch (final JavaModelException e) {
			throw new CoreException(new Status(IStatus.ERROR, EMSLUtil.PLUGIN_ID,
					String.format("%s happended while analyzing classpath: %s", e.getClass(), e.getMessage())));
		}
		return false;
	}

}