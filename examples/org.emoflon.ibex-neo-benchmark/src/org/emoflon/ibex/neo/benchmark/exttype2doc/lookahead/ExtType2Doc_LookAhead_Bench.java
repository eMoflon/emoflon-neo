package org.emoflon.ibex.neo.benchmark.exttype2doc.lookahead;

import org.eclipse.emf.ecore.resource.Resource;
import org.emoflon.ibex.neo.benchmark.IntegrationBench;
import org.emoflon.ibex.neo.benchmark.ModelAndDeltaGenerator;
import org.emoflon.neo.engine.modules.ilp.ILPBasedOperationalStrategy;

public class ExtType2Doc_LookAhead_Bench extends IntegrationBench<ExtType2Doc_LookAhead_Params> {

	public ExtType2Doc_LookAhead_Bench(String projectName, String pathName) {
		super(projectName, pathName);
	}

	@Override
	protected ModelAndDeltaGenerator<?, ?, ?, ?, ?, ExtType2Doc_LookAhead_Params> initModelAndDeltaGenerator(Resource s, Resource t, Resource c,
			Resource p, Resource d) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ILPBasedOperationalStrategy initOpStrat() {
		// TODO Auto-generated method stub
		return null;
	}

}
