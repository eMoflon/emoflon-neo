package org.emoflon.neo.cypher.common;

import java.util.Map;
import java.util.stream.Collectors;

import org.emoflon.neo.emsl.eMSL.ModelNodeBlock;
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement;
import org.emoflon.neo.emsl.util.EMSLUtil;

public class NeoRelation extends NeoElement {
	private NeoNode srcNode;
	private NeoNode trgNode;

	public NeoRelation(ModelRelationStatement rel, Map<ModelNodeBlock, NeoNode> nodeBlockToNeoNode) {
		super(namingConvention(rel), extractType(rel), rel.getProperties());
		this.srcNode = nodeBlockToNeoNode.get(rel.eContainer());
		this.trgNode = nodeBlockToNeoNode.get(rel.getTarget());
	}

	private static String extractType(ModelRelationStatement rel) {
		var types = rel.getTypes().stream()//
				.map(r -> r.getType().getName())//
				.collect(Collectors.joining("|"));

		var lower = interpretLowerBound(rel.getLower());
		var upper = interpretUpperBound(rel.getUpper());

		return "1".equals(lower) && "1".equals(upper) ? types : types + "*" + lower + ".." + upper;
	}

	public boolean isPath() {
		return type.contains("*");
	}

	@Override
	public String getName() {
		return isPath() ? "" : super.name;
	}

	private static String interpretLowerBound(String lowerBound) {
		return "".equals(lowerBound) || lowerBound == null ? "1" : lowerBound;
	}

	private static String interpretUpperBound(String upperBound) {
		return "*".equals(upperBound) ? "" : interpretLowerBound(upperBound);
	}

	private static String namingConvention(ModelRelationStatement r) {
		var src = (ModelNodeBlock) r.eContainer();
		return EMSLUtil.relationNameConvention(//
				src.getName(), //
				EMSLUtil.getAllTypes(r), //
				r.getTarget().getName(), //
				src.getRelations().indexOf(r));
	}

	public NeoNode getSrcNode() {
		return srcNode;
	}

	public NeoNode getTrgNode() {
		return trgNode;
	}

	public void setName(String name) {
		this.name = name;
	}
}
