package org.emoflon.neo.emsl.util

import com.google.inject.Injector
import java.util.ArrayList
import java.util.Collection
import java.util.HashSet
import java.util.List
import java.util.Set
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.EObject
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.ecore.resource.ResourceSet
import org.eclipse.emf.ecore.util.EcoreUtil
import org.eclipse.emf.mwe.utils.StandaloneSetup
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.emoflon.neo.emsl.EMSLStandaloneSetup
import org.emoflon.neo.emsl.eMSL.AttributeExpression
import org.emoflon.neo.emsl.eMSL.BuiltInType
import org.emoflon.neo.emsl.eMSL.DataType
import org.emoflon.neo.emsl.eMSL.EMSL_Spec
import org.emoflon.neo.emsl.eMSL.EnumValue
import org.emoflon.neo.emsl.eMSL.LinkAttributeExpTarget
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock
import org.emoflon.neo.emsl.eMSL.MetamodelPropertyStatement
import org.emoflon.neo.emsl.eMSL.MetamodelRelationStatement
import org.emoflon.neo.emsl.eMSL.ModelNodeBlock
import org.emoflon.neo.emsl.eMSL.ModelPropertyStatement
import org.emoflon.neo.emsl.eMSL.ModelRelationStatement
import org.emoflon.neo.emsl.eMSL.NodeAttributeExpTarget
import org.emoflon.neo.emsl.eMSL.PrimitiveBoolean
import org.emoflon.neo.emsl.eMSL.PrimitiveInt
import org.emoflon.neo.emsl.eMSL.PrimitiveString
import org.emoflon.neo.emsl.eMSL.UserDefinedType
import org.emoflon.neo.emsl.eMSL.Value
import org.emoflon.neo.emsl.eMSL.impl.EMSLPackageImpl

class EMSLUtil {
	public static final String PLUGIN_ID = "org.emoflon.neo.emsl";
	public static final String UI_PLUGIN_ID = "org.emoflon.neo.emsl.ui"

	public static final String ORG_EMOFLON_NEO_CORE = "org.emoflon.neo.neocore";
	public static final String ORG_EMOFLON_NEO_CORE_URI = "platform:/plugin/" + ORG_EMOFLON_NEO_CORE +
		"/model/NeoCore.msl"

	public static final String P_URI = "ConnectionURIPreference"
	public static final String P_USER = "UserPreference"
	public static final String P_PASSWORD = "PasswordPreference"

	def static EMSL_Spec loadSpecification(String modelURI, String platformResourceURIRoot,
		String platformPluginURIRoot) {
		EMSLPackageImpl.init()
		new StandaloneSetup().setPlatformUri(platformResourceURIRoot)
		var Injector injector = new EMSLStandaloneSetup().createInjectorAndDoEMFRegistration()
		var XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet)
		resourceSet.URIConverter.URIMap.put(URI.createURI("platform:/plugin/"), URI.createURI(platformPluginURIRoot))
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE)
		var Resource resource = resourceSet.getResource(URI.createURI(modelURI), true)
		var EMSL_Spec spec = (resource.getContents().get(0) as EMSL_Spec)
		EcoreUtil.resolveAll(resourceSet)

		val proxies = resourceSet.resources.flatMap [
			it.allContents.toList.flatMap [
				var allRefs = new ArrayList<EObject>
				allRefs.addAll(it.eAllContents.toList)
				allRefs.addAll(it.eCrossReferences)
				return allRefs
			]
		].filter[it.eIsProxy]

		if (!proxies.empty)
			throw new IllegalStateException("Your resource set contains unresolved proxies: " + proxies.toList)

		return spec
	}

	def static loadEMSL_Spec(String uri, EObject root) {
		val rs = root.eResource.resourceSet
		loadEMSL_Spec(uri, rs)
	}

	def static loadEMSL_Spec(String uri, ResourceSet rs) {
		val resource = rs.getResource(URI.createURI(uri), true)
		resource.contents.get(0)
	}

	def static Set<MetamodelNodeBlock> thisAndAllSuperTypes(MetamodelNodeBlock block) {
		val blocks = new HashSet
		if (block !== null) {
			blocks.add(block)
			block.superTypes.forEach[blocks.addAll(thisAndAllSuperTypes(it))]
		}

		return blocks
	}

	def static String relationNameConvention(String from, List<String> relType, String to, int index) {
		'''«from»_«relType.join("_")»_«index»_«to»'''
	}

	def static Object parseStringWithType(Value value, DataType type) {
		if (type instanceof BuiltInType) {
			switch (type.reference) {
				case ESTRING:
					return PrimitiveString.cast(value).literal
				case EINT:
					return PrimitiveInt.cast(value).literal
				case EBOOLEAN:
					return PrimitiveBoolean.cast(value).isTrue
				default:
					throw new IllegalStateException("This literal has to be handled: " + value)
			}
		} else if (type instanceof UserDefinedType) {
			var userDefinedType = type
			val enumLiteral = EnumValue.cast(value).getLiteral();
			if (userDefinedType.reference.literals.exists[l|l === enumLiteral])
				return enumLiteral.name
			else {
				throw new IllegalArgumentException(value + " is not a legal literal of " + type);
			}
		} else {
			throw new IllegalArgumentException("Unable to parse: " + value + " as a " + type);
		}
	}

	def static String getJavaType(DataType type) {
		if (type instanceof BuiltInType) {
			switch (type.reference) {
				case ESTRING:
					return "String"
				case EINT:
					return "int"
				case EBOOLEAN:
					return "boolean"
				case EDATE:
					return "LocalDate"
				default:
					throw new IllegalStateException("This type has to be handled: " + type)
			}
		} else if (type instanceof UserDefinedType) {
			return "String";
		} else {
			throw new IllegalArgumentException("Unknown type: " + type);
		}
	}

	def static String handleValue(Value value) {
		if(value instanceof PrimitiveString) return "\"" + PrimitiveString.cast(value).getLiteral() + "\""

		if(value instanceof PrimitiveInt) return Integer.toString(PrimitiveInt.cast(value).getLiteral())

		if(value instanceof PrimitiveBoolean) return Boolean.toString(PrimitiveBoolean.cast(value).isTrue())

		if(value instanceof EnumValue) return "\"" + EnumValue.cast(value).getLiteral().getName().toString() + "\""

		if (value instanceof AttributeExpression) {
			// node::<target>
			var node = value.node
			var target = value.target

			if (target instanceof NodeAttributeExpTarget) {
				// node::attribute
				var attr = target.attribute
				return node.name + "." + attr.name
			} else if (target instanceof LinkAttributeExpTarget) {
				// node::-link->target::attribute
				var link = target.link
				var trgBlock = target.target
				var attr = target.attribute
				return EMSLUtil.relationNameConvention(node.name, List.of(link.name), trgBlock.name,
					indexOf(link, node, trgBlock)) + "." + attr.name
			}
		}

		throw new IllegalArgumentException('''Not yet able to handle: «value»''')
	}
	
	private def static int indexOf(MetamodelRelationStatement ref, ModelNodeBlock node, ModelNodeBlock trg){
		val rel = node.relations.filter[!isVariableLink(it) && getOnlyType(it).equals(ref) && it.target.equals(trg)].get(0)
		return node.relations.indexOf(rel)
	}

	def static String handleValue(Object value) {
		if(value instanceof String) return "\"" + value + "\"" else return value.toString;
	}

	def static Collection<MetamodelPropertyStatement> allPropertiesOf(MetamodelNodeBlock type) {
		thisAndAllSuperTypes(type).flatMap[t|t.properties].toSet
	}

	def static getAllTypes(ModelRelationStatement rel) {
		rel.types.sortBy[t|t.type.name].map[t|t.type.name]
	}

	def static isVariableLink(ModelRelationStatement rel) {
		rel.types.size > 1
	}

	def static getOnlyType(ModelRelationStatement rel) {
		if (EMSLUtil.isVariableLink(rel))
			throw new IllegalArgumentException('''«rel» is a variable link and does not have a single type!''')

		rel.types.get(0).type
	}

	def static getNameOfType(ModelPropertyStatement p) {
		if (p.type !== null)
			p.type.name
		else // Link has an inferred type
			p.inferredType
	}
}
