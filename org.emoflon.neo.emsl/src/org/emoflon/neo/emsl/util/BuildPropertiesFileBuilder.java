package org.emoflon.neo.emsl.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

/**
 * Utility class for creating build.properties file.
 */
public class BuildPropertiesFileBuilder {

	private static final String BUILD_PROPERTIES_NAME = "build.properties";

	/**
	 * Creates a build.properties file in the given project with the eMoflon default
	 * build properties.
	 *
	 * @param currentProject
	 *            the project
	 * @param monitor
	 *            the progress monitor
	 * @throws CoreException
	 */
	public void createBuildProperties(final IProject currentProject, final IProgressMonitor monitor)
			throws CoreException {
		Properties buildProperties = new Properties();
		buildProperties.put("bin.includes", "META-INF/, bin/, model/, plugin.xml");
		buildProperties.put("source..", "src/,src-gen/");
		buildProperties.put("output..", "bin/");
		this.createBuildProperties(currentProject, monitor, buildProperties);
	}

	/**
	 * Creates a build.properties file in the given project with the the given build
	 * properties.
	 *
	 * @param currentProject
	 *            the project
	 * @param monitor
	 *            the progress monitor
	 * @param buildProperties
	 *            the build properties to set
	 * @throws CoreException
	 */
	public void createBuildProperties(final IProject currentProject, final IProgressMonitor monitor,
			final Properties buildProperties) throws CoreException {
		try {
			final SubMonitor subMon = SubMonitor.convert(monitor, "Creating build.properties", 2);

			final IFile file = getBuildPropertiesFile(currentProject);
			if (!file.exists()) {
				subMon.worked(1);

				final ByteArrayOutputStream stream = new ByteArrayOutputStream();
				buildProperties.store(stream, "");

				WorkspaceHelper.addFile(currentProject, BUILD_PROPERTIES_NAME, stream.toString(), subMon.split(1));
			}
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, WorkspaceHelper.getPluginId(getClass()),
					"Error while creating build.properties: " + e.getMessage()));
		}
	}

	public IFile getBuildPropertiesFile(final IProject currentProject) {
		return currentProject.getFile(BUILD_PROPERTIES_NAME);
	}
}
