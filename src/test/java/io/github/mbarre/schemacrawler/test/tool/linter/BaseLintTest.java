package io.github.mbarre.schemacrawler.test.tool.linter;

import io.github.mbarre.schemacrawler.test.utils.LintWrapper;
import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import schemacrawler.schemacrawler.SchemaCrawlerOptions;
import schemacrawler.tools.executable.Executable;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.lint.executable.LintOptionsBuilder;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.TextOutputFormat;

import java.io.File;
import java.io.FileInputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by barmi83 on 24/12/15.
 */
public abstract class BaseLintTest {

    protected List<LintWrapper> executeToJsonAndConvertToLintList(SchemaCrawlerOptions options, Connection connection) throws Exception {

        final Executable executable = new SchemaCrawlerExecutable("lint");
        final Path linterConfigsFile = FileSystems.getDefault().getPath("", this.getClass().getClassLoader().getResource("schemacrawler-linter-configs-test.xml").getPath());
        final LintOptionsBuilder optionsBuilder = new LintOptionsBuilder();
        optionsBuilder.withLinterConfigs(linterConfigsFile.toString());
        executable.setAdditionalConfiguration(optionsBuilder.toConfig());

        Path out = Paths.get("target/test_"+this.getClass().getSimpleName()+".json");
        OutputOptions outputOptions = new OutputOptions(TextOutputFormat.json, out);
        outputOptions.setOutputFile(Paths.get("target/test_"+this.getClass().getSimpleName()+".json"));

        executable.setOutputOptions(outputOptions);
        executable.setSchemaCrawlerOptions(options);
        executable.execute(connection);
        
        File output = new File(out.toString());
        String data = IOUtils.toString(new FileInputStream(output));
        Assert.assertNotNull(data);
        JSONObject json = new JSONObject(data.toString().substring(1, data.toString().length() - 2));
       
        List<LintWrapper>lints = new ArrayList<>();

        if( json.get("table_lints") instanceof  JSONObject) {

            Assert.assertNotNull(json.getJSONObject("table_lints"));
            JSONArray jsonLints = json.getJSONObject("table_lints").getJSONArray("lints");
            Assert.assertNotNull(jsonLints);

            if(options.getTableNamePattern() != null  && ! options.getTableNamePattern().isEmpty())
                Assert.assertEquals(options.getTableNamePattern(), json.getJSONObject("table_lints").getString("name"));

            for (int i = 0; i < jsonLints.length(); i++) {
                if(!"databasechangelog".equals(json.getJSONObject("table_lints").getString("name")) &&
                        !"databasechangeloglock".equals(json.getJSONObject("table_lints").getString("name")))
                    lints.add(createLintWrapper(json.getJSONObject("table_lints").getString("name"), jsonLints.getJSONObject(i)));
            }
        }
        else{
            Assert.assertNotNull(json.getJSONArray("table_lints"));
            JSONArray jsonTableLints = json.getJSONArray("table_lints");

            for (int i = 0; i < jsonTableLints.length(); i++) {
                JSONArray jsonLints = jsonTableLints.getJSONObject(i).getJSONArray("lints");
                Assert.assertNotNull(jsonLints);

                if(options.getTableNamePattern() != null  && ! options.getTableNamePattern().isEmpty())
                    Assert.assertEquals(options.getTableNamePattern(), json.getJSONObject("table_lints").getString("name"));

                for (int j = 0; j < jsonLints.length(); j++) {
                    if(!"databasechangelog".equals(jsonTableLints.getJSONObject(i).getString("name")) &&
                            !"databasechangeloglock".equals(jsonTableLints.getJSONObject(i).getString("name")))
                        lints.add(createLintWrapper(jsonTableLints.getJSONObject(i).getString("name"), jsonLints.getJSONObject(j)));
                }
            }
        }

        return lints;
    }

    private LintWrapper createLintWrapper(String tableName, JSONObject jsonLint){

        LintWrapper lint = new LintWrapper();
        lint.setId(jsonLint.getString("id"));
        Assert.assertNotNull(lint.getId());
        lint.setValue(jsonLint.getString("value").trim());
        Assert.assertNotNull(lint.getValue());
        lint.setDescription(jsonLint.getString("description").trim());
        Assert.assertNotNull(lint.getDescription());
        lint.setSeverity(jsonLint.getString("severity").trim());
        Assert.assertNotNull(lint.getSeverity());
        lint.setTableName(tableName);

        return lint;
    }
}
