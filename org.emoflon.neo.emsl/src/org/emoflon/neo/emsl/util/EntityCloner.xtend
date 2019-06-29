package org.emoflon.neo.emsl.util

import java.util.ArrayList
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.util.EcoreUtil

class EntityCloner {
	
	def EObject cloneEntity(EObject entity) {
		var collection = new ArrayList<EObject> ();
		
		collection.addAll((entity).eAllContents.toSet.toList)
		collection.add(entity)
		
		var collectionCopy = EcoreUtil.copyAll(collection)
		
		for (c : collectionCopy) {
			if (c.class == entity.class) {
				return c
			}
		}
	}
}