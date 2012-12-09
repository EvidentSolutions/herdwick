/*
 * Copyright (c) 2012 Evident Solutions Oy
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package fi.evident.herdwick.dialects;

import fi.evident.herdwick.model.*;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Provides database metadata using JDBC {@link DatabaseMetaData}.
 * Should work for for reasonable JDBC-drivers.
 */
public final class JdbcMetadataProvider implements MetadataProvider {

    @Override
    @NotNull
    public TableCollection loadTables(@NotNull Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        List<Name> names = loadTableNames(metaData);
        TableCollection tables = new TableCollection();

        for (Name name : names) {
            Table table = tables.addTable(name);

            createColumns(table, metaData);
            createUniqueConstraints(table, metaData);
        }

        for (Table table : tables) {
            createForeignKeys(tables, table, metaData);
        }

        return tables;
    }

    private static void createForeignKeys(@NotNull TableCollection tables, @NotNull Table table, @NotNull DatabaseMetaData metaData) throws SQLException {
        ResultSet rs = metaData.getImportedKeys(null, table.getName().getSchema(), table.getName().getName());
        try {
            Map<String,Reference.Builder> referencesBuilders = new HashMap<String, Reference.Builder>();
            while (rs.next()) {
                String foreignKeyName = rs.getString("FK_NAME");
                Name name = new Name(rs.getString("PKTABLE_SCHEM"), rs.getString("PKTABLE_NAME"));
                String pkColumn = rs.getString("PKCOLUMN_NAME");
                String fkColumn = rs.getString("FKCOLUMN_NAME");

                Column source = table.getColumn(fkColumn);
                Column target = tables.getTable(name).getColumn(pkColumn);

                Reference.Builder reference = referencesBuilders.get(foreignKeyName);
                if (reference == null) {
                    reference = Reference.builder(source, target);
                    referencesBuilders.put(foreignKeyName, reference);
                } else {
                    reference.addColumn(source, target);
                }
            }

            for (Reference.Builder builder : referencesBuilders.values())
                table.addForeignKey(builder.build());

        } finally {
            rs.close();
        }
    }

    @NotNull
    private static List<Name> loadTableNames(@NotNull DatabaseMetaData metaData) throws SQLException {
        ResultSet rs = metaData.getTables(null, null, null, new String[] { "TABLE" });
        try {
            List<Name> names = new ArrayList<Name>();
            while (rs.next()) {
                String schema = rs.getString("TABLE_SCHEM");
                String name = rs.getString("TABLE_NAME");

                names.add(new Name(schema, name));
            }
            return names;
        } finally {
            rs.close();
        }
    }

    private static void createUniqueConstraints(@NotNull Table table, @NotNull DatabaseMetaData databaseMetaData) throws SQLException {
        Map<String,List<Column>> uniqueIndices = new HashMap<String, List<Column>>();

        ResultSet rs = databaseMetaData.getIndexInfo(null, table.getName().getSchema(), table.getName().getName(), true, false);
        try {
            while (rs.next()) {
                String indexName = rs.getString("INDEX_NAME");
                List<Column> columns = uniqueIndices.get(indexName);
                if (columns == null) {
                    columns = new ArrayList<Column>();
                    uniqueIndices.put(indexName, columns);
                }

                String columnName = rs.getString("COLUMN_NAME");
                columns.add(table.getColumn(columnName));
            }
        } finally {
            rs.close();
        }

        for (Map.Entry<String, List<Column>> entry : uniqueIndices.entrySet())
            table.addUniqueConstraint(new UniqueConstraint(entry.getKey(), entry.getValue()));
    }

    private static void createColumns(@NotNull Table table, @NotNull DatabaseMetaData databaseMetaData) throws SQLException {
        Name name = table.getName();
        ResultSet rs = databaseMetaData.getColumns(null, name.getSchema(), name.getName(), null);
        try {

            while (rs.next()) {
                Column column = table.addColumn(rs.getString("COLUMN_NAME"));
                column.setNullable(rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable);
                column.setDataType(rs.getInt("DATA_TYPE"));
                column.setTypeName(rs.getString("TYPE_NAME"));
                column.setAutoIncrement("YES".equals(rs.getString("IS_AUTOINCREMENT")));
                column.setSize(rs.getInt("COLUMN_SIZE"));
                column.setDecimalDigits(rs.getInt("DECIMAL_DIGITS"));
            }

        } finally {
            rs.close();
        }
    }
}
