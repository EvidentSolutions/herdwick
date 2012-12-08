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

package fi.evident.herdwick.model;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Represents all the tables we are interested in. This is not necessarily a single schema,
 * since we could have tables from multiple schemas or we could have just a subset of tables
 * in a schema.
 */
public final class TableCollection implements Iterable<Table> {

    @NotNull
    private final Map<Name,Table> tables = new HashMap<Name,Table>();

    @NotNull
    public Table getTable(@NotNull Name name) {
        Table table = tables.get(name);
        if (table != null)
            return table;
        else
            throw new IllegalArgumentException("could not find table: " + name);
    }

    @NotNull
    public Table addTable(@NotNull Name name) {
        if (tables.containsKey(name))
            throw new IllegalStateException("table " + name + " already exists in the collection.");

        Table table = new Table(name);
        tables.put(table.getName(), table);
        return table;
    }

    @Override
    @NotNull
    public Iterator<Table> iterator() {
        return tables.values().iterator();
    }
}
