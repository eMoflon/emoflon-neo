package org.emoflon.neo.emsl.util

import com.google.inject.Injector
import java.util.Collection
import java.util.HashSet
import java.util.Set
import org.eclipse.emf.common.util.URI
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.emf.mwe.utils.StandaloneSetup
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.resource.XtextResourceSet
import org.emoflon.neo.emsl.EMSLStandaloneSetup
import org.emoflon.neo.emsl.eMSL.BuiltInType
import org.emoflon.neo.emsl.eMSL.DataType
import org.emoflon.neo.emsl.eMSL.EMSL_Spec
import org.emoflon.neo.emsl.eMSL.EnumValue
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock
import org.emoflon.neo.emsl.eMSL.MetamodelPropertyStatement
import org.emoflon.neo.emsl.eMSL.PrimitiveBoolean
import org.emoflon.neo.emsl.eMSL.PrimitiveInt
import org.emoflon.neo.emsl.eMSL.PrimitiveString
import org.emoflon.neo.emsl.eMSL.UserDefinedType
import org.emoflon.neo.emsl.eMSL.Value
import org.emoflon.neo.emsl.eMSL.impl.AttributeExpressionImpl
import org.emoflon.neo.emsl.eMSL.impl.EMSLPackageImpl

class EMSLUtil {
	public static final String ORG_EMOFLON_NEO_CORE = "org.emoflon.neo.neocore";
	public static final String ORG_EMOFLON_NEO_CORE_URI = "platform:/plugin/" + ORG_EMOFLON_NEO_CORE + "/model/NeoCore.msl"
	
	public static final String P_URI = "ConnectionURIPreference"
	public static final String P_USER = "UserPreference"
	public static final String P_PASSWORD = "PasswordPreference"

	def static EMSL_Spec loadSpecification(String modelURI, String platformResourceURIRoot, String platformPluginURIRoot) {
		EMSLPackageImpl.init()
		new StandaloneSetup().setPlatformUri(platformResourceURIRoot)
		var Injector injector = new EMSLStandaloneSetup().createInjectorAndDoEMFRegistration()
		var XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet)
		resourceSet.URIConverter.URIMap.put(URI.createURI("platform:/plugin/"), URI.createURI(platformPluginURIRoot))
		resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE)
		var Resource resource = resourceSet.getResource(URI.createURI(modelURI), true)
		var EMSL_Spec spec = (resource.getContents().get(0) as EMSL_Spec)
		return spec
	}

	def static Set<MetamodelNodeBlock> thisAndAllSuperTypes(MetamodelNodeBlock block) {
		val blocks = new HashSet
		if (block !== null) {
			blocks.add(block)
			block.superTypes.forEach[blocks.addAll(thisAndAllSuperTypes(it))]
		}
		return blocks
	}

	def static String relationNameConvention(String from, String relType, String to, int index) {
		'''«from»_«relType»_«index»_«to»'''
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

		if(value instanceof EnumValue) return "\""+EnumValue.cast(value).getLiteral().getName().toString()+"\""
		
		if(value instanceof AttributeExpressionImpl) return AttributeExpressionImpl.cast(value).node.name.toString + "." +
												allPropertiesOf(AttributeExpressionImpl.cast(value).node.type).get(0).name.toString
		
		throw new IllegalArgumentException('''Not yet able to handle: «value»''')
	}
	
	def static Collection<MetamodelPropertyStatement> allPropertiesOf(MetamodelNodeBlock type){
		thisAndAllSuperTypes(type).flatMap[t|t.properties].toSet
	}
}
