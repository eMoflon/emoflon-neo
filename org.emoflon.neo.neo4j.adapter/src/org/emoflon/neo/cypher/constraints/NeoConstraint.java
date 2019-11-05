package org.emoflon.neo.cypher.constraints;

import java.util.List;

import org.apache.log4j.Logger;
import org.emoflon.neo.cypher.common.NeoDatabaseException;
import org.emoflon.neo.cypher.models.IBuilder;
import org.emoflon.neo.cypher.patterns.NeoPattern;
import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.engine.api.constraints.IConstraint;

public class NeoConstraint extends NeoPattern implements IConstraint {
	private static final Logger logger = Logger.getLogger(NeoConstraint.class);

	public NeoConstraint(Constraint constraint, IBuilder builder) {
		super(constraint.getName(), List.of(), builder, constraint.getBody(), false);
	}

	public String getQuery() {
		return CypherConstraintQueryGenerator.query(this).toString();
	}

	@Override
	public boolean isSatisfied() {
		var cypherQuery = getQuery();
		var result = builder.executeQuery(cypherQuery);
		logger.debug("\n" + cypherQuery);

		if (result == null)
			throw new NeoDatabaseException();

		return result.hasNext();
	}
}
