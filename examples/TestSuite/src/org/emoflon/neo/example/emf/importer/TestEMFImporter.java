package org.emoflon.neo.example.emf.importer;

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
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

class TestEMFImporter {

	@BeforeAll
	static void initEMF() {
		Resource.Factory.Registry.INSTANCE.getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());		
	}
	
	@Test
	void testImportSimpleFamilies() throws IOException {
		var importer = new EMFImporter();
		
		var rs = new ResourceSetImpl();
		rs.getResource(URI.createFileURI("./resources/in/SimpleFamilies.ecore"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/SimpleFamilies.msl"), Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLSpecification(rs));
	}
	
	/*
	 * Test for Enum Import
	 */
	@Test
	void testOCLGrammar() throws IOException {
		var importer = new EMFImporter();
		
		var rs = new ResourceSetImpl();
		rs.getResource(URI.createFileURI("./resources/in/"
				+ "OCLGrammar.ecore"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/OCLGrammar.msl"), Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLSpecification(rs));
	}
	
	/*
	 * Test for Empty Meta model Import
	 */
	@Test
	void testEmptyModel() throws IOException {
		var importer = new EMFImporter();
		
		var rs = new ResourceSetImpl();
		rs.getResource(URI.createFileURI("./resources/in/"
				+ "EmptyMetamodel.ecore"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/EmptyMetamodel.msl"), Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLSpecification(rs));
	}
	
	/*
	 * Test for import of meta model with objects
	 */
	@Test
	void testObjectFields() throws IOException {
		var importer = new EMFImporter();
		
		var rs = new ResourceSetImpl();
		rs.getResource(URI.createFileURI("./resources/in/"
				+ "BlockLanguage.ecore"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/BlockLanguage.msl"), Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLSpecification(rs));
	}
	
	/*
	 * Test for Import of meta models with basic types
	 */
	@Test
	void testBasicFields() throws IOException {
		var importer = new EMFImporter();
		
		var rs = new ResourceSetImpl();
		rs.getResource(URI.createFileURI("./resources/in/"
				+ "BasicTypesMetamodel.ecore"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/BasicTypesMetamodel.msl"), Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLSpecification(rs));
	}
	
	/*
	 * Test for inherited class import
	 */
	@Test
	void testInheritance() throws IOException {
		var importer = new EMFImporter();
		
		var rs = new ResourceSetImpl();
		rs.getResource(URI.createFileURI("./resources/in/"
				+ "ClassInheritanceHeirarchy.ecore"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/ClassInheritanceHeirarchy.msl"), Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLSpecification(rs));
	}
	
	/*
	 * Test for abstract class import
	 * aggregation import
	 */
	@Test
	void testAbstractNAggregate() throws IOException {
		var importer = new EMFImporter();
		
		var rs = new ResourceSetImpl();
		rs.getResource(URI.createFileURI("./resources/in/"
				+ "SheRememberedCaterpillars.ecore"), true);

		String expected = FileUtils.readFileToString(new File("./resources/expected/SheRememberedCaterpillars.msl"), Charset.defaultCharset());
		assertEquals(expected, importer.generateEMSLSpecification(rs));
	}
}