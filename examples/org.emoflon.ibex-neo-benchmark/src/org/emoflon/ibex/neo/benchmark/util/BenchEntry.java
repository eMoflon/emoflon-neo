package org.emoflon.ibex.neo.benchmark.util;

import java.lang.reflect.Constructor;

public class BenchEntry<BP extends BenchParameters> {

	public final BP parameters;
	public final int elts;
	public final double init;
	public final double resolve;
	public final int ram;
	public final double successRate;

	public BenchEntry(BP parameters, int elts, double init, double resolve, int ram, double successRate) {
		this.parameters = parameters;
		this.elts = elts;
		this.init = init;
		this.resolve = resolve;
		this.ram = ram;
		this.successRate = successRate;
	}

	public BenchEntry(String args, Class<BP> clazz) throws Exception {
		String[] splitted = args.strip().split(";");
		// remove all irrelevant lines before first parameter:
		String[] firstParam = splitted[0].split("\n");
		splitted[0] = firstParam[firstParam.length - 1];

		Constructor<BP> constructor = clazz.getConstructor(String[].class);

		this.parameters = constructor.newInstance(new Object[] { splitted });
		this.elts = Integer.parseInt(splitted[splitted.length - 5]);
		this.init = Double.parseDouble(splitted[splitted.length - 4]);
		this.resolve = Double.parseDouble(splitted[splitted.length - 3]);
		this.ram = Integer.parseInt(splitted[splitted.length - 2]);
		this.successRate = Double.parseDouble(splitted[splitted.length - 1]);
	}

	@Override
	public String toString() {
		return String.join(";", parameters.serializeInputParameters()) + ";" + elts + ";" + init + ";" + resolve + ";" + ram + ";" + successRate;
	}

}
