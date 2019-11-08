package org.emoflon.neo.cypher.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.emoflon.neo.emsl.eMSL.ConditionOperator;
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement;

public abstract class NeoElement {
	protected String name;
	protected String type;
	protected Collection<NeoProperty> equalityChecks;
	protected Collection<NeoProperty> assignments;
	protected Collection<NeoAssertion> inequalityChecks;

	public NeoElement(String name, String type, List<ModelPropertyStatement> properties) {
		this.name = name;
		this.type = type;

		this.equalityChecks = new ArrayList<>();
		this.assignments = new ArrayList<>();
		this.inequalityChecks = new ArrayList<>();
		for (var prop : properties) {
			NeoProperty neoProp = null;
			if (ConditionOperator.EQ.equals(prop.getOp()) || //
					ConditionOperator.ASSIGN.equals(prop.getOp()))
				if (!prop.getInferredType().equals(null))
					neoProp = new NeoInternalProperty(prop, this);
				else
					neoProp = new NeoProperty(prop, this);

			switch (prop.getOp()) {
			case EQ:
				this.equalityChecks.add(neoProp);
				break;
			case ASSIGN:
				this.assignments.add(neoProp);
				break;
			default:
				this.inequalityChecks.add(new NeoAssertion(prop, this));
			}
		}
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public Collection<NeoProperty> getEqualityChecks() {
		return equalityChecks;
	}

	public Collection<NeoProperty> getAttributeAssignments() {
		return assignments;
	}

	public Collection<NeoAssertion> getInequalityChecks() {
		return inequalityChecks;
	}
}
