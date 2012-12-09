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
public final class DefaultDialect implements Dialect {

    private static final String QUOTE_BEGIN = "\"";
    private static final String QUOTE_CLOSE = "\"";

    @NotNull
    @Override
    public String createInsert(@NotNull Name table, @NotNull List<Column> columns) {
        StringBuilder sb = new StringBuilder("insert into ");
        appendName(sb, table);
        sb.append(" (");

        appendCommaSeparatorColumns(sb, columns);

        sb.append(") values (");

        appendCommaSeparatedPlaceholders(sb, columns.size());

        sb.append(')');

        return sb.toString();
    }

    @NotNull
    @Override
    public String selectAll(@NotNull List<Column> columns, @NotNull Table table) {
        StringBuilder sb = new StringBuilder(100);
        sb.append("select ");

        appendCommaSeparatorColumns(sb, columns);

        sb.append(" from ");

        appendName(sb, table.getName());


        return sb.toString();
    }

    private static void appendCommaSeparatorColumns(@NotNull StringBuilder sb, @NotNull List<Column> columns) {
        for (int i = 0, size = columns.size(); i < size; i++) {
            if (i != 0)
                sb.append(',');
            appendName(sb, columns.get(i).getName());
        }
    }

    private static void appendCommaSeparatedPlaceholders(@NotNull StringBuilder sb, int count) {
        for (int i = 0; i < count; i++) {
            if (i != 0)
                sb.append(',');
            sb.append('?');
        }
    }

    private static void appendName(@NotNull StringBuilder sb, @NotNull String name) {
        sb.append(QUOTE_BEGIN).append(name).append(QUOTE_CLOSE);
    }

    private static void appendName(@NotNull StringBuilder sb, @NotNull Name tableName) {
        if (tableName.getSchema() != null) {
            appendName(sb, tableName.getSchema());
            sb.append('.');
        }
        appendName(sb, tableName.getName());
    }
}
