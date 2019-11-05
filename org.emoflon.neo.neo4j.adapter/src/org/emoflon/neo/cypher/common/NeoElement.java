package org.emoflon.neo.cypher.common;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
			switch (prop.getOp()) {
			case EQ:
				this.equalityChecks.add(new NeoProperty(prop, this));
				break;
			case ASSIGN:
				this.assignments.add(new NeoProperty(prop, this));
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
