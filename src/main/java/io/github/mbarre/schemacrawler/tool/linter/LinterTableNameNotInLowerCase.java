package io.github.mbarre.schemacrawler.tool.linter;


import schemacrawler.schema.Table;
import schemacrawler.schema.Column;
import schemacrawler.tools.lint.BaseLinter;
import schemacrawler.tools.lint.LintSeverity;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static java.util.Objects.requireNonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Check that objects (tables, columns) have name in lower case
 * @author mbarre
 */
public class LinterTableNameNotInLowerCase extends BaseLinter
{
    private static final Logger LOGGER = LoggerFactory.getLogger(LinterTableNameNotInLowerCase.class);
    
    
    /**
     * The lint
     */
    public LinterTableNameNotInLowerCase() {
        setSeverity(LintSeverity.high);
    }
    
    /**
     * Get the lint description
     * @return lint description
     */
    @Override
    public String getDescription()
    {
        return getSummary();
    }
    
    /**
     * Get the lint summary
     * @return the lint summary
     */
    @Override
    public String getSummary()
    {
        return " name should be in lower case";
    }
    
    /**
     * The lint that does the job
     * @param table table
     */
    @Override
    protected void lint(final Table table, Connection connection)
    {
        requireNonNull(table, "No table provided");
        List<String> names = findColumnsWithUpperCase(table.getColumns());
        if (!isLowerCaseName(table.getName()))
        {
            names.add(0,table.getName());
            System.out.println(table.getName());
        }
        
        for (String name : names) {
            addLint(table, getDescription(), name);
        }
    }
    
    private boolean isLowerCaseName(final String name)
    {
        return name.toLowerCase().equals(name);
    }
    
    private List<String> findColumnsWithUpperCase(List<Column> columns){
        List<String> names = new ArrayList<>();
        for (Column column : columns) {
            if(!isLowerCaseName(column.getName())){
                names.add(column.getName());
            }
        }
        return names;
    }
    
}
