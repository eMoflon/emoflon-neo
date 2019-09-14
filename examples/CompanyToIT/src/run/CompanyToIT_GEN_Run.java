package run;

import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_CompanyToIT;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_GEN;
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
		var builder = API_Common.createBuilder();

		var api = new API_CompanyToIT(builder);
		api.exportMetamodelsForCompanyToIT();
		
		Generator<NeoMatch, NeoCoMatch> generator = new Generator<NeoMatch, NeoCoMatch>(//
				new TimedTerminationCondition(10000), //
				new SimpleNeoRuleScheduler(), //
				new SimpleNeoUpdatePolicy(), //
				new ParanoidNeoReprocessor(), //
				new NeoProgressMonitor());

		var genAPI = new API_CompanyToIT_GEN(builder);
				
		generator.generate(genAPI.getAllRules());
		
		builder.close();
	}
}
