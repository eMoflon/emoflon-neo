package org.emoflon.neo.neo4j.adapter;

import org.emoflon.neo.emsl.eMSL.AndBody;
import org.emoflon.neo.emsl.eMSL.ConstraintReference;
import org.emoflon.neo.emsl.eMSL.OrBody;

public class NeoAndBody {

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

				if (!consRef.isSatisfied()) {
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

}
