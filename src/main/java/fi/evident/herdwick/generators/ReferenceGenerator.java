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
import fi.evident.herdwick.model.Column;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public final class ReferenceGenerator implements Generator<Object> {

    @NotNull
    private final Database db;

    @NotNull
    private final Random random;

    public ReferenceGenerator(@NotNull Database db, @NotNull Random random) {
        this.db = db;
        this.random = random;
    }

    @NotNull
    @Override
    public List<Object> createValuesForColumn(int count, @NotNull Column column) {
        Column referencedColumn = column.references;
        assert referencedColumn != null;

        List<Object> ids = db.findAll(Object.class, "select " + referencedColumn.name + " from " + referencedColumn.table.getName());
        // TODO: support unique constraints

        if (ids.isEmpty())
            throw new IllegalStateException(referencedColumn + " has no values");

        List<Object> values = new ArrayList<Object>(count);
        for (int i = 0; i < count; i++)
            values.add(ids.get(random.nextInt(ids.size())));
        return values;
    }
}
