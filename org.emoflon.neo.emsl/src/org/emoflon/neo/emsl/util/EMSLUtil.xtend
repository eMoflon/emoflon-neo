package org.emoflon.neo.emsl.util

import com.google.inject.Injector
import java.util.ArrayList
import java.util.List
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.mwe.utils.StandaloneSetup
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.emoflon.neo.emsl.EMSLStandaloneSetup
import org.emoflon.neo.emsl.eMSL.EMSL_Spec
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock
import org.emoflon.neo.emsl.eMSL.impl.EMSLPackageImpl

class EMSLUtil {
	public static final String P_URI = "ConnectionURIPreference"
	public static final String P_USER = "UserPreference"
	public static final String P_PASSWORD = "PasswordPreference"

	def static EMSL_Spec loadSpecification(String modelURI, String platformURIRoot) {
		EMSLPackageImpl.init()
		new StandaloneSetup().setPlatformUri(platformURIRoot)
		var Injector injector = new EMSLStandaloneSetup().createInjectorAndDoEMFRegistration()
		var XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet)
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE)
		var Resource resource = resourceSet.getResource(URI.createURI(modelURI), true)
		var EMSL_Spec spec = (resource.getContents().get(0) as EMSL_Spec)
		return spec
	}
	
	def static List<MetamodelNodeBlock> thisAndAllSuperTypes(MetamodelNodeBlock block) {
		val blocks = new ArrayList
		if (block !== null) {
			blocks.add(block)
			block.superTypes.forEach[blocks.addAll(thisAndAllSuperTypes(it))]
		}
		return blocks
	}
}
