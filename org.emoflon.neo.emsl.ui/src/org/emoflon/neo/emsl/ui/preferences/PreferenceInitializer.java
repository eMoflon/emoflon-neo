package org.emoflon.neo.emsl.ui.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.emoflon.neo.emsl.ui.internal.EmslActivator;
import org.emoflon.neo.emsl.util.EMSUtil;

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
		store.setDefault(EMSUtil.P_URI, "bolt://localhost:11002");
		store.setDefault(EMSUtil.P_USER, "neo4j");
		store.setDefault(EMSUtil.P_PASSWORD, "test");
	}

}
