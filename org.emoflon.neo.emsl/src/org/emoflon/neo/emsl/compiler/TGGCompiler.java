package org.emoflon.neo.emsl.compiler;

import org.emoflon.neo.emsl.eMSL.TripleGrammar;
import org.emoflon.neo.emsl.eMSL.TripleRule;

public class TGGCompiler {
	public static void compileTGGs(Iterable<TripleGrammar> pTGGs) {
		System.out.println("####################### compile TGGs: " + pTGGs);
	}

	public static void compileTGGRules(Iterable<TripleRule> pTGGs) {
		System.out.println("####################### compile TGG rules: " + pTGGs);
	}
}
