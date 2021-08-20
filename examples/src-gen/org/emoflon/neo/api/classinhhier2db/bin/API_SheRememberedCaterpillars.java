/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.classinhhier2db.bin;

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
public class API_SheRememberedCaterpillars {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_SheRememberedCaterpillars(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_SheRememberedCaterpillars(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		this((EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI), builder);
	}

	public API_SheRememberedCaterpillars(EMSL_Spec spec, NeoCoreBuilder builder) {
		this.spec = spec;
		this.builder = builder;
	}

	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.0
	public Metamodel getMetamodel_SheRememberedCaterpillars(){
		return (Metamodel) spec.getEntities().get(0);
	}
	
	public static final String SheRememberedCaterpillars__Character = "SheRememberedCaterpillars__Character";
	public static final String SheRememberedCaterpillars__Platform = "SheRememberedCaterpillars__Platform";
	public static final String SheRememberedCaterpillars__Bridge = "SheRememberedCaterpillars__Bridge";
	public static final String SheRememberedCaterpillars__Wall = "SheRememberedCaterpillars__Wall";
	public static final String SheRememberedCaterpillars__ColouredThing = "SheRememberedCaterpillars__ColouredThing";
	public static final String SheRememberedCaterpillars__IsOnAPlatform = "SheRememberedCaterpillars__IsOnAPlatform";
	public static final String SheRememberedCaterpillars__ColouredThingOnPlatform = "SheRememberedCaterpillars__ColouredThingOnPlatform";
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.1
	public Model getModel_SimpleGame(){
		return (Model) spec.getEntities().get(1);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.2
	public CanCrossBridgeAccess getPattern_CanCrossBridge() {
		return new CanCrossBridgeAccess();
	}
	
	public class CanCrossBridgeAccess extends NeoPatternAccess<CanCrossBridgeData, CanCrossBridgeMask> {
		public final String _a = "a";
		public final String _b = "b";
		public final String _p1 = "p1";
		public final String _p2 = "p2";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(2);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<CanCrossBridgeData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CanCrossBridgeData(d));
		}
		
		@Override
		public CanCrossBridgeMask mask() {
			return new CanCrossBridgeMask();
		}
	}
	
	public class CanCrossBridgeData extends NeoData {
		public CanCrossBridgeData(Record data) {
			
		}
	}
	
	public class CanCrossBridgeMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.3
	public ColourBridgeREDAccess getRule_ColourBridgeRED() {
		return new ColourBridgeREDAccess();
	}
	
	public class ColourBridgeREDAccess extends NeoRuleCoAccess<ColourBridgeREDData, ColourBridgeREDCoData, ColourBridgeREDMask> {
		public final String _b = "b";
		public final String _p1 = "p1";
		public final String _p2 = "p2";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(3);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<ColourBridgeREDData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new ColourBridgeREDData(d));
		}
			
		@Override
		public Stream<ColourBridgeREDCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new ColourBridgeREDCoData(d));
		}
		
		@Override
		public ColourBridgeREDMask mask() {
			return new ColourBridgeREDMask();
		}
	}
	
	public class ColourBridgeREDData extends NeoData {
		public ColourBridgeREDData(Record data) {
		
		}
	}
	
	public class ColourBridgeREDCoData extends NeoData {
		public ColourBridgeREDCoData(Record data) {
		
		}
	}
	
	public class ColourBridgeREDMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.4
	public EverythingBlueAccess getPattern_EverythingBlue() {
		return new EverythingBlueAccess();
	}
	
	public class EverythingBlueAccess extends NeoPatternAccess<EverythingBlueData, EverythingBlueMask> {
		public final String _a = "a";
		public final String _b = "b";
		public final String _p1 = "p1";
		public final String _p2 = "p2";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(4);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<EverythingBlueData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new EverythingBlueData(d));
		}
		
		@Override
		public EverythingBlueMask mask() {
			return new EverythingBlueMask();
		}
	}
	
	public class EverythingBlueData extends NeoData {
		public EverythingBlueData(Record data) {
			
		}
	}
	
	public class EverythingBlueMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.5
	public BlueCharacterAccess getPattern_BlueCharacter() {
		return new BlueCharacterAccess();
	}
	
	public class BlueCharacterAccess extends NeoPatternAccess<BlueCharacterData, BlueCharacterMask> {
		public final String _a = "a";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(5);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<BlueCharacterData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new BlueCharacterData(d));
		}
		
		@Override
		public BlueCharacterMask mask() {
			return new BlueCharacterMask();
		}
	}
	
	public class BlueCharacterData extends NeoData {
		public BlueCharacterData(Record data) {
			
		}
	}
	
	public class BlueCharacterMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.6
	public BlueAndRedCharacterAccess getPattern_BlueAndRedCharacter() {
		return new BlueAndRedCharacterAccess();
	}
	
	public class BlueAndRedCharacterAccess extends NeoPatternAccess<BlueAndRedCharacterData, BlueAndRedCharacterMask> {
		public final String _a = "a";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(6);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<BlueAndRedCharacterData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new BlueAndRedCharacterData(d));
		}
		
		@Override
		public BlueAndRedCharacterMask mask() {
			return new BlueAndRedCharacterMask();
		}
	}
	
	public class BlueAndRedCharacterData extends NeoData {
		public BlueAndRedCharacterData(Record data) {
			
		}
	}
	
	public class BlueAndRedCharacterMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.7
	public StrangeBridgeAccess getPattern_StrangeBridge() {
		return new StrangeBridgeAccess();
	}
	
	public class StrangeBridgeAccess extends NeoPatternAccess<StrangeBridgeData, StrangeBridgeMask> {
		public final String _b = "b";
		public final String _p = "p";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(7);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<StrangeBridgeData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new StrangeBridgeData(d));
		}
		
		@Override
		public StrangeBridgeMask mask() {
			return new StrangeBridgeMask();
		}
	}
	
	public class StrangeBridgeData extends NeoData {
		public StrangeBridgeData(Record data) {
			
		}
	}
	
	public class StrangeBridgeMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.8
	public StandingInFrontOfBridgeAccess getPattern_StandingInFrontOfBridge() {
		return new StandingInFrontOfBridgeAccess();
	}
	
	public class StandingInFrontOfBridgeAccess extends NeoPatternAccess<StandingInFrontOfBridgeData, StandingInFrontOfBridgeMask> {
		public final String _a = "a";
		public final String _b = "b";
		public final String _p = "p";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(8);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<StandingInFrontOfBridgeData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new StandingInFrontOfBridgeData(d));
		}
		
		@Override
		public StandingInFrontOfBridgeMask mask() {
			return new StandingInFrontOfBridgeMask();
		}
	}
	
	public class StandingInFrontOfBridgeData extends NeoData {
		public StandingInFrontOfBridgeData(Record data) {
			
		}
	}
	
	public class StandingInFrontOfBridgeMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.9
	public ColouredThingOnPlatformAccess getPattern_ColouredThingOnPlatform() {
		return new ColouredThingOnPlatformAccess();
	}
	
	public class ColouredThingOnPlatformAccess extends NeoPatternAccess<ColouredThingOnPlatformData, ColouredThingOnPlatformMask> {
		public final String _c = "c";
		public final String _p = "p";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(9);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<ColouredThingOnPlatformData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new ColouredThingOnPlatformData(d));
		}
		
		@Override
		public ColouredThingOnPlatformMask mask() {
			return new ColouredThingOnPlatformMask();
		}
	}
	
	public class ColouredThingOnPlatformData extends NeoData {
		public ColouredThingOnPlatformData(Record data) {
			
		}
	}
	
	public class ColouredThingOnPlatformMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.10
	public ColouredThingAccess getPattern_ColouredThing() {
		return new ColouredThingAccess();
	}
	
	public class ColouredThingAccess extends NeoPatternAccess<ColouredThingData, ColouredThingMask> {
		public final String _c = "c";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(10);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<ColouredThingData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new ColouredThingData(d));
		}
		
		@Override
		public ColouredThingMask mask() {
			return new ColouredThingMask();
		}
	}
	
	public class ColouredThingData extends NeoData {
		public ColouredThingData(Record data) {
			
		}
	}
	
	public class ColouredThingMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.11
	public NoWayForwardAccess getPattern_NoWayForward() {
		return new NoWayForwardAccess();
	}
	
	public class NoWayForwardAccess extends NeoPatternAccess<NoWayForwardData, NoWayForwardMask> {
		public final String _a = "a";
		public final String _p = "p";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(11);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<NoWayForwardData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new NoWayForwardData(d));
		}
		
		@Override
		public NoWayForwardMask mask() {
			return new NoWayForwardMask();
		}
	}
	
	public class NoWayForwardData extends NeoData {
		public NoWayForwardData(Record data) {
			
		}
	}
	
	public class NoWayForwardMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.12
	public NoDeadEndAccess getPattern_NoDeadEnd() {
		return new NoDeadEndAccess();
	}
	
	public class NoDeadEndAccess extends NeoPatternAccess<NoDeadEndData, NoDeadEndMask> {
		public final String _a = "a";
		public final String _p1 = "p1";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(12);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<NoDeadEndData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new NoDeadEndData(d));
		}
		
		@Override
		public NoDeadEndMask mask() {
			return new NoDeadEndMask();
		}
	}
	
	public class NoDeadEndData extends NeoData {
		public NoDeadEndData(Record data) {
			
		}
	}
	
	public class NoDeadEndMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.13
	public BridgeAccess getPattern_Bridge() {
		return new BridgeAccess();
	}
	
	public class BridgeAccess extends NeoPatternAccess<BridgeData, BridgeMask> {
		public final String _p1 = "p1";
		public final String _p2 = "p2";
		public final String _b = "b";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(13);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<BridgeData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new BridgeData(d));
		}
		
		@Override
		public BridgeMask mask() {
			return new BridgeMask();
		}
	}
	
	public class BridgeData extends NeoData {
		public BridgeData(Record data) {
			
		}
	}
	
	public class BridgeMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.14
	public IConstraint getConstraint_NoStrangeBridges() {
		var c = (Constraint) spec.getEntities().get(14);
		return NeoConstraintFactory.createNeoConstraint(c, builder);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.15
	public IConstraint getConstraint_StrangeBridges() {
		var c = (Constraint) spec.getEntities().get(15);
		return NeoConstraintFactory.createNeoConstraint(c, builder);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.16
	public IConstraint getConstraint_CanCrossBridgeSomewhere() {
		var c = (Constraint) spec.getEntities().get(16);
		return NeoConstraintFactory.createNeoConstraint(c, builder);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.17
	public IConstraint getConstraint_AlwaysOnPlatform() {
		var c = (Constraint) spec.getEntities().get(17);
		return NeoConstraintFactory.createNeoConstraint(c, builder);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/SheRememberedCaterpillars/bin/SheRememberedCaterpillars.msl#//@entities.18
	public IConstraint getConstraint_NothingBlue() {
		var c = (Constraint) spec.getEntities().get(18);
		return NeoConstraintFactory.createNeoConstraint(c, builder);
	}
}
