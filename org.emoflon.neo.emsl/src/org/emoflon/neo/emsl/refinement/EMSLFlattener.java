package org.emoflon.neo.emsl.refinement;

import java.util.HashSet;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emoflon.neo.emsl.eMSL.AtomicPattern;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.SuperType;
import org.emoflon.neo.emsl.eMSL.TripleRule;
import org.emoflon.neo.emsl.util.FlattenerException;

public class EMSLFlattener {
	private SuperType entity;
	private AbstractEntityFlattener flattener;

	private EMSLFlattener(SuperType originalEntity) {
		EcoreUtil.resolveAll(originalEntity);
		entity = EcoreUtil.copy(originalEntity);

		if (entity instanceof TripleRule)
			flattener = new TripleRuleFlattener();
		else if (entity instanceof SuperType)
			flattener = new RuleFlattener();
		else
			throw new IllegalArgumentException(
					"I don't know how to flatten entities of type " + entity.eClass().getName());
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

	private SuperType flattenEntity() throws FlattenerException {
		return flattener.flatten(entity, new HashSet<String>());
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
