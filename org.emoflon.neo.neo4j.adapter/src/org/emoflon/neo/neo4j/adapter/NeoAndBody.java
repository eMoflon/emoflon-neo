package org.emoflon.neo.neo4j.adapter;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.log4j.Logger;
import org.emoflon.neo.emsl.eMSL.AndBody;
import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.OrBody;

public class NeoAndBody {

	private static final Logger logger = Logger.getLogger(NeoCoreBuilder.class);
	private AndBody body;
	private NeoCoreBuilder builder;

	public NeoAndBody(AndBody body, NeoCoreBuilder builder) {

		this.body = body;
		this.builder = builder;

	}

	public boolean isSatisfied() {

		for (Object b : body.getChildren()) {

			if (b instanceof ConstraintReference) {
				var consRef = new NeoConstraint(((ConstraintReference) b).getReference(), builder);

				if (((ConstraintReference) b).isNegated()) {
					logger.info("Attention: Constraint is negated!");
				}

				var satisfied = consRef.isSatisfied();

				if ((!satisfied && !((ConstraintReference) b).isNegated())
						|| satisfied && ((ConstraintReference) b).isNegated()) {

					return false;
				}

			} else if (b instanceof OrBody) {
				var orbody = new NeoOrBody((OrBody) b, builder);

				if (!orbody.isSatisfied()) {
					return false;
				}
			}
		}

		return true;

	}

	public Collection<NeoNode> getNodes() {

		Collection<NeoNode> nodes = new ArrayList<>();

		for (Object b : body.getChildren()) {

			if (b instanceof ConstraintReference) {
				var consRef = new NeoConstraint(((ConstraintReference) b).getReference(), builder);

				for (NeoNode node : consRef.getNodes()) {
					nodes.add(node);
				}

			} else if (b instanceof OrBody) {
				var orbody = new NeoOrBody((OrBody) b, builder);

				for (NeoNode node : orbody.getNodes()) {
					nodes.add(node);
				}
			}
		}

		return nodes;

	}

	public String getOptionalMatch() {
		
		var query = "";
		
		for (Object b : body.getChildren()) {

			if (b instanceof ConstraintReference) {
				var consRef = new NeoConstraint(((ConstraintReference) b).getReference(), builder);
				query += consRef.getOptionalQuery();

			} else if (b instanceof OrBody) {
				var orbody = new NeoOrBody((OrBody) b, builder);
				query += orbody.getOptionalQuery();
			}
		}
		
		return query;
	}

	public String getQueryString_Where() {
		
		var query = "(";
		var first = true;
		
		for (Object b : body.getChildren()) {

			if (b instanceof ConstraintReference) {
				var consRef = new NeoConstraint(((ConstraintReference) b).getReference(), builder);
				if(first) {
					first = false;
				} else {
					query += " AND ";
				}
				if(((ConstraintReference) b).isNegated())
					query += "NOT(" + consRef.getWhereQuery() + ")";
				else
					query += "(" + consRef.getWhereQuery() + ")";
				
			} else if (b instanceof OrBody) {
				var orbody = new NeoOrBody((OrBody) b, builder);
				if(first) {
					first = false;
				} else {
					query += " OR ";
				}
				query += orbody.getQueryString_Where();
			}
		}
		
		return query + ")";
	}

}
