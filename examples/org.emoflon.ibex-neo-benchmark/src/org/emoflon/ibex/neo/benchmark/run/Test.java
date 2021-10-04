package org.emoflon.ibex.neo.benchmark.run;

import org.emoflon.ibex.neo.benchmark.exttype2doc.concsync.ExtType2Doc_ConcSync_Bench;
import org.emoflon.ibex.neo.benchmark.exttype2doc.concsync.ExtType2Doc_ConcSync_Params;
import org.emoflon.ibex.neo.benchmark.exttype2doc.shortCut.ExtType2Doc_ShortCut_Bench;
import org.emoflon.ibex.neo.benchmark.exttype2doc.shortCut.ExtType2Doc_ShortCut_Params;
import org.emoflon.ibex.neo.benchmark.util.BenchEntry;
import org.emoflon.ibex.neo.benchmark.util.ScaleOrientation;

public class Test {

	public static void main(String[] args) {
//		ExtType2Doc_ConcSync_Bench bench = new ExtType2Doc_ConcSync_Bench("../ExtType2Doc_ConcSync/emf/", "../ExtType2Doc_ConcSync/emf/");
//
//		ExtType2Doc_ConcSync_Params params = new ExtType2Doc_ConcSync_Params( //
//				"presDel-scaled", // name
//				1, // model size
//				ScaleOrientation.HORIZONTAL, // scale orientation
//				1, // number of model changes
//				1.0, // conflict rati
//				""
//		);
//
//		BenchEntry result = bench.genAndBench(params);
//		System.out.println(result);
		
		args = new String[5];
		args[0] = "scaledModel";
		args[1] = "10";
		args[2] = "HORIZONTAL";
		args[3] = "10";
		args[4] = "CREATE_ROOT";
		
		ExtType2Doc_ShortCut_Bench bench = new ExtType2Doc_ShortCut_Bench("../ExtType2Doc_ShortCut/emf/", "../ExtType2Doc_ShortCut/emf/");

		ExtType2Doc_ShortCut_Params params = new ExtType2Doc_ShortCut_Params( //
				args[0], // name
				Integer.valueOf(args[1]), // model scale
				ScaleOrientation.valueOf(args[2].contains("H") ? "HORIZONTAL" : "VERTICAL"), // scale orientation
				Integer.valueOf(args[3]), // number of changes
				args[4]
		);

		BenchEntry<ExtType2Doc_ShortCut_Params> result = bench.genAndBench(params);
		System.out.println(result);
	}
}
