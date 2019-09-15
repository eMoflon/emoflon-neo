package run;

import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_CompanyToIT;
import org.emoflon.neo.api.CompanyToIT.API_CompanyToIT_CO;
import org.emoflon.neo.engine.generator.Generator;
import org.emoflon.neo.engine.modules.matchreprocessors.ParanoidNeoReprocessor;
import org.emoflon.neo.engine.modules.monitors.SimpleLoggerMonitor;
import org.emoflon.neo.engine.modules.ruleschedulers.SimpleNeoRuleScheduler;
import org.emoflon.neo.engine.modules.terminationcondition.TimedTerminationCondition;
import org.emoflon.neo.engine.modules.updatepolicies.SimpleNeoUpdatePolicy;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMatch;
import org.emoflon.neo.neo4j.adapter.rules.NeoCoMatch;

public class CompanyToIT_CO_Run {
	public static void main(String[] pArgs) throws Exception {
		var builder = API_Common.createBuilder();

		var api = new API_CompanyToIT(builder);
		api.exportMetamodelsForCompanyToIT();
		
		Generator<NeoMatch, NeoCoMatch> generator = new Generator<NeoMatch, NeoCoMatch>(//
				new TimedTerminationCondition(10000), //
				new SimpleNeoRuleScheduler(), //
				new SimpleNeoUpdatePolicy(), //
				new ParanoidNeoReprocessor(), //
				new SimpleLoggerMonitor());

		var coAPI = new API_CompanyToIT_CO(builder);

		generator.generate(coAPI.getAllRules());
		
		builder.close();
	}
}
