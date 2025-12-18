package ctorust.runtime;

import java.util.HashMap;
import java.util.Map;

import org.emoflon.neo.engine.modules.attributeConstraints.NeoAttributeConstraint;
import org.emoflon.neo.engine.modules.attributeConstraints.NeoAttributeConstraintVariable;

public class CEnumToRustEnum extends NeoAttributeConstraint {

	@Override
	public void solve() {
		if (variables.size() != 2)
			throw new RuntimeException("The CSP -CEnumToRustEnum- needs exactly two variables");
		
		NeoAttributeConstraintVariable cEnum = variables.get(0);
		NeoAttributeConstraintVariable rustEnum = variables.get(1);

		String bindingStates = getBindingStates(cEnum, rustEnum);

		Map<Object, Object> mapToRust = new HashMap<>();
		mapToRust.put("char", "char");
		mapToRust.put("signed_char", "i8");
		mapToRust.put("unsigned_char", "u8");
		
		if (bindingStates.equals("BB")) {
			setSatisfied(mapToRust.get(cEnum.getValue()).equals(rustEnum.getValue()));
		}
		else if (bindingStates.equals("BF")) {
			rustEnum.bindToValue(mapToRust.get(cEnum.getValue()));
			setSatisfied(true);
		} 
		else if (bindingStates.equals("FB")) {
			mapToRust.entrySet().forEach(entry -> {
				if(entry.getValue().equals(rustEnum.getValue())) {
					cEnum.bindToValue(entry.getKey());
					setSatisfied(true);					
				}
			});
		}
		else {
			throw new UnsupportedOperationException(
					"This case in the constraint has not been implemented yet: " + bindingStates);
		}
	}
}
