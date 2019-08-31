package org.emoflon.neo.emsl.compiler;

import org.emoflon.neo.emsl.compiler.ops.BWD;
import org.emoflon.neo.emsl.compiler.ops.CC;
import org.emoflon.neo.emsl.compiler.ops.CO;
import org.emoflon.neo.emsl.compiler.ops.FWD;
import org.emoflon.neo.emsl.compiler.ops.MODELGEN;
import org.emoflon.neo.emsl.eMSL.Action;

public interface Operation {

	public static Operation[] getAllOps() {
		return new Operation[] { new MODELGEN(), new FWD(), new BWD(), new CO(), new CC() };
	}

	public String getNameExtension();

	public String getAction(Action pAction, boolean pIsSrc);

	public String getTranslation(Action pAction, boolean pIsSrc);

	public String getCorrAction(Action pAction);
}
