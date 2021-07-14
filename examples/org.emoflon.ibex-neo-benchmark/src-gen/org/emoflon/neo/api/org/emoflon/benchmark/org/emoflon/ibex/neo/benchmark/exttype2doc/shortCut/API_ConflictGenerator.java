/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.org.emoflon.benchmark.org.emoflon.ibex.neo.benchmark.exttype2doc.shortCut;

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
import org.emoflon.neo.api.org.emoflon.benchmark.API_Common;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.Optional;
import java.time.LocalDate;

@SuppressWarnings("unused")
public class API_ConflictGenerator {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_ConflictGenerator(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_ConflictGenerator(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		this((EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/org.emoflon.benchmark/src/org/emoflon/ibex/neo/benchmark/exttype2doc/shortCut/ConflictGenerator.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI), builder);
	}

	public API_ConflictGenerator(EMSL_Spec spec, NeoCoreBuilder builder) {
		this.spec = spec;
		this.builder = builder;
	}

	//:~> platform:/resource/org.emoflon.benchmark/src/org/emoflon/ibex/neo/benchmark/exttype2doc/shortCut/ConflictGenerator.msl#//@entities.0
	public MovePackageAccess getRule_MovePackage() {
		return new MovePackageAccess();
	}
	
	public class MovePackageAccess extends NeoRuleCoAccess<MovePackageData, MovePackageCoData, MovePackageMask> {
		public final String _p0 = "p0";
		public final String _p1 = "p1";
		public final String _p2 = "p2";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(0);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<MovePackageData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new MovePackageData(d));
		}
			
		@Override
		public Stream<MovePackageCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new MovePackageCoData(d));
		}
		
		@Override
		public MovePackageMask mask() {
			return new MovePackageMask();
		}
	}
	
	public class MovePackageData extends NeoData {
		public MovePackageData(Record data) {
		
		}
	}
	
	public class MovePackageCoData extends NeoData {
		public MovePackageCoData(Record data) {
		
		}
	}
	
	public class MovePackageMask extends NeoMask {
	}
	
	//:~> platform:/resource/org.emoflon.benchmark/src/org/emoflon/ibex/neo/benchmark/exttype2doc/shortCut/ConflictGenerator.msl#//@entities.1
	public MoveTypeRootAccess getRule_MoveTypeRoot() {
		return new MoveTypeRootAccess();
	}
	
	public class MoveTypeRootAccess extends NeoRuleCoAccess<MoveTypeRootData, MoveTypeRootCoData, MoveTypeRootMask> {
		public final String _p0 = "p0";
		public final String _p1 = "p1";
		public final String _p2 = "p2";
		public final String _t1 = "t1";
		public final String _t2 = "t2";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(1);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<MoveTypeRootData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new MoveTypeRootData(d));
		}
			
		@Override
		public Stream<MoveTypeRootCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new MoveTypeRootCoData(d));
		}
		
		@Override
		public MoveTypeRootMask mask() {
			return new MoveTypeRootMask();
		}
	}
	
	public class MoveTypeRootData extends NeoData {
		public MoveTypeRootData(Record data) {
		
		}
	}
	
	public class MoveTypeRootCoData extends NeoData {
		public MoveTypeRootCoData(Record data) {
		
		}
	}
	
	public class MoveTypeRootMask extends NeoMask {
	}
	
	//:~> platform:/resource/org.emoflon.benchmark/src/org/emoflon/ibex/neo/benchmark/exttype2doc/shortCut/ConflictGenerator.msl#//@entities.2
	public OtherTypeAccess getPattern_OtherType() {
		return new OtherTypeAccess();
	}
	
	public class OtherTypeAccess extends NeoPatternAccess<OtherTypeData, OtherTypeMask> {
		public final String _p2 = "p2";
		public final String _t = "t";
		public final String _t1 = "t1";
		public final String _t2 = "t2";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(2);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<OtherTypeData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new OtherTypeData(d));
		}
		
		@Override
		public OtherTypeMask mask() {
			return new OtherTypeMask();
		}
	}
	
	public class OtherTypeData extends NeoData {
		public OtherTypeData(Record data) {
			
		}
	}
	
	public class OtherTypeMask extends NeoMask {
	}
	
	//:~> platform:/resource/org.emoflon.benchmark/src/org/emoflon/ibex/neo/benchmark/exttype2doc/shortCut/ConflictGenerator.msl#//@entities.3
	public MoveTypeLeafAccess getRule_MoveTypeLeaf() {
		return new MoveTypeLeafAccess();
	}
	
	public class MoveTypeLeafAccess extends NeoRuleCoAccess<MoveTypeLeafData, MoveTypeLeafCoData, MoveTypeLeafMask> {
		public final String _p0 = "p0";
		public final String _p1 = "p1";
		public final String _p2 = "p2";
		public final String _t1 = "t1";
		public final String _t2 = "t2";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(3);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<MoveTypeLeafData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new MoveTypeLeafData(d));
		}
			
		@Override
		public Stream<MoveTypeLeafCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new MoveTypeLeafCoData(d));
		}
		
		@Override
		public MoveTypeLeafMask mask() {
			return new MoveTypeLeafMask();
		}
	}
	
	public class MoveTypeLeafData extends NeoData {
		public MoveTypeLeafData(Record data) {
		
		}
	}
	
	public class MoveTypeLeafCoData extends NeoData {
		public MoveTypeLeafCoData(Record data) {
		
		}
	}
	
	public class MoveTypeLeafMask extends NeoMask {
	}
	
	//:~> platform:/resource/org.emoflon.benchmark/src/org/emoflon/ibex/neo/benchmark/exttype2doc/shortCut/ConflictGenerator.msl#//@entities.4
	public OtherTypeInheritsAccess getPattern_OtherTypeInherits() {
		return new OtherTypeInheritsAccess();
	}
	
	public class OtherTypeInheritsAccess extends NeoPatternAccess<OtherTypeInheritsData, OtherTypeInheritsMask> {
		public final String _t = "t";
		public final String _t2 = "t2";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(4);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<OtherTypeInheritsData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new OtherTypeInheritsData(d));
		}
		
		@Override
		public OtherTypeInheritsMask mask() {
			return new OtherTypeInheritsMask();
		}
	}
	
	public class OtherTypeInheritsData extends NeoData {
		public OtherTypeInheritsData(Record data) {
			
		}
	}
	
	public class OtherTypeInheritsMask extends NeoMask {
	}
	
	//:~> platform:/resource/org.emoflon.benchmark/src/org/emoflon/ibex/neo/benchmark/exttype2doc/shortCut/ConflictGenerator.msl#//@entities.5
	public CreateTypeRootAccess getRule_CreateTypeRoot() {
		return new CreateTypeRootAccess();
	}
	
	public class CreateTypeRootAccess extends NeoRuleCoAccess<CreateTypeRootData, CreateTypeRootCoData, CreateTypeRootMask> {
		public final String _p0 = "p0";
		public final String _t0 = "t0";
		public final String _t1 = "t1";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(5);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<CreateTypeRootData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateTypeRootData(d));
		}
			
		@Override
		public Stream<CreateTypeRootCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreateTypeRootCoData(d));
		}
		
		@Override
		public CreateTypeRootMask mask() {
			return new CreateTypeRootMask();
		}
	}
	
	public class CreateTypeRootData extends NeoData {
		public CreateTypeRootData(Record data) {
		
		}
	}
	
	public class CreateTypeRootCoData extends NeoData {
		public CreateTypeRootCoData(Record data) {
		
		}
	}
	
	public class CreateTypeRootMask extends NeoMask {
	}
	
	//:~> platform:/resource/org.emoflon.benchmark/src/org/emoflon/ibex/neo/benchmark/exttype2doc/shortCut/ConflictGenerator.msl#//@entities.6
	public OtherTypesIsExtendedAccess getPattern_OtherTypesIsExtended() {
		return new OtherTypesIsExtendedAccess();
	}
	
	public class OtherTypesIsExtendedAccess extends NeoPatternAccess<OtherTypesIsExtendedData, OtherTypesIsExtendedMask> {
		public final String _t = "t";
		public final String _t1 = "t1";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(6);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<OtherTypesIsExtendedData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new OtherTypesIsExtendedData(d));
		}
		
		@Override
		public OtherTypesIsExtendedMask mask() {
			return new OtherTypesIsExtendedMask();
		}
	}
	
	public class OtherTypesIsExtendedData extends NeoData {
		public OtherTypesIsExtendedData(Record data) {
			
		}
	}
	
	public class OtherTypesIsExtendedMask extends NeoMask {
	}
	
	//:~> platform:/resource/org.emoflon.benchmark/src/org/emoflon/ibex/neo/benchmark/exttype2doc/shortCut/ConflictGenerator.msl#//@entities.7
	public CreatePackageRootAccess getRule_CreatePackageRoot() {
		return new CreatePackageRootAccess();
	}
	
	public class CreatePackageRootAccess extends NeoRuleCoAccess<CreatePackageRootData, CreatePackageRootCoData, CreatePackageRootMask> {
		public final String _p1 = "p1";
		public final String _p0 = "p0";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(7);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<CreatePackageRootData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreatePackageRootData(d));
		}
			
		@Override
		public Stream<CreatePackageRootCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CreatePackageRootCoData(d));
		}
		
		@Override
		public CreatePackageRootMask mask() {
			return new CreatePackageRootMask();
		}
	}
	
	public class CreatePackageRootData extends NeoData {
		public CreatePackageRootData(Record data) {
		
		}
	}
	
	public class CreatePackageRootCoData extends NeoData {
		public CreatePackageRootCoData(Record data) {
		
		}
	}
	
	public class CreatePackageRootMask extends NeoMask {
	}
	
	//:~> platform:/resource/org.emoflon.benchmark/src/org/emoflon/ibex/neo/benchmark/exttype2doc/shortCut/ConflictGenerator.msl#//@entities.8
	public OtherSubPackageAccess getPattern_OtherSubPackage() {
		return new OtherSubPackageAccess();
	}
	
	public class OtherSubPackageAccess extends NeoPatternAccess<OtherSubPackageData, OtherSubPackageMask> {
		public final String _p1 = "p1";
		public final String _p = "p";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(8);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<OtherSubPackageData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new OtherSubPackageData(d));
		}
		
		@Override
		public OtherSubPackageMask mask() {
			return new OtherSubPackageMask();
		}
	}
	
	public class OtherSubPackageData extends NeoData {
		public OtherSubPackageData(Record data) {
			
		}
	}
	
	public class OtherSubPackageMask extends NeoMask {
	}
}
