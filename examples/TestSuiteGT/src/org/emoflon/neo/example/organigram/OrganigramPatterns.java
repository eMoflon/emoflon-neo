package org.emoflon.neo.example.organigram;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

import org.emoflon.neo.api.organigram.API_Organigram;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

@SuppressWarnings("deprecation")
public class OrganigramPatterns extends ENeoTest {
	private API_Organigram entities = new API_Organigram(builder);
	
	@BeforeEach
	public void initDB() {
		initDB(entities.getModel_SimpleCompany());
	}
	
	@Test
	public void test_CeoIsEmployed() {
		expectSingleMatch(entities.getPattern_CeoIsEmployed());
	}
	
	@Test
	public void test_ManagerIsEmployed() {
		assertThat(entities.getPattern_ManagersMustBeEmployed().pattern().countMatches(), is(2));
	}
	
	@Test
	public void test_CeoAndManagerEmployed() {
		assertThat(entities.getPattern_CeoAndManagerEmployed().pattern().countMatches(), is(2));
	}
	
}
