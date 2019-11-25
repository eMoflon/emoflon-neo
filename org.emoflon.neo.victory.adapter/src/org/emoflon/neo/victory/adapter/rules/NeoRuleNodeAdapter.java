package org.emoflon.neo.victory.adapter.rules;

import java.util.List;
import java.util.stream.Collectors;

import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.util.EMSLUtil;
import org.emoflon.neo.victory.adapter.common.NeoNodeAdapter;
import org.emoflon.victory.ui.api.enums.Action;
import org.emoflon.victory.ui.api.enums.Domain;

/**
 * Represents an edge in a rule so it can be visualised by Victory.
 * 
 * @author anthonyanjorin
 */
public class NeoRuleNodeAdapter extends NeoNodeAdapter {
	public NeoRuleNodeAdapter(ModelNodeBlock node, Domain domain, Action action) {
		super(domain, action, computeAttributes(node), node.getType().getName(), node.getName());
	}

	private static List<String> computeAttributes(ModelNodeBlock node) {
		return node.getProperties().stream()//
				.map(p -> p.getType().getName() + " " + p.getOp().getLiteral() + " "
						+ EMSLUtil.handleValueForCypher(p.getValue()))//
				.collect(Collectors.toList());
	}
}
