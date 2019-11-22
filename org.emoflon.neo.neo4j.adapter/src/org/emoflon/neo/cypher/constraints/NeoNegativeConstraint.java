package org.emoflon.neo.cypher.constraints;

import java.util.Collection;

import org.emoflon.neo.cypher.factories.NeoPatternFactory;
import org.emoflon.neo.cypher.models.IBuilder;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.patterns.NeoPattern;
import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.engine.api.constraints.INegativeConstraint;

public class NeoNegativeConstraint extends NeoConstraint implements INegativeConstraint<NeoMatch> {

	private NeoPattern negativePattern;
	
	public NeoNegativeConstraint(Constraint constraint, IBuilder builder) {
		super(constraint, builder);
		var body = (NegativeConstraint)constraint.getBody();
		negativePattern = NeoPatternFactory.createNeoPattern(body.getPattern(), builder);
	}

	@Override
	public Collection<NeoMatch> getViolations() {
		return negativePattern.determineMatches();
	}
}
