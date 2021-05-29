/** 
 * EMSL-API generated by eMoflon::Neo - Do not edit as this file will be overwritten
 */
package org.emoflon.neo.api.companytoit;

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
public class API_CompanyToIT {
	private EMSL_Spec spec;
	private NeoCoreBuilder builder;

	/** Use this constructor for default values */
	public API_CompanyToIT(NeoCoreBuilder builder) {
		this(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
	}

	/** Use this constructor to configure values for loading EMSL files */
	public API_CompanyToIT(NeoCoreBuilder builder, String platformResourceURIRoot, String platformPluginURIRoot, String neocoreURI){
		this((EMSL_Spec) EMSLUtil.loadSpecification("platform:/resource/CompanyToIT/src/CompanyToIT.msl", platformResourceURIRoot, platformPluginURIRoot, neocoreURI), builder);
	}

	public API_CompanyToIT(EMSL_Spec spec, NeoCoreBuilder builder) {
		this.spec = spec;
		this.builder = builder;
	}

	//:~> platform:/resource/CompanyToIT/src/CompanyToIT.msl#//@entities.0
	public void exportMetamodelsForCompanyToIT() throws FlattenerException {
		{
			var api = new org.emoflon.neo.api.companytoit.metamodels.API_Company(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
			builder.exportEMSLEntityToNeo4j(api.getMetamodel_Company());
		}
		{
			var api = new org.emoflon.neo.api.companytoit.metamodels.API_IT(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI, API_Common.NEOCORE_URI_INSTALLED);
			builder.exportEMSLEntityToNeo4j(api.getMetamodel_IT());
		}
	}
	
	public Collection<TripleRule> getTripleRulesOfCompanyToIT(){
		var rules = new HashSet<TripleRule>();
		var rs = spec.eResource().getResourceSet();
		{
			var uri = "platform:/resource/CompanyToIT/src/CompanyToIT.msl#//@entities.1";
			rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
		}
		{
			var uri = "platform:/resource/CompanyToIT/src/CompanyToIT.msl#//@entities.3";
			rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
		}
		{
			var uri = "platform:/resource/CompanyToIT/src/CompanyToIT.msl#//@entities.6";
			rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
		}
		{
			var uri = "platform:/resource/CompanyToIT/src/CompanyToIT.msl#//@entities.7";
			rules.add((TripleRule) rs.getEObject(URI.createURI(uri), true));
		}
		return rules;
	}
	
	public Collection<IConstraint> getConstraintsOfCompanyToIT(){
		var constraints = new HashSet<IConstraint>();
		var rs = spec.eResource().getResourceSet();
		return constraints;
	}
	
	//:~> platform:/resource/CompanyToIT/src/CompanyToIT.msl#//@entities.1
	public static final String CompanyToIT__CompanyToITRule = "CompanyToITRule";
	public static final String CompanyToIT__CompanyToITRule__ceo = "ceo";
	public static final String CompanyToIT__CompanyToITRule__company = "company";
	public static final String CompanyToIT__CompanyToITRule__it = "it";
	
	//:~> platform:/resource/CompanyToIT/src/CompanyToIT.msl#//@entities.2
	public CompanyNameIsTakenAccess getPattern_CompanyNameIsTaken() {
		return new CompanyNameIsTakenAccess();
	}
	
	public class CompanyNameIsTakenAccess extends NeoPatternAccess<CompanyNameIsTakenData, CompanyNameIsTakenMask> {
		public final String _company = "company";
		
		public final String _param__companyName = "companyName";
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(2);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<CompanyNameIsTakenData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new CompanyNameIsTakenData(d));
		}
		
		@Override
		public CompanyNameIsTakenMask mask() {
			return new CompanyNameIsTakenMask();
		}
	}
	
	public class CompanyNameIsTakenData extends NeoData {
		public CompanyNameIsTakenData(Record data) {
			
		}
	}
	
	public class CompanyNameIsTakenMask extends NeoMask {
	}
	
	//:~> platform:/resource/CompanyToIT/src/CompanyToIT.msl#//@entities.3
	public static final String CompanyToIT__AdminToRouterRule = "AdminToRouterRule";
	public static final String CompanyToIT__AdminToRouterRule__company = "company";
	public static final String CompanyToIT__AdminToRouterRule__ceo = "ceo";
	public static final String CompanyToIT__AdminToRouterRule__admin = "admin";
	public static final String CompanyToIT__AdminToRouterRule__router = "router";
	public static final String CompanyToIT__AdminToRouterRule__it = "it";
	public static final String CompanyToIT__AdminToRouterRule__network = "network";
	
	//:~> platform:/resource/CompanyToIT/src/CompanyToIT.msl#//@entities.4
	public AlreadyHasAnAdminAccess getPattern_AlreadyHasAnAdmin() {
		return new AlreadyHasAnAdminAccess();
	}
	
	public class AlreadyHasAnAdminAccess extends NeoPatternAccess<AlreadyHasAnAdminData, AlreadyHasAnAdminMask> {
		public final String _company = "company";
		public final String _other = "other";
		public final String _ceo = "ceo";
		
		
		@Override
		public NeoPattern pattern(){
			var p = (Pattern) spec.getEntities().get(4);
			return NeoPatternFactory.createNeoPattern(p, builder);
		}
		
		@Override
		public Stream<AlreadyHasAnAdminData> data(Collection<NeoMatch> matches) {
			var data = NeoMatch.getData(matches);
			return data.stream().map(d -> new AlreadyHasAnAdminData(d));
		}
		
		@Override
		public AlreadyHasAnAdminMask mask() {
			return new AlreadyHasAnAdminMask();
		}
	}
	
	public class AlreadyHasAnAdminData extends NeoData {
		public AlreadyHasAnAdminData(Record data) {
			
		}
	}
	
	public class AlreadyHasAnAdminMask extends NeoMask {
	}
	
	//:~> platform:/resource/CompanyToIT/src/CompanyToIT.msl#//@entities.5
	
	//:~> platform:/resource/CompanyToIT/src/CompanyToIT.msl#//@entities.6
	public static final String CompanyToIT__EmployeeToPCRule = "EmployeeToPCRule";
	public static final String CompanyToIT__EmployeeToPCRule__employee = "employee";
	public static final String CompanyToIT__EmployeeToPCRule__computer = "computer";
	public static final String CompanyToIT__EmployeeToPCRule__network = "network";
	
	//:~> platform:/resource/CompanyToIT/src/CompanyToIT.msl#//@entities.7
	public static final String CompanyToIT__EmployeeToLaptopRule = "EmployeeToLaptopRule";
	public static final String CompanyToIT__EmployeeToLaptopRule__employee = "employee";
	public static final String CompanyToIT__EmployeeToLaptopRule__computer = "computer";
	public static final String CompanyToIT__EmployeeToLaptopRule__network = "network";
}