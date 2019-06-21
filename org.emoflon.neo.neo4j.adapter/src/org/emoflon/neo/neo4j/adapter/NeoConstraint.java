package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.emsl.eMSL.Implication;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.emsl.eMSL.PositiveConstraint;
import org.emoflon.neo.engine.api.constraints.IConstraint;

/**
 * TODO[Jannik] Implement constraints
 * 
 */
public class NeoConstraint implements IConstraint {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);

	private NeoCoreBuilder builder;

	private Constraint c;
	private Collection<NeoPositiveConstraint> positiveConstraint;
	private Collection<NeoNegativeConstraint> negativeConstraint;
	private Collection<NeoImplication> implicationConstraint;
	List<EObject> ref;
		
	public NeoConstraint(Constraint c, NeoCoreBuilder builder) {
		this.positiveConstraint = new ArrayList<NeoPositiveConstraint>();
		this.negativeConstraint = new ArrayList<NeoNegativeConstraint>();
		this.implicationConstraint = new ArrayList<NeoImplication>();
		this.builder = builder;
		this.c = c;
		
		if(c.getBody() instanceof PositiveConstraint) {			
			var ap = (AtomicPattern) c.getBody().eCrossReferences().get(0);
			positiveConstraint.add(new NeoPositiveConstraint(ap,builder));
			
		} else if(c.getBody() instanceof NegativeConstraint) {
			var ap = (AtomicPattern) c.getBody().eCrossReferences().get(0);
			negativeConstraint.add(new NeoNegativeConstraint(ap,builder));
			
		} else if(c.getBody() instanceof Implication) {
			var apIf = (AtomicPattern) c.getBody().eCrossReferences().get(0);
			var apThen = (AtomicPattern) c.getBody().eCrossReferences().get(1);
			implicationConstraint.add(new NeoImplication(apIf,apThen,builder));
			
		} else {
			throw new UnsupportedOperationException(c.getBody().toString());
		}
		
		logger.info(c.getBody().toString());

	}

	@Override
	public boolean isSatisfied() {
		
		for(NeoPositiveConstraint ap : positiveConstraint) {
			if(!ap.isSatisfied())
				return false;
		}
		for(NeoNegativeConstraint ap : negativeConstraint) {
			if(!ap.isSatisfied())
				return false;
		}
		for(NeoImplication ap : implicationConstraint) {
			if(!ap.isSatisfied())
				return false;
		}
		return true;
	}

}
