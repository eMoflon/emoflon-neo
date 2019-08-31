package org.emoflon.neo.emsl.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.jar.Attributes;
import java.util.jar.Attributes.Name;
import java.util.jar.Manifest;
import java.util.stream.Collectors;

import org.apache.log4j.Logger;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;

/**
 * This class provides functionality to modify manifest files.
 *
 */
public class ManifestFileUpdater {
	private static final Logger logger = Logger.getLogger(ManifestFileUpdater.class);

	public enum AttributeUpdatePolicy {
		FORCE, KEEP;
	}

	/**
	 * This means that the dependency is not available as a plugin -> the user must
	 * manipulate the projects buildpath manually!
	 **/
	public static final String IGNORE_PLUGIN_ID = "__ignore__";

	public static IFile getManifestFile(final IProject project) {
		return project.getFolder("META-INF").getFile("MANIFEST.MF");
	}

	/**
	 * Delegates to {@link #processManifest(IProject, Function, IProgressMonitor)}
	 * using a {@link NullProgressMonitor}
	 */
	public void processManifest(final IProject project, final Function<Manifest, Boolean> consumer)
			throws CoreException {
		this.processManifest(project, consumer, new NullProgressMonitor());
	}

	/**
	 * Modifies the manifest of the given project.
	 *
	 * The method reads the manifest, applies the given function, and, if the
	 * function returns true, saves the manifest again.
	 *
	 * @param consumer A function that returns whether it has modified the manifest.
	 * @throws CoreException
	 * @throws IOException
	 */
	public void processManifest(final IProject project, final Function<Manifest, Boolean> consumer,
			final IProgressMonitor monitor) throws CoreException {
		final SubMonitor subMon = SubMonitor.convert(monitor, "Processing manifest of project " + project.getName(),
				100);
		IFile manifestFile = getManifestFile(project);
		Manifest manifest = new Manifest();

		if (manifestFile.exists()) {
			readManifestFile(manifestFile, manifest);
		}
		subMon.worked(10);

		final boolean hasManifestChanged = consumer.apply(manifest);
		subMon.worked(80);

		if (hasManifestChanged) {

			try(final ByteArrayOutputStream stream = new ByteArrayOutputStream()) {
				new ManifestWriter().write(manifest, stream);
				String formattedManifestString = prettyPrintManifest(stream.toString());
				if (!manifestFile.exists()) {
					WorkspaceHelper.addAllFoldersAndFile(project, manifestFile.getProjectRelativePath(),
							formattedManifestString, subMon.split(10));
				} else {
					final ByteArrayInputStream fileOutputStream = new ByteArrayInputStream(
							formattedManifestString.getBytes());
					manifestFile.setContents(fileOutputStream, IFile.FORCE, subMon.split(10));
				}
			} catch (final IOException e) {
				throw new CoreException(new Status(IStatus.ERROR, WorkspaceHelper.getPluginId(getClass()),
						"Problem while stream Manifest file: " + e.getMessage(), e));
			}
		} else {
			subMon.worked(10);
		}
	}

	/**
	 * Updates the given attribute in the manifest.
	 *
	 * @return whether the value of the attribute changed
	 */
	public static boolean updateAttribute(final Manifest manifest, final Name attribute, final String value,
			final AttributeUpdatePolicy updatePolicy) {
		Attributes attributes = manifest.getMainAttributes();
		if ((!attributes.containsKey(attribute) || attributes.get(attribute) == null //
				|| attributes.get(attribute).equals("null")) //
				|| (attributes.containsKey(attribute) && updatePolicy == AttributeUpdatePolicy.FORCE)) {
			Object previousValue = attributes.get(attributes);
			if (!value.equals(previousValue)) {
				attributes.put(attribute, value);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	/**
	 * Updates the manifest (if necessary) to contain the given dependencies.
	 *
	 * @param newDependencies the dependencies to be added (if not present yet)
	 * @return whether the manifest was changed
	 */
	public static boolean updateDependencies(final Manifest manifest, final List<String> newDependencies) {
		final List<String> currentDependencies = extractDependencies(manifest);

		final List<String> missingNewDependencies = calculateMissingDependencies(currentDependencies, newDependencies);

		if (!missingNewDependencies.isEmpty()) {
			for (final String newDependency : missingNewDependencies) {
				currentDependencies.add(newDependency);
			}

			setDependencies(manifest, currentDependencies);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Removes the given plugin ID from the dependencies of the given manifest
	 *
	 * @param manifest
	 * @return true if the manifest has been modified by this methood, false
	 *         otherwise
	 */
	public static boolean removeDependency(final Manifest manifest, final String dependencyPluginIDToBeRemoved) {
		return removeDependencies(manifest, Arrays.asList(dependencyPluginIDToBeRemoved));
	}

	/**
	 * Removes the given plugin IDs from the dependencies of the given manifest
	 *
	 * @return true if the manifest has been modified by this methood, false
	 *         otherwise
	 */
	public static boolean removeDependencies(final Manifest manifest,
			final List<String> dependencyPluginIDsToBeRemoved) {
		final List<String> currentDependencies = extractDependencies(manifest);

		final List<String> newDependencies = currentDependencies.stream()//
				.filter(dependency -> !dependencyPluginIDsToBeRemoved.contains(extractPluginId(dependency)))//
				.collect(Collectors.toList());

		if (!currentDependencies.equals(newDependencies)) {
			setDependencies(manifest, newDependencies);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Removes the given plugin IDs from the dependencies of the given manifest
	 *
	 * @return true if the manifest has been modified by this methood, false
	 *         otherwise
	 */
	public static boolean replaceDependencies(final Manifest manifest,
			final Map<String, String> dependencyReplacementMap) {
		final List<String> currentDependencies = extractDependencies(manifest);

		final List<String> newDependencies = currentDependencies.stream()//
				.map(dependency -> getReplacementCandidate(dependency, dependencyReplacementMap))//
				.collect(Collectors.toList());

		if (!currentDependencies.equals(newDependencies)) {
			setDependencies(manifest, newDependencies);
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Looks up the dependency in the given map. If a key exists for the plugin ID
	 * of the dependency, the corresponding value is returned, otherwise the
	 * original dependency is returned
	 */
	private static String getReplacementCandidate(final String dependency,
			final Map<String, String> dependencyReplacementMap) {
		String oldPluginId = extractPluginId(dependency);
		return dependencyReplacementMap.containsKey(oldPluginId) ? (String) dependencyReplacementMap.get(oldPluginId)
				: dependency;
	}

	/**
	 * Calculates all dependencies in newDependencies that are not present in
	 * existingDependencies.
	 *
	 * All dependencies containing {@link ManifestFileUpdater#IGNORE_PLUGIN_ID} are
	 * ignored. If a dependency appears in both lists with different metadata (e.g.,
	 * bundle-version), nothing happens.
	 *
	 * @param existingDependencies
	 * @param newDependencies
	 * @return the missing dependencies, a sublist of newDependencies
	 */
	public static List<String> calculateMissingDependencies(final List<String> existingDependencies,
			final List<String> newDependencies) {
		final Collection<String> existingDependencyPluginIds = existingDependencies.stream()
				.map(ManifestFileUpdater::extractPluginId).collect(Collectors.toList());

		final List<String> missingDependencies = newDependencies.stream()//
				.filter(newDependency -> !newDependency.contains(IGNORE_PLUGIN_ID))//
				.filter(newDependency -> !existingDependencyPluginIds
						.contains(ManifestFileUpdater.extractPluginId(newDependency)))//
				.collect(Collectors.toList());

		return missingDependencies;
	}

	/**
	 * Reads the dependencies of the given manifest (i.e., values of the key
	 * Require-Bundle) and splits them at commas.
	 *
	 * @param manifest
	 * @return
	 */
	public static List<String> extractDependencies(final Manifest manifest) {
		final String currentDependencies = (String) manifest.getMainAttributes()
				.get(PluginManifestConstants.REQUIRE_BUNDLE);
		final List<String> dependencies = ManifestFileUpdater.extractDependencies(currentDependencies);
		return dependencies;
	}

	/**
	 * Creates an ID-to-project mapping from the given list of projects.
	 *
	 * The projects must be plugin projects.
	 *
	 * @param projects
	 * @return
	 */
	public Map<String, IProject> extractPluginIDToProjectMap(final Collection<IProject> projects) {
		final Map<String, IProject> idToProject = new HashMap<>();
		projects.stream().forEach(p -> {
			try {
				processManifest(p, manifest -> {
					idToProject.put(extractPluginId(getID(manifest)), p);
					return false;
				});
			} catch (Exception e) {
				idToProject.put(p.getName(), p);
			}
		});

		return idToProject;
	}

	/**
	 * Returns the list of dependencies as plugin id from the manifest file of the
	 * given project.
	 *
	 * @param project
	 * @return
	 */
	public Collection<String> getDependenciesAsPluginIDs(final IProject project) {
		Collection<String> dependencies = new ArrayList<>();

		try {
			processManifest(project, manifest -> {
				dependencies.addAll(extractDependencies(
						(String) manifest.getMainAttributes().get(PluginManifestConstants.REQUIRE_BUNDLE)));
				return false;
			});
		} catch (Exception e) {
			LogUtils.error(logger, e);
		}

		return dependencies.stream().map(dep -> extractPluginId(dep)).collect(Collectors.toList());
	}

	/**
	 * Returns the plugin ID for a given dependency entry, which may contain
	 * additional metadata, e.g.
	 *
	 * Input: org.moflon.ide.core;bundle-version="1.0.0"
	 *
	 * Output: org.moflon.ide.core
	 */
	public static String extractPluginId(final String existingDependency) {
		int indexOfSemicolon = existingDependency.indexOf(";");
		if (indexOfSemicolon > 0) {
			return existingDependency.substring(0, indexOfSemicolon);
		} else {
			return existingDependency;
		}
	}

	/**
	 * Extracts the dependencies from the given list of properties.
	 */
	public static List<String> extractDependencies(final String dependencies) {
		List<String> extractedDependencies = new ArrayList<>();
		if (dependencies != null && !dependencies.isEmpty()) {
			extractedDependencies.addAll(Arrays.asList(dependencies.split(",")));
		}

		return extractedDependencies;
	}

	private String prettyPrintManifest(final String string) {
		return new ManifestPrettyPrinter().print(string);
	}

	private void readManifestFile(final IFile manifestFile, final Manifest manifest) throws CoreException {
		try {
			InputStream manifestFileContents = manifestFile.getContents();
			manifest.read(manifestFileContents);
			manifestFileContents.close();
		} catch (IOException e) {
			throw new CoreException(new Status(IStatus.ERROR, WorkspaceHelper.getPluginId(ManifestFileUpdater.class),
					"Failed to read existing MANIFEST.MF: " + e.getMessage(), e));
		}
	}

	/**
	 * Sets the Require-Bundle property of the given manifest
	 *
	 * @param manifest     the manifest to be manipulated
	 * @param dependencies the dependencies to be used for Require-Bundle
	 */
	private static void setDependencies(final Manifest manifest, final List<String> dependencies) {
		String dependenciesString = ManifestFileUpdater.createDependenciesString(dependencies);

		if (!dependenciesString.matches("\\s*")) {
			manifest.getMainAttributes().put(PluginManifestConstants.REQUIRE_BUNDLE, dependenciesString);
		}
	}

	/**
	 * Joins the given list of dependencies using ","
	 *
	 * @param dependencies the dependencies
	 * @return the dependency string combining all dependencies
	 */
	private static String createDependenciesString(final List<String> dependencies) {
		return dependencies.stream().filter(dep -> !dep.isEmpty()).collect(Collectors.joining(","));
	}

	private static String getID(final Manifest manifest) {
		return (String) manifest.getMainAttributes().get(PluginManifestConstants.BUNDLE_SYMBOLIC_NAME);
	}

	/**
	 * Sets the required properties of the manifest if not set already.
	 * 
	 * @param manifest    the manifest to update
	 * @param projectName the name of the project
	 * @return whether the property was changed
	 */
	public static boolean setBasicProperties(final Manifest manifest, final String projectName) {
		boolean changed = false;
		changed |= ManifestFileUpdater.updateAttribute(manifest, PluginManifestConstants.MANIFEST_VERSION, "1.0",
				AttributeUpdatePolicy.KEEP);
		changed |= ManifestFileUpdater.updateAttribute(manifest, PluginManifestConstants.BUNDLE_NAME, projectName,
				AttributeUpdatePolicy.KEEP);
		changed |= ManifestFileUpdater.updateAttribute(manifest, PluginManifestConstants.BUNDLE_MANIFEST_VERSION, "2",
				AttributeUpdatePolicy.KEEP);
		changed |= ManifestFileUpdater.updateAttribute(manifest, PluginManifestConstants.BUNDLE_VERSION, "0.0.1",
				AttributeUpdatePolicy.KEEP);
		changed |= ManifestFileUpdater.updateAttribute(manifest, PluginManifestConstants.BUNDLE_SYMBOLIC_NAME,
				projectName + ";singleton:=true", AttributeUpdatePolicy.KEEP);
		changed |= ManifestFileUpdater.updateAttribute(manifest, PluginManifestConstants.BUNDLE_ACTIVATION_POLICY,
				"lazy", AttributeUpdatePolicy.KEEP);
		changed |= ManifestFileUpdater.updateAttribute(manifest, PluginManifestConstants.BUNDLE_EXECUTION_ENVIRONMENT,
				"JavaSE-1.8", AttributeUpdatePolicy.KEEP);
		return changed;
	}

	/**
	 * Updates the Export-Package property of the manifest.
	 * 
	 * @param manifest   the manifest to update
	 * @param newExports the exports to add
	 * @return whether the property was changed
	 */
	public static boolean updateExports(final Manifest manifest, final List<String> newExports) {
		List<String> exportsList = ManifestFileUpdater
				.extractDependencies((String) manifest.getMainAttributes().get(PluginManifestConstants.EXPORT_PACKAGE));
		boolean updated = false;
		for (String newExport : newExports) {
			if (!exportsList.contains(newExport)) {
				exportsList.add(newExport);
				updated = true;
			}
		}
		if (updated) {
			manifest.getMainAttributes().put(PluginManifestConstants.EXPORT_PACKAGE,
					exportsList.stream().filter(e -> !e.isEmpty()).collect(Collectors.joining(",")));
		}
		return updated;
	}
}