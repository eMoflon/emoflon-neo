package org.emoflon.neo.cypher.models;

import static org.emoflon.neo.neocore.util.NeoCoreConstants.ABSTRACT_PROP;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.CONFORMS_TO_PROP;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.CORR;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EATTRIBUTE;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EATTRIBUTED_ELEMENT;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EATTRIBUTES;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EATTRIBUTE_TYPE;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EBOOLEAN;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.ECHAR;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.ECLASS;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.ECLASSIFIER;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.ECONTAINER;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EDATA_TYPE;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EDATE;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EDOUBLE;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EENUM;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EENUM_LITERAL;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EFLOAT;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EINT;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.ELITERALS;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.ELONG;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EOBJECT;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EREFERENCE;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EREFERENCES;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.EREFERENCE_TYPE;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.ESTRING;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.ESTRUCTURAL_FEATURE;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.ESUPER_TYPE;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.ETYPED_ELEMENT;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.ISCOMPOSITION_PROP;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.ISCONTAINMENT_PROP;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.METAMODEL;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.META_EL_OF;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.META_TYPE;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.MODEL;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.NAMESPACE_PROP;
import static org.emoflon.neo.neocore.util.NeoCoreConstants.NAME_PROP;
import static org.emoflon.neo.neocore.util.NeoCoreConstants._DLT_PROP;
import static org.emoflon.neo.neocore.util.NeoCoreConstants._TR_PROP;
import static org.emoflon.neo.neocore.util.NeoCoreConstants._TYPE_PROP;

import java.util.List;
import java.util.stream.Collectors;

import org.emoflon.neo.cypher.models.templates.NeoProp;
import org.emoflon.neo.emsl.util.EMSLUtil;

public class NeoCoreBootstrapper {
	// Lists of properties and labels for meta types
	public static final List<String> LABELS_FOR_AN_ECLASS = //
			addNeoCoreNamespace(ECLASS, ECLASSIFIER, EATTRIBUTED_ELEMENT, EOBJECT);
	public static final List<String> LABELS_FOR_AN_EATTRIBUTE = //
			addNeoCoreNamespace(EATTRIBUTE, EOBJECT, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT);
	public static final List<String> LABELS_FOR_AN_EREFERENCE = //
			addNeoCoreNamespace(EREFERENCE, EATTRIBUTED_ELEMENT, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT, EOBJECT);
	public static final List<String> LABELS_FOR_AN_EDATATYPE = //
			addNeoCoreNamespace(EDATA_TYPE, ECLASSIFIER, EOBJECT);

	public static final List<String> LABELS_FOR_A_METAMODEL = addNeoCoreNamespace(METAMODEL, MODEL, EOBJECT);
	public static final List<String> LABELS_FOR_A_MODEL = addNeoCoreNamespace(MODEL, EOBJECT);
	public static final List<String> LABELS_FOR_AN_ENUM = addNeoCoreNamespace(EENUM, EDATA_TYPE, EOBJECT, ECLASSIFIER);
	public static final List<String> LABELS_FOR_AN_ENUMLITERAL = //
			addNeoCoreNamespace(EENUM_LITERAL, EOBJECT, EDATA_TYPE, ECLASSIFIER);
	private static final NeoProp neocoreNamespaceProp = new NeoProp(NAMESPACE_PROP, EMSLUtil.ORG_EMOFLON_NEO_CORE);
 
	public static final List<NeoProp> neoCoreProps = List.of(new NeoProp(NAME_PROP, EMSLUtil.ORG_EMOFLON_NEO_CORE));
	public static final List<String> neoCoreLabels = LABELS_FOR_A_METAMODEL;

	public static final List<NeoProp> eclassProps = List.of(new NeoProp(NAME_PROP, ECLASS), neocoreNamespaceProp);
	public static final List<String> eclassLabels = LABELS_FOR_AN_ECLASS;

	public static final List<NeoProp> mmodelProps = List.of(new NeoProp(NAME_PROP, METAMODEL), neocoreNamespaceProp);
	public static final List<String> mmodelLabels = LABELS_FOR_AN_ECLASS;

	public static final List<NeoProp> modelProps = List.of(new NeoProp(NAME_PROP, MODEL), neocoreNamespaceProp);
	public static final List<String> modelLabels = LABELS_FOR_AN_ECLASS;

	public static final List<NeoProp> eobjectProps = List.of(new NeoProp(NAME_PROP, EOBJECT), neocoreNamespaceProp);
	public static final List<String> eobjectLabels = LABELS_FOR_AN_ECLASS;

	public static final List<NeoProp> erefProps = List.of(new NeoProp(NAME_PROP, EREFERENCE), neocoreNamespaceProp);
	public static final List<String> erefLabels = LABELS_FOR_AN_ECLASS;

	private static final List<NeoProp> eleofProps = List.of(new NeoProp(NAME_PROP, META_EL_OF));
	private static final List<String> eleofLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> conformtoProps = List.of(new NeoProp(NAME_PROP, CONFORMS_TO_PROP));
	private static final List<String> conformtoLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> erefsProps = List.of(new NeoProp(NAME_PROP, EREFERENCES));
	private static final List<String> erefsLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> eRefTypeProps = List.of(new NeoProp(NAME_PROP, EREFERENCE_TYPE));
	private static final List<String> eRefTypeLabels = LABELS_FOR_AN_EREFERENCE;

	static final List<NeoProp> eattrProps = List.of(new NeoProp(NAME_PROP, EATTRIBUTE), neocoreNamespaceProp);
	static final List<String> eattrLabels = LABELS_FOR_AN_ECLASS;

	private static final List<NeoProp> nameProps = List.of(new NeoProp(NAME_PROP, NAME_PROP));
	private static final List<String> nameLabels = LABELS_FOR_AN_EATTRIBUTE;

	private static final List<NeoProp> _type_Props = List.of(new NeoProp(NAME_PROP, _TYPE_PROP));
	private static final List<String> _type_Labels = LABELS_FOR_AN_EATTRIBUTE;

	private static final List<NeoProp> _tr_Props = List.of(new NeoProp(NAME_PROP, _TR_PROP));
	private static final List<String> _tr_Labels = LABELS_FOR_AN_EATTRIBUTE;
	
	private static final List<NeoProp> _dlt_Props = List.of(new NeoProp(NAME_PROP, _DLT_PROP));
	private static final List<String> _dlt_Labels = LABELS_FOR_AN_EATTRIBUTE;

	private static final List<NeoProp> isCompositionProps = List.of(new NeoProp(NAME_PROP, ISCOMPOSITION_PROP));
	private static final List<String> isCompositionLabels = LABELS_FOR_AN_EATTRIBUTE;

	private static final List<NeoProp> isContainmentProps = List.of(new NeoProp(NAME_PROP, ISCONTAINMENT_PROP));
	private static final List<String> isContainmentLabels = LABELS_FOR_AN_EATTRIBUTE;

	static final List<NeoProp> eDataTypeProps = List.of(new NeoProp(NAME_PROP, EDATA_TYPE), neocoreNamespaceProp);
	static final List<String> eDataTypeLabels = LABELS_FOR_AN_ECLASS;

	private static final List<NeoProp> eAttrEleProps = List.of(new NeoProp(NAME_PROP, EATTRIBUTED_ELEMENT), neocoreNamespaceProp);
	private static final List<String> eAttrEleLabels = LABELS_FOR_AN_ECLASS;

	private static final List<NeoProp> eStringProps = List.of(new NeoProp(NAME_PROP, ESTRING));
	private static final List<String> eStringLabels = LABELS_FOR_AN_EDATATYPE;

	private static final List<NeoProp> eintProps = List.of(new NeoProp(NAME_PROP, EINT));
	private static final List<String> eintLabels = LABELS_FOR_AN_EDATATYPE;

	private static final List<NeoProp> echarProps = List.of(new NeoProp(NAME_PROP, ECHAR));
	private static final List<String> echarLabels = LABELS_FOR_AN_EDATATYPE;

	private static final List<NeoProp> elongProps = List.of(new NeoProp(NAME_PROP, ELONG));
	private static final List<String> elongLabels = LABELS_FOR_AN_EDATATYPE;

	private static final List<NeoProp> efloatProps = List.of(new NeoProp(NAME_PROP, EFLOAT));
	private static final List<String> efloatLabels = LABELS_FOR_AN_EDATATYPE;

	private static final List<NeoProp> edoubleProps = List.of(new NeoProp(NAME_PROP, EDOUBLE));
	private static final List<String> edoubleLabels = LABELS_FOR_AN_EDATATYPE;

	private static final List<NeoProp> edateProps = List.of(new NeoProp(NAME_PROP, EDATE));
	private static final List<String> edateLabels = LABELS_FOR_AN_EDATATYPE;

	private static final List<NeoProp> eAttrTypeProps = List.of(new NeoProp(NAME_PROP, EATTRIBUTE_TYPE));
	private static final List<String> eAttrTypeLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> eSupTypeProps = List.of(new NeoProp(NAME_PROP, ESUPER_TYPE));
	private static final List<String> eSupTypeLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> eclassifierProps = List.of(new NeoProp(NAME_PROP, ECLASSIFIER), neocoreNamespaceProp);
	private static final List<String> eclassifierLabels = LABELS_FOR_AN_ECLASS;

	private static final List<NeoProp> eTypedeleProps = List.of(new NeoProp(NAME_PROP, ETYPED_ELEMENT), neocoreNamespaceProp);
	private static final List<String> eTypedeleLabels = LABELS_FOR_AN_ECLASS;

	private static final List<NeoProp> metaTypeProps = List.of(new NeoProp(NAME_PROP, META_TYPE));
	private static final List<String> metaTypeLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> corrProps = List.of(new NeoProp(NAME_PROP, CORR));
	private static final List<String> corrLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> econtainerProps = List.of(new NeoProp(NAME_PROP, ECONTAINER));
	private static final List<String> econtainerLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> eAttributesProps = List.of(new NeoProp(NAME_PROP, EATTRIBUTES));
	private static final List<String> eAttributesLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> eStructProps = List.of(new NeoProp(NAME_PROP, ESTRUCTURAL_FEATURE), neocoreNamespaceProp);
	private static final List<String> eStructLabels = LABELS_FOR_AN_ECLASS;

	private static final List<NeoProp> abstractattrProps = List.of(new NeoProp(NAME_PROP, ABSTRACT_PROP));
	private static final List<String> abstractattrLabels = LABELS_FOR_AN_EATTRIBUTE;
	
	private static final List<NeoProp> enamespaceProps = List.of(new NeoProp(NAME_PROP, NAMESPACE_PROP));
	private static final List<String> enamespaceLabels = LABELS_FOR_AN_EATTRIBUTE;

	private static final List<NeoProp> eBooleanProps = List.of(new NeoProp(NAME_PROP, EBOOLEAN));
	private static final List<String> eBooleanLabels = LABELS_FOR_AN_EDATATYPE;

	static final List<NeoProp> eenumProps = List.of(new NeoProp(NAME_PROP, EENUM), neocoreNamespaceProp);
	static final List<String> eenumLabels = LABELS_FOR_AN_ECLASS;

	static final List<NeoProp> eenumLiteralProps = List.of(new NeoProp(NAME_PROP, EENUM_LITERAL), neocoreNamespaceProp);
	static final List<String> eenumLiteralLabels = LABELS_FOR_AN_ECLASS;

	private static final List<NeoProp> eLiteralsProps = List.of(new NeoProp(NAME_PROP, ELITERALS));
	private static final List<String> eLiteralsLabels = LABELS_FOR_AN_EREFERENCE;

	public void bootstrapNeoCore(NeoCoreBuilder builder) {
		builder.executeActionAsCreateTransaction((cb) -> {
			var neocore = cb.createNode(neoCoreProps, neoCoreLabels);
			var eclass = cb.createNodeWithCont(eclassProps, eclassLabels, neocore);
			var mmodel = cb.createNodeWithContAndType(mmodelProps, mmodelLabels, eclass, neocore);
			var model = cb.createNodeWithContAndType(modelProps, modelLabels, eclass, neocore);
			var eobject = cb.createNodeWithContAndType(eobjectProps, eobjectLabels, eclass, neocore);
			var eref = cb.createNodeWithContAndType(erefProps, erefLabels, eclass, neocore);
			var eleof = cb.createNodeWithContAndType(eleofProps, eleofLabels, eref, neocore);
			var conformto = cb.createNodeWithContAndType(conformtoProps, conformtoLabels, eref, neocore);
			var erefs = cb.createNodeWithContAndType(erefsProps, erefsLabels, eref, neocore);
			var eRefType = cb.createNodeWithContAndType(eRefTypeProps, eRefTypeLabels, eref, neocore);
			var eattr = cb.createNodeWithContAndType(eattrProps, eattrLabels, eclass, neocore);
			var name = cb.createNodeWithContAndType(nameProps, nameLabels, eattr, neocore);
			var _type_ = cb.createNodeWithContAndType(_type_Props, _type_Labels, eattr, neocore);
			var _tr_ = cb.createNodeWithContAndType(_tr_Props, _tr_Labels, eattr, neocore);
			var _dlt_ = cb.createNodeWithContAndType(_dlt_Props, _dlt_Labels, eattr, neocore);
			var isComposition = cb.createNodeWithContAndType(isCompositionProps, isCompositionLabels, eattr, neocore);
			var isContainment = cb.createNodeWithContAndType(isContainmentProps, isContainmentLabels, eattr, neocore);
			var eDataType = cb.createNodeWithContAndType(eDataTypeProps, eDataTypeLabels, eclass, neocore);
			var eAttrEle = cb.createNodeWithContAndType(eAttrEleProps, eAttrEleLabels, eclass, neocore);
			var eString = cb.createNodeWithContAndType(eStringProps, eStringLabels, eDataType, neocore);
			@SuppressWarnings("unused")
			var eint = cb.createNodeWithContAndType(eintProps, eintLabels, eDataType, neocore);
			@SuppressWarnings("unused")
			var echar = cb.createNodeWithContAndType(echarProps, echarLabels, eDataType, neocore);
			@SuppressWarnings("unused")
			var elong = cb.createNodeWithContAndType(elongProps, elongLabels, eDataType, neocore);
			@SuppressWarnings("unused")
			var efloat = cb.createNodeWithContAndType(efloatProps, efloatLabels, eDataType, neocore);
			@SuppressWarnings("unused")
			var edate = cb.createNodeWithContAndType(edateProps, edateLabels, eDataType, neocore);
			@SuppressWarnings("unused")
			var edouble = cb.createNodeWithContAndType(edoubleProps, edoubleLabels, eDataType, neocore);
			var eAttrType = cb.createNodeWithContAndType(eAttrTypeProps, eAttrTypeLabels, eref, neocore);
			var eSupType = cb.createNodeWithContAndType(eSupTypeProps, eSupTypeLabels, eref, neocore);
			var eclassifier = cb.createNodeWithContAndType(eclassifierProps, eclassifierLabels, eclass, neocore);
			var eTypedele = cb.createNodeWithContAndType(eTypedeleProps, eTypedeleLabels, eclass, neocore);
			var metaType = cb.createNodeWithContAndType(metaTypeProps, metaTypeLabels, eref, neocore);
			var eAttributes = cb.createNodeWithContAndType(eAttributesProps, eAttributesLabels, eref, neocore);
			var eStruct = cb.createNodeWithContAndType(eStructProps, eStructLabels, eclass, neocore);
			var abstractattr = cb.createNodeWithContAndType(abstractattrProps, abstractattrLabels, eattr, neocore);
			var enamespaceattr = cb.createNodeWithContAndType(enamespaceProps, enamespaceLabels, eattr, neocore);
			var eBoolean = cb.createNodeWithContAndType(eBooleanProps, eBooleanLabels, eDataType, neocore);
			var eenum = cb.createNodeWithContAndType(eenumProps, eenumLabels, eclass, neocore);
			var eenumLiteral = cb.createNodeWithContAndType(eenumLiteralProps, eenumLabels, eclass, neocore);
			var eLiterals = cb.createNodeWithContAndType(eLiteralsProps, eLiteralsLabels, eref, neocore);
			var corr = cb.createNodeWithContAndType(corrProps, corrLabels, eref, neocore);
			var econtainer = cb.createNodeWithContAndType(econtainerProps, econtainerLabels, eref, neocore);

			cb.createEdge(CONFORMS_TO_PROP, neocore, neocore);
			cb.createEdge(META_TYPE, neocore, mmodel);
			cb.createEdge(META_TYPE, eclass, eclass);
			cb.createEdge(EREFERENCES, eclass, erefs);
			cb.createEdge(EREFERENCE_TYPE, erefs, eref);
			cb.createEdge(EREFERENCES, eref, eRefType);
			cb.createEdge(EREFERENCES, eclass, eSupType);
			cb.createEdge(EREFERENCE_TYPE, eSupType, eclass);
			cb.createEdge(EATTRIBUTES, eobject, name);
			cb.createEdge(EATTRIBUTE_TYPE, name, eString);
			cb.createEdge(EATTRIBUTES, corr, _type_);
			cb.createEdge(EATTRIBUTE_TYPE, _type_, eString);
			cb.createEdge(EATTRIBUTES, eobject, _tr_);
			cb.createEdge(EATTRIBUTE_TYPE, _tr_, eBoolean);
			cb.createEdge(EATTRIBUTES, corr, _dlt_);
			cb.createEdge(EATTRIBUTE_TYPE, _dlt_, eString);
			cb.createEdge(EATTRIBUTES, econtainer, isComposition);
			cb.createEdge(EATTRIBUTES, eref, isComposition);
			cb.createEdge(EATTRIBUTE_TYPE, isComposition, eBoolean);
			cb.createEdge(EATTRIBUTES, eref, isContainment);
			cb.createEdge(EATTRIBUTE_TYPE, isContainment, eBoolean);
			cb.createEdge(EREFERENCES, eattr, eAttrType);
			cb.createEdge(EREFERENCE_TYPE, eAttrType, eDataType);
			cb.createEdge(EREFERENCES, eobject, metaType);
			cb.createEdge(EREFERENCE_TYPE, metaType, eobject);
			cb.createEdge(EREFERENCES, eAttrEle, eAttributes);
			cb.createEdge(EREFERENCE_TYPE, eAttributes, eattr);
			cb.createEdge(EATTRIBUTES, eclass, abstractattr);
			cb.createEdge(EATTRIBUTE_TYPE, abstractattr, eBoolean);
			cb.createEdge(EATTRIBUTES, eclass, enamespaceattr);
			cb.createEdge(EATTRIBUTE_TYPE, enamespaceattr, eString);
			cb.createEdge(EREFERENCES, model, conformto);
			cb.createEdge(EREFERENCE_TYPE, conformto, mmodel);
			cb.createEdge(EREFERENCES, eobject, eleof);
			cb.createEdge(EREFERENCE_TYPE, eleof, model);
			cb.createEdge(EREFERENCES, eenum, eLiterals);
			cb.createEdge(EREFERENCE_TYPE, eLiterals, eenumLiteral);
			cb.createEdge(ESUPER_TYPE, eclass, eAttrEle);
			cb.createEdge(ESUPER_TYPE, eclass, eclassifier);
			cb.createEdge(ESUPER_TYPE, eDataType, eclassifier);
			cb.createEdge(ESUPER_TYPE, eref, eAttrEle);
			cb.createEdge(ESUPER_TYPE, eref, eStruct);
			cb.createEdge(ESUPER_TYPE, eattr, eStruct);
			cb.createEdge(ESUPER_TYPE, eenum, eDataType);
			cb.createEdge(ESUPER_TYPE, mmodel, model);
			cb.createEdge(ESUPER_TYPE, eStruct, eTypedele);
			cb.createEdge(ESUPER_TYPE, eTypedele, eobject);
			cb.createEdge(ESUPER_TYPE, eclassifier, eobject);
			cb.createEdge(ESUPER_TYPE, model, eobject);
			cb.createEdge(ESUPER_TYPE, eAttrEle, eobject);
			cb.createEdge(EREFERENCES, eobject, corr);
			cb.createEdge(EREFERENCE_TYPE, corr, eobject);
			cb.createEdge(EREFERENCES, eobject, econtainer);
			cb.createEdge(EREFERENCE_TYPE, econtainer, eobject);
		});
	}

	public static String addNeoCoreNamespace(String label) {
		var namespace = EMSLUtil.ORG_EMOFLON_NEO_CORE;
		return addNameSpace(namespace, label);
	}

	public static String addNameSpace(String namespace, String label) {
		return namespace + "__" + label;
	}

	public static List<String> addNeoCoreNamespace(String... labels) {
		var namespace = EMSLUtil.ORG_EMOFLON_NEO_CORE;
		return addNameSpace(namespace, labels);
	}

	public static List<String> addNameSpace(String namespace, String... labels) {
		return List.of(labels).stream()//
				.map(NeoCoreBootstrapper::addNeoCoreNamespace)//
				.distinct()//
				.collect(Collectors.toList());
	}
}
