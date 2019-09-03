package org.emoflon.neo.neo4j.adapter.constraints;

import org.emoflon.neo.emsl.eMSL.AndBody;
import org.emoflon.neo.neo4j.adapter.models.IBuilder;
import org.emoflon.neo.neo4j.adapter.patterns.NeoMask;
import org.emoflon.neo.neo4j.adapter.util.NeoQueryData;

/**
 * Class for creating nested AndBodies used in NeoConstraints or NeoConditions
 * for proofing directing query fragments of constraints or conditions, and
 * calculating if they satisfy, getting the matches
 * 
 * @author Jannik Hinz
 *
 */
public class NeoAndBody extends NeoCombinator {
	
	/**
	 * @param body      of the current AndBody
	 * @param builder   for creating and running Cypher queries
	 * @param queryData for creating nodes and relation with a unique name and
	 *                  central node storage
	 */
	public NeoAndBody(AndBody body, IBuilder builder, NeoQueryData queryData, NeoMask mask, boolean injective) {
		super("AND", body.getChildren(), builder, queryData, mask, injective);
	}

	@Override
	public String getName() {
		return "AND(...)";
	}

}