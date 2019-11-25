package org.emoflon.neo.victory.adapter.common;

import java.util.List;

import org.emoflon.neo.cypher.models.NeoCoreBuilder;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;
import org.emoflon.victory.ui.api.enums.Action;

public class NeoVictoryUtil {

	public static Action computeAction(org.emoflon.neo.emsl.eMSL.Action action,
			List<ModelPropertyStatement> properties) {
		// No action means (to be translated) context
		if (action == null) {
			if (properties.stream()//
					.anyMatch(p -> p.getType().getName().equals(NeoCoreBuilder.TRANSLATION_MARKER)))
				return Action.TRANSLATE;
			else
				return Action.CONTEXT;
		}

		return Action.CREATE;
	}

}
