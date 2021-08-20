/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.classinhhier2db.src;

import org.emoflon.neo.cypher.common.*;
import org.emoflon.neo.cypher.constraints.*;
import org.emoflon.neo.cypher.factories.*;
import org.emoflon.neo.cypher.models.*;
import org.emoflon.neo.cypher.patterns.*;
import org.emoflon.neo.cypher.rules.*;
import org.emoflon.neo.engine.api.patterns.*;
import org.emoflon.neo.engine.api.constraints.*;
import org.emoflon.neo.engine.api.rules.*;
import org.emoflon.neo.emsl.eMSL.*;
import org.emoflon.neo.emsl.util.*;
import org.neo4j.driver.Value;
import org.neo4j.driver.Record;
import org.eclipse.emf.common.util.URI;
import org.emoflon.neo.api.classinhhier2db.API_Common;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.Optional;
import java.time.LocalDate;

@SuppressWarnings("unused")
public class API_QuiltographyLanguage {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_QuiltographyLanguage(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_QuiltographyLanguage(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		this((EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/ClassInhHier2DB/Quiltography/src/QuiltographyLanguage.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI), builder);
	}

	public API_QuiltographyLanguage(EMSL_Spec spec, NeoCoreBuilder builder) {
		this.spec = spec;
		this.builder = builder;
	}

	//:~> platform:/resource/ClassInhHier2DB/Quiltography/src/QuiltographyLanguage.msl#//@entities.0
	public Metamodel getMetamodel_Quiltography(){
		return (Metamodel) spec.getEntities().get(0);
	}
	
	public static final String Quiltography__Book = "Quiltography__Book";
	public static final String Quiltography__Page = "Quiltography__Page";
	public static final String Quiltography__ContentType = "Quiltography__ContentType";
	public static final String Quiltography__BlockPattern = "Quiltography__BlockPattern";
	public static final String Quiltography__Location = "Quiltography__Location";
	public static final String Quiltography__Author = "Quiltography__Author";
	public static final String Quiltography__Classification = "Quiltography__Classification";
}
