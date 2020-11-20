package org.emoflon.neo.emsl.refinement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.apache.commons.lang.Validate;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.SuperType;
import org.emoflon.neo.emsl.eMSL.TripleRule;
import org.emoflon.neo.emsl.util.FlattenerException;

public class EMSLFlattener {
	private List<SuperType> entities;
	private AbstractEntityFlattener flattener;

	private EMSLFlattener(SuperType originalEntity) {
		EcoreUtil.resolveAll(originalEntity);
		entities = List.of(EcoreUtil.copy(originalEntity));

		if (originalEntity instanceof TripleRule)
			flattener = new TripleRuleFlattener();
		else if (originalEntity instanceof SuperType)
			flattener = new RuleFlattener();
		else
			throw new IllegalArgumentException(
					"I don't know how to flatten entities of type " + originalEntity.eClass().getName());
	}

	private EMSLFlattener(Collection<Model> models) {
		models.forEach(m -> EcoreUtil.resolveAll(m));
		entities = new ArrayList<>(EcoreUtil.copyAll(models));
		flattener = new RuleFlattener();
	}

	/**
	 * Returns a flattened copy of the given entity.
	 * 
	 * @param originalEntity that is to be copied and flattened.
	 * @return flattened copy of given entity.
	 * @throws FlattenerException is thrown if the entity could not be flattened.
	 */
	public static SuperType flatten(SuperType originalEntity) throws FlattenerException {
		return new EMSLFlattener(originalEntity).flattenEntity();
	}

	public static Collection<Model> flattenAllModels(Collection<Model> models) throws FlattenerException {
		return new EMSLFlattener(models).flattenAllModels();
	}

	private Collection<Model> flattenAllModels() throws FlattenerException {
		var flattenedModels = new ArrayList<Model>();
		for (var model : entities) {
			flattenedModels.add((Model) flattener.flatten(model, new HashSet<String>()));
		}

		return flattenedModels;
	}

	private SuperType flattenEntity() throws FlattenerException {
		Validate.isTrue(entities.size() == 1);
		return flattener.flatten(entities.get(0), new HashSet<String>());
	}

	public static Pattern flattenPattern(Pattern p) throws FlattenerException {
		var flattenedBody = (AtomicPattern) flatten(p.getBody());
		var pattern = EcoreUtil.copy(p);
		pattern.setBody(flattenedBody);
		return pattern;
	}

	public static Pattern flattenToPattern(AtomicPattern ap) throws FlattenerException {
		var flattenedBody = (AtomicPattern) flatten(ap);
		var pattern = EMSLFactory.eINSTANCE.createPattern();
		pattern.setBody(flattenedBody);
		return pattern;
	}
}
