package org.emoflon.neo.emsl.refinement;

import java.util.HashSet;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.emoflon.neo.emsl.eMSL.Entity;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.eMSL.Model;
import org.emoflon.neo.emsl.eMSL.Pattern;
import org.emoflon.neo.emsl.eMSL.TripleRule;
import org.emoflon.neo.emsl.util.FlattenerException;

public class EMSLFlattener<T extends Entity> {
	private T entity;
	private IEntityFlattener flattener;

	private EMSLFlattener(T originalEntity) {
		entity = EcoreUtil.copy(originalEntity);
		if (entity instanceof TripleRule)
			flattener = new TripleRuleFlattener();
		else if (entity instanceof Model)
			flattener = new ModelFlattener();
		else if (entity instanceof Pattern)
			flattener = new PatternFlattener();
		else if (entity instanceof Metamodel)
			flattener = new MetamodelFlattener();
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
	public static <T extends Entity> T flatten(T originalEntity) throws FlattenerException {
		return new EMSLFlattener<T>(originalEntity).flattenEntity();
	}

	private T flattenEntity() throws FlattenerException {
		return flattener.flatten(entity, new HashSet<String>());
	}
}
