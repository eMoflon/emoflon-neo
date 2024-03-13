// Setup 2
package org.eneo.class2relational;

//Setup 2
import org.emoflon.neo.engine.modules.attributeConstraints.NeoAttributeConstraint;
//Setup 2
import org.emoflon.neo.engine.modules.attributeConstraints.NeoAttributeConstraintVariable;

//Setup 4
public class FirstToLowerCase extends NeoAttributeConstraint {

	/**
	 * FirstToLowerCase(word, lowerCase) - lowerCase is word with its first character in lower case.
	 * 
	 */
	@Override
	// Setup 2
	public void solve() {
		// Setup 5
		if (variables.size() != 2)
			// Setup 4
			throw new RuntimeException("The CSP -ToLowerCase- needs exactly two variables");

		// Setup 3
		NeoAttributeConstraintVariable word = variables.get(0);
		// Setup 3
		NeoAttributeConstraintVariable lowerCase = variables.get(1);

		// Setup 5
		String bindingStates = getBindingStates(word, lowerCase);

		// BB - simple check
		// Tracing 4
		if (bindingStates.equals("BB")) {
			// Tracing 9
			setSatisfied(firstToLower(word.getValue().toString()).equals(lowerCase.getValue().toString()));
		}
		// BF - make first letter lower case
		// Transformation 5
		else if (bindingStates.equals("BF")) {
			// Transformation 6
			lowerCase.bindToValue(firstToLower(word.getValue().toString()));
			// Setup 2
			setSatisfied(true);
		} else {
			// Setup 5
			throw new UnsupportedOperationException(
					"This case in the constraint has not been implemented yet: " + bindingStates);
		}
	}

	// Setup 5
	private String firstToLower(String s) {
		// Transformation 6
		return s.toLowerCase().charAt(0) + s.substring(1, s.length());
	}
}
