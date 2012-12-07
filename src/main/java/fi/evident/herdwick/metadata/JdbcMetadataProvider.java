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

package fi.evident.herdwick.metadata;

import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;

public final class JdbcMetadataProvider implements MetadataProvider {

    @NotNull
    @Override
    public Table getTable(@NotNull Connection connection, @NotNull Name tableName) throws SQLException {
        DatabaseMetaData databaseMetaData = connection.getMetaData();
        ResultSet rs = databaseMetaData.getColumns(null, tableName.getSchema(), tableName.getName(), null);
        try {
            Table table = new Table(tableName);

            while (rs.next()) {
                Column column = table.addColumn(rs.getString("COLUMN_NAME"));
                column.nullable = rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                column.dataType = rs.getInt("DATA_TYPE");
                column.typeName = rs.getString("TYPE_NAME");
                column.autoIncrement = "YES".equals(rs.getString("IS_AUTOINCREMENT"));
                column.size = rs.getInt("COLUMN_SIZE");
            }

            return table;

        } finally {
            rs.close();
        }
    }
}
