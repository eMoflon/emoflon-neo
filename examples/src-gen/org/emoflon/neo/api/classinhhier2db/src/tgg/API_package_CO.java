/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.classinhhier2db.src.tgg;

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
public class API_package_CO {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_package_CO(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_package_CO(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		this((EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/ClassInhHier2DB/tgg-gen/src/tgg/package_CO.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI), builder);
	}

	public API_package_CO(EMSL_Spec spec, NeoCoreBuilder builder) {
		this.spec = spec;
		this.builder = builder;
	}

	//:~> platform:/resource/ClassInhHier2DB/tgg-gen/src/tgg/package_CO.msl#//@entities.0
	public Collection<NeoRule> getAllRulesForPackage_CO() {
		Collection<NeoRule> rules = new HashSet<>();
		
		rules.add(getRule_Static().rule());
		return rules;
	}
	
	public Collection<NeoConstraint> getAllConstraintsForPackage_CO() {
		Collection<NeoConstraint> constraints = new HashSet<>();
		return constraints;
	}
	
	public Collection<Rule> getAllEMSLRulesForPackage_CO(){
		var rules = new HashSet<Rule>();
		rules.add((Rule) spec.getEntities().get(1));
		return rules;
	}
	
	//:~> platform:/resource/ClassInhHier2DB/tgg-gen/src/tgg/package_CO.msl#//@entities.1
	public StaticAccess getRule_Static() {
		return new StaticAccess();
	}
	
	public class StaticAccess extends NeoRuleCoAccess<StaticData, StaticCoData, StaticMask> {
		public final String _this = "this";
		public final String _try = "try";
		public final String _interface = "interface";
		
		public final String _param____srcModelName = "__srcModelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(1);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<StaticData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new StaticData(d));
		}
			
		@Override
		public Stream<StaticCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new StaticCoData(d));
		}
		
		@Override
		public StaticMask mask() {
			return new StaticMask();
		}
	}
	
	public class StaticData extends NeoData {
		public StaticData(Record data) {
		
		}
	}
	
	public class StaticCoData extends NeoData {
		public StaticCoData(Record data) {
		
		}
	}
	
	public class StaticMask extends NeoMask {
	}
}