package org.emoflon.neo.emsl.ui.preferences;

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.emoflon.neo.emsl.ui.internal.EmslActivator;
import org.emoflon.neo.emsl.util.EMSLUtil;

public class MIPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

	public MIPreferencePage() {
		super(GRID);
		setPreferenceStore(EmslActivator.getInstance().getPreferenceStore());
		setDescription("Preferences for configuring the concurrent synchronization operation");
	}

	/**
	 * Creates the field editors. Field editors are abstractions of the common GUI
	 * blocks needed to manipulate various types of preferences. Each field editor
	 * knows how to save and restore itself.
	 */
	@Override
	public void createFieldEditors() {
		addField(new StringFieldEditor(EMSLUtil.P_ALPHA, "&Deletion weight (alpha):", getFieldEditorParent()));
		addField(new StringFieldEditor(EMSLUtil.P_BETA, "&Creation weight (beta):", getFieldEditorParent()));
		addField(new StringFieldEditor(EMSLUtil.P_GAMMA, "&Induced weight (gamma):", getFieldEditorParent()));
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