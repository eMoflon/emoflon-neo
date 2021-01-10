package org.emoflon.neo.example.typing;

import org.emoflon.neo.api.testsuitegt.org.emoflon.neo.example.typing.API_TypingTest;
import org.emoflon.neo.emsl.util.FlattenerException;
import org.emoflon.neo.example.ENeoTest;
import org.junit.jupiter.api.BeforeEach;

public class TypingTest extends ENeoTest {
	private API_TypingTest entities = new API_TypingTest(builder);

	@BeforeEach
	public void initDB() {
		try {
			builder.exportEMSLEntityToNeo4j(entities.getMetamodel_T1());
			builder.exportEMSLEntityToNeo4j(entities.getMetamodel_T2());
			builder.exportEMSLEntityToNeo4j(entities.getMetamodel_T3());
			builder.exportEMSLEntityToNeo4j(entities.getMetamodel_T4());
			builder.exportEMSLEntityToNeo4j(entities.getMetamodel_T5());
		} catch (FlattenerException e) {
			e.printStackTrace();
		}
	}
}
