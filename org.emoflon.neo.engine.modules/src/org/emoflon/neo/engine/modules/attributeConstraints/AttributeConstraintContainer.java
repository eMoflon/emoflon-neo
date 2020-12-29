package org.emoflon.neo.engine.modules.attributeConstraints;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.emoflon.neo.cypher.common.NeoMask;
import org.emoflon.neo.cypher.patterns.NeoMatch;
import org.emoflon.neo.emsl.eMSL.AttributeConstraint;
import org.emoflon.neo.emsl.eMSL.ConstraintArgValue;
import org.emoflon.neo.emsl.eMSL.Parameter;
import org.emoflon.neo.engine.modules.attributeConstraints.standardLibrary.AddPrefix;
import org.emoflon.neo.engine.modules.attributeConstraints.standardLibrary.AddSuffix;
import org.emoflon.neo.engine.modules.attributeConstraints.standardLibrary.Concat;
import org.neo4j.driver.Record;

public class AttributeConstraintContainer {
	private Map<String, Supplier<NeoAttributeConstraint>> creators;
	private List<AttributeConstraint> attributeConstraints;
	
	public AttributeConstraintContainer() {
		creators = new HashMap<>();
		
		creators.put("concat", () -> new Concat());
		creators.put("addSuffix", () -> new AddSuffix());
		creators.put("addPrefix", () -> new AddPrefix());
	}
	
	public void addCreator(String key, Supplier<NeoAttributeConstraint> creator) {
		creators.put(key, creator);
	}

	public void initialise(List<AttributeConstraint> attributeConstraints) {
		this.attributeConstraints = attributeConstraints;
	}

	public Collection<NeoMatch> solveFilterAndMask(Collection<NeoMatch> matches, NeoMask mask) {
		var dataItr = NeoMatch.getData(matches, mask).iterator();
		var filteredMatches = new ArrayList<NeoMatch>();
		for (var match : matches) {
			var data = dataItr.next();
			var constraints = new ArrayList<NeoAttributeConstraint>();
			var parameterVariables = new HashMap<String, NeoAttributeConstraintVariable>();
			for (var attrConstr : attributeConstraints) {	
				if(!creators.containsKey(attrConstr.getType().getName()))
					throw new IllegalStateException("You have to add a creator for: " + attrConstr.getType().getName());
				
				var constraint = creators.get(attrConstr.getType().getName()).get();
				for (var value : attrConstr.getValues()) {
					var variable = createAndAddVariable(parameterVariables, value, data);
					constraint.addVariable(variable);
				}
				constraints.add(constraint);
			}
			
			var isSatisfied = true;
			for (var constraint : constraints) {
				constraint.solve();
				if (!constraint.isSatisfied()) {
					isSatisfied = false;
					break;
				}
			}
			
			if(isSatisfied) {				
				parameterVariables.forEach((k, p) -> match.addParameter(k, p.getValue()));
				filteredMatches.add(match);
			}
		}
		
		return filteredMatches;
	}

	private NeoAttributeConstraintVariable createAndAddVariable(Map<String, NeoAttributeConstraintVariable> parameterVariables, ConstraintArgValue value, Record data) {
		var type = value.getType().getType();
		var valueExp = value.getValue();

		if (valueExp instanceof Parameter) {
			var param = (Parameter) valueExp;
			if(parameterVariables.keySet().contains(param.getName())) {
				return parameterVariables.get(param.getName());
			} else {				
				var variable = new NeoAttributeConstraintVariable(type, param.getName());
				parameterVariables.put(param.getName(), variable);
				return variable;
			}
		} else {
			return new NeoAttributeConstant(type, valueExp, data);		
		}
	}
}
