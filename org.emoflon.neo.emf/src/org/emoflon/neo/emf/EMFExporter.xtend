package org.emoflon.neo.emf

import java.util.HashMap
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EClass
import org.eclipse.emf.ecore.EcoreFactory
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl
import org.emoflon.neo.emsl.eMSL.EMSL_Spec
import org.emoflon.neo.emsl.eMSL.Metamodel
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock

/**
 * Transforms EMSL to EMF.
 * Currently only metamodels and a very limited set of features (just meant as a stub).
 */
class EMFExporter {
	val ResourceSet emslSpecs
	val ResourceSet output
	val String uriPrefix
	val String uriPostfix

	new(ResourceSet emslSpecs, String uriPrefix, String uriPostfix) {
		this.emslSpecs = emslSpecs
		this.uriPrefix = uriPrefix
		this.uriPostfix = uriPostfix

		output = new ResourceSetImpl
	}

	def ResourceSet generateEMFModelsFromEMSL() {
		emslSpecs.resources.flatMap [ r |
			r.contents
		].filter [ c |
			c instanceof EMSL_Spec
		].flatMap [ s |
			(s as EMSL_Spec).entities
		].filter [ e |
			e instanceof Metamodel
		].forEach [ m |
			generateEMFMetamodel(m as Metamodel)
		]

		output
	}

	private def generateEMFMetamodel(Metamodel m) {
		val r = output.createResource(URI.createURI(uriPrefix + m.name + uriPostfix))
		val root = EcoreFactory.eINSTANCE.createEPackage
		root.name = m.name.replace(".", "_")
		root.nsURI = m.name
		root.nsPrefix = m.name
		r.contents.add(root)

		val blocksToEClasses = new HashMap<MetamodelNodeBlock, EClass>
		m.nodeBlocks.forEach [ nb |
			val eclass = EcoreFactory.eINSTANCE.createEClass
			blocksToEClasses.put(nb, eclass)
			eclass.abstract = nb.abstract
			eclass.name = nb.name
			root.EClassifiers.add(eclass)
		]
		blocksToEClasses.forEach [ nb, eclass |
			eclass.ESuperTypes.addAll(nb.superTypes.map[st|blocksToEClasses.get(st)])
		]

		m.enums.forEach [ enm |
			val eenum = EcoreFactory.eINSTANCE.createEEnum
			eenum.name = enm.name
			enm.literals.forEach [ lt |
				val elt = EcoreFactory.eINSTANCE.createEEnumLiteral
				elt.name = lt.name
				elt.literal = lt.name
				elt.value = eenum.ELiterals.size
				eenum.ELiterals.add(elt)
			]
			root.EClassifiers.add(eenum)
		]
	}
}
