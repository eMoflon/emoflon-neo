package org.emoflon.ibex.neo.benchmark.exttype2doc.lookahead;

import java.io.IOException;

import org.eclipse.emf.ecore.resource.Resource;
import org.emoflon.ibex.neo.benchmark.IntegrationBench;
import org.emoflon.ibex.neo.benchmark.ModelAndDeltaGenerator;
import org.emoflon.ibex.tgg.operational.strategies.integrate.INTEGRATE;
import org.emoflon.ibex.tgg.operational.strategies.modules.TGGResourceHandler;

public class ExtType2Doc_LookAhead_Bench extends IntegrationBench<ExtType2Doc_LookAhead_Params> {

	public ExtType2Doc_LookAhead_Bench(String projectName) {
		super(projectName);
	}

	@Override
	protected INTEGRATE initStub(TGGResourceHandler resourceHandler) throws IOException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected ModelAndDeltaGenerator<?, ?, ?, ?, ?, ExtType2Doc_LookAhead_Params> initModelAndDeltaGenerator(Resource s, Resource t, Resource c,
			Resource p, Resource d) {
		// TODO Auto-generated method stub
		return null;
	}

}
