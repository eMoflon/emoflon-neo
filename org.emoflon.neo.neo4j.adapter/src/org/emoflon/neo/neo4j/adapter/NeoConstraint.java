package org.emoflon.neo.neo4j.adapter;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.Constraint;
import org.emoflon.neo.emsl.eMSL.Implication;
import org.emoflon.neo.emsl.eMSL.NegativeConstraint;
import org.emoflon.neo.emsl.eMSL.OrBody;
import org.emoflon.neo.emsl.eMSL.PositiveConstraint;
import org.emoflon.neo.engine.api.constraints.IConstraint;

public class NeoConstraint implements IConstraint {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private NeoCoreBuilder builder;
	private Constraint c;

	public NeoConstraint(Constraint c, NeoCoreBuilder builder) {
		this.builder = builder;
		this.c = c;

	}
	
	public String getName() {
		return c.getName();
	}
	
	public NeoReturn getConstraintData() {
		
		NeoReturn returnStmt = new NeoReturn();
		
		if (c.getBody() instanceof PositiveConstraint) {
			var ap = (AtomicPattern) c.getBody().eCrossReferences().get(0);
			var co = new NeoPositiveConstraint(ap, builder);

			returnStmt.addNodes(co.getNodes());
			returnStmt.addOptionalMatch(co.getQueryString_OptionalMatch());
			returnStmt.addWhereClause(co.getQueryString_Where());
			

		} else if (c.getBody() instanceof NegativeConstraint) {
			var ap = (AtomicPattern) c.getBody().eCrossReferences().get(0);
			var co = new NeoNegativeConstraint(ap, builder);

			returnStmt.addNodes(co.getNodes());
			returnStmt.addOptionalMatch(co.getQueryString_OptionalMatch());
			returnStmt.addWhereClause(co.getQueryString_Where());

		} else if (c.getBody() instanceof Implication) {
			var apIf = (AtomicPattern) c.getBody().eCrossReferences().get(0);
			var apThen = (AtomicPattern) c.getBody().eCrossReferences().get(1);
			var co = new NeoImplication(apIf, apThen, builder);
			
			returnStmt.addNodes(co.getIfNodes());
			returnStmt.addNodes(co.getThenNodes());
			returnStmt.addOptionalMatch(co.getQueryString_OptionalMatch());
			returnStmt.addWhereClause(co.getQueryString_Where());

		} else if (c.getBody() instanceof OrBody) {

			var body = (OrBody) c.getBody();
			var neoBody = new NeoOrBody(body, builder);
			
			returnStmt.addNodes(neoBody.getConstraintData().getNodes());
			returnStmt.addOptionalMatch(neoBody.getConstraintData().getOptionalMatchString());
			returnStmt.addWhereClause(neoBody.getConstraintData().getWhereClause());

		} else {
			logger.info("Its an Unkown Type!");
			throw new UnsupportedOperationException(c.getBody().toString());
		}
		
		return returnStmt;
	}

	@Override
	public boolean isSatisfied() {

		logger.info("Check constraint: " + c.getName());

		if (c.getBody() instanceof PositiveConstraint) {
			var ap = (AtomicPattern) c.getBody().eCrossReferences().get(0);
			var co = new NeoPositiveConstraint(ap, builder);

			return co.isSatisfied();

		} else if (c.getBody() instanceof NegativeConstraint) {
			var ap = (AtomicPattern) c.getBody().eCrossReferences().get(0);
			var co = new NeoNegativeConstraint(ap, builder);

			return co.isSatisfied();

		} else if (c.getBody() instanceof Implication) {
			var apIf = (AtomicPattern) c.getBody().eCrossReferences().get(0);
			var apThen = (AtomicPattern) c.getBody().eCrossReferences().get(1);
			var co = new NeoImplication(apIf, apThen, builder);

			return co.isSatisfied();

		} else if (c.getBody() instanceof OrBody) {

			var body = (OrBody) c.getBody();
			var neoBody = new NeoOrBody(body, builder);

			return neoBody.isSatisfied();

		} else {
			logger.info("Its an Unkown Type!");
			throw new UnsupportedOperationException(c.getBody().toString());
		}

	}

}
