package org.emoflon.ibex.neo.benchmark.util;

public class BenchEntry {

	public int n;
	public int c;
	public int elts;
	public double init;
	public double resolve;

	public BenchEntry(int n, int c, int elts, double init, double resolve) {
		this.n = n;
		this.c = c;
		this.elts = elts;
		this.init = init;
		this.resolve = resolve;
	}

	@Override
	public String toString() {
		return n + ";" + c + ";" + elts + ";" + init + ";" + resolve;
	}

}
