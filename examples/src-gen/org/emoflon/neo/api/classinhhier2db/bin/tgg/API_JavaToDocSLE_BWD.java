/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.classinhhier2db.bin.tgg;

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
public class API_JavaToDocSLE_BWD {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_JavaToDocSLE_BWD(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_JavaToDocSLE_BWD(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		this((EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/ClassInhHier2DB/JavaToDocSLE/bin/tgg/JavaToDocSLE_BWD.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI), builder);
	}

	public API_JavaToDocSLE_BWD(EMSL_Spec spec, NeoCoreBuilder builder) {
		this.spec = spec;
		this.builder = builder;
	}

	//:~> platform:/resource/ClassInhHier2DB/JavaToDocSLE/bin/tgg/JavaToDocSLE_BWD.msl#//@entities.0
	public Collection<NeoRule> getAllRulesForJavaToDocSLE_BWD() {
		Collection<NeoRule> rules = new HashSet<>();
		
		rules.add(getRule_ClazzToDocRule().rule());
		rules.add(getRule_SubClazzToSubDocRule().rule());
		rules.add(getRule_MethodToEntryRule().rule());
		rules.add(getRule_AddParameterRule().rule());
		rules.add(getRule_FieldToEntryRule().rule());
		rules.add(getRule_AddGlossaryRule().rule());
		rules.add(getRule_LinkGlossaryEntryRule().rule());
		rules.add(getRule_AddGlossaryEntryRule().rule());
		return rules;
	}
	
	public Collection<NeoConstraint> getAllConstraintsForJavaToDocSLE_BWD() {
		Collection<NeoConstraint> constraints = new HashSet<>();
		return constraints;
	}
	
	public Collection<Rule> getAllEMSLRulesForJavaToDocSLE_BWD(){
		var rules = new HashSet<Rule>();
		rules.add((Rule) spec.getEntities().get(1));
		rules.add((Rule) spec.getEntities().get(2));
		rules.add((Rule) spec.getEntities().get(3));
		rules.add((Rule) spec.getEntities().get(4));
		rules.add((Rule) spec.getEntities().get(5));
		rules.add((Rule) spec.getEntities().get(6));
		rules.add((Rule) spec.getEntities().get(7));
		rules.add((Rule) spec.getEntities().get(8));
		return rules;
	}
	
	//:~> platform:/resource/ClassInhHier2DB/JavaToDocSLE/bin/tgg/JavaToDocSLE_BWD.msl#//@entities.1
	public ClazzToDocRuleAccess getRule_ClazzToDocRule() {
		return new ClazzToDocRuleAccess();
	}
	
	public class ClazzToDocRuleAccess extends NeoRuleCoAccess<ClazzToDocRuleData, ClazzToDocRuleCoData, ClazzToDocRuleMask> {
		public final String _c = "c";
		public final String _d = "d";
		
		public final String _param____srcModelName = "__srcModelName";
		public final String _param____trgModelName = "__trgModelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(1);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<ClazzToDocRuleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new ClazzToDocRuleData(d));
		}
			
		@Override
		public Stream<ClazzToDocRuleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new ClazzToDocRuleCoData(d));
		}
		
		@Override
		public ClazzToDocRuleMask mask() {
			return new ClazzToDocRuleMask();
		}
	}
	
	public class ClazzToDocRuleData extends NeoData {
		public ClazzToDocRuleData(Record data) {
		
		}
	}
	
	public class ClazzToDocRuleCoData extends NeoData {
		public ClazzToDocRuleCoData(Record data) {
		
		}
	}
	
	public class ClazzToDocRuleMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/JavaToDocSLE/bin/tgg/JavaToDocSLE_BWD.msl#//@entities.2
	public SubClazzToSubDocRuleAccess getRule_SubClazzToSubDocRule() {
		return new SubClazzToSubDocRuleAccess();
	}
	
	public class SubClazzToSubDocRuleAccess extends NeoRuleCoAccess<SubClazzToSubDocRuleData, SubClazzToSubDocRuleCoData, SubClazzToSubDocRuleMask> {
		public final String _c = "c";
		public final String _sc = "sc";
		public final String _d = "d";
		public final String _sd = "sd";
		
		public final String _param____srcModelName = "__srcModelName";
		public final String _param____trgModelName = "__trgModelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(2);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<SubClazzToSubDocRuleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new SubClazzToSubDocRuleData(d));
		}
			
		@Override
		public Stream<SubClazzToSubDocRuleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new SubClazzToSubDocRuleCoData(d));
		}
		
		@Override
		public SubClazzToSubDocRuleMask mask() {
			return new SubClazzToSubDocRuleMask();
		}
	}
	
	public class SubClazzToSubDocRuleData extends NeoData {
		public SubClazzToSubDocRuleData(Record data) {
		
		}
	}
	
	public class SubClazzToSubDocRuleCoData extends NeoData {
		public SubClazzToSubDocRuleCoData(Record data) {
		
		}
	}
	
	public class SubClazzToSubDocRuleMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/JavaToDocSLE/bin/tgg/JavaToDocSLE_BWD.msl#//@entities.3
	public MethodToEntryRuleAccess getRule_MethodToEntryRule() {
		return new MethodToEntryRuleAccess();
	}
	
	public class MethodToEntryRuleAccess extends NeoRuleCoAccess<MethodToEntryRuleData, MethodToEntryRuleCoData, MethodToEntryRuleMask> {
		public final String _c = "c";
		public final String _m = "m";
		public final String _d = "d";
		public final String _e = "e";
		
		public final String _param____srcModelName = "__srcModelName";
		public final String _param____trgModelName = "__trgModelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(3);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<MethodToEntryRuleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new MethodToEntryRuleData(d));
		}
			
		@Override
		public Stream<MethodToEntryRuleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new MethodToEntryRuleCoData(d));
		}
		
		@Override
		public MethodToEntryRuleMask mask() {
			return new MethodToEntryRuleMask();
		}
	}
	
	public class MethodToEntryRuleData extends NeoData {
		public MethodToEntryRuleData(Record data) {
		
		}
	}
	
	public class MethodToEntryRuleCoData extends NeoData {
		public MethodToEntryRuleCoData(Record data) {
		
		}
	}
	
	public class MethodToEntryRuleMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/JavaToDocSLE/bin/tgg/JavaToDocSLE_BWD.msl#//@entities.4
	public AddParameterRuleAccess getRule_AddParameterRule() {
		return new AddParameterRuleAccess();
	}
	
	public class AddParameterRuleAccess extends NeoRuleCoAccess<AddParameterRuleData, AddParameterRuleCoData, AddParameterRuleMask> {
		public final String _m = "m";
		public final String _p = "p";
		public final String _e = "e";
		
		public final String _param____srcModelName = "__srcModelName";
		public final String _param__paramName = "paramName";
		public final String _param____trgModelName = "__trgModelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(4);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<AddParameterRuleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new AddParameterRuleData(d));
		}
			
		@Override
		public Stream<AddParameterRuleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new AddParameterRuleCoData(d));
		}
		
		@Override
		public AddParameterRuleMask mask() {
			return new AddParameterRuleMask();
		}
	}
	
	public class AddParameterRuleData extends NeoData {
		public AddParameterRuleData(Record data) {
		
		}
	}
	
	public class AddParameterRuleCoData extends NeoData {
		public AddParameterRuleCoData(Record data) {
		
		}
	}
	
	public class AddParameterRuleMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/JavaToDocSLE/bin/tgg/JavaToDocSLE_BWD.msl#//@entities.5
	public FieldToEntryRuleAccess getRule_FieldToEntryRule() {
		return new FieldToEntryRuleAccess();
	}
	
	public class FieldToEntryRuleAccess extends NeoRuleCoAccess<FieldToEntryRuleData, FieldToEntryRuleCoData, FieldToEntryRuleMask> {
		public final String _c = "c";
		public final String _f = "f";
		public final String _d = "d";
		public final String _e = "e";
		
		public final String _param____srcModelName = "__srcModelName";
		public final String _param____trgModelName = "__trgModelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(5);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<FieldToEntryRuleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new FieldToEntryRuleData(d));
		}
			
		@Override
		public Stream<FieldToEntryRuleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new FieldToEntryRuleCoData(d));
		}
		
		@Override
		public FieldToEntryRuleMask mask() {
			return new FieldToEntryRuleMask();
		}
	}
	
	public class FieldToEntryRuleData extends NeoData {
		public FieldToEntryRuleData(Record data) {
		
		}
	}
	
	public class FieldToEntryRuleCoData extends NeoData {
		public FieldToEntryRuleCoData(Record data) {
		
		}
	}
	
	public class FieldToEntryRuleMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/JavaToDocSLE/bin/tgg/JavaToDocSLE_BWD.msl#//@entities.6
	public AddGlossaryRuleAccess getRule_AddGlossaryRule() {
		return new AddGlossaryRuleAccess();
	}
	
	public class AddGlossaryRuleAccess extends NeoRuleCoAccess<AddGlossaryRuleData, AddGlossaryRuleCoData, AddGlossaryRuleMask> {
		public final String _g = "g";
		
		public final String _param____trgModelName = "__trgModelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(6);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<AddGlossaryRuleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new AddGlossaryRuleData(d));
		}
			
		@Override
		public Stream<AddGlossaryRuleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new AddGlossaryRuleCoData(d));
		}
		
		@Override
		public AddGlossaryRuleMask mask() {
			return new AddGlossaryRuleMask();
		}
	}
	
	public class AddGlossaryRuleData extends NeoData {
		public AddGlossaryRuleData(Record data) {
		
		}
	}
	
	public class AddGlossaryRuleCoData extends NeoData {
		public AddGlossaryRuleCoData(Record data) {
		
		}
	}
	
	public class AddGlossaryRuleMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/JavaToDocSLE/bin/tgg/JavaToDocSLE_BWD.msl#//@entities.7
	public LinkGlossaryEntryRuleAccess getRule_LinkGlossaryEntryRule() {
		return new LinkGlossaryEntryRuleAccess();
	}
	
	public class LinkGlossaryEntryRuleAccess extends NeoRuleCoAccess<LinkGlossaryEntryRuleData, LinkGlossaryEntryRuleCoData, LinkGlossaryEntryRuleMask> {
		public final String _e = "e";
		public final String _ge = "ge";
		
		public final String _param____trgModelName = "__trgModelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(7);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<LinkGlossaryEntryRuleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new LinkGlossaryEntryRuleData(d));
		}
			
		@Override
		public Stream<LinkGlossaryEntryRuleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new LinkGlossaryEntryRuleCoData(d));
		}
		
		@Override
		public LinkGlossaryEntryRuleMask mask() {
			return new LinkGlossaryEntryRuleMask();
		}
	}
	
	public class LinkGlossaryEntryRuleData extends NeoData {
		public LinkGlossaryEntryRuleData(Record data) {
		
		}
	}
	
	public class LinkGlossaryEntryRuleCoData extends NeoData {
		public LinkGlossaryEntryRuleCoData(Record data) {
		
		}
	}
	
	public class LinkGlossaryEntryRuleMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/JavaToDocSLE/bin/tgg/JavaToDocSLE_BWD.msl#//@entities.8
	public AddGlossaryEntryRuleAccess getRule_AddGlossaryEntryRule() {
		return new AddGlossaryEntryRuleAccess();
	}
	
	public class AddGlossaryEntryRuleAccess extends NeoRuleCoAccess<AddGlossaryEntryRuleData, AddGlossaryEntryRuleCoData, AddGlossaryEntryRuleMask> {
		public final String _g = "g";
		public final String _ge = "ge";
		
		public final String _param____trgModelName = "__trgModelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(8);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<AddGlossaryEntryRuleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new AddGlossaryEntryRuleData(d));
		}
			
		@Override
		public Stream<AddGlossaryEntryRuleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new AddGlossaryEntryRuleCoData(d));
		}
		
		@Override
		public AddGlossaryEntryRuleMask mask() {
			return new AddGlossaryEntryRuleMask();
		}
	}
	
	public class AddGlossaryEntryRuleData extends NeoData {
		public AddGlossaryEntryRuleData(Record data) {
		
		}
	}
	
	public class AddGlossaryEntryRuleCoData extends NeoData {
		public AddGlossaryEntryRuleCoData(Record data) {
		
		}
	}
	
	public class AddGlossaryEntryRuleMask extends NeoMask {
	}
}