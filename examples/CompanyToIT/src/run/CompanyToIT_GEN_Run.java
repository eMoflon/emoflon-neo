package run;

import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_GEN;
import org.emoflon.neo.api.metamodels.API_Company;
import org.emoflon.neo.api.metamodels.API_IT;
import org.emoflon.neo.engine.generator.Generator;
import org.emoflon.neo.engine.modules.NeoProgressMonitor;
import org.emoflon.neo.engine.modules.ParanoidNeoReprocessor;
import org.emoflon.neo.engine.modules.SimpleNeoRuleScheduler;
import org.emoflon.neo.engine.modules.SimpleNeoUpdatePolicy;
import org.emoflon.neo.engine.modules.TimedTerminationCondition;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class CompanyToIT_GEN_Run {
	public static void main(String[] pArgs) throws Exception {

		Generator<NeoMatch, NeoCoMatch> generator = new Generator<NeoMatch, NeoCoMatch>(//
				new TimedTerminationCondition(10000), //
				new SimpleNeoRuleScheduler(), //
				new SimpleNeoUpdatePolicy(), //
				new ParanoidNeoReprocessor(), //
				new NeoProgressMonitor());
		
		var builder = API_Common.createBuilder();

		var genAPI = new API_CompanyToIT_GEN(builder, API_Common.PLATFORM_RESOURCE_URI,
				API_Common.PLATFORM_PLUGIN_URI);
		
		var companyAPI = new API_Company(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
		var itAPI = new API_IT(builder, API_Common.PLATFORM_RESOURCE_URI, API_Common.PLATFORM_PLUGIN_URI);
		
		// I think all metamodels have to be exported first (unless they are already in the DB) :(
		// This will also be the case for input models for other ops (or GEN for an existing triple)
		builder.exportEMSLEntityToNeo4j(companyAPI.getMetamodel_Company());
		builder.exportEMSLEntityToNeo4j(itAPI.getMetamodel_IT());

		generator.generate(genAPI.getAllRules());
		
		// Program doesn't terminate without this :)
		builder.close();
	}
}
