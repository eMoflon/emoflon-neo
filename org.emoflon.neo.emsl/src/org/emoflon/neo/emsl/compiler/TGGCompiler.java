package org.emoflon.neo.emsl.compiler;

import org.emoflon.neo.emsl.EMSLFlattener;
import org.emoflon.neo.emsl.eMSL.TripleGrammar;

public class TGGCompiler {

	private EMSLFlattener flattener;

	public TGGCompiler() {
		flattener = new EMSLFlattener();
	}

	public void compile(TripleGrammar pTGG) {
		// TODO convert TGG to GT rules, generate msl files directly
	}
}
