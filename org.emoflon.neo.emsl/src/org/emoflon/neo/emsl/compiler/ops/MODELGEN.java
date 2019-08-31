package org.emoflon.neo.emsl.compiler.ops;

import org.emoflon.neo.emsl.compiler.Operation;
import org.emoflon.neo.emsl.eMSL.Action;
import org.emoflon.neo.emsl.eMSL.ActionOperator;

public class MODELGEN implements Operation {

	@Override
	public String getNameExtension() {
		return "_GEN.msl";
	}

	@Override
	public String getAction(Action pAction, boolean pIsSrc) {
		if (pAction == null || !ActionOperator.CREATE.equals(pAction.getOp()))
			return "";
		else
			return "++";
	}

	@Override
	public String getTranslation(Action pAction, boolean pIsSrc) {
		return "";
	}

	@Override
	public String getCorrAction(Action pAction) {
		if (pAction == null || !ActionOperator.CREATE.equals(pAction.getOp()))
			return "";
		else
			return "++";
	}
}
