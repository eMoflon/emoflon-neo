package org.emoflon.neo.example.quiltography;

import static org.junit.Assert.assertEquals;

import org.emoflon.neo.api.API_Common;
import org.emoflon.neo.api.API_QuiltographyPages;
import org.emoflon.neo.api.API_QuiltographyQueries;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class TestQueries extends ENeoTest {
	private API_QuiltographyQueries queries = new API_QuiltographyQueries(builder, API_Common.PLATFORM_RESOURCE_URI,
			API_Common.PLATFORM_PLUGIN_URI);
	private API_QuiltographyPages data = new API_QuiltographyPages(builder, API_Common.PLATFORM_RESOURCE_URI,
			API_Common.PLATFORM_PLUGIN_URI);

	@BeforeEach
	public void initDB() {
		initDB(data.getModel_GabisQuiltographyPages());
	}

	@Test
	@Disabled("//TODO[Jannik]:  Filter matches with mask")
	public void test_BooksOfSomeAuthor() {
		var mask = queries.getPattern_AllBooksOfAParticularAuthor().mask()//
				.setSomeAuthorName("Jean Ann")//
				.setSomeAuthorSurname("Wright");
				
		assertEquals(1, queries.getPattern_AllBooksOfAParticularAuthor().matcher(mask).countMatches());
	
		mask = queries.getPattern_AllBooksOfAParticularAuthor().mask()//
				.setSomeAuthorName("Hui")//
				.setSomeAuthorSurname("Boo");
		
		assertEquals(0, queries.getPattern_AllBooksOfAParticularAuthor().matcher(mask).countMatches());
	}
}
