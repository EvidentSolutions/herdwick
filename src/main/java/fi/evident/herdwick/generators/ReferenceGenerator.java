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

package fi.evident.herdwick.generators;

import fi.evident.dalesbred.Database;
import fi.evident.dalesbred.query.QueryBuilder;
import fi.evident.herdwick.model.Column;
import fi.evident.herdwick.model.Reference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Iterator;
import java.util.List;
import java.util.Random;

/**
 * Generates random foreign key references.
 */
final class ReferenceGenerator implements Generator<Object> {

    @NotNull
    private final List<Object> ids;

    public ReferenceGenerator(@NotNull Database db, @NotNull Column column) {
        Reference reference = column.getReference();
        if (reference == null)
            throw new IllegalArgumentException("column " + column + " has no foreign key reference");

        QueryBuilder qb = new QueryBuilder();
        qb.append("select ");
        for (Iterator<Column> it = reference.getColumns().iterator(); it.hasNext(); ) {
            qb.append(it.next().name);
            if (it.hasNext())
                qb.append(",");
        }
        qb.append(" from ").append(reference.getTable().getName().toString());

        ids = db.findAll(Object.class, qb.build());
    }

    @Nullable
    @Override
    public Object randomValue(@NotNull Random random) {
        return ids.get(random.nextInt(ids.size()));
    }
}
