package org.emoflon.neo.emsl.util

import java.util.ArrayList
import org.eclipse.emf.ecore.EObject
import org.emoflon.neo.emsl.eMSL.Pattern
import org.eclipse.emf.ecore.util.EcoreUtil

class EntityCloner {
	
	def EObject cloneEntity(EObject entity) {
		var collection = new ArrayList<EObject> ();
		
		collection.addAll((entity).eAllContents.toSet.toList)
		collection.add(entity as Pattern)
		
		var collectionCopy = EcoreUtil.copyAll(collection)
		
		for (c : collectionCopy) {
			if (c.class == entity.class) {
				System.out.println((c as Pattern).body.name)
				return c
			}
		}
	}
}