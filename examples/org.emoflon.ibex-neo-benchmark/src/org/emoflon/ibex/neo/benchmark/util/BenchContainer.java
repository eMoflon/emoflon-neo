package org.emoflon.ibex.neo.benchmark.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BenchContainer<BP extends BenchParameters> {

	Map<BP, List<BenchEntry<BP>>> params2entries = new HashMap<>();

	public void addBench(BenchEntry<BP> entry) {
		params2entries.computeIfAbsent(entry.parameters, key -> new LinkedList<>()).add(entry);
	}

	public void print() {
		BP bp = null;
		for (BP p : params2entries.keySet()) {
			bp = p;
			break;
		}

		if(bp == null) {
			System.out.println("No measurements found! Aborting...");
			return;
		}
		
		System.out.println();
		System.out.println(String.join(";", bp.getInputParameterNames())
				+ ";elts;avg_init;median_init;avg_resolve;median_resolve;avg_ram;median_ram;success_rate");
		for (BP params : params2entries.keySet()) {
			System.out.println(average(params, params2entries.get(params)));
		}
	}

	private String average(BP params, List<BenchEntry<BP>> entries) {
		double avg_init = 0;
		double avg_resolve = 0;
		double avg_ram = 0;
		double avg_successRate = 0;
		int elts = -1;

		List<Double> inits = new LinkedList<>();
		List<Double> resolves = new LinkedList<>();
		List<Integer> rams = new LinkedList<>();

		for (BenchEntry<BP> entry : entries) {
			elts = entry.elts;
			avg_init += entry.init;
			avg_resolve += entry.resolve;
			avg_ram += entry.ram;
			avg_successRate += entry.successRate;

			inits.add(entry.init);
			resolves.add(entry.resolve);
			rams.add(entry.ram);
		}

		Collections.sort(inits);
		Collections.sort(resolves);
		Collections.sort(rams);

		avg_init /= entries.size();
		avg_resolve /= entries.size();
		avg_ram /= entries.size();
		avg_successRate /= entries.size();
		double med_init = inits.get((int) (inits.size() / 2));
		double med_resolve = resolves.get((int) (resolves.size() / 2));
		int med_ram = rams.get((int) (rams.size() / 2));

		return String.join(";", params.serializeInputParameters()) + ";" + elts + ";" //
				+ avg_init + ";" + med_init + ";" //
				+ avg_resolve + ";" + med_resolve + ";" //
				+ avg_ram + ";" + med_ram + ";" //
				+ avg_successRate;
	}
}
