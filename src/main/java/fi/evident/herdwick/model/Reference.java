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

import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * A foreign key reference.
 */
public final class Reference {

    @NotNull
    private final Table table;

    @NotNull
    private final List<Column> columns;

    public Reference(@NotNull Table table, @NotNull List<Column> columns) {
        if (columns.isEmpty()) throw new IllegalArgumentException("no columns for reference");

        this.table = table;
        this.columns = columns;
    }

    @NotNull
    public Table getTable() {
        return table;
    }

    @NotNull
    public List<Column> getColumns() {
        return unmodifiableList(columns);
    }
}
