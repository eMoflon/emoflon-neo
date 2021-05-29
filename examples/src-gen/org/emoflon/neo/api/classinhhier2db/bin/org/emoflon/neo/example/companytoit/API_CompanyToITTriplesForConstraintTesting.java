/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.classinhhier2db.bin.org.emoflon.neo.example.companytoit;

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
public class API_CompanyToITTriplesForConstraintTesting {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_CompanyToITTriplesForConstraintTesting(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_CompanyToITTriplesForConstraintTesting(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		this((EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/companytoit/CompanyToITTriplesForConstraintTesting.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI), builder);
	}

	public API_CompanyToITTriplesForConstraintTesting(EMSL_Spec spec, NeoCoreBuilder builder) {
		this.spec = spec;
		this.builder = builder;
	}

	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/companytoit/CompanyToITTriplesForConstraintTesting.msl#//@entities.0
	public Model getModel_InconsistentSource1(){
		return (Model) spec.getEntities().get(0);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/companytoit/CompanyToITTriplesForConstraintTesting.msl#//@entities.1
	public Model getModel_InconsistentTarget1(){
		return (Model) spec.getEntities().get(1);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/companytoit/CompanyToITTriplesForConstraintTesting.msl#//@entities.2
	public InconsistentTriple1Access getRule_InconsistentTriple1() {
		return new InconsistentTriple1Access();
	}
	
	public class InconsistentTriple1Access extends NeoRuleCoAccess<InconsistentTriple1Data, InconsistentTriple1CoData, InconsistentTriple1Mask> {
		public final String _c = "c";
		public final String _a1 = "a1";
		public final String _e1 = "e1";
		public final String _it = "it";
		public final String _router = "router";
		public final String _l1 = "l1";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(2);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<InconsistentTriple1Data> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new InconsistentTriple1Data(d));
		}
			
		@Override
		public Stream<InconsistentTriple1CoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new InconsistentTriple1CoData(d));
		}
		
		@Override
		public InconsistentTriple1Mask mask() {
			return new InconsistentTriple1Mask();
		}
	}
	
	public class InconsistentTriple1Data extends NeoData {
		public InconsistentTriple1Data(Record data) {
		
		}
	}
	
	public class InconsistentTriple1CoData extends NeoData {
		public InconsistentTriple1CoData(Record data) {
		
		}
	}
	
	public class InconsistentTriple1Mask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/companytoit/CompanyToITTriplesForConstraintTesting.msl#//@entities.3
	public Model getModel_ConsistentSource1(){
		return (Model) spec.getEntities().get(3);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/companytoit/CompanyToITTriplesForConstraintTesting.msl#//@entities.4
	public Model getModel_ConsistentTarget1(){
		return (Model) spec.getEntities().get(4);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/companytoit/CompanyToITTriplesForConstraintTesting.msl#//@entities.5
	public ConsistentTriple1Access getRule_ConsistentTriple1() {
		return new ConsistentTriple1Access();
	}
	
	public class ConsistentTriple1Access extends NeoRuleCoAccess<ConsistentTriple1Data, ConsistentTriple1CoData, ConsistentTriple1Mask> {
		public final String _a1 = "a1";
		public final String _router = "router";
		public final String _c = "c";
		public final String _l1 = "l1";
		public final String _l2 = "l2";
		public final String _it = "it";
		public final String _e1 = "e1";
		public final String _e2 = "e2";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(5);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<ConsistentTriple1Data> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new ConsistentTriple1Data(d));
		}
			
		@Override
		public Stream<ConsistentTriple1CoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new ConsistentTriple1CoData(d));
		}
		
		@Override
		public ConsistentTriple1Mask mask() {
			return new ConsistentTriple1Mask();
		}
	}
	
	public class ConsistentTriple1Data extends NeoData {
		public ConsistentTriple1Data(Record data) {
		
		}
	}
	
	public class ConsistentTriple1CoData extends NeoData {
		public ConsistentTriple1CoData(Record data) {
		
		}
	}
	
	public class ConsistentTriple1Mask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/companytoit/CompanyToITTriplesForConstraintTesting.msl#//@entities.6
	public Model getModel_InconsistentSource2(){
		return (Model) spec.getEntities().get(6);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/companytoit/CompanyToITTriplesForConstraintTesting.msl#//@entities.7
	public Model getModel_InconsistentTarget2(){
		return (Model) spec.getEntities().get(7);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/companytoit/CompanyToITTriplesForConstraintTesting.msl#//@entities.8
	public InconsistentTriple2Access getRule_InconsistentTriple2() {
		return new InconsistentTriple2Access();
	}
	
	public class InconsistentTriple2Access extends NeoRuleCoAccess<InconsistentTriple2Data, InconsistentTriple2CoData, InconsistentTriple2Mask> {
		public final String _a1 = "a1";
		public final String _router = "router";
		public final String _c = "c";
		public final String _l1 = "l1";
		public final String _it = "it";
		public final String _e1 = "e1";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(8);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<InconsistentTriple2Data> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new InconsistentTriple2Data(d));
		}
			
		@Override
		public Stream<InconsistentTriple2CoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new InconsistentTriple2CoData(d));
		}
		
		@Override
		public InconsistentTriple2Mask mask() {
			return new InconsistentTriple2Mask();
		}
	}
	
	public class InconsistentTriple2Data extends NeoData {
		public InconsistentTriple2Data(Record data) {
		
		}
	}
	
	public class InconsistentTriple2CoData extends NeoData {
		public InconsistentTriple2CoData(Record data) {
		
		}
	}
	
	public class InconsistentTriple2Mask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/companytoit/CompanyToITTriplesForConstraintTesting.msl#//@entities.9
	public Model getModel_InconsistentSource3(){
		return (Model) spec.getEntities().get(9);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/companytoit/CompanyToITTriplesForConstraintTesting.msl#//@entities.10
	public Model getModel_InconsistentTarget3(){
		return (Model) spec.getEntities().get(10);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/companytoit/CompanyToITTriplesForConstraintTesting.msl#//@entities.11
	public InconsistentTriple3Access getRule_InconsistentTriple3() {
		return new InconsistentTriple3Access();
	}
	
	public class InconsistentTriple3Access extends NeoRuleCoAccess<InconsistentTriple3Data, InconsistentTriple3CoData, InconsistentTriple3Mask> {
		public final String _a1 = "a1";
		public final String _router = "router";
		public final String _c = "c";
		public final String _l1 = "l1";
		public final String _it = "it";
		public final String _e1 = "e1";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(11);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<InconsistentTriple3Data> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new InconsistentTriple3Data(d));
		}
			
		@Override
		public Stream<InconsistentTriple3CoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new InconsistentTriple3CoData(d));
		}
		
		@Override
		public InconsistentTriple3Mask mask() {
			return new InconsistentTriple3Mask();
		}
	}
	
	public class InconsistentTriple3Data extends NeoData {
		public InconsistentTriple3Data(Record data) {
		
		}
	}
	
	public class InconsistentTriple3CoData extends NeoData {
		public InconsistentTriple3CoData(Record data) {
		
		}
	}
	
	public class InconsistentTriple3Mask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/companytoit/CompanyToITTriplesForConstraintTesting.msl#//@entities.12
	public Model getModel_InconsistentSource4(){
		return (Model) spec.getEntities().get(12);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/companytoit/CompanyToITTriplesForConstraintTesting.msl#//@entities.13
	public Model getModel_InconsistentTarget4(){
		return (Model) spec.getEntities().get(13);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/TestSuiteTGG/bin/org/emoflon/neo/example/companytoit/CompanyToITTriplesForConstraintTesting.msl#//@entities.14
	public InconsistentTriple4Access getRule_InconsistentTriple4() {
		return new InconsistentTriple4Access();
	}
	
	public class InconsistentTriple4Access extends NeoRuleCoAccess<InconsistentTriple4Data, InconsistentTriple4CoData, InconsistentTriple4Mask> {
		public final String _a1 = "a1";
		public final String _a2 = "a2";
		public final String _router = "router";
		public final String _c = "c";
		public final String _l1 = "l1";
		public final String _it = "it";
		public final String _e1 = "e1";
		public final String _router2 = "router2";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(14);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<InconsistentTriple4Data> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new InconsistentTriple4Data(d));
		}
			
		@Override
		public Stream<InconsistentTriple4CoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new InconsistentTriple4CoData(d));
		}
		
		@Override
		public InconsistentTriple4Mask mask() {
			return new InconsistentTriple4Mask();
		}
	}
	
	public class InconsistentTriple4Data extends NeoData {
		public InconsistentTriple4Data(Record data) {
		
		}
	}
	
	public class InconsistentTriple4CoData extends NeoData {
		public InconsistentTriple4CoData(Record data) {
		
		}
	}
	
	public class InconsistentTriple4Mask extends NeoMask {
	}
}