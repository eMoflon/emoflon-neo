/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.classinhhier2db.tgg;

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
public class API_ClassInhHier2DB_GEN {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_ClassInhHier2DB_GEN(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_ClassInhHier2DB_GEN(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		this((EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/ClassInhHier2DB/tgg-gen/tgg/ClassInhHier2DB_GEN.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI), builder);
	}

	public API_ClassInhHier2DB_GEN(EMSL_Spec spec, NeoCoreBuilder builder) {
		this.spec = spec;
		this.builder = builder;
	}

	//:~> platform:/resource/ClassInhHier2DB/tgg-gen/tgg/ClassInhHier2DB_GEN.msl#//@entities.0
	public Collection<NeoRule> getAllRulesForClassInhHier2DB_GEN() {
		Collection<NeoRule> rules = new HashSet<>();
		
		rules.add(getRule_PackageToDatabaseRule().rule());
		rules.add(getRule_ClassToTableRule().rule());
		rules.add(getRule_SubClassToTableRule().rule());
		rules.add(getRule_AttributeToColumnRule().rule());
		return rules;
	}
	
	public Collection<NeoConstraint> getAllConstraintsForClassInhHier2DB_GEN() {
		Collection<NeoConstraint> constraints = new HashSet<>();
		return constraints;
	}
	
	public Collection<Rule> getAllEMSLRulesForClassInhHier2DB_GEN(){
		var rules = new HashSet<Rule>();
		rules.add((Rule) spec.getEntities().get(1));
		rules.add((Rule) spec.getEntities().get(4));
		rules.add((Rule) spec.getEntities().get(5));
		rules.add((Rule) spec.getEntities().get(6));
		return rules;
	}
	
	//:~> platform:/resource/ClassInhHier2DB/tgg-gen/tgg/ClassInhHier2DB_GEN.msl#//@entities.1
	public PackageToDatabaseRuleAccess getRule_PackageToDatabaseRule() {
		return new PackageToDatabaseRuleAccess();
	}
	
	public class PackageToDatabaseRuleAccess extends NeoRuleCoAccess<PackageToDatabaseRuleData, PackageToDatabaseRuleCoData, PackageToDatabaseRuleMask> {
		public final String _classpackage = "classpackage";
		public final String _db = "db";
		
		public final String _param____srcModelName = "__srcModelName";
		public final String _param__packageName = "packageName";
		public final String _param____trgModelName = "__trgModelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(1);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<PackageToDatabaseRuleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new PackageToDatabaseRuleData(d));
		}
			
		@Override
		public Stream<PackageToDatabaseRuleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new PackageToDatabaseRuleCoData(d));
		}
		
		@Override
		public PackageToDatabaseRuleMask mask() {
			return new PackageToDatabaseRuleMask();
		}
	}
	
	public class PackageToDatabaseRuleData extends NeoData {
		public PackageToDatabaseRuleData(Record data) {
		
		}
	}
	
	public class PackageToDatabaseRuleCoData extends NeoData {
		public PackageToDatabaseRuleCoData(Record data) {
		
		}
	}
	
	public class PackageToDatabaseRuleMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/tgg-gen/tgg/ClassInhHier2DB_GEN.msl#//@entities.2
	public IConstraint getConstraint_PackageToDatabaseRuleNAC() {
		var c = (Constraint) spec.getEntities().get(2);
		return NeoConstraintFactory.createNeoConstraint(c, builder);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/tgg-gen/tgg/ClassInhHier2DB_GEN.msl#//@entities.3
	public PackageToDatabaseRule_packageNameIsTakenAccess getPattern_PackageToDatabaseRule_packageNameIsTaken() {
		return new PackageToDatabaseRule_packageNameIsTakenAccess();
	}
	
	public class PackageToDatabaseRule_packageNameIsTakenAccess extends NeoPatternAccess<PackageToDatabaseRule_packageNameIsTakenData, PackageToDatabaseRule_packageNameIsTakenMask> {
		public final String _classpackage = "classpackage";
		
		public final String _param____srcModelName = "__srcModelName";
		public final String _param__packageName = "packageName";
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(3);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<PackageToDatabaseRule_packageNameIsTakenData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new PackageToDatabaseRule_packageNameIsTakenData(d));
		}
		
		@Override
		public PackageToDatabaseRule_packageNameIsTakenMask mask() {
			return new PackageToDatabaseRule_packageNameIsTakenMask();
		}
	}
	
	public class PackageToDatabaseRule_packageNameIsTakenData extends NeoData {
		public PackageToDatabaseRule_packageNameIsTakenData(Record data) {
			
		}
	}
	
	public class PackageToDatabaseRule_packageNameIsTakenMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/tgg-gen/tgg/ClassInhHier2DB_GEN.msl#//@entities.4
	public ClassToTableRuleAccess getRule_ClassToTableRule() {
		return new ClassToTableRuleAccess();
	}
	
	public class ClassToTableRuleAccess extends NeoRuleCoAccess<ClassToTableRuleData, ClassToTableRuleCoData, ClassToTableRuleMask> {
		public final String _classpackage = "classpackage";
		public final String _clazz = "clazz";
		public final String _db = "db";
		public final String _table = "table";
		
		public final String _param____srcModelName = "__srcModelName";
		public final String _param__clazzname = "clazzname";
		public final String _param____trgModelName = "__trgModelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(4);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<ClassToTableRuleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new ClassToTableRuleData(d));
		}
			
		@Override
		public Stream<ClassToTableRuleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new ClassToTableRuleCoData(d));
		}
		
		@Override
		public ClassToTableRuleMask mask() {
			return new ClassToTableRuleMask();
		}
	}
	
	public class ClassToTableRuleData extends NeoData {
		public ClassToTableRuleData(Record data) {
		
		}
	}
	
	public class ClassToTableRuleCoData extends NeoData {
		public ClassToTableRuleCoData(Record data) {
		
		}
	}
	
	public class ClassToTableRuleMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/tgg-gen/tgg/ClassInhHier2DB_GEN.msl#//@entities.5
	public SubClassToTableRuleAccess getRule_SubClassToTableRule() {
		return new SubClassToTableRuleAccess();
	}
	
	public class SubClassToTableRuleAccess extends NeoRuleCoAccess<SubClassToTableRuleData, SubClassToTableRuleCoData, SubClassToTableRuleMask> {
		public final String _subClazz = "subClazz";
		public final String _classpackage = "classpackage";
		public final String _clazz = "clazz";
		public final String _table = "table";
		
		public final String _param____srcModelName = "__srcModelName";
		public final String _param____trgModelName = "__trgModelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(5);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<SubClassToTableRuleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new SubClassToTableRuleData(d));
		}
			
		@Override
		public Stream<SubClassToTableRuleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new SubClassToTableRuleCoData(d));
		}
		
		@Override
		public SubClassToTableRuleMask mask() {
			return new SubClassToTableRuleMask();
		}
	}
	
	public class SubClassToTableRuleData extends NeoData {
		public SubClassToTableRuleData(Record data) {
		
		}
	}
	
	public class SubClassToTableRuleCoData extends NeoData {
		public SubClassToTableRuleCoData(Record data) {
		
		}
	}
	
	public class SubClassToTableRuleMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/tgg-gen/tgg/ClassInhHier2DB_GEN.msl#//@entities.6
	public AttributeToColumnRuleAccess getRule_AttributeToColumnRule() {
		return new AttributeToColumnRuleAccess();
	}
	
	public class AttributeToColumnRuleAccess extends NeoRuleCoAccess<AttributeToColumnRuleData, AttributeToColumnRuleCoData, AttributeToColumnRuleMask> {
		public final String _clazz = "clazz";
		public final String _attribute = "attribute";
		public final String _column = "column";
		public final String _table = "table";
		
		public final String _param____srcModelName = "__srcModelName";
		public final String _param__attributeName = "attributeName";
		public final String _param____trgModelName = "__trgModelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(6);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<AttributeToColumnRuleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new AttributeToColumnRuleData(d));
		}
			
		@Override
		public Stream<AttributeToColumnRuleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new AttributeToColumnRuleCoData(d));
		}
		
		@Override
		public AttributeToColumnRuleMask mask() {
			return new AttributeToColumnRuleMask();
		}
	}
	
	public class AttributeToColumnRuleData extends NeoData {
		public AttributeToColumnRuleData(Record data) {
		
		}
	}
	
	public class AttributeToColumnRuleCoData extends NeoData {
		public AttributeToColumnRuleCoData(Record data) {
		
		}
	}
	
	public class AttributeToColumnRuleMask extends NeoMask {
	}
}
