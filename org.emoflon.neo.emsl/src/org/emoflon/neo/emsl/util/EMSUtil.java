package org.emoflon.neo.emsl.util;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.emoflon.neo.emsl.EMSLStandaloneSetup;
import org.emoflon.neo.emsl.eMSL.EMSL_Spec;
import org.emoflon.neo.emsl.eMSL.impl.EMSLPackageImpl;

import com.google.inject.Injector;

public class EMSUtil {

	public static final String P_URI = "ConnectionURIPreference";
	public static final String P_USER = "UserPreference";
	public static final String P_PASSWORD = "PasswordPreference";

	public static EMSL_Spec loadSpecification(String modelURI, String platformURIRoot) {
		EMSLPackageImpl.init();
		new org.eclipse.emf.mwe.utils.StandaloneSetup().setPlatformUri(platformURIRoot);
		Injector injector = new EMSLStandaloneSetup().createInjectorAndDoEMFRegistration();
		XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
		Resource resource = resourceSet.getResource(URI.createURI(modelURI), true);
		EMSL_Spec spec = (EMSL_Spec) resource.getContents().get(0);
		return spec;
	}
}
