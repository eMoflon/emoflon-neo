/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.org.emoflon.neo.emf;

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
import org.neo4j.driver.Value;
import org.neo4j.driver.Record;
import org.eclipse.emf.common.util.URI;
import org.emoflon.neo.api.org.emoflon.neo.emf.API_Common;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.Optional;
import java.time.LocalDate;

@SuppressWarnings("unused")
public class API_RulesForImporter {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_RulesForImporter(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_RulesForImporter(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		this((EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/org.emoflon.neo.emf/src/RulesForImporter.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI), builder);
	}

	public API_RulesForImporter(EMSL_Spec spec, NeoCoreBuilder builder) {
		this.spec = spec;
		this.builder = builder;
	}

	//:~> platform:/resource/org.emoflon.neo.emf/src/RulesForImporter.msl#//@entities.0
	public CreateMetamodelAccess getRule_CreateMetamodel() {
		return new CreateMetamodelAccess();
	}
	
	public class CreateMetamodelAccess extends NeoRuleCoAccess<CreateMetamodelData, CreateMetamodelCoData, CreateMetamodelMask> {
		public final String _neoCore = "neoCore";
		public final String _mm = "mm";
		
		public final String _param__name = "name";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(0);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<CreateMetamodelData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateMetamodelData(d));
		}
			
		@Override
		public Stream<CreateMetamodelCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateMetamodelCoData(d));
		}
		
		@Override
		public CreateMetamodelMask mask() {
			return new CreateMetamodelMask();
		}
	}
	
	public class CreateMetamodelData extends NeoData {
		public CreateMetamodelData(Record data) {
		
		}
	}
	
	public class CreateMetamodelCoData extends NeoData {
		public CreateMetamodelCoData(Record data) {
		
		}
	}
	
	public class CreateMetamodelMask extends NeoMask {
	}
	
	//:~> platform:/resource/org.emoflon.neo.emf/src/RulesForImporter.msl#//@entities.1
	public CreateEClassAccess getRule_CreateEClass() {
		return new CreateEClassAccess();
	}
	
	public class CreateEClassAccess extends NeoRuleCoAccess<CreateEClassData, CreateEClassCoData, CreateEClassMask> {
		public final String _eclass = "eclass";
		
		public final String _param__name = "name";
		public final String _param__namespace = "namespace";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(1);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<CreateEClassData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateEClassData(d));
		}
			
		@Override
		public Stream<CreateEClassCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateEClassCoData(d));
		}
		
		@Override
		public CreateEClassMask mask() {
			return new CreateEClassMask();
		}
	}
	
	public class CreateEClassData extends NeoData {
		public CreateEClassData(Record data) {
		
		}
	}
	
	public class CreateEClassCoData extends NeoData {
		public CreateEClassCoData(Record data) {
		
		}
	}
	
	public class CreateEClassMask extends NeoMask {
	}
	
	//:~> platform:/resource/org.emoflon.neo.emf/src/RulesForImporter.msl#//@entities.2
	public CreateInheritanceAccess getRule_CreateInheritance() {
		return new CreateInheritanceAccess();
	}
	
	public class CreateInheritanceAccess extends NeoRuleCoAccess<CreateInheritanceData, CreateInheritanceCoData, CreateInheritanceMask> {
		public final String _superClass = "superClass";
		public final String _subClass = "subClass";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(2);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<CreateInheritanceData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateInheritanceData(d));
		}
			
		@Override
		public Stream<CreateInheritanceCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateInheritanceCoData(d));
		}
		
		@Override
		public CreateInheritanceMask mask() {
			return new CreateInheritanceMask();
		}
	}
	
	public class CreateInheritanceData extends NeoData {
		public CreateInheritanceData(Record data) {
		
		}
	}
	
	public class CreateInheritanceCoData extends NeoData {
		public CreateInheritanceCoData(Record data) {
		
		}
	}
	
	public class CreateInheritanceMask extends NeoMask {
	}
	
	//:~> platform:/resource/org.emoflon.neo.emf/src/RulesForImporter.msl#//@entities.3
	public ECoreTypesAccess getPattern_ECoreTypes() {
		return new ECoreTypesAccess();
	}
	
	public class ECoreTypesAccess extends NeoPatternAccess<ECoreTypesData, ECoreTypesMask> {
		public final String _eref = "eref";
		public final String _eclass = "eclass";
		public final String _eob = "eob";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(3);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<ECoreTypesData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new ECoreTypesData(d));
		}
		
		@Override
		public ECoreTypesMask mask() {
			return new ECoreTypesMask();
		}
	}
	
	public class ECoreTypesData extends NeoData {
		public ECoreTypesData(Record data) {
			
		}
	}
	
	public class ECoreTypesMask extends NeoMask {
	}
	
	//:~> platform:/resource/org.emoflon.neo.emf/src/RulesForImporter.msl#//@entities.4
	public CreateRelationAccess getRule_CreateRelation() {
		return new CreateRelationAccess();
	}
	
	public class CreateRelationAccess extends NeoRuleCoAccess<CreateRelationData, CreateRelationCoData, CreateRelationMask> {
		public final String _src = "src";
		public final String _r = "r";
		public final String _trg = "trg";
		
		public final String _param__refName = "refName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(4);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<CreateRelationData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateRelationData(d));
		}
			
		@Override
		public Stream<CreateRelationCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateRelationCoData(d));
		}
		
		@Override
		public CreateRelationMask mask() {
			return new CreateRelationMask();
		}
	}
	
	public class CreateRelationData extends NeoData {
		public CreateRelationData(Record data) {
		
		}
	}
	
	public class CreateRelationCoData extends NeoData {
		public CreateRelationCoData(Record data) {
		
		}
	}
	
	public class CreateRelationMask extends NeoMask {
	}
	
	//:~> platform:/resource/org.emoflon.neo.emf/src/RulesForImporter.msl#//@entities.5
	public CreateModelAccess getRule_CreateModel() {
		return new CreateModelAccess();
	}
	
	public class CreateModelAccess extends NeoRuleCoAccess<CreateModelData, CreateModelCoData, CreateModelMask> {
		public final String _m = "m";
		
		public final String _param__modelName = "modelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(5);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<CreateModelData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateModelData(d));
		}
			
		@Override
		public Stream<CreateModelCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateModelCoData(d));
		}
		
		@Override
		public CreateModelMask mask() {
			return new CreateModelMask();
		}
	}
	
	public class CreateModelData extends NeoData {
		public CreateModelData(Record data) {
		
		}
	}
	
	public class CreateModelCoData extends NeoData {
		public CreateModelCoData(Record data) {
		
		}
	}
	
	public class CreateModelMask extends NeoMask {
	}
	
	//:~> platform:/resource/org.emoflon.neo.emf/src/RulesForImporter.msl#//@entities.6
	public TypeModelAccess getRule_TypeModel() {
		return new TypeModelAccess();
	}
	
	public class TypeModelAccess extends NeoRuleCoAccess<TypeModelData, TypeModelCoData, TypeModelMask> {
		public final String _m = "m";
		public final String _mm = "mm";
		
		public final String _param__modelName = "modelName";
		public final String _param__metamodelName = "metamodelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(6);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<TypeModelData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new TypeModelData(d));
		}
			
		@Override
		public Stream<TypeModelCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new TypeModelCoData(d));
		}
		
		@Override
		public TypeModelMask mask() {
			return new TypeModelMask();
		}
	}
	
	public class TypeModelData extends NeoData {
		public TypeModelData(Record data) {
		
		}
	}
	
	public class TypeModelCoData extends NeoData {
		public TypeModelCoData(Record data) {
		
		}
	}
	
	public class TypeModelMask extends NeoMask {
	}
	
	//:~> platform:/resource/org.emoflon.neo.emf/src/RulesForImporter.msl#//@entities.7
	public CreateAttributeAccess getRule_CreateAttribute() {
		return new CreateAttributeAccess();
	}
	
	public class CreateAttributeAccess extends NeoRuleCoAccess<CreateAttributeData, CreateAttributeCoData, CreateAttributeMask> {
		public final String _cls = "cls";
		public final String _eAttr = "eAttr";
		public final String _type = "type";
		
		public final String _param__attrName = "attrName";
		public final String _param__typeName = "typeName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(7);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<CreateAttributeData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateAttributeData(d));
		}
			
		@Override
		public Stream<CreateAttributeCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateAttributeCoData(d));
		}
		
		@Override
		public CreateAttributeMask mask() {
			return new CreateAttributeMask();
		}
	}
	
	public class CreateAttributeData extends NeoData {
		public CreateAttributeData(Record data) {
		
		}
	}
	
	public class CreateAttributeCoData extends NeoData {
		public CreateAttributeCoData(Record data) {
		
		}
	}
	
	public class CreateAttributeMask extends NeoMask {
	}
	
	//:~> platform:/resource/org.emoflon.neo.emf/src/RulesForImporter.msl#//@entities.8
	public CreateEnumAccess getRule_CreateEnum() {
		return new CreateEnumAccess();
	}
	
	public class CreateEnumAccess extends NeoRuleCoAccess<CreateEnumData, CreateEnumCoData, CreateEnumMask> {
		public final String _en = "en";
		
		public final String _param__name = "name";
		public final String _param__namespace = "namespace";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(8);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<CreateEnumData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateEnumData(d));
		}
			
		@Override
		public Stream<CreateEnumCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateEnumCoData(d));
		}
		
		@Override
		public CreateEnumMask mask() {
			return new CreateEnumMask();
		}
	}
	
	public class CreateEnumData extends NeoData {
		public CreateEnumData(Record data) {
		
		}
	}
	
	public class CreateEnumCoData extends NeoData {
		public CreateEnumCoData(Record data) {
		
		}
	}
	
	public class CreateEnumMask extends NeoMask {
	}
	
	//:~> platform:/resource/org.emoflon.neo.emf/src/RulesForImporter.msl#//@entities.9
	public CreateEEnumLiteralAccess getRule_CreateEEnumLiteral() {
		return new CreateEEnumLiteralAccess();
	}
	
	public class CreateEEnumLiteralAccess extends NeoRuleCoAccess<CreateEEnumLiteralData, CreateEEnumLiteralCoData, CreateEEnumLiteralMask> {
		public final String _en = "en";
		public final String _lit = "lit";
		
		public final String _param__name = "name";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(9);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<CreateEEnumLiteralData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateEEnumLiteralData(d));
		}
			
		@Override
		public Stream<CreateEEnumLiteralCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateEEnumLiteralCoData(d));
		}
		
		@Override
		public CreateEEnumLiteralMask mask() {
			return new CreateEEnumLiteralMask();
		}
	}
	
	public class CreateEEnumLiteralData extends NeoData {
		public CreateEEnumLiteralData(Record data) {
		
		}
	}
	
	public class CreateEEnumLiteralCoData extends NeoData {
		public CreateEEnumLiteralCoData(Record data) {
		
		}
	}
	
	public class CreateEEnumLiteralMask extends NeoMask {
	}
}