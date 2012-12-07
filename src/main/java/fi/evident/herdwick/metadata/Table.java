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

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

public final class Table {

    @NotNull
    private final Name name;

    @NotNull
    private final List<Column> columns = new ArrayList<Column>();

    Table(@NotNull Name name) {
        this.name = name;
    }

    @NotNull
    public Name getName() {
        return name;
    }

    @NotNull
    public List<Column> getColumns() {
        return unmodifiableList(columns);
    }

    @NotNull
    public List<Column> getNonAutoIncrementColumns() {
        List<Column> result = new ArrayList<Column>(columns.size());
        for (Column column : columns)
            if (!column.autoIncrement)
                result.add(column);
        return result;
    }

    @Override
    @NotNull
    public String toString() {
        return "Table " + name.toString();
    }

    @NotNull
    public Column addColumn(@NotNull String columnName) {
        Column column = new Column(this, columnName);
        columns.add(column);
        return column;
    }
}
