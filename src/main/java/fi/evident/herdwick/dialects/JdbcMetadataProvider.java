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

import fi.evident.herdwick.model.Column;
import fi.evident.herdwick.model.Name;
import fi.evident.herdwick.model.Table;
import fi.evident.herdwick.model.TableCollection;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public final class JdbcMetadataProvider implements MetadataProvider {

    @Override
    @NotNull
    public TableCollection loadTables(@NotNull Connection connection) throws SQLException {
        DatabaseMetaData metaData = connection.getMetaData();
        List<Name> names = loadTableNames(metaData);
        TableCollection result = new TableCollection();

        for (Name name : names) {
            Table table = result.addTable(name);

            createColumns(table, metaData);
            createUniqueConstraints(table, metaData);
        }

        return result;
    }

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

    private static void createUniqueConstraints(Table table, DatabaseMetaData databaseMetaData) throws SQLException {
        ResultSet rs = databaseMetaData.getIndexInfo(null, table.getName().getSchema(), table.getName().getName(), true, false);
        try {
            while (rs.next()) {
                String columnName = rs.getString("COLUMN_NAME");
                table.getColumn(columnName).unique = true;
            }

        } finally {
            rs.close();
        }
    }

    private static void createColumns(Table table, DatabaseMetaData databaseMetaData) throws SQLException {
        Name name = table.getName();
        ResultSet rs = databaseMetaData.getColumns(null, name.getSchema(), name.getName(), null);
        try {

            while (rs.next()) {
                Column column = table.addColumn(rs.getString("COLUMN_NAME"));
                column.nullable = rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                column.dataType = rs.getInt("DATA_TYPE");
                column.typeName = rs.getString("TYPE_NAME");
                column.autoIncrement = "YES".equals(rs.getString("IS_AUTOINCREMENT"));
                column.size = rs.getInt("COLUMN_SIZE");
                column.decimalDigits = rs.getInt("DECIMAL_DIGITS");
            }

        } finally {
            rs.close();
        }
    }
}