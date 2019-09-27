package org.emoflon.neo.emsl.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.wizards.JavaCapabilityConfigurationPage;
import org.eclipse.ui.PlatformUI;

/**
 * Utility class for manipulating and analyzing the classpath
 *
 * @author Roland Kluge - Initial implementation
 *
 */
public final class ClasspathUtil {
	static final Logger logger = Logger.getLogger(ClasspathUtil.class);

	public static final String XTEXT_NATURE_ID = "org.eclipse.xtext.ui.shared.xtextNature";
	public static final String PLUGIN_NATURE_ID = "org.eclipse.pde.PluginNature";
	private static final String JAVA_NATURE = "org.eclipse.jdt.core.javanature";

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
		if (folder.exists() && !ClasspathUtil.isSourceFolder(folder)) {
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

	public static void setUpAsXtextProject(IProject project) throws CoreException {
		WorkspaceHelper.addNature(project, XTEXT_NATURE_ID, new NullProgressMonitor());
	}

	public static void setUpAsPluginProject(IProject project) throws CoreException, IOException {
		if (WorkspaceHelper.addNature(project, PLUGIN_NATURE_ID, new NullProgressMonitor())) {
			setUpBuildProperties(project);
			setUpManifestFile(project);
			addContainerToBuildPath(project, "org.eclipse.pde.core.requiredPlugins");
		}
	}

	public static void setUpAsJavaProject(IProject project) {
		if (!WorkspaceHelper.hasNature(project, JAVA_NATURE)) {
			final SubMonitor subMon = SubMonitor.convert(new NullProgressMonitor(), "Set up Java project", 1);
			final JavaCapabilityConfigurationPage jcpage = new JavaCapabilityConfigurationPage();
			final IJavaProject javaProject = JavaCore.create(project);

			PlatformUI.getWorkbench().getDisplay().syncExec(() -> {
				jcpage.init(javaProject, null, null, true);
				try {
					jcpage.configureJavaProject(subMon.newChild(1));
				} catch (final Exception e) {
					LogUtils.error(logger, "Exception during setup of Java project", e);
				}
			});
		}
	}

	public static void addDependencies(IProject project, List<String> deps) {
		try {
			new ManifestFileUpdater().processManifest(project,
					(manifest) -> ManifestFileUpdater.updateDependencies(manifest, deps));
		} catch (CoreException e) {
			LogUtils.error(logger, e);
		}
	}

	/**
	 * Adds the given container to the build path of the given project if it
	 * contains no entry with the same name, yet.
	 */
	public static void addContainerToBuildPath(final IProject project, final String container) {
		addContainerToBuildPath(JavaCore.create(project), container);
	}

	/**
	 * Adds the given container to the build path of the given java project.
	 */
	private static void addContainerToBuildPath(final IJavaProject iJavaProject, final String container) {
		try {
			// Get current entries on the classpath
			Collection<IClasspathEntry> classpathEntries = new ArrayList<>(
					Arrays.asList(iJavaProject.getRawClasspath()));

			addContainerToBuildPath(classpathEntries, container);

			setBuildPath(iJavaProject, classpathEntries);
		} catch (JavaModelException e) {
			LogUtils.error(logger, e, "Unable to set classpath variable");
		}
	}

	/**
	 * Adds the given container to the list of build path entries (if not included,
	 * yet)
	 */
	private static void addContainerToBuildPath(final Collection<IClasspathEntry> classpathEntries,
			final String container) {
		IClasspathEntry entry = JavaCore.newContainerEntry(new Path(container));
		for (IClasspathEntry iClasspathEntry : classpathEntries) {
			if (iClasspathEntry.getPath().equals(entry.getPath())) {
				// No need to add variable - already on classpath
				return;
			}
		}

		classpathEntries.add(entry);
	}

	private static void setBuildPath(final IJavaProject javaProject, final Collection<IClasspathEntry> entries,
			final IProgressMonitor monitor) throws JavaModelException {
		final SubMonitor subMon = SubMonitor.convert(monitor, "Set build path", 1);
		// Create new buildpath
		IClasspathEntry[] newEntries = new IClasspathEntry[entries.size()];
		entries.toArray(newEntries);

		// Set new classpath with added entries
		javaProject.setRawClasspath(newEntries, subMon.newChild(1));
	}

	private static void setBuildPath(final IJavaProject javaProject, final Collection<IClasspathEntry> entries)
			throws JavaModelException {
		setBuildPath(javaProject, entries, new NullProgressMonitor());
	}

	private static void setUpBuildProperties(IProject project) throws CoreException {
		logger.debug("Adding build.properties");
		Properties buildProperties = new Properties();
		buildProperties.put("bin.includes", "META-INF/, bin/");
		buildProperties.put("source..", "src/, src-gen");
		buildProperties.put("output..", "bin/");
		new BuildPropertiesFileBuilder().createBuildProperties(project, new NullProgressMonitor(), buildProperties);
	}

	private static void setUpManifestFile(IProject project) throws CoreException, IOException {
		logger.debug("Adding MANIFEST.MF");
		new ManifestFileUpdater().processManifest(project, manifest -> {
			return ManifestFileUpdater.setBasicProperties(manifest, project.getName());
		});
	}

}