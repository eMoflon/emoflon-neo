package org.emoflon.ibex.neo.benchmark.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class BenchContainer {

	Map<Integer, Map<Integer, List<BenchEntry>>> n2c2entries = new HashMap<>();

	public void addBench(BenchEntry entry) {
		if (!n2c2entries.containsKey(entry.n)) {
			n2c2entries.put(entry.n, new HashMap<>());
		}
		Map<Integer, List<BenchEntry>> c2entries = n2c2entries.get(entry.n);
		if (!c2entries.containsKey(entry.c)) {
			c2entries.put(entry.c, new LinkedList<>());
		}
		c2entries.get(entry.c).add(entry);
	}

	public void print() {
		System.out.println();
		System.out.println("n;c;elts;avg_init;median_init;avg_resolve;median_resolve");
		for (Integer n : n2c2entries.keySet()) {
			Map<Integer, List<BenchEntry>> c2entries = n2c2entries.get(n);
			for (Integer c : c2entries.keySet()) {
				System.out.println(average(c2entries.get(c)));
			}
		}
	}

	private String average(List<BenchEntry> entries) {
		double avg_init = 0;
		double avg_resolve = 0;
		int n = -1;
		int c = -1;
		int elts = -1;
		List<Double> inits = new LinkedList<>();
		List<Double> resolves = new LinkedList<>();
		for (BenchEntry entry : entries) {
			n = entry.n;
			c = entry.c;
			elts = entry.elts;
			avg_init += entry.init;
			avg_resolve += entry.resolve;
			inits.add(entry.init);
			resolves.add(entry.resolve);
		}
		Collections.sort(inits);
		Collections.sort(resolves);
		return n + ";" + c + ";" + elts + ";" + avg_init / entries.size() + ";" + inits.get((int) (inits.size() / 2)) + ";"
				+ avg_resolve / entries.size() + ";" + resolves.get((int) (resolves.size() / 2));
	}
}
