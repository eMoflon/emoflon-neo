package org.emoflon.neo.neocore;

import org.emoflon.neo.cypher.models.NeoCoreBootstrapper;
import org.emoflon.neo.emsl.eMSL.BuiltInDataTypes;
import org.emoflon.neo.emsl.eMSL.DataType;
import org.emoflon.neo.emsl.eMSL.EMSLFactory;
import org.emoflon.neo.emsl.eMSL.Metamodel;
import org.emoflon.neo.emsl.eMSL.MetamodelNodeBlock;
import org.emoflon.neo.emsl.eMSL.MetamodelPropertyStatement;
import org.emoflon.neo.emsl.eMSL.MetamodelRelationStatement;
import org.emoflon.neo.emsl.eMSL.RelationKind;
import org.emoflon.neo.emsl.util.EMSLUtil;

public class PreProcessorUtil {
	private static PreProcessorUtil instance;

	public PreProcessorUtil instance() {
		if (instance == null)
			instance = new PreProcessorUtil();
		return instance;
	}

	// ----------

	private Metamodel neoCore;

	private MetamodelNodeBlock eClass;
	private MetamodelNodeBlock eReference;
	private MetamodelNodeBlock eAttributedElement;
	private MetamodelNodeBlock eAttribute;
	private MetamodelNodeBlock eDataType;
	private MetamodelNodeBlock eStructuralFeature;
	private MetamodelNodeBlock eTypedElement;
	private MetamodelNodeBlock eClassifier;
	private MetamodelNodeBlock eObject;
	private MetamodelNodeBlock model;
	private MetamodelNodeBlock metaModel;

	private MetamodelPropertyStatement enamespace;
	private MetamodelPropertyStatement isContainment;
	private MetamodelPropertyStatement isComposition;
	private MetamodelPropertyStatement ename;
	private MetamodelPropertyStatement _tr_;
	private MetamodelPropertyStatement _type_;

	private MetamodelRelationStatement eReferences;
	private MetamodelRelationStatement eSuperType;
	private MetamodelRelationStatement eReferenceType;
	private MetamodelRelationStatement eAttributes;
	private MetamodelRelationStatement eAttributeType;
	private MetamodelRelationStatement elementOf;
	private MetamodelRelationStatement metaType;
	private MetamodelRelationStatement corr;
	private MetamodelRelationStatement conformsTo;

	public Metamodel neoCore() {
		if (neoCore == null) {
			neoCore = EMSLFactory.eINSTANCE.createMetamodel();
			neoCore.setName(EMSLUtil.ORG_EMOFLON_NEO_CORE);
			neoCore.getNodeBlocks().add(eClass());
			neoCore.getNodeBlocks().add(eReference());
			neoCore.getNodeBlocks().add(eAttributedElement());
			neoCore.getNodeBlocks().add(eAttribute());
			neoCore.getNodeBlocks().add(eDataType());
			neoCore.getNodeBlocks().add(eStructuralFeature());
			neoCore.getNodeBlocks().add(eTypedElement());
			neoCore.getNodeBlocks().add(eClassifier());
			neoCore.getNodeBlocks().add(eObject());
			neoCore.getNodeBlocks().add(model());
			neoCore.getNodeBlocks().add(metaModel());
		}

		return neoCore;
	}

	public MetamodelNodeBlock eClass() {
		if (eClass == null) {
			eClass = EMSLFactory.eINSTANCE.createMetamodelNodeBlock();
			eClass.setName(NeoCoreBootstrapper.ECLASS);
			eClass.getSuperTypes().add(eClassifier());
			eClass.getSuperTypes().add(eAttributedElement());
			eClass.getProperties().add(enamespace());
			eClass.getRelations().add(eReferences());
			eClass.getRelations().add(eSuperType());
		}

		return eClass;
	}

	public MetamodelNodeBlock eReference() {
		if (eReference == null) {
			eReference = EMSLFactory.eINSTANCE.createMetamodelNodeBlock();
			eReference.setName(NeoCoreBootstrapper.EREFERENCE);
			eReference.getSuperTypes().add(eStructuralFeature());
			eReference.getSuperTypes().add(eAttributedElement());
			eReference.getProperties().add(isContainment());
			eReference.getProperties().add(isComposition());
			eReference.getRelations().add(eReferenceType());
		}

		return eReference;
	}

	public MetamodelNodeBlock eAttributedElement() {
		if (eAttributedElement == null) {
			eAttributedElement = EMSLFactory.eINSTANCE.createMetamodelNodeBlock();
			eAttributedElement.setName(NeoCoreBootstrapper.EATTRIBUTED_ELEMENT);
			eAttributedElement.getSuperTypes().add(eObject());
			eAttributedElement.getRelations().add(eAttributes());
		}

		return eAttributedElement;
	}

	public MetamodelNodeBlock eAttribute() {
		if (eAttribute == null) {
			eAttribute = EMSLFactory.eINSTANCE.createMetamodelNodeBlock();
			eAttribute.setName(NeoCoreBootstrapper.EATTRIBUTE);
			eAttribute.getSuperTypes().add(eStructuralFeature());
			eAttribute.getRelations().add(eAttributeType());
		}

		return eAttribute;
	}

	public MetamodelNodeBlock eDataType() {
		if (eDataType == null) {
			eDataType = EMSLFactory.eINSTANCE.createMetamodelNodeBlock();
			eDataType.setName(NeoCoreBootstrapper.EDATA_TYPE);
			eDataType.getSuperTypes().add(eClassifier());
		}

		return eDataType;
	}

	public MetamodelNodeBlock eStructuralFeature() {
		if (eStructuralFeature == null) {
			eStructuralFeature = EMSLFactory.eINSTANCE.createMetamodelNodeBlock();
			eStructuralFeature.setName(NeoCoreBootstrapper.ESTRUCTURAL_FEATURE);
			eStructuralFeature.getSuperTypes().add(eTypedElement());
		}

		return eStructuralFeature;
	}

	public MetamodelNodeBlock eTypedElement() {
		if (eTypedElement == null) {
			eTypedElement = EMSLFactory.eINSTANCE.createMetamodelNodeBlock();
			eTypedElement.setName(NeoCoreBootstrapper.ETYPED_ELEMENT);
			eTypedElement.getSuperTypes().add(eObject());
		}

		return eTypedElement;
	}

	public MetamodelNodeBlock eClassifier() {
		if (eClassifier == null) {
			eClassifier = EMSLFactory.eINSTANCE.createMetamodelNodeBlock();
			eClassifier.setName(NeoCoreBootstrapper.ECLASSIFIER);
			eClassifier.getSuperTypes().add(eObject());
		}

		return eClassifier;
	}

	public MetamodelNodeBlock eObject() {
		if (eObject == null) {
			eObject = EMSLFactory.eINSTANCE.createMetamodelNodeBlock();
			eObject.setName(NeoCoreBootstrapper.EOBJECT);
			eObject.getProperties().add(ename());
			eObject.getProperties().add(_tr_());
			eObject.getRelations().add(elementOf());
			eObject.getRelations().add(metaType());
			eObject.getRelations().add(corr());
		}

		return eObject;
	}

	public MetamodelNodeBlock model() {
		if (model == null) {
			model = EMSLFactory.eINSTANCE.createMetamodelNodeBlock();
			model.setName(NeoCoreBootstrapper.MODEL);
			model.getSuperTypes().add(eObject());
			model.getRelations().add(conformsTo());
		}

		return model;
	}

	public MetamodelNodeBlock metaModel() {
		if (metaModel == null) {
			metaModel = EMSLFactory.eINSTANCE.createMetamodelNodeBlock();
			metaModel.setName(NeoCoreBootstrapper.METAMODEL);
			metaModel.getSuperTypes().add(model());
		}

		return metaModel;
	}

	public MetamodelPropertyStatement enamespace() {
		if (enamespace == null) {
			enamespace = EMSLFactory.eINSTANCE.createMetamodelPropertyStatement();
			enamespace.setName(NeoCoreBootstrapper.NAMESPACE_PROP);
			enamespace.setType(eString());
		}

		return enamespace;
	}

	public MetamodelPropertyStatement isContainment() {
		if (isContainment == null) {
			isContainment = EMSLFactory.eINSTANCE.createMetamodelPropertyStatement();
			isContainment.setName(NeoCoreBootstrapper.ISCONTAINMENT_PROP);
			isContainment.setType(eBoolean());
		}

		return isContainment;
	}

	public MetamodelPropertyStatement isComposition() {
		if (isComposition == null) {
			isComposition = EMSLFactory.eINSTANCE.createMetamodelPropertyStatement();
			isComposition.setName(NeoCoreBootstrapper.ISCOMPOSITION_PROP);
			isComposition.setType(eBoolean());
		}

		return isComposition;
	}

	public MetamodelPropertyStatement ename() {
		if (ename == null) {
			ename = EMSLFactory.eINSTANCE.createMetamodelPropertyStatement();
			ename.setName(NeoCoreBootstrapper.NAME_PROP);
			ename.setType(eString());
		}

		return ename;
	}

	public MetamodelPropertyStatement _tr_() {
		if (_tr_ == null) {
			_tr_ = EMSLFactory.eINSTANCE.createMetamodelPropertyStatement();
			_tr_.setName(NeoCoreBootstrapper._TR_PROP);
			_tr_.setType(eBoolean());
		}

		return _tr_;
	}

	public MetamodelPropertyStatement _type_() {
		if (_type_ == null) {
			_type_ = EMSLFactory.eINSTANCE.createMetamodelPropertyStatement();
			_type_.setName(NeoCoreBootstrapper._TYPE_PROP);
			_type_.setType(eString());
		}

		return _type_;
	}

	public MetamodelRelationStatement eReferences() {
		if (eReferences == null) {
			eReferences = EMSLFactory.eINSTANCE.createMetamodelRelationStatement();
			eReferences.setKind(RelationKind.REFERENCE);
			eReferences.setName(NeoCoreBootstrapper.EREFERENCES);
			eReferences.setLower("0");
			eReferences.setUpper("*");
			eReferences.setTarget(eReference());
		}

		return eReferences;
	}

	public MetamodelRelationStatement eSuperType() {
		if (eSuperType == null) {
			eSuperType = EMSLFactory.eINSTANCE.createMetamodelRelationStatement();
			eSuperType.setKind(RelationKind.REFERENCE);
			eSuperType.setName(NeoCoreBootstrapper.ESUPER_TYPE);
			eSuperType.setLower("0");
			eSuperType.setUpper("*");
			eSuperType.setTarget(eClass());
		}

		return eSuperType;
	}

	public MetamodelRelationStatement eReferenceType() {
		if (eReferenceType == null) {
			eReferenceType = EMSLFactory.eINSTANCE.createMetamodelRelationStatement();
			eReferenceType.setKind(RelationKind.REFERENCE);
			eReferenceType.setName(NeoCoreBootstrapper.EREFERENCE_TYPE);
			eReferenceType.setLower("1");
			eReferenceType.setUpper("1");
			eReferenceType.setTarget(eClass());
		}

		return eReferenceType;
	}

	public MetamodelRelationStatement eAttributes() {
		if (eAttributes == null) {
			eAttributes = EMSLFactory.eINSTANCE.createMetamodelRelationStatement();
			eAttributes.setKind(RelationKind.REFERENCE);
			eAttributes.setName(NeoCoreBootstrapper.EATTRIBUTES);
			eAttributes.setLower("0");
			eAttributes.setUpper("*");
			eAttributes.setTarget(eAttribute());
		}

		return eAttributes;
	}

	public MetamodelRelationStatement eAttributeType() {
		if (eAttributeType == null) {
			eAttributeType = EMSLFactory.eINSTANCE.createMetamodelRelationStatement();
			eAttributeType.setKind(RelationKind.REFERENCE);
			eAttributeType.setName(NeoCoreBootstrapper.EATTRIBUTE_TYPE);
			eAttributeType.setLower("1");
			eAttributeType.setUpper("1");
			eAttributeType.setTarget(eDataType());
		}

		return eAttributeType;
	}

	public MetamodelRelationStatement elementOf() {
		if (elementOf == null) {
			elementOf = EMSLFactory.eINSTANCE.createMetamodelRelationStatement();
			elementOf.setKind(RelationKind.REFERENCE);
			elementOf.setName(NeoCoreBootstrapper.META_EL_OF);
			elementOf.setLower("1");
			elementOf.setUpper("1");
			elementOf.setTarget(model());
		}

		return elementOf;
	}

	public MetamodelRelationStatement metaType() {
		if (metaType == null) {
			metaType = EMSLFactory.eINSTANCE.createMetamodelRelationStatement();
			metaType.setKind(RelationKind.REFERENCE);
			metaType.setName(NeoCoreBootstrapper.META_TYPE);
			metaType.setLower("1");
			metaType.setUpper("1");
			metaType.setTarget(eObject());
		}

		return metaType;
	}

	public MetamodelRelationStatement corr() {
		if (corr == null) {
			corr = EMSLFactory.eINSTANCE.createMetamodelRelationStatement();
			corr.setKind(RelationKind.REFERENCE);
			corr.setName(NeoCoreBootstrapper.CORR);
			corr.setLower("0");
			corr.setUpper("*");
			corr.setTarget(eObject());
			corr.getProperties().add(_type_());
		}

		return corr;
	}

	public MetamodelRelationStatement conformsTo() {
		if (conformsTo == null) {
			conformsTo = EMSLFactory.eINSTANCE.createMetamodelRelationStatement();
			conformsTo.setKind(RelationKind.REFERENCE);
			conformsTo.setName(NeoCoreBootstrapper.CONFORMS_TO_PROP);
			conformsTo.setLower("1");
			conformsTo.setUpper("1");
			conformsTo.setTarget(metaModel());
		}

		return conformsTo;
	}

	private DataType eString() {
		var eString = EMSLFactory.eINSTANCE.createBuiltInType();
		eString.setReference(BuiltInDataTypes.ESTRING);
		return eString;
	}

	private DataType eBoolean() {
		var eString = EMSLFactory.eINSTANCE.createBuiltInType();
		eString.setReference(BuiltInDataTypes.EBOOLEAN);
		return eString;
	}
}
