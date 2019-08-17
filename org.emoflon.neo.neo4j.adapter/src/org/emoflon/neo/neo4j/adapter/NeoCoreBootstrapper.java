package org.emoflon.neo.neo4j.adapter;

import java.util.List;

import org.emoflon.neo.emsl.util.EMSLUtil;

public class NeoCoreBootstrapper {
	static final String META_TYPE = "metaType";
	static final String META_EL_OF = "elementOf";
	static final String CORR = "corr";
	static final String ECONTAINER = "eContainer";

	// EClasses
	static final String ECLASSIFIER = "EClassifier";
	static final String ECLASS = "EClass";
	static final String EATTRIBUTE = "EAttribute";
	static final String EREFERENCE = "EReference";
	static final String EDATA_TYPE = "EDataType";
	static final String ESTRUCTURAL_FEATURE = "EStructuralFeature";
	static final String ETYPED_ELEMENT = "ETypedElement";
	static final String EATTRIBUTED_ELEMENT = "EAttributedElement";
	static final String METAMODEL = "MetaModel";
	static final String MODEL = "Model";
	static final String EOBJECT = "EObject";

	// EReferences
	static final String EREFERENCE_TYPE = "eReferenceType";
	static final String EREFERENCES = "eReferences";
	static final String ESUPER_TYPE = "eSuperType";
	static final String EATTRIBUTE_TYPE = "eAttributeType";
	static final String EATTRIBUTES = "eAttributes";
	static final String ELITERALS = "eLiterals";

	// EDataType
	static final String EENUM = "EEnum";
	static final String EENUM_LITERAL = "EEnumLiteral";
	static final String ESTRING = "EString";
	static final String EINT = "EInt";
	static final String EBOOLEAN = "EBoolean";

	// Attributes
	static final String NAME_PROP = "ename";
	static final String ABSTRACT_PROP = "abstract";
	static final String _TYPE_PROP = "_type_";
	static final String ISCOMPOSITION_PROP = "isComposition";

	// Meta attributes and relations
	static final String CONFORMS_TO_PROP = "conformsTo";

	// Lists of properties and labels for meta types
	private static final List<String> LABELS_FOR_AN_ECLASS = //
			List.of(ECLASS, ECLASSIFIER, EATTRIBUTED_ELEMENT, EOBJECT);
	private static final List<String> LABELS_FOR_AN_EATTRIBUTE = //
			List.of(EATTRIBUTE, EOBJECT, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT);
	private static final List<String> LABELS_FOR_AN_EREFERENCE = //
			List.of(EREFERENCE, EATTRIBUTED_ELEMENT, ESTRUCTURAL_FEATURE, ETYPED_ELEMENT, EOBJECT);
	private static final List<String> LABELS_FOR_AN_EDATATYPE = //
			List.of(EDATA_TYPE, ECLASSIFIER, EOBJECT);

	static final List<NeoProp> neoCoreProps = List.of(new NeoProp(NAME_PROP, EMSLUtil.ORG_EMOFLON_NEO_CORE));
	static final List<String> neoCoreLabels = List.of(METAMODEL, MODEL, EOBJECT);

	static final List<NeoProp> eclassProps = List.of(new NeoProp(NAME_PROP, ECLASS));
	static final List<String> eclassLabels = LABELS_FOR_AN_ECLASS;

	static final List<NeoProp> mmodelProps = List.of(new NeoProp(NAME_PROP, METAMODEL));
	static final List<String> mmodelLabels = LABELS_FOR_AN_ECLASS;

	static final List<NeoProp> modelProps = List.of(new NeoProp(NAME_PROP, MODEL));
	static final List<String> modelLabels = LABELS_FOR_AN_ECLASS;

	static final List<NeoProp> eobjectProps = List.of(new NeoProp(NAME_PROP, EOBJECT));
	static final List<String> eobjectLabels = LABELS_FOR_AN_ECLASS;

	static final List<NeoProp> erefProps = List.of(new NeoProp(NAME_PROP, EREFERENCE));
	static final List<String> erefLabels = LABELS_FOR_AN_ECLASS;

	private static final List<NeoProp> eleofProps = List.of(new NeoProp(NAME_PROP, META_EL_OF));
	private static final List<String> eleofLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> conformtoProps = List.of(new NeoProp(NAME_PROP, CONFORMS_TO_PROP));
	private static final List<String> conformtoLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> erefsProps = List.of(new NeoProp(NAME_PROP, EREFERENCES));
	private static final List<String> erefsLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> eRefTypeProps = List.of(new NeoProp(NAME_PROP, EREFERENCE_TYPE));
	private static final List<String> eRefTypeLabels = LABELS_FOR_AN_EREFERENCE;

	static final List<NeoProp> eattrProps = List.of(new NeoProp(NAME_PROP, EATTRIBUTE));
	static final List<String> eattrLabels = LABELS_FOR_AN_ECLASS;

	private static final List<NeoProp> nameProps = List.of(new NeoProp(NAME_PROP, NAME_PROP));
	private static final List<String> nameLabels = LABELS_FOR_AN_EATTRIBUTE;

	private static final List<NeoProp> _type_Props = List.of(new NeoProp(NAME_PROP, _TYPE_PROP));
	private static final List<String> _type_Labels = LABELS_FOR_AN_EATTRIBUTE;

	private static final List<NeoProp> isCompositionProps = List.of(new NeoProp(NAME_PROP, ISCOMPOSITION_PROP));
	private static final List<String> isCompositionLabels = LABELS_FOR_AN_EATTRIBUTE;

	static final List<NeoProp> eDataTypeProps = List.of(new NeoProp(NAME_PROP, EDATA_TYPE));
	static final List<String> eDataTypeLabels = LABELS_FOR_AN_ECLASS;

	private static final List<NeoProp> eAttrEleProps = List.of(new NeoProp(NAME_PROP, EATTRIBUTED_ELEMENT));
	private static final List<String> eAttrEleLabels = LABELS_FOR_AN_ECLASS;

	private static final List<NeoProp> eStringProps = List.of(new NeoProp(NAME_PROP, ESTRING));
	private static final List<String> eStringLabels = LABELS_FOR_AN_EDATATYPE;

	private static final List<NeoProp> eintProps = List.of(new NeoProp(NAME_PROP, EINT));
	private static final List<String> eintLabels = LABELS_FOR_AN_EDATATYPE;

	private static final List<NeoProp> eAttrTypeProps = List.of(new NeoProp(NAME_PROP, EATTRIBUTE_TYPE));
	private static final List<String> eAttrTypeLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> eSupTypeProps = List.of(new NeoProp(NAME_PROP, ESUPER_TYPE));
	private static final List<String> eSupTypeLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> eclassifierProps = List.of(new NeoProp(NAME_PROP, ECLASSIFIER));
	private static final List<String> eclassifierLabels = LABELS_FOR_AN_ECLASS;

	private static final List<NeoProp> eTypedeleProps = List.of(new NeoProp(NAME_PROP, ETYPED_ELEMENT));
	private static final List<String> eTypedeleLabels = LABELS_FOR_AN_ECLASS;

	private static final List<NeoProp> metaTypeProps = List.of(new NeoProp(NAME_PROP, META_TYPE));
	private static final List<String> metaTypeLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> corrProps = List.of(new NeoProp(NAME_PROP, CORR));
	private static final List<String> corrLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> econtainerProps = List.of(new NeoProp(NAME_PROP, ECONTAINER));
	private static final List<String> econtainerLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> eAttributesProps = List.of(new NeoProp(NAME_PROP, EATTRIBUTES));
	private static final List<String> eAttributesLabels = LABELS_FOR_AN_EREFERENCE;

	private static final List<NeoProp> eStructProps = List.of(new NeoProp(NAME_PROP, ESTRUCTURAL_FEATURE));
	private static final List<String> eStructLabels = LABELS_FOR_AN_ECLASS;

	private static final List<NeoProp> abstractattrProps = List.of(new NeoProp(NAME_PROP, ABSTRACT_PROP));
	private static final List<String> abstractattrLabels = LABELS_FOR_AN_EATTRIBUTE;

	private static final List<NeoProp> eBooleanProps = List.of(new NeoProp(NAME_PROP, EBOOLEAN));
	private static final List<String> eBooleanLabels = LABELS_FOR_AN_EDATATYPE;

	static final List<NeoProp> eenumProps = List.of(new NeoProp(NAME_PROP, EENUM));
	static final List<String> eenumLabels = LABELS_FOR_AN_ECLASS;

	static final List<NeoProp> eenumLiteralProps = List.of(new NeoProp(NAME_PROP, EENUM_LITERAL));
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
			var isComposition = cb.createNodeWithContAndType(isCompositionProps, isCompositionLabels, eattr, neocore);
			var eDataType = cb.createNodeWithContAndType(eDataTypeProps, eDataTypeLabels, eclass, neocore);
			var eAttrEle = cb.createNodeWithContAndType(eAttrEleProps, eAttrEleLabels, eclass, neocore);
			var eString = cb.createNodeWithContAndType(eStringProps, eStringLabels, eDataType, neocore);
			@SuppressWarnings("unused")
			var eint = cb.createNodeWithContAndType(eintProps, eintLabels, eDataType, neocore);
			var eAttrType = cb.createNodeWithContAndType(eAttrTypeProps, eAttrTypeLabels, eref, neocore);
			var eSupType = cb.createNodeWithContAndType(eSupTypeProps, eSupTypeLabels, eref, neocore);
			var eclassifier = cb.createNodeWithContAndType(eclassifierProps, eclassifierLabels, eclass, neocore);
			var eTypedele = cb.createNodeWithContAndType(eTypedeleProps, eTypedeleLabels, eclass, neocore);
			var metaType = cb.createNodeWithContAndType(metaTypeProps, metaTypeLabels, eref, neocore);
			var eAttributes = cb.createNodeWithContAndType(eAttributesProps, eAttributesLabels, eref, neocore);
			var eStruct = cb.createNodeWithContAndType(eStructProps, eStructLabels, eclass, neocore);
			var abstractattr = cb.createNodeWithContAndType(abstractattrProps, abstractattrLabels, eattr, neocore);
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
			cb.createEdge(EATTRIBUTES, econtainer, isComposition);
			cb.createEdge(EATTRIBUTES, eref, isComposition);
			cb.createEdge(EATTRIBUTE_TYPE, isComposition, eBoolean);
			cb.createEdge(EREFERENCES, eattr, eAttrType);
			cb.createEdge(EREFERENCE_TYPE, eAttrType, eDataType);
			cb.createEdge(EREFERENCES, eobject, metaType);
			cb.createEdge(EREFERENCE_TYPE, metaType, eobject);
			cb.createEdge(EREFERENCES, eAttrEle, eAttributes);
			cb.createEdge(EREFERENCE_TYPE, eAttributes, eattr);
			cb.createEdge(EATTRIBUTES, eclass, abstractattr);
			cb.createEdge(EATTRIBUTE_TYPE, abstractattr, eBoolean);
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
}
