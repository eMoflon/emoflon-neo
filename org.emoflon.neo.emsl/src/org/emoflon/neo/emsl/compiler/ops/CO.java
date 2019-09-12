package org.emoflon.neo.emsl.compiler.ops;

import org.emoflon.neo.emsl.compiler.Operation;
import org.emoflon.neo.emsl.eMSL.Action;

public class CO implements Operation {

	@Override
	public String getNameExtension() {
		return "_CO";
	}

	@Override
	public String getAction(Action pAction, boolean pIsSrc) {
		return "";
	}

	@Override
	public String getTranslation(Action pAction, boolean pIsSrc) {
		return "";
	}

	@Override
	public String getCorrAction(Action pAction) {
		return "";
	}

}
