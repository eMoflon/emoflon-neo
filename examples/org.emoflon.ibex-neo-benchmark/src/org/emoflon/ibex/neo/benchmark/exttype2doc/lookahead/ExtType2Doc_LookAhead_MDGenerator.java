package org.emoflon.ibex.neo.benchmark.exttype2doc.lookahead;

import org.eclipse.emf.ecore.resource.Resource;
import org.emoflon.ibex.neo.benchmark.exttype2doc.ExtType2Doc_MDGenerator;

import ExtType2Doc_LookAhead.ExtType2Doc_LookAheadFactory;

public class ExtType2Doc_LookAhead_MDGenerator extends ExtType2Doc_MDGenerator<ExtType2Doc_LookAheadFactory, ExtType2Doc_LookAhead_Params> {

	public ExtType2Doc_LookAhead_MDGenerator(Resource source, Resource target, Resource corr, Resource protocol, Resource delta) {
		super(source, target, corr, protocol, delta);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected ExtType2Doc_LookAheadFactory corrFactoryInstance() {
		return ExtType2Doc_LookAheadFactory.eINSTANCE;
	}

	@Override
	protected void genModels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void genDelta() {
		// TODO Auto-generated method stub
		
	}

}
