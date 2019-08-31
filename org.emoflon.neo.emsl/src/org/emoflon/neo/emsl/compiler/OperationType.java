package org.emoflon.neo.emsl.compiler;

public enum OperationType {
	MODELGEN("_GEN.msl"), //
	FWD("_FWD.msl"), //
	BWD("_BWD.msl"), //
	CC("_CC.msl"), //
	CO("_CO.msl");

	private String opNameExtension;

	private OperationType(String pOpNameExtension) {
		opNameExtension = pOpNameExtension;
	}

	public String getOpNameExtension() {
		return opNameExtension;
	}
}
