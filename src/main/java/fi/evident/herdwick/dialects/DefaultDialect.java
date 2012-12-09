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
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;

/**
 * A default implementation of {@link Dialect} that should be fine
 * for most databases.
 */
public final class DefaultDialect implements Dialect {

    @NotNull
    @Override
    public String createInsert(@NotNull Name table, @NotNull List<Column> columns) {
        StringBuilder qb = new StringBuilder("insert into ").append(table).append(" (");

        for (Iterator<Column> it = columns.iterator(); it.hasNext(); ) {
            Column column = it.next();
            qb.append(column.name);
            if (it.hasNext())
                qb.append(',');
        }

        qb.append(") values (");

        for (int i = 0; i < columns.size(); i++) {
            if (i != 0)
                qb.append(',');
            qb.append('?');
        }

        qb.append(')');

        return qb.toString();
    }
}
