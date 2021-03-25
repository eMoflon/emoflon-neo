package org.emoflon.ibex.neo.benchmark.exttype2doc;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.emoflon.ibex.neo.benchmark.ModelAndDeltaGenerator;
import org.emoflon.ibex.neo.benchmark.util.BenchParameters;

import ExtDocModel.Annotation;
import ExtDocModel.Doc;
import ExtDocModel.DocContainer;
import ExtDocModel.Entry;
import ExtDocModel.EntryType;
import ExtDocModel.ExtDocModelFactory;
import ExtDocModel.ExtDocModelPackage;
import ExtDocModel.Folder;
import ExtDocModel.Glossary;
import ExtDocModel.GlossaryEntry;
import ExtTypeModel.ExtTypeModelFactory;
import ExtTypeModel.ExtTypeModelPackage;
import ExtTypeModel.Field;
import ExtTypeModel.JavaDoc;
import ExtTypeModel.Method;
import ExtTypeModel.Package;
import ExtTypeModel.Parameter;
import ExtTypeModel.Project;
import ExtTypeModel.Type;
import delta.Delta;

public abstract class ExtType2Doc_MDGenerator<CF extends EFactory, BP extends BenchParameters>
		extends ModelAndDeltaGenerator<CF, ExtTypeModelFactory, ExtTypeModelPackage, ExtDocModelFactory, ExtDocModelPackage, BP> {

	//// SRC ////
	protected Project sContainer;
	protected Map<String, Package> name2package;
	protected Map<String, Type> name2type;
	protected Map<String, Method> name2method;
	protected Map<String, Field> name2field;
	protected Map<String, Parameter> name2param;
	protected Map<String, JavaDoc> name2javadoc;

	protected Map<EObject, EObject> src2corr;

	//// TRG ////
	protected DocContainer tContainer;
	protected Map<String, Folder> name2folder;
	protected Map<String, Doc> name2doc;
	protected Map<String, Entry> name2entry;
	protected Map<String, Annotation> name2annotation;
	protected Map<String, GlossaryEntry> name2glossaryEntry;

	public ExtType2Doc_MDGenerator(Resource source, Resource target, Resource corr, Resource protocol, Resource delta) {
		super(source, target, corr, protocol, delta);
	}

	@Override
	protected ExtTypeModelPackage srcPackageInstance() {
		return ExtTypeModelPackage.eINSTANCE;
	}

	@Override
	protected ExtDocModelPackage trgPackageInstance() {
		return ExtDocModelPackage.eINSTANCE;
	}

	@Override
	protected void clearAll() {
		sContainer = null;
		name2package = new HashMap<>();
		name2type = new HashMap<>();
		name2method = new HashMap<>();
		name2field = new HashMap<>();
		name2param = new HashMap<>();
		name2javadoc = new HashMap<>();

		src2corr = new HashMap<>();

		tContainer = null;
		name2folder = new HashMap<>();
		name2doc = new HashMap<>();
		name2entry = new HashMap<>();
		name2annotation = new HashMap<>();
		name2glossaryEntry = new HashMap<>();
	}

	//// MODEL ////

	@Override
	protected <Corr extends EObject> Corr createCorr(Corr corr, EObject src, EObject trg) {
		super.createCorr(corr, src, trg);
		src2corr.put(src, corr);
		return corr;
	}

	protected void createProject() {
		sContainer = sFactory.createProject();
		source.getContents().add(sContainer);
		numOfElements++;
	}

	protected void createDocContainer() {
		tContainer = tFactory.createDocContainer();
		target.getContents().add(tContainer);
		numOfElements++;
	}

	protected Glossary createGlossary() {
		Glossary g = tFactory.createGlossary();
		tContainer.setGlossary(g);
		numOfElements++;
		return g;
	}

	protected Package createRootPackage(String postfix) {
		Package p = sFactory.createPackage();
		p.setName("Package" + postfix);
		p.setProject(sContainer);
		name2package.put(p.getName(), p);
		numOfElements++;
		return p;
	}

	protected Package createPackage(String postfix, Package superPackage) {
		Package p = sFactory.createPackage();
		p.setName("Package" + postfix);
		p.setSuperPackage(superPackage);
		name2package.put(p.getName(), p);
		numOfElements++;
		return p;
	}

	protected Folder createFolder(String postfix) {
		Folder f = tFactory.createFolder();
		f.setName("Package" + postfix);
		f.setContainer(tContainer);
		name2folder.put(f.getName(), f);
		numOfElements++;
		return f;
	}

	protected Type createType(String postfix, boolean isInterface, Package p) {
		Type t = sFactory.createType();
		t.setName("Type" + postfix);
		t.setInterface(isInterface);
		t.setPackage(p);
		name2type.put(t.getName(), t);
		numOfElements++;
		return t;
	}

	protected Doc createDoc(String postfix, Folder f) {
		Doc d = tFactory.createDoc();
		d.setName("Type" + postfix);
		d.setFolder(f);
		name2doc.put(d.getName(), d);
		numOfElements++;
		return d;
	}

	protected void createTypeInheritance(Type extendee, Type extender) {
		extendee.getExtendedBy().add(extender);
	}

	protected void createDocLink(Doc superDoc, Doc subDoc) {
		superDoc.getSubDocs().add(subDoc);
	}

	protected Method createMethod(String postfix, Type t) {
		Method m = sFactory.createMethod();
		m.setName("Method" + postfix);
		m.setType(t);
		name2method.put(m.getName(), m);
		numOfElements++;
		return m;
	}

	protected Field createField(String postfix, Type t) {
		Field f = sFactory.createField();
		f.setName("Field" + postfix);
		f.setType(t);
		name2field.put(f.getName(), f);
		numOfElements++;
		return f;
	}

	protected Entry createEntry(String postfix, EntryType entryType, Doc d) {
		Entry e = tFactory.createEntry();
		String name = entryType == EntryType.METHOD ? "Method" : "Field";
		e.setName(name + postfix);
		e.setType(entryType);
		e.setDoc(d);
		name2entry.put(e.getName(), e);
		numOfElements++;
		return e;
	}

	protected Parameter createParameter(String postfix, Method m) {
		Parameter p = sFactory.createParameter();
		p.setName("Param" + postfix);
		p.setMethod(m);
		name2param.put(p.getName(), p);
		numOfElements++;
		return p;
	}

	protected JavaDoc createJavaDoc(String postfix, Method m) {
		JavaDoc jd = sFactory.createJavaDoc();
		jd.setComment("JavaDoc" + postfix);
		jd.setMethod(m);
		name2javadoc.put(jd.getComment(), jd);
		numOfElements++;
		return jd;
	}

	protected Annotation createAnnotation(String postfix, Entry e) {
		Annotation a = tFactory.createAnnotation();
		a.setValue("JavaDoc" + postfix);
		a.setEntry(e);
		name2annotation.put(a.getValue(), a);
		numOfElements++;
		return a;
	}

	protected GlossaryEntry createGlossaryEntry(String postfix) {
		GlossaryEntry ge = tFactory.createGlossaryEntry();
		ge.setName("GlossaryEntry" + postfix);
		ge.setGlossary(tContainer.getGlossary());
		name2glossaryEntry.put(ge.getName(), ge);
		numOfElements++;
		return ge;
	}

	protected void createGlossaryLink(Entry entry, GlossaryEntry glossaryEntry) {
		entry.getGlossaryEntries().add(glossaryEntry);
	}

	//// DELTA ////

	protected void deleteType(Type type, Delta delta) {
		type.getMethods().forEach(m -> deleteMethod(m, delta));
		type.getFields().forEach(f -> deleteField(f, delta));

		deleteObject(type, delta);
		deleteLink(type.getPackage(), type, sPackage.getPackage_Types(), delta);
		for (Type subT : type.getExtendedBy())
			deleteLink(type, subT, sPackage.getType_ExtendedBy(), delta);
	}

	protected void deleteMethod(Method method, Delta delta) {
		method.getParams().forEach(p -> deleteParameter(p, delta));
		method.getDocs().forEach(jd -> deleteJavaDoc(jd, delta));

		deleteObject(method, delta);
		deleteLink(method.getType(), method, sPackage.getType_Methods(), delta);
	}

	protected void deleteField(Field field, Delta delta) {
		deleteObject(field, delta);
		deleteLink(field.getType(), field, sPackage.getType_Fields(), delta);
	}

	protected void deleteParameter(Parameter parameter, Delta delta) {
		deleteObject(parameter, delta);
		deleteLink(parameter.getMethod(), parameter, sPackage.getMethod_Params(), delta);
	}

	protected void deleteJavaDoc(JavaDoc javaDoc, Delta delta) {
		deleteObject(javaDoc, delta);
		deleteLink(javaDoc.getMethod(), javaDoc, sPackage.getMethod_Docs(), delta);
	}

}
