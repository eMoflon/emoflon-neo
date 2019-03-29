package org.emoflon.neo.emsl.ui.visualisation;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IGotoMarker;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.ui.editor.XtextEditor;

import net.sourceforge.plantuml.eclipse.utils.ILinkOpener;
import net.sourceforge.plantuml.eclipse.utils.LinkData;

/**
 * Custom {@link ILinkOpener} for opening the editor on the line of the fragment
 * in a platform link.
 */
public class PlatformFragmentLinkOpener implements ILinkOpener {

	@Override
	public int supportsLink(final LinkData link) {
		URI uri = URI.createURI(link.href);
		if (uri.fileExtension().equals("gt") && uri.isPlatformResource() && uri.hasFragment()) {
			return CUSTOM_SUPPORT;
		}
		return NO_SUPPORT;
	}

	@Override
	public void openLink(final LinkData link) {
		URI uri = URI.createURI(link.href);

		IPath path = new Path(uri.path()).removeFirstSegments(1);
		IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);

		try {
			IEditorPart editorPart = openEditor(file);
			if (editorPart instanceof XtextEditor) {
				goToFragmentLine((XtextEditor) editorPart, file, uri.fragment());
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Opens the default editor for the given file and returns it.
	 * 
	 * @param file
	 *            the file
	 * @return the opened editor
	 * @throws PartInitException
	 *             if the editor could not be created or initialized
	 */
	private static IEditorPart openEditor(final IFile file) throws PartInitException {
		IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(file.getName());
		return page.openEditor(new FileEditorInput(file), desc.getId());
	}

	/**
	 * Goes to the line of the given URI's fragment in the opened editor
	 * 
	 * @param xtextEditor
	 *            the editor for the line
	 * @param file
	 *            the file
	 * @param fragment
	 *            the URI fragment
	 * @throws CoreException
	 *             if an error occurs during setting the marker for the target
	 */
	private static void goToFragmentLine(final XtextEditor xtextEditor, final IFile file, final String fragment)
			throws CoreException {
		EObject fragmentObject = xtextEditor.getDocument().readOnly(res -> res.getEObject(fragment));
		if (fragmentObject == null) {
			return;
		}

		IMarker marker = file.createMarker(IMarker.TEXT);
		marker.setAttribute(IMarker.LINE_NUMBER, NodeModelUtils.getNode(fragmentObject).getStartLine());

		IGotoMarker goToMarker = (IGotoMarker) xtextEditor.getAdapter(IGotoMarker.class);
		goToMarker.gotoMarker(marker);

		marker.delete();
	}
}