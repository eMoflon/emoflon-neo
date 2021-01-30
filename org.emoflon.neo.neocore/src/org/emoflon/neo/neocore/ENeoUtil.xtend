package org.emoflon.neo.neocore

import com.google.inject.Injector
import java.net.URL
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.emf.mwe.utils.StandaloneSetup
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.emoflon.neo.emsl.EMSLStandaloneSetup
import org.emoflon.neo.emsl.eMSL.EMSL_Spec
import org.emoflon.neo.emsl.eMSL.impl.EMSLPackageImpl
import org.emoflon.neo.emsl.util.EMSLUtil

class ENeoUtil {
	def static createEMSLStandaloneResourceSet(String platformResourceURIRoot){
		EMSLPackageImpl.init()
		new StandaloneSetup().setPlatformUri(platformResourceURIRoot)
		var Injector injector = new EMSLStandaloneSetup().createInjectorAndDoEMFRegistration()
		var XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet)
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE)
		return resourceSet
	}
	
	def static loadEMSLSpec(URL url, ResourceSet rs, URI resourceURI) {
		val streamRules = url.openStream
		var Resource resourceRules = rs.createResource(resourceURI)
		resourceRules.load(streamRules, rs.getLoadOptions())

		var EMSL_Spec spec = (resourceRules.getContents().get(0) as EMSL_Spec)
		EcoreUtil.resolveAll(rs)
	
		return spec
	}
	
	def static loadNeoCore(ResourceSet rs) {
		val urlNeoCore = ENeoUtil.classLoader.getResource("NeoCore.msl")
		loadEMSLSpec(urlNeoCore, rs, URI.createURI(EMSLUtil.ORG_EMOFLON_NEO_CORE_URI))
	}
}