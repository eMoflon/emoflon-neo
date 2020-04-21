package org.emoflon.neo.example.emf;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.apache.commons.io.FileUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.emoflon.neo.emf.EMFImporter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TestEMFImporter {

	private static final String IN_FOLDER = "platform:/resource/TestSuite/resources/";

	@BeforeAll
	static void initEMF() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
	}

	@Test
	void testMetamodelImportSimpleFamilies() throws IOException {
		var importer = new EMFImporter();

		var rs = new ResourceSetImpl();
		rs.getResource(URI.createFileURI("./resources/in/metamodel/SimpleFamilies.ecore"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/metamodel/SimpleFamilies.msl"),
				Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLSpecification(rs));
	}

	/*
	 * Test for Enum Import
	 */
	@Test
	void testMetamodelImportOCLGrammar() throws IOException {
		var importer = new EMFImporter();

		var rs = new ResourceSetImpl();
		rs.getResource(URI.createFileURI("./resources/in/metamodel/" + "OCLGrammar.ecore"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/metamodel/OCLGrammar.msl"),
				Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLSpecification(rs));
	}

	/*
	 * Test for Empty Meta model Import
	 */
	@Test
	void testMetamodelImportEmptyModel() throws IOException {
		var importer = new EMFImporter();

		var rs = new ResourceSetImpl();
		rs.getResource(URI.createFileURI("./resources/in/metamodel/" + "EmptyMetamodel.ecore"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/metamodel/EmptyMetamodel.msl"),
				Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLSpecification(rs));
	}

	/*
	 * Test for import of meta model with objects
	 */
	@Test
	void testMetamodelImportWithRelations() throws IOException {
		var importer = new EMFImporter();

		var rs = new ResourceSetImpl();
		rs.getResource(URI.createFileURI("./resources/in/metamodel/" + "BlockLanguage.ecore"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/metamodel/BlockLanguage.msl"),
				Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLSpecification(rs));
	}

	/*
	 * Test for Import of meta models with basic types
	 */
	@Test
	void testMetamodelImportBasicFields() throws IOException {
		var importer = new EMFImporter();

		var rs = new ResourceSetImpl();
		rs.getResource(URI.createFileURI("./resources/in/metamodel/" + "BasicTypesMetamodel.ecore"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/metamodel/BasicTypesMetamodel.msl"),
				Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLSpecification(rs));
	}

	/*
	 * Test for inherited class import
	 */
	@Test
	void testMetamodelImportInheritance() throws IOException {
		var importer = new EMFImporter();

		var rs = new ResourceSetImpl();
		rs.getResource(URI.createFileURI("./resources/in/metamodel/" + "ClassInheritanceHierarchy.ecore"), true);

		String expected = FileUtils.readFileToString(
				new File("./resources/expected/metamodel/ClassInheritanceHierarchy.msl"), Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLSpecification(rs));
	}

	/*
	 * Test for abstract class import aggregation import
	 */
	@Test
	void testMetamodelImportAbstractNAggregate() throws IOException {
		var importer = new EMFImporter();

		var rs = new ResourceSetImpl();
		rs.getResource(URI.createFileURI("./resources/in/metamodel/" + "SheRememberedCaterpillars.ecore"), true);

		String expected = FileUtils.readFileToString(
				new File("./resources/expected/metamodel/SheRememberedCaterpillars.msl"), Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLSpecification(rs));
	}

	/*
	 * Test Model Import with no fields
	 */
	@Test
	void testModelImportEmptyClass() throws IOException {
		var importer = new EMFImporter();

		var rs = new ResourceSetImpl();
		rs.getURIConverter().getURIMap().put(URI.createURI(IN_FOLDER), URI.createURI("./resources/"));
		rs.getResource(URI.createFileURI("./resources/in/model/" + "EmptyClass.xmi"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/model/EmptyClass.msl"),
				Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLModel(rs));
	}
	
	/*
	 * Test Model Import with Object References, Inheritance, Enum
	 */
	@Test
	void testModelImportWithObjectReference() throws IOException {
		var importer = new EMFImporter();

		var rs = new ResourceSetImpl();
		rs.getURIConverter().getURIMap().put(URI.createURI(IN_FOLDER), URI.createURI("./resources/"));
		rs.getResource(URI.createFileURI("./resources/in/model/" + "OCLExpression.xmi"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/model/OCLExpression.msl"),
				Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLModel(rs));
	}
	
	/*
	 * Test Model Import with fields of basic types
	 */
	@Test
	void testModelImportWithBasicFieldTypes() throws IOException {
		var importer = new EMFImporter();

		var rs = new ResourceSetImpl();
		rs.getURIConverter().getURIMap().put(URI.createURI(IN_FOLDER), URI.createURI("./resources/"));
		rs.getResource(URI.createFileURI("./resources/in/model/" + "Block.xmi"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/model/Block.msl"),
				Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLModel(rs));
	}
	
	/*
	 * Test Model Import with deep hierarchy
	 */
	@Test
	void testModelImportWithDeepHierarchy() throws IOException {
		var importer = new EMFImporter();

		var rs = new ResourceSetImpl();
		rs.getURIConverter().getURIMap().put(URI.createURI(IN_FOLDER), URI.createURI("./resources/"));
		rs.getResource(URI.createFileURI("./resources/in/model/" + "Game.xmi"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/model/Game.msl"),
				Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLModel(rs));
	}
	
	/*
	 * Test Model Import with deep hierarchy and not siblings
	 */
	@Test
	void testModelImportWithDeepHierarchy2() throws IOException {
		var importer = new EMFImporter();

		var rs = new ResourceSetImpl();
		rs.getURIConverter().getURIMap().put(URI.createURI(IN_FOLDER), URI.createURI("./resources/"));
		rs.getResource(URI.createFileURI("./resources/in/model/" + "Expression.xmi"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/model/Expression.msl"),
				Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLModel(rs));
	}
	
	/*
	 * Test Model Import with multiple references to same Class
	 */
	@Test
	void testModelImportWithMultipleReferencesToSameClass() throws IOException {
		var importer = new EMFImporter();

		var rs = new ResourceSetImpl();
		rs.getURIConverter().getURIMap().put(URI.createURI(IN_FOLDER), URI.createURI("./resources/"));
		rs.getResource(URI.createFileURI("./resources/in/model/" + "Platform.xmi"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/model/Platform.msl"),
				Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLModel(rs));
	}
	
	/*
	 * Test Model Import with missing reference
	 */
	@Test
	void testModelImportWithMissingReference() throws IOException {
		var importer = new EMFImporter();

		var rs = new ResourceSetImpl();
		rs.getURIConverter().getURIMap().put(URI.createURI(IN_FOLDER), URI.createURI("./resources/"));
		rs.getResource(URI.createFileURI("./resources/in/model/" + "Call.xmi"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/model/Call.msl"),
				Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLModel(rs));
	}
}

