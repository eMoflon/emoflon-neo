/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.classinhhier2db.resources.expected.metamodel;

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
public class API_OCLGrammar {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_OCLGrammar(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_OCLGrammar(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		this((EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/ClassInhHier2DB/TestSuiteGT/resources/expected/metamodel/OCLGrammar.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI), builder);
	}

	public API_OCLGrammar(EMSL_Spec spec, NeoCoreBuilder builder) {
		this.spec = spec;
		this.builder = builder;
	}

	//:~> platform:/resource/ClassInhHier2DB/TestSuiteGT/resources/expected/metamodel/OCLGrammar.msl#//@entities.0
	public Metamodel getMetamodel_OCLGrammar(){
		return (Metamodel) spec.getEntities().get(0);
	}
	
	public static final String OCLGrammar__OCLString = "OCLGrammar__OCLString";
	public static final String OCLGrammar__Expression = "OCLGrammar__Expression";
	public static final String OCLGrammar__MethodCall = "OCLGrammar__MethodCall";
	public static final String OCLGrammar__Parameter = "OCLGrammar__Parameter";
	public static final String OCLGrammar__Collection = "OCLGrammar__Collection";
	public static final String OCLGrammar__Literal = "OCLGrammar__Literal";
	public static final String OCLGrammar__StringLiteral = "OCLGrammar__StringLiteral";
	public static final String OCLGrammar__NumberRange = "OCLGrammar__NumberRange";
	public static final String OCLGrammar__NumberLiteral = "OCLGrammar__NumberLiteral";
	public static final String OCLGrammar__DoubleLiteral = "OCLGrammar__DoubleLiteral";
	public static final String OCLGrammar__NullLiteral = "OCLGrammar__NullLiteral";
	public static final String OCLGrammar__InvalidLiteral = "OCLGrammar__InvalidLiteral";
}
