package org.emoflon.ibex.neo.benchmark.util;

public abstract class BenchParameters {

	public final String name;
	public final int modelScale;
	public final ScaleOrientation scaleOrientation;
	public final int numOfChanges;
	public final String delta;

	public BenchParameters(String name, int modelScale, ScaleOrientation scaleOrientation, int numOfChanges, String delta) {
		this.name = name;
		this.modelScale = modelScale;
		this.scaleOrientation = scaleOrientation;
		this.numOfChanges = numOfChanges;
		this.delta = delta;
	}

	public abstract String[] serializeInputParameters();
	
	public abstract String[] getInputParameterNames();
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(name);
		builder.append("_n");
		builder.append(modelScale);
		builder.append("_c");
		builder.append(numOfChanges);
		builder.append("_");
		builder.append(scaleOrientation == ScaleOrientation.HORIZONTAL ? "H" : "V");
		return builder.toString();
	}

}
