package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.EObject;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.emsl.eMSL.ConstraintBody;
import org.emoflon.neo.emsl.eMSL.ConstraintReference;
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
	private Collection<NeoConstraint> cChilds;
	private Collection<ConstraintReference> refs;

	public NeoConstraint(Constraint c, NeoCoreBuilder builder) {
		this.refs = new ArrayList<ConstraintReference>();
		this.cChilds = new ArrayList<NeoConstraint>();
		this.builder = builder;
		this.c = c;
		
	}

	@Override
	public boolean isSatisfied() {
		
		if(c.getBody() instanceof PositiveConstraint) {			
			var ap = (AtomicPattern) c.getBody().eCrossReferences().get(0);
			var co = new NeoPositiveConstraint(ap,builder);
			
			if(co.isSatisfied())
				return true;
			else
				return false;
			
		} else if(c.getBody() instanceof NegativeConstraint) {
			var ap = (AtomicPattern) c.getBody().eCrossReferences().get(0);
			var co = new NeoNegativeConstraint(ap,builder);
			
			if(co.isSatisfied())
				return true;
			else
				return false;
			
		} else if(c.getBody() instanceof Implication) {
			var apIf = (AtomicPattern) c.getBody().eCrossReferences().get(0);
			var apThen = (AtomicPattern) c.getBody().eCrossReferences().get(1);
			var co = new NeoImplication(apIf,apThen,builder);
			
			if(co.isSatisfied())
				return true;
			else
				return false;
			
		} else if(c.getBody() instanceof ConstraintReference) { 
		
			logger.info("Its a ConstraintReference!");
			throw new UnsupportedOperationException(c.getBody().toString());
		
		} else if (c.getBody() instanceof ConstraintBody){
			
			for(int j=0; j<c.getBody().getChildren().size(); j++) {
				
				ConstraintBody cpr = c.getBody().getChildren().get(j);
				
				for(int i=0; i<c.getBody().getChildren().get(j).getChildren().size(); i++) {
										
					if(c.getBody().getChildren().get(j).getChildren().get(i) instanceof ConstraintReference) {
						var r = (ConstraintReference) c.getBody().getChildren().get(j).getChildren().get(i);
						logger.info(r.isNegated() + " <-> " + r.getReference().getName());
						//cChilds.add(new NeoConstraint(r.getReference(), builder));
					}
				}
			}	
			
		} else {
			logger.info("Its an Unkown Type!");
			throw new UnsupportedOperationException(c.getBody().toString());
		}
		
		throw new UnsupportedOperationException();
	}

}
