package org.emoflon.neo.neo4j.adapter.constraints;

import org.emoflon.neo.cypher.models.IBuilder;
import org.emoflon.neo.emsl.eMSL.OrBody;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;

/**
 * Class for creating nested AndBodies used in NeoConstraints or NeoConditions
 * for proofing if a constraint or condition is satisfied
 * 
 * @author Jannik Hinz
 *
 */
public class NeoOrBody extends NeoCombinator {

	/**
	 * @param body      of the current OrBody
	 * @param builder   for creating and running Cypher queries
	 * @param queryData for creating nodes and relation with a unique name and
	 *                  central node storage
	 */
	public NeoOrBody(OrBody body, IBuilder builder, NeoQueryData queryData, NeoMask mask, boolean injective) {
		super("OR", body.getChildren(), builder, queryData, mask, injective);
	}

	@Override
	public String getName() {
		return "OR(...)";
	}
}
