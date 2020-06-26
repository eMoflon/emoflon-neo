package org.emoflon.neo.engine.modules.attributeConstraints.standardLibrary;

import org.emoflon.neo.engine.modules.attributeConstraints.NeoAttributeConstraint;
import org.emoflon.neo.engine.modules.attributeConstraints.NeoAttributeConstraintVariable;

public class AddPrefix extends NeoAttributeConstraint {

	/**
	 * addPrefix(prefix,word,result) prefix+word = result (prefix concatenated
	 * with word equals result)
	 * 
	 */
	@Override
	public void solve() {
		if (variables.size() != 3)
			throw new RuntimeException("The CSP -ADD_PREFIX- needs exactly three variables");

		NeoAttributeConstraintVariable prefix = variables.get(0);
		NeoAttributeConstraintVariable word = variables.get(1);
		NeoAttributeConstraintVariable result = variables.get(2);

		String bindingStates = getBindingStates(prefix, word, result);

		// BBB - check prefix
		if (bindingStates.equals("BBB")) {
			setSatisfied(("" + prefix.getValue() + word.getValue()).equals(result.getValue()));
		}
		// BBF - add prefix
		else if (bindingStates.equals("BBF")) {
			result.bindToValue("" + prefix.getValue() + word.getValue());
			setSatisfied(true);
		}
		// BFB - remove prefix
		else if (bindingStates.equals("BFB")) {
			if (("" + result.getValue()).startsWith("" + prefix.getValue())) {
				word.bindToValue(("" + result.getValue()).substring(("" + prefix.getValue()).length()));
				setSatisfied(true);
			}
		}
		// FBB - determine prefix
		else if (bindingStates.equals("FBB")) {
			if (("" + result.getValue()).endsWith("" + word.getValue())) {
				prefix.bindToValue(("" + result.getValue()).substring(0,
						("" + result.getValue()).length() - ("" + word.getValue()).length()));
				setSatisfied(true);
			}
		} 
		
		// modelgen implementations
		else if (bindingStates.equals("BFF")) {
			String randomWord = "" + generateValue(word.getType());
			word.bindToValue(randomWord);
			result.bindToValue(prefix.getValue() + randomWord);
			setSatisfied(true);
		} else if (bindingStates.equals("FFF")) {
			String randomWord = "" + generateValue(word.getType());
			prefix.bindToValue("prefix");
			word.bindToValue(randomWord);
			result.bindToValue(prefix.getValue() + randomWord);
			setSatisfied(true);
		} else if (bindingStates.equals("FBF")) {
			String randomWord = "" + generateValue(prefix.getType());
			prefix.bindToValue(randomWord);
			result.bindToValue(prefix.getValue().toString() + word.getValue());
			setSatisfied(true);
		} else {
			throw new UnsupportedOperationException(
					"This case in the constraint has not been implemented yet: " + bindingStates);
		}
	}
}
