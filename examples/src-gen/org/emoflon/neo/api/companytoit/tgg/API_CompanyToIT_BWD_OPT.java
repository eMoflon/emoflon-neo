/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.companytoit.tgg;

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
import org.emoflon.neo.api.companytoit.API_Common;
import java.util.Collection;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;
import java.util.Optional;
import java.time.LocalDate;

@SuppressWarnings("unused")
public class API_CompanyToIT_BWD_OPT {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_CompanyToIT_BWD_OPT(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_CompanyToIT_BWD_OPT(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		this((EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/CompanyToIT/tgg-gen/tgg/CompanyToIT_BWD_OPT.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI), builder);
	}

	public API_CompanyToIT_BWD_OPT(EMSL_Spec spec, NeoCoreBuilder builder) {
		this.spec = spec;
		this.builder = builder;
	}

	//:~> platform:/resource/CompanyToIT/tgg-gen/tgg/CompanyToIT_BWD_OPT.msl#//@entities.0
	public Collection<NeoRule> getAllRulesForCompanyToIT_BWD_OPT() {
		Collection<NeoRule> rules = new HashSet<>();
		
		rules.add(getRule_CompanyToITRule().rule());
		rules.add(getRule_AdminToRouterRule().rule());
		rules.add(getRule_EmployeeToPCRule().rule());
		rules.add(getRule_EmployeeToLaptopRule().rule());
		return rules;
	}
	
	public Collection<NeoConstraint> getAllConstraintsForCompanyToIT_BWD_OPT() {
		Collection<NeoConstraint> constraints = new HashSet<>();
		return constraints;
	}
	
	public Collection<Rule> getAllEMSLRulesForCompanyToIT_BWD_OPT(){
		var rules = new HashSet<Rule>();
		rules.add((Rule) spec.getEntities().get(1));
		rules.add((Rule) spec.getEntities().get(2));
		rules.add((Rule) spec.getEntities().get(3));
		rules.add((Rule) spec.getEntities().get(4));
		return rules;
	}
	
	//:~> platform:/resource/CompanyToIT/tgg-gen/tgg/CompanyToIT_BWD_OPT.msl#//@entities.1
	public CompanyToITRuleAccess getRule_CompanyToITRule() {
		return new CompanyToITRuleAccess();
	}
	
	public class CompanyToITRuleAccess extends NeoRuleCoAccess<CompanyToITRuleData, CompanyToITRuleCoData, CompanyToITRuleMask> {
		public final String _ceo = "ceo";
		public final String _company = "company";
		public final String _it = "it";
		
		public final String _param____srcModelName = "__srcModelName";
		public final String _param__ceoName = "ceoName";
		public final String _param____trgModelName = "__trgModelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(1);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<CompanyToITRuleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CompanyToITRuleData(d));
		}
			
		@Override
		public Stream<CompanyToITRuleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CompanyToITRuleCoData(d));
		}
		
		@Override
		public CompanyToITRuleMask mask() {
			return new CompanyToITRuleMask();
		}
	}
	
	public class CompanyToITRuleData extends NeoData {
		public CompanyToITRuleData(Record data) {
		
		}
	}
	
	public class CompanyToITRuleCoData extends NeoData {
		public CompanyToITRuleCoData(Record data) {
		
		}
	}
	
	public class CompanyToITRuleMask extends NeoMask {
	}
	
	//:~> platform:/resource/CompanyToIT/tgg-gen/tgg/CompanyToIT_BWD_OPT.msl#//@entities.2
	public AdminToRouterRuleAccess getRule_AdminToRouterRule() {
		return new AdminToRouterRuleAccess();
	}
	
	public class AdminToRouterRuleAccess extends NeoRuleCoAccess<AdminToRouterRuleData, AdminToRouterRuleCoData, AdminToRouterRuleMask> {
		public final String _company = "company";
		public final String _ceo = "ceo";
		public final String _admin = "admin";
		public final String _router = "router";
		public final String _it = "it";
		public final String _network = "network";
		
		public final String _param____srcModelName = "__srcModelName";
		public final String _param____trgModelName = "__trgModelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(2);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<AdminToRouterRuleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new AdminToRouterRuleData(d));
		}
			
		@Override
		public Stream<AdminToRouterRuleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new AdminToRouterRuleCoData(d));
		}
		
		@Override
		public AdminToRouterRuleMask mask() {
			return new AdminToRouterRuleMask();
		}
	}
	
	public class AdminToRouterRuleData extends NeoData {
		public AdminToRouterRuleData(Record data) {
		
		}
	}
	
	public class AdminToRouterRuleCoData extends NeoData {
		public AdminToRouterRuleCoData(Record data) {
		
		}
	}
	
	public class AdminToRouterRuleMask extends NeoMask {
	}
	
	//:~> platform:/resource/CompanyToIT/tgg-gen/tgg/CompanyToIT_BWD_OPT.msl#//@entities.3
	public EmployeeToPCRuleAccess getRule_EmployeeToPCRule() {
		return new EmployeeToPCRuleAccess();
	}
	
	public class EmployeeToPCRuleAccess extends NeoRuleCoAccess<EmployeeToPCRuleData, EmployeeToPCRuleCoData, EmployeeToPCRuleMask> {
		public final String _admin = "admin";
		public final String _company = "company";
		public final String _ceo = "ceo";
		public final String _employee = "employee";
		public final String _router = "router";
		public final String _computer = "computer";
		public final String _it = "it";
		public final String _network = "network";
		
		public final String _param____srcModelName = "__srcModelName";
		public final String _param__employeeName = "employeeName";
		public final String _param____trgModelName = "__trgModelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(3);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<EmployeeToPCRuleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new EmployeeToPCRuleData(d));
		}
			
		@Override
		public Stream<EmployeeToPCRuleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new EmployeeToPCRuleCoData(d));
		}
		
		@Override
		public EmployeeToPCRuleMask mask() {
			return new EmployeeToPCRuleMask();
		}
	}
	
	public class EmployeeToPCRuleData extends NeoData {
		public EmployeeToPCRuleData(Record data) {
		
		}
	}
	
	public class EmployeeToPCRuleCoData extends NeoData {
		public EmployeeToPCRuleCoData(Record data) {
		
		}
	}
	
	public class EmployeeToPCRuleMask extends NeoMask {
	}
	
	//:~> platform:/resource/CompanyToIT/tgg-gen/tgg/CompanyToIT_BWD_OPT.msl#//@entities.4
	public EmployeeToLaptopRuleAccess getRule_EmployeeToLaptopRule() {
		return new EmployeeToLaptopRuleAccess();
	}
	
	public class EmployeeToLaptopRuleAccess extends NeoRuleCoAccess<EmployeeToLaptopRuleData, EmployeeToLaptopRuleCoData, EmployeeToLaptopRuleMask> {
		public final String _admin = "admin";
		public final String _company = "company";
		public final String _ceo = "ceo";
		public final String _employee = "employee";
		public final String _router = "router";
		public final String _computer = "computer";
		public final String _it = "it";
		public final String _network = "network";
		
		public final String _param____srcModelName = "__srcModelName";
		public final String _param__employeeName = "employeeName";
		public final String _param____trgModelName = "__trgModelName";
		
		@Override
		public NeoRule rule(){
			var r = (Rule) spec.getEntities().get(4);
			return NeoRuleFactory.createNeoRule(r, builder);
		}
		
		@Override
		public Stream<EmployeeToLaptopRuleData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new EmployeeToLaptopRuleData(d));
		}
			
		@Override
		public Stream<EmployeeToLaptopRuleCoData> codata(Collection<NeoCoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new EmployeeToLaptopRuleCoData(d));
		}
		
		@Override
		public EmployeeToLaptopRuleMask mask() {
			return new EmployeeToLaptopRuleMask();
		}
	}
	
	public class EmployeeToLaptopRuleData extends NeoData {
		public EmployeeToLaptopRuleData(Record data) {
		
		}
	}
	
	public class EmployeeToLaptopRuleCoData extends NeoData {
		public EmployeeToLaptopRuleCoData(Record data) {
		
		}
	}
	
	public class EmployeeToLaptopRuleMask extends NeoMask {
	}
}
