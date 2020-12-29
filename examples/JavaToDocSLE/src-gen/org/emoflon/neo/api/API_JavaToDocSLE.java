/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api;

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
import org.neo4j.driver.v1.Value;
import org.neo4j.driver.v1.Record;
import org.eclipse.emf.common.util.URI;
import org.emoflon.neo.api.API_Common;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.Optional;
import java.time.LocalDate;

@SuppressWarnings("unused")
public class API_JavaToDocSLE {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_JavaToDocSLE(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_JavaToDocSLE(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		spec = (EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/JavaToDocSLE/src/JavaToDocSLE.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI);
		this.builder = builder;
	}

	//:~> platform:/resource/JavaToDocSLE/src/JavaToDocSLE.msl#//@entities.0
	public void exportMetamodelsForJavaToDocSLE() throws FlattenerException {
		{
			var api = new org.emoflon.neo.api.metamodels.API_SimpleJavaSLE(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
			builder.exportEMSLEntityToNeo4j(api.getMetamodel_SimpleJavaSLE());
		}
		{
			var api = new org.emoflon.neo.api.metamodels.API_SimpleDocSLE(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
			builder.exportEMSLEntityToNeo4j(api.getMetamodel_SimpleDocSLE());
		}
	}
	
	public Collection<TripleRule> getTripleRulesOfJavaToDocSLE(){
		var rules = new HashSet<TripleRule>();
		var rs = spec.eResource().getResourceSet();
		{
			var uri = "platform:/resource/JavaToDocSLE/src/JavaToDocSLE.msl#//@entities.1";
			rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
		}
		{
			var uri = "platform:/resource/JavaToDocSLE/src/JavaToDocSLE.msl#//@entities.2";
			rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
		}
		{
			var uri = "platform:/resource/JavaToDocSLE/src/JavaToDocSLE.msl#//@entities.3";
			rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
		}
		{
			var uri = "platform:/resource/JavaToDocSLE/src/JavaToDocSLE.msl#//@entities.4";
			rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
		}
		{
			var uri = "platform:/resource/JavaToDocSLE/src/JavaToDocSLE.msl#//@entities.5";
			rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
		}
		return rules;
	}
	
	public Collection<IConstraint> getConstraintsOfJavaToDocSLE(){
		var constraints = new HashSet<IConstraint>();
		var rs = spec.eResource().getResourceSet();
		return constraints;
	}
	
	//:~> platform:/resource/JavaToDocSLE/src/JavaToDocSLE.msl#//@entities.1
	public static final String JavaToDocSLE__ClazzToDocRule = "ClazzToDocRule";
	public static final String JavaToDocSLE__ClazzToDocRule__c = "c";
	public static final String JavaToDocSLE__ClazzToDocRule__d = "d";
	
	//:~> platform:/resource/JavaToDocSLE/src/JavaToDocSLE.msl#//@entities.2
	public static final String JavaToDocSLE__SubClazzToSubDocRule = "SubClazzToSubDocRule";
	public static final String JavaToDocSLE__SubClazzToSubDocRule__c = "c";
	public static final String JavaToDocSLE__SubClazzToSubDocRule__sc = "sc";
	public static final String JavaToDocSLE__SubClazzToSubDocRule__d = "d";
	public static final String JavaToDocSLE__SubClazzToSubDocRule__sd = "sd";
	
	//:~> platform:/resource/JavaToDocSLE/src/JavaToDocSLE.msl#//@entities.3
	public static final String JavaToDocSLE__MethodToEntryRule = "MethodToEntryRule";
	public static final String JavaToDocSLE__MethodToEntryRule__c = "c";
	public static final String JavaToDocSLE__MethodToEntryRule__m = "m";
	public static final String JavaToDocSLE__MethodToEntryRule__d = "d";
	public static final String JavaToDocSLE__MethodToEntryRule__e = "e";
	
	//:~> platform:/resource/JavaToDocSLE/src/JavaToDocSLE.msl#//@entities.4
	public static final String JavaToDocSLE__AddParameterRule = "AddParameterRule";
	public static final String JavaToDocSLE__AddParameterRule__m = "m";
	public static final String JavaToDocSLE__AddParameterRule__p = "p";
	public static final String JavaToDocSLE__AddParameterRule__e = "e";
	
	//:~> platform:/resource/JavaToDocSLE/src/JavaToDocSLE.msl#//@entities.5
	public static final String JavaToDocSLE__FieldToEntryRule = "FieldToEntryRule";
	public static final String JavaToDocSLE__FieldToEntryRule__c = "c";
	public static final String JavaToDocSLE__FieldToEntryRule__f = "f";
	public static final String JavaToDocSLE__FieldToEntryRule__d = "d";
	public static final String JavaToDocSLE__FieldToEntryRule__e = "e";
}