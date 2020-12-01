package org.emoflon.neo.cypher.constraints;

import java.util.Collection;

import org.emoflon.neo.cypher.factories.NeoPatternFactory;
import org.emoflon.neo.cypher.models.IBuilder;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.cypher.patterns.NeoPattern;
import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.emsl.eMSL.Implication;
import org.emoflon.neo.engine.api.constraints.IPositiveConstraint;

public class NeoPositiveConstraint extends NeoConstraint implements IPositiveConstraint<NeoMatch> {
	
	private NeoPattern premisePattern;
	private NeoPattern conclusionPattern;
	
	public NeoPositiveConstraint(Constraint constraint, IBuilder builder) {
		super(constraint, builder);
		var body = (Implication)constraint.getBody();
		
		premisePattern = NeoPatternFactory.createNeoPattern(body.getPremise(), builder);
		conclusionPattern = NeoPatternFactory.createNeoPattern(body.getConclusion(), builder);
	}
	
	
	@Override
	public Collection<NeoMatch> getPremise() {
		return premisePattern.determineMatches();
		
	}

	@Override
	public Collection<NeoMatch> getConclusion() {
		return conclusionPattern.determineMatches();
	}
	
}
