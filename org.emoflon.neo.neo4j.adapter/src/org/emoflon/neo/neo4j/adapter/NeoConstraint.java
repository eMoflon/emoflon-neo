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
	private Collection<NeoNode> nodes;
	private Collection<NeoPositiveConstraint> positivePattern;
	private Collection<NeoNegativeConstraint> negativePattern;
	private Collection<NeoImplication> ifThenPattern;
	List<EObject> ref;
		
	public NeoConstraint(Constraint c, NeoCoreBuilder builder) {
		this.nodes = new ArrayList<>();
		this.positivePattern = new ArrayList<NeoPositiveConstraint>();
		this.negativePattern = new ArrayList<NeoNegativeConstraint>();
		this.ifThenPattern = new ArrayList<NeoImplication>();
		this.builder = builder;
		this.c = c;
		
		if(c.getBody() instanceof PositiveConstraint) {
			
			if(c.getBody().eCrossReferences().get(0) instanceof AtomicPattern) {
				var ap = (AtomicPattern) c.getBody().eCrossReferences().get(0);
				positivePattern.add(new NeoPositiveConstraint(ap,builder));
				
			} else {
				throw new UnsupportedOperationException();
			}
			
		} else if(c.getBody() instanceof NegativeConstraint) {
			
			if(c.getBody().eCrossReferences().get(0) instanceof AtomicPattern) {
				var ap = (AtomicPattern) c.getBody().eCrossReferences().get(0);
				negativePattern.add(new NeoNegativeConstraint(ap,builder));
				
			} else {
				throw new UnsupportedOperationException();
			}
			
		}else if(c.getBody() instanceof Implication) {
			
			if(c.getBody().eCrossReferences().get(0) instanceof AtomicPattern && c.getBody().eCrossReferences().get(1) instanceof AtomicPattern) {
				var apIf = (AtomicPattern) c.getBody().eCrossReferences().get(0);
				var apThen = (AtomicPattern) c.getBody().eCrossReferences().get(1);
				ifThenPattern.add(new NeoImplication(apIf,apThen,builder));
				
			} else {
				throw new UnsupportedOperationException();
			}
			
		}
		
		logger.info(c.getBody().toString());

	}

	@Override
	public boolean isSatisfied() {
		
		for(NeoPositiveConstraint ap : positivePattern) {
			
			return ap.isSatisfied();
			
		}
		for(NeoNegativeConstraint ap : negativePattern) {
			
			return ap.isSatisfied();
			
		}
		for(NeoImplication ap : ifThenPattern) {
			
			return ap.isSatisfied();
			
		}
		return false;
	}

}
