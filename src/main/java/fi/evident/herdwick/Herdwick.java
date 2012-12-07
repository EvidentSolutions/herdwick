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

package fi.evident.herdwick;

import fi.evident.dalesbred.Database;
import fi.evident.dalesbred.TransactionCallback;
import fi.evident.dalesbred.TransactionContext;
import fi.evident.dalesbred.query.QueryBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.*;
import java.util.*;

import static java.lang.Math.min;

public final class Herdwick {

    @NotNull
    private final Database db;

    private final Map<ColumnMetadata,Set<Object>> generatedValues = new HashMap<ColumnMetadata, Set<Object>>();

    public Herdwick(@NotNull Database db) {
        this.db = db;
    }

    private final Random random = new Random();

    public void populate(@NotNull final String table, final int count) {
        db.withTransaction(new TransactionCallback<Void>() {
            @Override
            @Nullable
            public Void execute(@NotNull TransactionContext tx) throws SQLException {
                Connection connection = tx.getConnection();
                DatabaseMetaData databaseMetaData = connection.getMetaData();
                List<ColumnMetadata> columns = getColumns(databaseMetaData, table);

                for (int i = 0; i < count; i++)
                    insert(table, columns);

                return null;
            }
        });
    }

    private void insert(@NotNull String table, @NotNull List<ColumnMetadata> columns) {
        QueryBuilder qb = new QueryBuilder("insert into ").append(table).append(" (");

        for (Iterator<ColumnMetadata> it = columns.iterator(); it.hasNext(); ) {
            ColumnMetadata column = it.next();
            qb.append(column.name);
            if (it.hasNext())
                qb.append(",");
        }
        qb.append(") values (");

        for (Iterator<ColumnMetadata> it = columns.iterator(); it.hasNext(); ) {
            ColumnMetadata column = it.next();

            qb.append("?", distinctRandomValue(column));
            if (it.hasNext())
                qb.append(",");
        }
        qb.append(")");
        db.update(qb.build());
    }

    @Nullable
    private Object distinctRandomValue(@NotNull ColumnMetadata columnMetadata) {
        int retries = 10;

        for (int i = 0; i < retries; i++) {
            Object value = randomValue(columnMetadata);

            if (getGeneratedValuesForColumn(columnMetadata).add(value)) {
                return value;
            }
        }

        throw new RuntimeException("tried " + retries + " times but couldn't come up with unique random value for " + columnMetadata.name);
    }

    @NotNull
    private Set<Object> getGeneratedValuesForColumn(@NotNull ColumnMetadata columnMetadata) {
        Set<Object> values = generatedValues.get(columnMetadata);
        if (values == null) {
            values = new HashSet<Object>();
            generatedValues.put(columnMetadata, values);
        }
        return values;
    }

    @Nullable
    private Object randomValue(@NotNull ColumnMetadata column) {
        switch (column.dataType) {
        case Types.VARCHAR:
            return randomString(min(column.size, 10000));
        case Types.INTEGER:
            return random.nextInt();
        default:
            throw new IllegalArgumentException("unknown sql-type: " + column.dataType + " (" + column.typeName + ')');
        }
    }

    private String randomString(int size) {
        int length = random.nextInt(size);

        StringBuilder sb = new StringBuilder(length);

        String alphabet = "abcdefghijklmnopqrstuvwxyz0123456789-_ ";

        for (int i = 0; i < length; i++)
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));

        return sb.toString();
    }

    @NotNull
    private static List<ColumnMetadata> getColumns(@NotNull DatabaseMetaData databaseMetaData, @NotNull String table) throws SQLException {

        ResultSet rs = databaseMetaData.getColumns(null, null, table, "%");
        try {
            List<ColumnMetadata> result = new ArrayList<ColumnMetadata>();

            while (rs.next()) {
                ColumnMetadata column = new ColumnMetadata(rs.getString("COLUMN_NAME"));
                column.nullable = rs.getInt("NULLABLE") == DatabaseMetaData.columnNullable;
                column.dataType = rs.getInt("DATA_TYPE");
                column.typeName = rs.getString("TYPE_NAME");
                column.autoIncrement = "YES".equals(rs.getString("IS_AUTOINCREMENT"));
                column.size = rs.getInt("COLUMN_SIZE");

                if (!column.autoIncrement)
                    result.add(column);
            }

            return result;

        } finally {
            rs.close();
        }
    }

    private static class ColumnMetadata {
        final String name;
        boolean nullable;
        int dataType;
        boolean autoIncrement;
        String typeName;
        int size;

        public ColumnMetadata(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "[name=" + name + ", nullable=" + nullable + ", typeName=" + typeName + ", dataType=" + dataType + ", size=" + size + ", autoIncrement=" + autoIncrement + ']';
        }
    }
}
