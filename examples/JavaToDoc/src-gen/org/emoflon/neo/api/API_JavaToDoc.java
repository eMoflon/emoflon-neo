/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api;

import org.emoflon.neo.cypher.common.*;
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
import org.emoflon.neo.api.API_Common;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.Optional;
import java.time.LocalDate;

@SuppressWarnings("unused")
public class API_JavaToDoc {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_JavaToDoc(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_JavaToDoc(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot){
		spec = (EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/JavaToDoc/src/JavaToDoc.msl", platformResourceURIRoot, platformPluginURIRoot);
		this.builder = builder;
	}

	//:~> platform:/resource/JavaToDoc/src/JavaToDoc.msl#//@entities.0
	public void exportMetamodelsForJavaToDoc() throws FlattenerException {
		{
			var api = new org.emoflon.neo.api.metamodels.API_SimpleJava(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
			builder.exportEMSLEntityToNeo4j(api.getMetamodel_SimpleJava());
		}
		{
			var api = new org.emoflon.neo.api.metamodels.API_SimpleDoc(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
			builder.exportEMSLEntityToNeo4j(api.getMetamodel_SimpleDoc());
		}
	}
	
	public Collection<TripleRule> getTripleRulesOfJavaToDoc(){
		var rules = new HashSet<TripleRule>();
		rules.add((TripleRule) spec.getEntities().get(1));
		rules.add((TripleRule) spec.getEntities().get(3));
		rules.add((TripleRule) spec.getEntities().get(5));
		return rules;
	}
	
	//:~> platform:/resource/JavaToDoc/src/JavaToDoc.msl#//@entities.1
	public static final String JavaToDoc__ClazzToDocRule = "ClazzToDocRule";
	public static final String JavaToDoc__ClazzToDocRule__p = "p";
	public static final String JavaToDoc__ClazzToDocRule__c = "c";
	public static final String JavaToDoc__ClazzToDocRule__f = "f";
	public static final String JavaToDoc__ClazzToDocRule__doc = "doc";
	
	//:~> platform:/resource/JavaToDoc/src/JavaToDoc.msl#//@entities.2
	public ClazzNameIsTakenAccess getPattern_ClazzNameIsTaken() {
		return new ClazzNameIsTakenAccess();
	}
	
	public class ClazzNameIsTakenAccess extends NeoPatternAccess<ClazzNameIsTakenData, ClazzNameIsTakenMask> {
		public final String _clazz = "clazz";
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(2);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<ClazzNameIsTakenData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new ClazzNameIsTakenData(d));
		}
		
		@Override
		public ClazzNameIsTakenMask mask() {
			return new ClazzNameIsTakenMask();
		}
	}
	
	public class ClazzNameIsTakenData extends NeoData {
		public final ClazzNode _clazz;
		
		public ClazzNameIsTakenData(Record data) {
			var _clazz = data.get("clazz");
			this._clazz = new ClazzNode(_clazz);
		}
		
		
		public class ClazzNode {
			public String _name;
			public String _body;
			
			public ClazzNode(Value _clazz) {
				if(!_clazz.get("name").isNull())
					this._name = _clazz.get("name").asString();
				if(!_clazz.get("body").isNull())
					this._body = _clazz.get("body").asString();
			}
		}
		
	}
	
	public class ClazzNameIsTakenMask extends NeoMask {
		public ClazzNameIsTakenMask setClazz(Long value) {
			nodeMask.put("clazz", value);
			return this;
		}
		public ClazzNameIsTakenMask setClazzName(String value) {
			attributeMask.put("clazz.name", value);
			return this;
		}
		public ClazzNameIsTakenMask setClazzBody(String value) {
			attributeMask.put("clazz.body", value);
			return this;
		}
	
	}
	
	//:~> platform:/resource/JavaToDoc/src/JavaToDoc.msl#//@entities.3
	public static final String JavaToDoc__RootToRootRule = "RootToRootRule";
	public static final String JavaToDoc__RootToRootRule__p = "p";
	public static final String JavaToDoc__RootToRootRule__f = "f";
	
	//:~> platform:/resource/JavaToDoc/src/JavaToDoc.msl#//@entities.4
	public PackageNameIsTakenAccess getPattern_PackageNameIsTaken() {
		return new PackageNameIsTakenAccess();
	}
	
	public class PackageNameIsTakenAccess extends NeoPatternAccess<PackageNameIsTakenData, PackageNameIsTakenMask> {
		public final String _package = "package";
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(4);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<PackageNameIsTakenData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new PackageNameIsTakenData(d));
		}
		
		@Override
		public PackageNameIsTakenMask mask() {
			return new PackageNameIsTakenMask();
		}
	}
	
	public class PackageNameIsTakenData extends NeoData {
		public final PackageNode _package;
		
		public PackageNameIsTakenData(Record data) {
			var _package = data.get("package");
			this._package = new PackageNode(_package);
		}
		
		
		public class PackageNode {
			public String _name;
			public String _fullQualifier;
			
			public PackageNode(Value _package) {
				if(!_package.get("name").isNull())
					this._name = _package.get("name").asString();
				if(!_package.get("fullQualifier").isNull())
					this._fullQualifier = _package.get("fullQualifier").asString();
			}
		}
		
	}
	
	public class PackageNameIsTakenMask extends NeoMask {
		public PackageNameIsTakenMask setPackage(Long value) {
			nodeMask.put("package", value);
			return this;
		}
		public PackageNameIsTakenMask setPackageName(String value) {
			attributeMask.put("package.name", value);
			return this;
		}
		public PackageNameIsTakenMask setPackageFullQualifier(String value) {
			attributeMask.put("package.fullQualifier", value);
			return this;
		}
	
	}
	
	//:~> platform:/resource/JavaToDoc/src/JavaToDoc.msl#//@entities.5
	public static final String JavaToDoc__SubToSubRule = "SubToSubRule";
	public static final String JavaToDoc__SubToSubRule__p = "p";
	public static final String JavaToDoc__SubToSubRule__subP = "subP";
	public static final String JavaToDoc__SubToSubRule__f = "f";
	public static final String JavaToDoc__SubToSubRule__subF = "subF";
	public static final String JavaToDoc__SubToSubRule__doc = "doc";
}
