/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.classinhhier2db.bin.rules;

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
public class API_SokobanTGGs {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_SokobanTGGs(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_SokobanTGGs(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		this((EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI), builder);
	}

	public API_SokobanTGGs(EMSL_Spec spec, NeoCoreBuilder builder) {
		this.spec = spec;
		this.builder = builder;
	}

	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.0
	public void exportMetamodelsForSokobanImportExport() throws FlattenerException {
		{
			var api = new org.emoflon.neo.api.sokobanlanguage.metamodels.API_SokobanLanguage(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
			builder.exportEMSLEntityToNeo4j(api.getMetamodel_SokobanExchangeFormat());
		}
		{
			var api = new org.emoflon.neo.api.sokobanlanguage.metamodels.API_SokobanLanguage(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
			builder.exportEMSLEntityToNeo4j(api.getMetamodel_SokobanLanguage());
		}
	}
	
	public Collection<TripleRule> getTripleRulesOfSokobanImportExport(){
		var rules = new HashSet<TripleRule>();
		var rs = spec.eResource().getResourceSet();
		{
			var uri = "platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.2";
			rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
		}
		{
			var uri = "platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.3";
			rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
		}
		{
			var uri = "platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.17";
			rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
		}
		{
			var uri = "platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.18";
			rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
		}
		{
			var uri = "platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.12";
			rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
		}
		{
			var uri = "platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.14";
			rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
		}
		{
			var uri = "platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.7";
			rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
		}
		{
			var uri = "platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.10";
			rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
		}
		return rules;
	}
	
	public Collection<IConstraint> getConstraintsOfSokobanImportExport(){
		var constraints = new HashSet<IConstraint>();
		var rs = spec.eResource().getResourceSet();
		return constraints;
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.1
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.2
	public static final String SokobanImportExport__BoardEndEntryRule = "BoardEndEntryRule";
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.3
	public static final String SokobanImportExport__BoardNormalEntryRule = "BoardNormalEntryRule";
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.4
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.5
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.6
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.7
	public static final String SokobanImportExport__AllOtherFieldsEnd = "AllOtherFieldsEnd";
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.8
	public NoDoubleBottomTooAccess getPattern_NoDoubleBottomToo() {
		return new NoDoubleBottomTooAccess();
	}
	
	public class NoDoubleBottomTooAccess extends NeoPatternAccess<NoDoubleBottomTooData, NoDoubleBottomTooMask> {
		public final String _other = "other";
		public final String _ur = "ur";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(8);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<NoDoubleBottomTooData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new NoDoubleBottomTooData(d));
		}
		
		@Override
		public NoDoubleBottomTooMask mask() {
			return new NoDoubleBottomTooMask();
		}
	}
	
	public class NoDoubleBottomTooData extends NeoData {
		public NoDoubleBottomTooData(Record data) {
			
		}
	}
	
	public class NoDoubleBottomTooMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.9
	public NoDoubleRightTooAccess getPattern_NoDoubleRightToo() {
		return new NoDoubleRightTooAccess();
	}
	
	public class NoDoubleRightTooAccess extends NeoPatternAccess<NoDoubleRightTooData, NoDoubleRightTooMask> {
		public final String _other = "other";
		public final String _bl = "bl";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(9);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<NoDoubleRightTooData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new NoDoubleRightTooData(d));
		}
		
		@Override
		public NoDoubleRightTooMask mask() {
			return new NoDoubleRightTooMask();
		}
	}
	
	public class NoDoubleRightTooData extends NeoData {
		public NoDoubleRightTooData(Record data) {
			
		}
	}
	
	public class NoDoubleRightTooMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.10
	public static final String SokobanImportExport__AllOtherFieldsNormal = "AllOtherFieldsNormal";
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.11
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.12
	public static final String SokobanImportExport__FirstRowAllColsEnd = "FirstRowAllColsEnd";
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.13
	public NoDoubleRightAccess getPattern_NoDoubleRight() {
		return new NoDoubleRightAccess();
	}
	
	public class NoDoubleRightAccess extends NeoPatternAccess<NoDoubleRightData, NoDoubleRightMask> {
		public final String _f = "f";
		public final String _other = "other";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(13);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<NoDoubleRightData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new NoDoubleRightData(d));
		}
		
		@Override
		public NoDoubleRightMask mask() {
			return new NoDoubleRightMask();
		}
	}
	
	public class NoDoubleRightData extends NeoData {
		public NoDoubleRightData(Record data) {
			
		}
	}
	
	public class NoDoubleRightMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.14
	public static final String SokobanImportExport__FirstRowAllColsNormal = "FirstRowAllColsNormal";
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.15
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.16
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.17
	public static final String SokobanImportExport__FirstColAllRowsEnd = "FirstColAllRowsEnd";
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.18
	public static final String SokobanImportExport__FirstColAllRowsNormal = "FirstColAllRowsNormal";
	
	//:~> platform:/resource/ClassInhHier2DB/SokobanLanguage/bin/rules/SokobanTGGs.msl#//@entities.19
	public NoDoubleBottomAccess getPattern_NoDoubleBottom() {
		return new NoDoubleBottomAccess();
	}
	
	public class NoDoubleBottomAccess extends NeoPatternAccess<NoDoubleBottomData, NoDoubleBottomMask> {
		public final String _f = "f";
		public final String _other = "other";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(19);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<NoDoubleBottomData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new NoDoubleBottomData(d));
		}
		
		@Override
		public NoDoubleBottomMask mask() {
			return new NoDoubleBottomMask();
		}
	}
	
	public class NoDoubleBottomData extends NeoData {
		public NoDoubleBottomData(Record data) {
			
		}
	}
	
	public class NoDoubleBottomMask extends NeoMask {
	}
}
