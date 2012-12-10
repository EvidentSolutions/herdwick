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
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * A default implementation of {@link Dialect} that should be fine
 * for most databases.
 */
public final class DefaultDialect extends Dialect {

    private final MetadataProvider metadataProvider = new JdbcMetadataProvider();

    @NotNull
    @Override
    public MetadataProvider getMetadataProvider() {
        return metadataProvider;
    }

    @NotNull
    @Override
    public String createInsert(@NotNull Name table, @NotNull List<Column> columns) {
        SqlBuilder sql = new SqlBuilder();
        sql.append("insert into ").appendName(table);
        sql.append(" (").appendCommaSeparatorColumns(columns);
        sql.append(") values (").appendCommaSeparatedPlaceholders(columns.size()).append(')');
        return sql.toString();
    }

    @NotNull
    @Override
    public String selectAll(@NotNull List<Column> columns, @NotNull Table table) {
        SqlBuilder sql = new SqlBuilder();
        sql.append("select ").appendCommaSeparatorColumns(columns).append(" from ").appendName(table.getName());
        return sql.toString();
    }

    private static final class SqlBuilder {

        @NotNull
        private final StringBuilder sql = new StringBuilder(100);

        @Override
        @NotNull
        public String toString() {
            return sql.toString();
        }

        @NotNull
        SqlBuilder append(@NotNull String s) {
            sql.append(s);
            return this;
        }

        @NotNull
        SqlBuilder append(char c) {
            sql.append(c);
            return this;
        }

        @NotNull
        SqlBuilder appendCommaSeparatorColumns(@NotNull List<Column> columns) {
            for (int i = 0, size = columns.size(); i < size; i++) {
                if (i != 0)
                    sql.append(',');
                appendName(columns.get(i).getName());
            }
            return this;
        }

        @NotNull
        SqlBuilder appendCommaSeparatedPlaceholders(int count) {
            for (int i = 0; i < count; i++) {
                if (i != 0)
                    sql.append(',');
                sql.append('?');
            }
            return this;
        }

        @NotNull
        SqlBuilder appendName(@NotNull String name) {
            sql.append('"').append(name).append('"');
            return this;
        }

        @NotNull
        SqlBuilder appendName(@NotNull Name tableName) {
            String schema = tableName.getSchema();
            if (schema != null) {
                appendName(schema);
                sql.append('.');
            }
            appendName(tableName.getName());
            return this;
        }
    }
}
