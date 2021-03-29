package org.emoflon.ibex.neo.benchmark.exttype2doc.shortCut;

import org.eclipse.emf.ecore.resource.Resource;
import org.emoflon.ibex.neo.benchmark.exttype2doc.ExtType2Doc_MDGenerator;

import ExtDocModel.Doc;
import ExtDocModel.Entry;
import ExtDocModel.EntryType;
import ExtDocModel.Folder;
import ExtType2Doc_ShortCut.ExtType2Doc_ShortCutFactory;
import ExtType2Doc_ShortCut.ExtendingType2Doc__Marker;
import ExtType2Doc_ShortCut.Field2Entry;
import ExtType2Doc_ShortCut.Field2Entry__Marker;
import ExtType2Doc_ShortCut.Method2Entry;
import ExtType2Doc_ShortCut.Method2Entry__Marker;
import ExtType2Doc_ShortCut.Package2Folder;
import ExtType2Doc_ShortCut.Package2Folder__Marker;
import ExtType2Doc_ShortCut.Param2Entry;
import ExtType2Doc_ShortCut.Param2Entry__Marker;
import ExtType2Doc_ShortCut.Project2DocCont__Marker;
import ExtType2Doc_ShortCut.Project2DocContainer;
import ExtType2Doc_ShortCut.SubPackage2Folder__Marker;
import ExtType2Doc_ShortCut.Type2Doc;
import ExtType2Doc_ShortCut.Type2Doc__Marker;
import ExtTypeModel.Field;
import ExtTypeModel.Method;
import ExtTypeModel.Package;
import ExtTypeModel.Parameter;
import ExtTypeModel.Type;

public class ExtType2Doc_ShortCut_MDGenerator extends ExtType2Doc_MDGenerator<ExtType2Doc_ShortCutFactory, ExtType2Doc_ShortCut_Params> {

	public ExtType2Doc_ShortCut_MDGenerator(Resource source, Resource target, Resource corr, Resource protocol, Resource delta) {
		super(source, target, corr, protocol, delta);
	}

	@Override
	protected ExtType2Doc_ShortCutFactory corrFactoryInstance() {
		return ExtType2Doc_ShortCutFactory.eINSTANCE;
	}

	//// MODEL ////

	@Override
	protected void genModels() {
		createContainers();
		createPackageAndFolderHierarchies();
	}

	private void createContainers() {
		// SRC
		createProject();
		// TRG
		createDocContainer();
		createGlossary();
		// CORR
		Project2DocContainer pr2dc = createCorr(cFactory.createProject2DocContainer(), sContainer, tContainer);
		// MARKER
		Project2DocCont__Marker marker = cFactory.createProject2DocCont__Marker();
		marker.setCREATE__SRC__pr(sContainer);
		marker.setCREATE__CORR__pr2dc(pr2dc);
		marker.setCREATE__TRG__dc(tContainer);
		protocol.getContents().add(marker);
	}

	private void createPackageAndFolderHierarchies() {
		if (parameters.horizontal_package_scales.length == 0)
			return;

		for (int i = 0; i < parameters.horizontal_package_scales[0]; i++)
			createRootPackageAndFolder(i);
	}

	private void createRootPackageAndFolder(int index) {
		String postfix = SEP + index;

		// SRC
		Package p = createRootPackage(postfix);
		// TRG
		Folder f = createFolder(postfix);
		// CORR
		Package2Folder p2f = createCorr(cFactory.createPackage2Folder(), p, f);
		// MARKER
		Package2Folder__Marker marker = cFactory.createPackage2Folder__Marker();
		marker.setCREATE__SRC__p(p);
		marker.setCREATE__CORR__p2f(p2f);
		marker.setCREATE__TRG__f(f);
		marker.setCONTEXT__SRC__pr(sContainer);
		marker.setCONTEXT__CORR__pr2dc((Project2DocContainer) src2corr.get(sContainer));
		marker.setCONTEXT__TRG__dc(tContainer);
		protocol.getContents().add(marker);

		if (parameters.types_for_packages.length != 0 && parameters.types_for_packages[0])
			createTypeAndDocHierarchies(p, f, postfix);

		createPackageAndFolderHierarchies(p, f, 1, postfix);
	}

	private void createPackageAndFolderHierarchies(Package rootP, Folder rootF, int currentDepth, String oldPostfix) {
		if (currentDepth >= parameters.horizontal_package_scales.length)
			return;

		for (int i = 0; i < parameters.horizontal_package_scales[currentDepth]; i++)
			createPackageAndFolderHierarchy(rootP, rootF, currentDepth, oldPostfix, i);
	}

	private void createPackageAndFolderHierarchy(Package superP, Folder f, int currentDepth, String oldPostfix, int index) {
		String postfix = oldPostfix + SEP + index;

		// SRC
		Package p = createPackage(postfix, superP);
		// CORR
		Package2Folder p2f = createCorr(cFactory.createPackage2Folder(), p, f);
		// MARKER
		SubPackage2Folder__Marker marker = cFactory.createSubPackage2Folder__Marker();
		marker.setCREATE__SRC__sp(p);
		marker.setCREATE__CORR__sp2f(p2f);
		marker.setCONTEXT__SRC__p(superP);
		marker.setCONTEXT__CORR__p2f((Package2Folder) src2corr.get(superP));
		marker.setCONTEXT__TRG__f(f);
		protocol.getContents().add(marker);

		createPackageAndFolderHierarchies(p, f, currentDepth + 1, postfix);
	}

	private void createTypeAndDocHierarchies(Package p, Folder f, String oldPostfix) {
		for (int i = 0; i < parameters.num_of_root_types; i++)
			createRootTypeAndDoc(p, f, oldPostfix, i);
	}

	private void createRootTypeAndDoc(Package p, Folder f, String oldPostfix, int index) {
		String postfix = oldPostfix + SEP + index;

		// SRC
		Type t = createType(postfix, false, p);
		// TRG
		Doc d = createDoc(postfix, f);
		// CORR
		Type2Doc t2d = createCorr(cFactory.createType2Doc(), t, d);
		// MARKER
		Type2Doc__Marker marker = cFactory.createType2Doc__Marker();
		marker.setCONTEXT__SRC__p(p);
		marker.setCONTEXT__CORR__p2f((Package2Folder) src2corr.get(p));
		marker.setCONTEXT__TRG__f(f);
		marker.setCREATE__SRC__t(t);
		marker.setCREATE__CORR__t2d(t2d);
		marker.setCREATE__TRG__d(d);
		protocol.getContents().add(marker);
		
		createMethodsAndEntries(t, d, postfix);
		createFieldsAndEntries(t, d, postfix);

		createTypeAndDocHierarchy(p, f, t, d, 1, postfix);
	}

	private void createTypeAndDocHierarchy(Package p, Folder f, Type rootT, Doc rootD, int currentDepth, String oldPostfix) {
		if (currentDepth >= parameters.type_inheritance_depth)
			return;

		for (int i = 0; i < parameters.horizontal_type_inheritance_scale; i++)
			createTypeAndDocInheritance(p, f, rootT, rootD, currentDepth, oldPostfix, i);
	}

	private void createTypeAndDocInheritance(Package p, Folder f, Type superT, Doc superD, int currentDepth, String oldPostfix, int index) {
		String postfix = oldPostfix + SEP + index;

		// SRC
		Type t = createType(postfix, false, p);
		createTypeInheritance(superT, t);
		// TRG
		Doc d = createDoc(postfix, f);
		createDocLink(superD, d);
		// CORR
		Type2Doc t2d = createCorr(cFactory.createType2Doc(), t, d);
		// MARKER
		ExtendingType2Doc__Marker marker = cFactory.createExtendingType2Doc__Marker();
		marker.setCONTEXT__SRC__p(p);
		marker.setCONTEXT__CORR__p2f((Package2Folder) src2corr.get(p));
		marker.setCONTEXT__TRG__f(f);
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
			createTypeAndDocHierarchy(p, f, t, d, currentDepth + 1, postfix);
			break;
		case VERTICAL:
			if (index == 0)
				createTypeAndDocHierarchy(p, f, t, d, currentDepth + 1, postfix);
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

	@Override
	protected void genDelta() {
		// TODO Auto-generated method stub

	}

}
