package org.emoflon.neo.emsl.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.emoflon.neo.emsl.ui.internal.EmslActivator;
import org.emoflon.neo.emsl.util.EMSUtil;

public class Neo4JPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public Neo4JPreferencePage() {
		super(GRID);
		setPreferenceStore(EmslActivator.getInstance().getPreferenceStore());
		setDescription("Preferences for communicating with your Neo4j database");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI
	 * blocks needed to manipulate various types of preferences. Each field editor
	 * knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		addField(new StringFieldEditor(EMSUtil.P_URI, "&Connection URI:", getFieldEditorParent()));
		addField(new StringFieldEditor(EMSUtil.P_USER, "&User:", getFieldEditorParent()));
		addField(new StringFieldEditor(EMSUtil.P_PASSWORD, "&Password:", getFieldEditorParent()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
	 */
	@Override
	public void init(IWorkbench workbench) {
	}

}