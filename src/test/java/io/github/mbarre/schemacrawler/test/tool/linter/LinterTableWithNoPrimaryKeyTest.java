/**
 * 
 */
package io.github.mbarre.schemacrawler.test.tool.linter;

import io.github.mbarre.schemacrawler.test.utils.LintWrapper;
import io.github.mbarre.schemacrawler.test.utils.PostgreSqlDatabase;
import io.github.mbarre.schemacrawler.tool.linter.LinterTableWithNoPrimaryKey;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.schemacrawler.SchemaInfoLevelBuilder;
import schemacrawler.tools.lint.LinterRegistry;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * @author mbarre
 */
public class LinterTableWithNoPrimaryKeyTest extends BaseLintTest {

	private static final String CHANGE_LOG_PRIMARY_KEY_CHECK = "src/test/db/liquibase/primaryKeyCheck/db.changelog.xml";
	private Logger logger = LoggerFactory.getLogger(LinterTableWithNoPrimaryKeyTest.class);
	private static PostgreSqlDatabase database;

	@BeforeClass
	public static void  init(){
		database = new PostgreSqlDatabase();
		database.setUp(CHANGE_LOG_PRIMARY_KEY_CHECK);
	}

	@Test
	public void testLint() throws Exception{

		final LinterRegistry registry = new LinterRegistry();
		Assert.assertTrue(registry.hasLinter(LinterTableWithNoPrimaryKey.class.getName()));

		final SchemaCrawlerOptions options = new SchemaCrawlerOptions();
		// Set what details are required in the schema - this affects the
		// time taken to crawl the schema
		options.setSchemaInfoLevel(SchemaInfoLevelBuilder.standard());
		options.setTableNamePattern("test_primary_key");
		
		Connection connection = DriverManager.getConnection(PostgreSqlDatabase.CONNECTION_STRING, 
				PostgreSqlDatabase.USER_NAME, database.getPostgresPassword());

		List<LintWrapper> lints = executeToJsonAndConvertToLintList(options, connection);
		boolean lintDetected = false;
		for (LintWrapper lint : lints) {
				if(LinterTableWithNoPrimaryKey.class.getName().equals(lint.getId())){
					if("test_primary_key".equals(lint.getValue())){
						Assert.assertEquals("table should have a primary key", lint.getDescription());
						Assert.assertEquals("high", lint.getSeverity());
						lintDetected = true;
					}
					else{
						Assert.fail("Not expected error detected :"+lint.getValue());
					}
				}
			}

			Assert.assertTrue("Some expected errors have not been detected.", lintDetected);
		}
}
