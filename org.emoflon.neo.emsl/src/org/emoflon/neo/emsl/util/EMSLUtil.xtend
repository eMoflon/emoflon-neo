package org.emoflon.neo.emsl.util

import com.google.inject.Injector
import java.util.ArrayList
import java.util.Collection
import java.util.HashSet
import java.util.List
import java.util.Optional
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
import org.emoflon.neo.emsl.eMSL.BinaryExpression
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
import org.emoflon.neo.emsl.eMSL.ValueExpression
import org.emoflon.neo.emsl.eMSL.impl.EMSLPackageImpl
import org.emoflon.neo.emsl.eMSL.PrimitiveDouble
import java.time.LocalDate
import org.emoflon.neo.emsl.eMSL.Parameter
import org.emoflon.neo.emsl.eMSL.BuiltInDataTypes
import org.emoflon.neo.emsl.eMSL.ConstraintBody
import org.emoflon.neo.emsl.eMSL.NegativeConstraint
import org.emoflon.neo.emsl.eMSL.PositiveConstraint
import org.emoflon.neo.emsl.eMSL.Implication
import org.emoflon.neo.emsl.eMSL.OrBody
import org.emoflon.neo.emsl.eMSL.AndBody
import org.emoflon.neo.emsl.eMSL.ConstraintReference
import org.emoflon.neo.emsl.eMSL.AtomicPattern
import java.util.function.Consumer

class EMSLUtil {
	public static final String PLUGIN_ID = "org.emoflon.neo.emsl";
	public static final String UI_PLUGIN_ID = "org.emoflon.neo.emsl.ui"

	public static final String ORG_EMOFLON_NEO_CORE = "NeoCore";
	public static final String ORG_EMOFLON_NEO_CORE_URI = "platform:/plugin/org.emoflon.neo.neocore/model/NeoCore.msl"
	public static final String ORG_EMOFLON_ATR_CNSTR_LIB_URI = "platform:/plugin/org.emoflon.neo.neocore/model/AttributeConstraintsLibrary.msl"

	public static final String P_URI = "ConnectionURIPreference"
	public static final String P_USER = "UserPreference"
	public static final String P_PASSWORD = "PasswordPreference"

	public static final String RESERVED_PREFIX = "____"
	public static final String PARAM_NAME_FOR_MATCH = "match";

	def static EMSL_Spec loadSpecification(String modelURI, String platformResourceURIRoot,
		String platformPluginURIRoot, String neocoreURI) {
		EMSLPackageImpl.init()
		new StandaloneSetup().setPlatformUri(platformResourceURIRoot)
		var Injector injector = new EMSLStandaloneSetup().createInjectorAndDoEMFRegistration()
		var XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet)
		resourceSet.URIConverter.URIMap.put(URI.createURI(ORG_EMOFLON_NEO_CORE_URI), URI.createURI(neocoreURI))
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

	def static Optional<EObject> loadEMSL_Spec(String uri, ResourceSet rs) {
		if (uri === null || rs === null)
			return Optional.empty

		val resource = rs.getResource(URI.createURI(uri), true)
		return Optional.of(resource.contents.get(0))
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

	def static Object parseStringWithType(ValueExpression value, DataType type) {
		if (type instanceof BuiltInType) {
			switch (type.reference) {
				case ESTRING:
					return PrimitiveString.cast(value).literal
				case EINT:
					return PrimitiveInt.cast(value).literal
				case EBOOLEAN:
					return PrimitiveBoolean.cast(value).isTrue
				case ECHAR:
					return PrimitiveString.cast(value).literal.charAt(0)
				case ELONG:
					return PrimitiveInt.cast(value).literal as long
				case EFLOAT:
					return PrimitiveDouble.cast(value).literal as float
				case EDOUBLE:
					return PrimitiveDouble.cast(value).literal
				case EDATE:
					return LocalDate.parse(PrimitiveString.cast(value).literal)
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
				case EDOUBLE:
					return "double"
				case EFLOAT:
					return "float"
				case ECHAR:
					return "String"
				case ELONG:
					return "long"
				default:
					throw new IllegalStateException("This type has to be handled: " + type)
			}
		} else if (type instanceof UserDefinedType) {
			return "String";
		} else {
			throw new IllegalArgumentException("Unknown type: " + type);
		}
	}

	def static Optional<BuiltInDataTypes> castToBuiltInType(DataType type) {
		if (type instanceof BuiltInType)
			Optional.of(type.reference)
		else
			Optional.empty
	}

	def static String handleValueForCypher(ValueExpression value) {
		if(value instanceof PrimitiveString) return "\"" + PrimitiveString.cast(value).getLiteral() + "\""

		if(value instanceof PrimitiveInt) return Integer.toString(PrimitiveInt.cast(value).getLiteral())

		if(value instanceof PrimitiveBoolean) return Boolean.toString(PrimitiveBoolean.cast(value).isTrue())

		if(value instanceof EnumValue) return "\"" + EnumValue.cast(value).getLiteral().getName().toString() + "\""

		if (value instanceof AttributeExpression) {
			// node::<target>
			val node = value.node
			val target = value.target

			if (target instanceof NodeAttributeExpTarget) {
				// node::attribute
				val attr = target.attribute
				return node.name + "." + attr.name
			} else if (target instanceof LinkAttributeExpTarget) {
				// node::-link->target::attribute
				val link = target.link
				val trgBlock = target.target
				val attr = target.attribute
				return EMSLUtil.relationNameConvention(node.name, List.of(link.name), trgBlock.name,
					indexOf(link, node, trgBlock)) + "." + attr.name
			}
		}

		if (value instanceof BinaryExpression) {
			return org.emoflon.neo.emsl.util.EMSLUtil.handleValueForCypher(value.left) + value.op +
				org.emoflon.neo.emsl.util.EMSLUtil.handleValueForCypher(value.right)
		}

		if (value instanceof Parameter) {
			return "$" + value.name
		}

		throw new IllegalArgumentException('''Not yet able to handle: «value»''')
	}

	private def static int indexOf(MetamodelRelationStatement ref, ModelNodeBlock node, ModelNodeBlock trg) {
		val rel = node.relations.filter[!isVariableLink(it) && getOnlyType(it).equals(ref) && it.target.equals(trg)].
			get(0)
		return node.relations.indexOf(rel)
	}

	def static String returnValueAsString(Object value) {
		if (value instanceof String) {
			if (value.startsWith("$"))
				return value
			else if (value.startsWith("\"") && value.endsWith("\""))
				return value
			else
				return "\"" + value + "\""
		} else
			return value.toString;
	}

	def static Collection<MetamodelPropertyStatement> allPropertiesOf(MetamodelNodeBlock type) {
		thisAndAllSuperTypes(type).flatMap[t|t.properties].toSet
	}

	def static Collection<MetamodelRelationStatement> allRelationsOf(MetamodelNodeBlock type) {
		thisAndAllSuperTypes(type).flatMap[t|t.relations].toSet
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

	def static void iterateConstraintPatterns(ConstraintBody body, Consumer<AtomicPattern> action) {
		if(body instanceof NegativeConstraint)
			action.accept(body.pattern)
		else if(body instanceof PositiveConstraint)
			action.accept(body.pattern)
		else if(body instanceof Implication) {
			action.accept(body.premise)
			action.accept(body.conclusion)
		}
		else if(body instanceof OrBody) {
			body.children.forEach[iterateConstraintPatterns(it, action)]
		}
		else if(body instanceof AndBody) {
			body.children.forEach[iterateConstraintPatterns(it, action)]
		}
		else if(body instanceof ConstraintReference) {
			iterateConstraintPatterns(body.reference.body, action)
		}
	}
}
