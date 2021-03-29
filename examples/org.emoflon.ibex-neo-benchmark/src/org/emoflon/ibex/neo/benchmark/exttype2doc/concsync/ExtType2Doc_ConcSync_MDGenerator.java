package org.emoflon.ibex.neo.benchmark.exttype2doc.concsync;

import java.util.LinkedList;
import java.util.List;
import java.util.function.BiConsumer;

import org.eclipse.emf.ecore.resource.Resource;
import org.emoflon.ibex.neo.benchmark.exttype2doc.ExtType2Doc_MDGenerator;

import ExtDocModel.Annotation;
import ExtDocModel.Doc;
import ExtDocModel.Entry;
import ExtDocModel.EntryType;
import ExtDocModel.Folder;
import ExtDocModel.Glossary;
import ExtDocModel.GlossaryEntry;
import ExtType2Doc_ConcSync.ExtType2Doc_ConcSyncFactory;
import ExtType2Doc_ConcSync.ExtendingType2Doc__Marker;
import ExtType2Doc_ConcSync.Field2Entry;
import ExtType2Doc_ConcSync.Field2Entry__Marker;
import ExtType2Doc_ConcSync.GlossaryEntry__Marker;
import ExtType2Doc_ConcSync.GlossaryLink__Marker;
import ExtType2Doc_ConcSync.Glossary__Marker;
import ExtType2Doc_ConcSync.JDoc2Annotation;
import ExtType2Doc_ConcSync.JDoc2Annotation__Marker;
import ExtType2Doc_ConcSync.Method2Entry;
import ExtType2Doc_ConcSync.Method2Entry__Marker;
import ExtType2Doc_ConcSync.Package2Folder;
import ExtType2Doc_ConcSync.Package2Folder__Marker;
import ExtType2Doc_ConcSync.Param2Entry;
import ExtType2Doc_ConcSync.Param2Entry__Marker;
import ExtType2Doc_ConcSync.Project2DocCont__Marker;
import ExtType2Doc_ConcSync.Project2DocContainer;
import ExtType2Doc_ConcSync.Type2Doc;
import ExtType2Doc_ConcSync.Type2Doc__Marker;
import ExtTypeModel.Field;
import ExtTypeModel.JavaDoc;
import ExtTypeModel.Method;
import ExtTypeModel.Package;
import ExtTypeModel.Parameter;
import ExtTypeModel.Type;
import delta.Delta;

public class ExtType2Doc_ConcSync_MDGenerator extends ExtType2Doc_MDGenerator<ExtType2Doc_ConcSyncFactory, ExtType2Doc_ConcSync_Params> {

	private Package rootPackage;
	private Folder rootFolder;

	private List<Type> rootTypes;

	private int glossaryLinkCounter;

	public ExtType2Doc_ConcSync_MDGenerator(Resource source, Resource target, Resource corr, Resource protocol, Resource delta) {
		super(source, target, corr, protocol, delta);
	}

	@Override
	protected ExtType2Doc_ConcSyncFactory corrFactoryInstance() {
		return ExtType2Doc_ConcSyncFactory.eINSTANCE;
	}

	//// MODEL ////

	@Override
	protected void genModels() {
		rootTypes = new LinkedList<>();
		glossaryLinkCounter = 0;

		createContainers();
		createGlossaryEntries();
		createTypeAndDocHierarchies();
	}

	private void createContainers() {
		// SRC
		createProject();
		// TRG
		createDocContainer();
		// CORR
		Project2DocContainer pr2dc = createCorr(cFactory.createProject2DocContainer(), sContainer, tContainer);
		// MARKER
		Project2DocCont__Marker marker0 = cFactory.createProject2DocCont__Marker();
		marker0.setCREATE__SRC__pr(sContainer);
		marker0.setCREATE__CORR__pr2dc(pr2dc);
		marker0.setCREATE__TRG__dc(tContainer);
		protocol.getContents().add(marker0);

		// TRG
		Glossary g = createGlossary();
		// MARKER
		Glossary__Marker marker1 = cFactory.createGlossary__Marker();
		marker1.setCONTEXT__TRG__dc(tContainer);
		marker1.setCREATE__TRG__g(g);
		protocol.getContents().add(marker1);

		createRootPackageAndFolder();
	}

	private void createRootPackageAndFolder() {
		// SRC
		rootPackage = createRootPackage("");
		// TRG
		rootFolder = createFolder("");
		// CORR
		Package2Folder p2f = createCorr(cFactory.createPackage2Folder(), rootPackage, rootFolder);
		// MARKER
		Package2Folder__Marker marker = cFactory.createPackage2Folder__Marker();
		marker.setCONTEXT__SRC__pr(sContainer);
		marker.setCONTEXT__CORR__pr2dc((Project2DocContainer) src2corr.get(sContainer));
		marker.setCONTEXT__TRG__dc(tContainer);
		marker.setCREATE__SRC__p(rootPackage);
		marker.setCREATE__CORR__p2f(p2f);
		marker.setCREATE__TRG__f(rootFolder);
		protocol.getContents().add(marker);
	}

	private void createTypeAndDocHierarchies() {
		for (int i = 0; i < parameters.num_of_root_types; i++)
			createRootTypeAndDoc(i);
	}

	private void createRootTypeAndDoc(int index) {
		String postfix = SEP + index;

		// SRC
		Type t = createType(postfix, false, rootPackage);
		rootTypes.add(t);
		// TRG
		Doc d = createDoc(postfix, rootFolder);
		// CORR
		Type2Doc t2d = createCorr(cFactory.createType2Doc(), t, d);
		// MARKER
		Type2Doc__Marker marker = cFactory.createType2Doc__Marker();
		marker.setCONTEXT__SRC__p(rootPackage);
		marker.setCONTEXT__CORR__p2f((Package2Folder) src2corr.get(rootPackage));
		marker.setCONTEXT__TRG__f(rootFolder);
		marker.setCREATE__SRC__t(t);
		marker.setCREATE__CORR__t2d(t2d);
		marker.setCREATE__TRG__d(d);
		protocol.getContents().add(marker);

		createMethodsAndEntries(t, d, postfix);
		createFieldsAndEntries(t, d, postfix);

		createTypeAndDocHierarchy(t, d, 1, postfix);
	}

	private void createTypeAndDocHierarchy(Type rootT, Doc rootD, int currentDepth, String oldPostfix) {
		if (currentDepth >= parameters.inheritance_depth)
			return;

		for (int i = 0; i < parameters.horizontal_inheritance_scale; i++)
			createTypeAndDocInheritance(rootT, rootD, currentDepth, oldPostfix, i);
	}

	private void createTypeAndDocInheritance(Type superT, Doc superD, int currentDepth, String oldPostfix, int index) {
		String postfix = oldPostfix + SEP + index;

		// SRC
		Type t = createType(postfix, false, rootPackage);
		createTypeInheritance(superT, t);
		// TRG
		Doc d = createDoc(postfix, rootFolder);
		createDocLink(superD, d);
		// CORR
		Type2Doc t2d = createCorr(cFactory.createType2Doc(), t, d);
		// MARKER
		ExtendingType2Doc__Marker marker = cFactory.createExtendingType2Doc__Marker();
		marker.setCONTEXT__SRC__p(rootPackage);
		marker.setCONTEXT__CORR__p2f((Package2Folder) src2corr.get(rootPackage));
		marker.setCONTEXT__TRG__f(rootFolder);
		marker.setCONTEXT__SRC__t(superT);
		marker.setCONTEXT__CORR__t2d((Type2Doc) src2corr.get(superT));
		marker.setCONTEXT__TRG__d(superD);
		marker.setCREATE__SRC__nt(t);
		marker.setCREATE__CORR__nt2nd(t2d);
		marker.setCREATE__TRG__nd(d);
		protocol.getContents().add(marker);

		createMethodsAndEntries(t, d, postfix);
		createFieldsAndEntries(t, d, postfix);

		switch (parameters.scaleOrientation) {
		case HORIZONTAL:
			createTypeAndDocHierarchy(t, d, currentDepth + 1, postfix);
			break;
		case VERTICAL:
			if (index == 0)
				createTypeAndDocHierarchy(t, d, currentDepth + 1, postfix);
			break;
		default:
			break;
		}
	}

	private void createMethodsAndEntries(Type t, Doc d, String oldPostfix) {
		for (int i = 0; i < parameters.num_of_methods; i++)
			createMethodAndEntry(t, d, oldPostfix, i);
	}

	private void createMethodAndEntry(Type t, Doc d, String oldPostfix, int index) {
		String postfix = oldPostfix + SEP + index;

		// SRC
		Method m = createMethod(postfix, t);
		// TRG
		Entry e = createEntry(postfix, EntryType.METHOD, d);
		// CORR
		Method2Entry m2e = createCorr(cFactory.createMethod2Entry(), m, e);
		// MARKER
		Method2Entry__Marker marker = cFactory.createMethod2Entry__Marker();
		marker.setCONTEXT__SRC__t(t);
		marker.setCONTEXT__CORR__t2d((Type2Doc) src2corr.get(t));
		marker.setCONTEXT__TRG__d(d);
		marker.setCREATE__SRC__m(m);
		marker.setCREATE__CORR__m2e(m2e);
		marker.setCREATE__TRG__e(e);
		protocol.getContents().add(marker);

		createParameters(m, e, postfix);
		createJavaDocsAndAnnotations(m, e, postfix);
		createGlossaryLinks(e);
	}

	private void createFieldsAndEntries(Type t, Doc d, String oldPostfix) {
		for (int i = 0; i < parameters.num_of_fields; i++)
			createFieldAndEntry(t, d, oldPostfix, i);
	}

	private void createFieldAndEntry(Type t, Doc d, String oldPostfix, int index) {
		String postfix = oldPostfix + SEP + index;

		// SRC
		Field f = createField(postfix, t);
		// TRG
		Entry e = createEntry(postfix, EntryType.FIELD, d);
		// CORR
		Field2Entry f2e = createCorr(cFactory.createField2Entry(), f, e);
		// MARKER
		Field2Entry__Marker marker = cFactory.createField2Entry__Marker();
		marker.setCONTEXT__SRC__t(t);
		marker.setCONTEXT__CORR__t2d((Type2Doc) src2corr.get(t));
		marker.setCONTEXT__TRG__d(d);
		marker.setCREATE__SRC__f(f);
		marker.setCREATE__CORR__f2e(f2e);
		marker.setCREATE__TRG__e(e);
		protocol.getContents().add(marker);

		createGlossaryLinks(e);
	}

	private void createParameters(Method m, Entry e, String oldPostfix) {
		for (int i = 0; i < parameters.num_of_parameters; i++)
			createParameters(m, e, oldPostfix, i);
	}

	private void createParameters(Method m, Entry e, String oldPostfix, int index) {
		String postfix = oldPostfix + SEP + index;

		// SRC
		Parameter p = createParameter(postfix, m);
		// CORR
		Param2Entry p2e = createCorr(cFactory.createParam2Entry(), p, e);
		// MARKER
		Param2Entry__Marker marker = cFactory.createParam2Entry__Marker();
		marker.setCONTEXT__SRC__m(m);
		marker.setCONTEXT__CORR__m2e((Method2Entry) src2corr.get(m));
		marker.setCONTEXT__TRG__e(e);
		marker.setCREATE__SRC__p(p);
		marker.setCREATE__CORR__p2e(p2e);
		protocol.getContents().add(marker);
	}

	private void createJavaDocsAndAnnotations(Method m, Entry e, String oldPostfix) {
		for (int i = 0; i < parameters.num_of_javadocs; i++)
			createJavaDocAndAnnotation(m, e, oldPostfix, i);
	}

	private void createJavaDocAndAnnotation(Method m, Entry e, String oldPostfix, int index) {
		String postfix = oldPostfix + SEP + index;

		// SRC
		JavaDoc jd = createJavaDoc(postfix, m);
		// TRG
		Annotation a = createAnnotation(postfix, e);
		// CORR
		JDoc2Annotation jd2a = createCorr(cFactory.createJDoc2Annotation(), jd, a);
		// MARKER
		JDoc2Annotation__Marker marker = cFactory.createJDoc2Annotation__Marker();
		marker.setCONTEXT__SRC__m(m);
		marker.setCONTEXT__CORR__m2e((Method2Entry) src2corr.get(m));
		marker.setCONTEXT__TRG__e(e);
		marker.setCREATE__SRC__j(jd);
		marker.setCREATE__CORR__j2a(jd2a);
		marker.setCREATE__TRG__a(a);
		protocol.getContents().add(marker);
	}

	private void createGlossaryEntries() {
		for (int i = 0; i < parameters.num_of_glossar_entries; i++)
			createGlossaryEntry(i);
	}

	private void createGlossaryEntry(int index) {
		String postfix = SEP + index;

		// TRG
		GlossaryEntry ge = createGlossaryEntry(postfix);
		// MARKER
		GlossaryEntry__Marker marker = cFactory.createGlossaryEntry__Marker();
		marker.setCONTEXT__TRG__g(tContainer.getGlossary());
		marker.setCREATE__TRG__ge(ge);
		protocol.getContents().add(marker);
	}

	private void createGlossaryLinks(Entry e) {
		for (int i = 0; i < parameters.num_of_glossar_links_per_entry; i++)
			createGlossaryLink(e, i);
	}

	private void createGlossaryLink(Entry e, int index) {
		String glossaryEntryName = "GlossaryEntry" + SEP + (glossaryLinkCounter % parameters.num_of_glossar_entries);
		GlossaryEntry ge = name2glossaryEntry.get(glossaryEntryName);
		glossaryLinkCounter++;

		// TRG
		createGlossaryLink(e, ge);
		// MARKER
		GlossaryLink__Marker marker = cFactory.createGlossaryLink__Marker();
		marker.setCONTEXT__TRG__e(e);
		marker.setCONTEXT__TRG__ge(ge);
		protocol.getContents().add(marker);
	}

	//// DELTA ////

	@Override
	protected void genDelta() {
		List<BiConsumer<Type, Boolean>> deltaFunctions = new LinkedList<>();
		switch (parameters.scaleOrientation) {
		case HORIZONTAL:
			deltaFunctions.add(this::createAttributeConflict);
			deltaFunctions.add(this::createContradictingMoveConflict);
			deltaFunctions.add(this::createDeletePreserveConflict_Horizontal);

			if (parameters.num_of_changes > parameters.num_of_root_types)
				throw new RuntimeException("Too many conflicts for this model");
			break;
		case VERTICAL:
			deltaFunctions.add(this::createDeletePreserveConflict_Vertical);

			if (parameters.num_of_changes > parameters.inheritance_depth)
				throw new RuntimeException("Too many conflicts for this model");
			break;
		default:
			break;
		}

		for (int i = 0; i < parameters.num_of_changes; i++) {
			int deltaIndex = i % deltaFunctions.size();
			boolean generateConflict = i <= parameters.num_of_conflicts;
			deltaFunctions.get(deltaIndex).accept(rootTypes.get(i), generateConflict);
		}
	}

	private void createDeletePreserveConflict_Horizontal(Type t, boolean generateConflict) {
		Delta delta = createDelta(false, true);

		Doc d = name2doc.get(t.getName());
		String newRootName = "DELETE_PRESERVE_" + t.getName();

		createAttrDelta(t, sPackage.getNamedElement_Name(), newRootName, delta);
		createAttrDelta(d, sPackage.getNamedElement_Name(), newRootName, delta);

		Type subT = t.getExtendedBy().get(0);
		Doc subD = d.getSubDocs().get(0);

		deleteType(subT, delta);

		if (generateConflict) {
			if (!subD.getSubDocs().isEmpty())
				subD = subD.getSubDocs().get(0);

			Entry newE = createEntry(t.getName().substring(4) + "_new_method", EntryType.METHOD, null);

			createObject(newE, delta);
			createLink(subD, newE, tPackage.getDoc_Entries(), delta);
		}
	}

	private void createDeletePreserveConflict_Vertical(Type t, boolean generateConflict) {
		// TODO
	}

	private void createAttributeConflict(Type t, boolean generateConflict) {
		Delta delta = createDelta(false, true);

		Doc d = name2doc.get(t.getName());
		String newRootName = "ATTR_CONFLICT_" + t.getName();

		createAttrDelta(t, sPackage.getNamedElement_Name(), newRootName + "_a", delta);
		if (generateConflict)
			createAttrDelta(d, sPackage.getNamedElement_Name(), newRootName + "_b", delta);
	}

	private void createContradictingMoveConflict(Type t, boolean generateConflict) {
		Delta delta = createDelta(false, true);

		Doc d = name2doc.get(t.getName());
		String newRootName = "MOVE_CONFLICT_" + t.getName();

		createAttrDelta(t, sPackage.getNamedElement_Name(), newRootName, delta);
		createAttrDelta(d, sPackage.getNamedElement_Name(), newRootName, delta);

		Doc subD1 = d.getSubDocs().get(0);
		Doc subD2 = d.getSubDocs().get(1);

		deleteLink(d, subD1, tPackage.getDoc_SubDocs(), delta);
		createLink(subD2, subD1, tPackage.getDoc_SuperDocs(), delta);

		if (generateConflict) {
			Type subT1 = t.getExtendedBy().get(0);
			Type subT3 = t.getExtendedBy().get(2);

			deleteLink(t, subT1, sPackage.getType_ExtendedBy(), delta);
			createLink(subT3, subT1, sPackage.getType_ExtendedBy(), delta);
		}
	}

}
