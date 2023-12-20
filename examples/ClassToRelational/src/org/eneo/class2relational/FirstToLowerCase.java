package org.eneo.class2relational;

import org.emoflon.neo.engine.modules.attributeConstraints.NeoAttributeConstraint;
import org.emoflon.neo.engine.modules.attributeConstraints.NeoAttributeConstraintVariable;

public class FirstToLowerCase extends NeoAttributeConstraint {

	/**
	 * FirstToLowerCase(word, lowerCase) - lowerCase is word with its first character in lower case.
	 * 
	 */
	@Override
	public void solve() {
		if (variables.size() != 2)
			throw new RuntimeException("The CSP -ToLowerCase- needs exactly two variables");

		NeoAttributeConstraintVariable word = variables.get(0);
		NeoAttributeConstraintVariable lowerCase = variables.get(1);

		String bindingStates = getBindingStates(word, lowerCase);

		// BB - simple check
		if (bindingStates.equals("BB")) {
			setSatisfied(firstToLower(word.getValue().toString()).equals(lowerCase.getValue().toString()));
		}
		// BF - make first letter lower case
		else if (bindingStates.equals("BF")) {
			lowerCase.bindToValue(firstToLower(word.getValue().toString()));
			setSatisfied(true);
		} else {
			throw new UnsupportedOperationException(
					"This case in the constraint has not been implemented yet: " + bindingStates);
		}
	}

	private String firstToLower(String s) {
		return s.toLowerCase().charAt(0) + s.substring(1, s.length());
	}
}
