package org.emoflon.neo.emsl.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.emoflon.neo.emsl.ui.internal.EmslActivator;
import org.emoflon.neo.emsl.util.EMSLUtil;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#
	 * initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		IPreferenceStore store = EmslActivator.getInstance().getPreferenceStore();
		store.setDefault(EMSLUtil.P_URI, "bolt://localhost:7687");
		store.setDefault(EMSLUtil.P_USER, "neo4j");
		store.setDefault(EMSLUtil.P_PASSWORD, "test");
		
		store.setDefault(EMSLUtil.P_ALPHA, -5);
		store.setDefault(EMSLUtil.P_BETA, 5);
		store.setDefault(EMSLUtil.P_GAMMA, -1);
	}

}
