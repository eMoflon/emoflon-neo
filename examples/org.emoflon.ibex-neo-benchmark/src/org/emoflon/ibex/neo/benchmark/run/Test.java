package org.emoflon.ibex.neo.benchmark.run;

import org.emoflon.ibex.neo.benchmark.exttype2doc.concsync.ExtType2Doc_ConcSync_Bench;
import org.emoflon.ibex.neo.benchmark.exttype2doc.concsync.ExtType2Doc_ConcSync_Params;
import org.emoflon.ibex.neo.benchmark.util.BenchEntry;
import org.emoflon.ibex.neo.benchmark.util.ScaleOrientation;

public class Test {

	public static void main(String[] args) {
		ExtType2Doc_ConcSync_Bench bench = new ExtType2Doc_ConcSync_Bench("org.emoflon.ibex-neo-benchmark");

		ExtType2Doc_ConcSync_Params params = new ExtType2Doc_ConcSync_Params( //
				"presDel-scaled", // name
				16, // model size
				ScaleOrientation.HORIZONTAL, // scale orientation
				16, // number of model changes
				1.0 // conflict ratio
		);

//		BenchEntry result = bench.genAndBench(params, true);
//		System.out.println(result);
		boolean gen = true;
		if (gen) {
			bench.genAndStore(params);
		} else {
			BenchEntry result2 = bench.loadAndBench(params, true);
			System.out.println(result2);
		}
	}

}
