package org.emoflon.neo.emsl.compiler.ops;

import org.emoflon.neo.emsl.compiler.Operation;
import org.emoflon.neo.emsl.eMSL.Action;
import org.emoflon.neo.emsl.eMSL.ActionOperator;

public class CC implements Operation {

	@Override
	public String getNameExtension() {
		return "_CC.msl";
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
		if (pAction == null || !ActionOperator.CREATE.equals(pAction.getOp()))
			return "";
		else
			return "++";
	}

}
