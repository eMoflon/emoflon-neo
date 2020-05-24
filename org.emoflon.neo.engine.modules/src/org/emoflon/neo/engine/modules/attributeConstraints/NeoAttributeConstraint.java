package org.emoflon.neo.engine.modules.attributeConstraints;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.engine.generator.modules.IParameterValueGenerator;
import org.emoflon.neo.engine.modules.valueGenerators.LoremIpsumStringValueGenerator;

public abstract class NeoAttributeConstraint {
	private char B = 'B';
	private char F = 'F';
	private boolean satisfied = false;
	protected List<NeoAttributeConstraintVariable> variables = new ArrayList<>();
	
	private static List<IParameterValueGenerator<DataType, ?>> randomGenerators = List.of(
			new LoremIpsumStringValueGenerator()
	);
	
	public abstract void solve();
	
	public void setSatisfied(boolean b) {
		satisfied = b;
	}
	
	public boolean isSatisfied() {
		return satisfied;
	}
	
	protected String getBindingStates(NeoAttributeConstraintVariable... variables) {
		if (variables.length == 0) {
			throw new IllegalArgumentException("Cannot determine binding states from an empty list of variables!");
		}
		char[] result = new char[variables.length];
		for (int i = 0; i < variables.length; i++) {
			result[i] = variables[i].isBound() ? B : F;
		}

		return String.valueOf(result);
	}
	
	public Object generateValue(DataType dataType) {
		return randomGenerators.stream()//
						.filter(rg -> rg.generatesValue(dataType))//
						.findAny()//
						.map(rg -> rg.generateValue(dataType))//
						.orElseThrow(() -> new RuntimeException("The type " + dataType + " is not supported for random value generation"));
	}

	protected void addVariable(NeoAttributeConstraintVariable variable) {
		variables.add(variable);
	}
	
	@Override
	public String toString() {
		return this.getClass().getName() + "[" + variables.stream()//
				.map(v -> v.getName() + ":" + v.getType())//
				.collect(Collectors.joining("_")) + "]";
	}
}
