/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.classinhhier2db.bin.org.eneo.fruitgarden;

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
public class API_PatternsAndRules {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_PatternsAndRules(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_PatternsAndRules(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		this((EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/ClassInhHier2DB/FruitGardenLanguage/bin/org/eneo/fruitgarden/PatternsAndRules.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI), builder);
	}

	public API_PatternsAndRules(EMSL_Spec spec, NeoCoreBuilder builder) {
		this.spec = spec;
		this.builder = builder;
	}

	//:~> platform:/resource/ClassInhHier2DB/FruitGardenLanguage/bin/org/eneo/fruitgarden/PatternsAndRules.msl#//@entities.0
	public IConstraint getConstraint_GameIsLost() {
		var c = (Constraint) spec.getEntities().get(0);
		return NeoConstraintFactory.createNeoConstraint(c, builder);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/FruitGardenLanguage/bin/org/eneo/fruitgarden/PatternsAndRules.msl#//@entities.1
	public IConstraint getConstraint_GameIsWon() {
		var c = (Constraint) spec.getEntities().get(1);
		return NeoConstraintFactory.createNeoConstraint(c, builder);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/FruitGardenLanguage/bin/org/eneo/fruitgarden/PatternsAndRules.msl#//@entities.2
	public IConstraint getConstraint_GameOver() {
		var c = (Constraint) spec.getEntities().get(2);
		return NeoConstraintFactory.createNeoConstraint(c, builder);
	}
	
	//:~> platform:/resource/ClassInhHier2DB/FruitGardenLanguage/bin/org/eneo/fruitgarden/PatternsAndRules.msl#//@entities.3
	public CrowNotOnLastSegmentAccess getPattern_CrowNotOnLastSegment() {
		return new CrowNotOnLastSegmentAccess();
	}
	
	public class CrowNotOnLastSegmentAccess extends NeoPatternAccess<CrowNotOnLastSegmentData, CrowNotOnLastSegmentMask> {
		public final String _garden = "garden";
		public final String _crow = "crow";
		public final String _seg = "seg";
		public final String _next = "next";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(3);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<CrowNotOnLastSegmentData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CrowNotOnLastSegmentData(d));
		}
		
		@Override
		public CrowNotOnLastSegmentMask mask() {
			return new CrowNotOnLastSegmentMask();
		}
	}
	
	public class CrowNotOnLastSegmentData extends NeoData {
		public CrowNotOnLastSegmentData(Record data) {
			
		}
	}
	
	public class CrowNotOnLastSegmentMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/FruitGardenLanguage/bin/org/eneo/fruitgarden/PatternsAndRules.msl#//@entities.4
	public OneFruitOnTreeAccess getPattern_OneFruitOnTree() {
		return new OneFruitOnTreeAccess();
	}
	
	public class OneFruitOnTreeAccess extends NeoPatternAccess<OneFruitOnTreeData, OneFruitOnTreeMask> {
		public final String _garden = "garden";
		public final String _tree = "tree";
		public final String _f = "f";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(4);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<OneFruitOnTreeData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new OneFruitOnTreeData(d));
		}
		
		@Override
		public OneFruitOnTreeMask mask() {
			return new OneFruitOnTreeMask();
		}
	}
	
	public class OneFruitOnTreeData extends NeoData {
		public OneFruitOnTreeData(Record data) {
			
		}
	}
	
	public class OneFruitOnTreeMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/FruitGardenLanguage/bin/org/eneo/fruitgarden/PatternsAndRules.msl#//@entities.5
	public MoveCrowForwardAccess getRule_MoveCrowForward() {
		return new MoveCrowForwardAccess();
	}
	
	public class MoveCrowForwardAccess extends NeoRuleCoAccess<MoveCrowForwardData, MoveCrowForwardCoData, MoveCrowForwardMask> {
		public final String _crow = "crow";
		public final String _seg = "seg";
		public final String _next = "next";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(5);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<MoveCrowForwardData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new MoveCrowForwardData(d));
		}
			
		@Override
		public Stream<MoveCrowForwardCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new MoveCrowForwardCoData(d));
		}
		
		@Override
		public MoveCrowForwardMask mask() {
			return new MoveCrowForwardMask();
		}
	}
	
	public class MoveCrowForwardData extends NeoData {
		public MoveCrowForwardData(Record data) {
		
		}
	}
	
	public class MoveCrowForwardCoData extends NeoData {
		public MoveCrowForwardCoData(Record data) {
		
		}
	}
	
	public class MoveCrowForwardMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/FruitGardenLanguage/bin/org/eneo/fruitgarden/PatternsAndRules.msl#//@entities.6
	
	//:~> platform:/resource/ClassInhHier2DB/FruitGardenLanguage/bin/org/eneo/fruitgarden/PatternsAndRules.msl#//@entities.7
	public PickAnAppleAccess getRule_PickAnApple() {
		return new PickAnAppleAccess();
	}
	
	public class PickAnAppleAccess extends NeoRuleCoAccess<PickAnAppleData, PickAnAppleCoData, PickAnAppleMask> {
		public final String _fruitToPick = "fruitToPick";
		public final String _basket = "basket";
		public final String _tree = "tree";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(7);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<PickAnAppleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new PickAnAppleData(d));
		}
			
		@Override
		public Stream<PickAnAppleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new PickAnAppleCoData(d));
		}
		
		@Override
		public PickAnAppleMask mask() {
			return new PickAnAppleMask();
		}
	}
	
	public class PickAnAppleData extends NeoData {
		public PickAnAppleData(Record data) {
		
		}
	}
	
	public class PickAnAppleCoData extends NeoData {
		public PickAnAppleCoData(Record data) {
		
		}
	}
	
	public class PickAnAppleMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/FruitGardenLanguage/bin/org/eneo/fruitgarden/PatternsAndRules.msl#//@entities.8
	public PickAPlumAccess getRule_PickAPlum() {
		return new PickAPlumAccess();
	}
	
	public class PickAPlumAccess extends NeoRuleCoAccess<PickAPlumData, PickAPlumCoData, PickAPlumMask> {
		public final String _fruitToPick = "fruitToPick";
		public final String _basket = "basket";
		public final String _tree = "tree";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(8);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<PickAPlumData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new PickAPlumData(d));
		}
			
		@Override
		public Stream<PickAPlumCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new PickAPlumCoData(d));
		}
		
		@Override
		public PickAPlumMask mask() {
			return new PickAPlumMask();
		}
	}
	
	public class PickAPlumData extends NeoData {
		public PickAPlumData(Record data) {
		
		}
	}
	
	public class PickAPlumCoData extends NeoData {
		public PickAPlumCoData(Record data) {
		
		}
	}
	
	public class PickAPlumMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/FruitGardenLanguage/bin/org/eneo/fruitgarden/PatternsAndRules.msl#//@entities.9
	public PickALemonAccess getRule_PickALemon() {
		return new PickALemonAccess();
	}
	
	public class PickALemonAccess extends NeoRuleCoAccess<PickALemonData, PickALemonCoData, PickALemonMask> {
		public final String _fruitToPick = "fruitToPick";
		public final String _basket = "basket";
		public final String _tree = "tree";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(9);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<PickALemonData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new PickALemonData(d));
		}
			
		@Override
		public Stream<PickALemonCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new PickALemonCoData(d));
		}
		
		@Override
		public PickALemonMask mask() {
			return new PickALemonMask();
		}
	}
	
	public class PickALemonData extends NeoData {
		public PickALemonData(Record data) {
		
		}
	}
	
	public class PickALemonCoData extends NeoData {
		public PickALemonCoData(Record data) {
		
		}
	}
	
	public class PickALemonMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/FruitGardenLanguage/bin/org/eneo/fruitgarden/PatternsAndRules.msl#//@entities.10
	public PickALemonBackAccess getRule_PickALemonBack() {
		return new PickALemonBackAccess();
	}
	
	public class PickALemonBackAccess extends NeoRuleCoAccess<PickALemonBackData, PickALemonBackCoData, PickALemonBackMask> {
		public final String _fruitToPick = "fruitToPick";
		public final String _tree = "tree";
		public final String _basket = "basket";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(10);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<PickALemonBackData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new PickALemonBackData(d));
		}
			
		@Override
		public Stream<PickALemonBackCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new PickALemonBackCoData(d));
		}
		
		@Override
		public PickALemonBackMask mask() {
			return new PickALemonBackMask();
		}
	}
	
	public class PickALemonBackData extends NeoData {
		public PickALemonBackData(Record data) {
		
		}
	}
	
	public class PickALemonBackCoData extends NeoData {
		public PickALemonBackCoData(Record data) {
		
		}
	}
	
	public class PickALemonBackMask extends NeoMask {
	}
	
	//:~> platform:/resource/ClassInhHier2DB/FruitGardenLanguage/bin/org/eneo/fruitgarden/PatternsAndRules.msl#//@entities.11
	public PickAPearAccess getRule_PickAPear() {
		return new PickAPearAccess();
	}
	
	public class PickAPearAccess extends NeoRuleCoAccess<PickAPearData, PickAPearCoData, PickAPearMask> {
		public final String _fruitToPick = "fruitToPick";
		public final String _basket = "basket";
		public final String _tree = "tree";
		
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(11);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<PickAPearData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new PickAPearData(d));
		}
			
		@Override
		public Stream<PickAPearCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new PickAPearCoData(d));
		}
		
		@Override
		public PickAPearMask mask() {
			return new PickAPearMask();
		}
	}
	
	public class PickAPearData extends NeoData {
		public PickAPearData(Record data) {
		
		}
	}
	
	public class PickAPearCoData extends NeoData {
		public PickAPearCoData(Record data) {
		
		}
	}
	
	public class PickAPearMask extends NeoMask {
	}
}
