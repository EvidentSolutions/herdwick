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

import fi.evident.herdwick.model.Column;
import fi.evident.herdwick.model.Table;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.herdwick.utils.CollectionUtils.transposed;
import static java.util.Collections.unmodifiableList;

public final class Batch {

    private final List<List<?>> data = new ArrayList<List<?>>();

    @NotNull
    private final Table table;

    @NotNull
    private final List<Column> columns;
    private final int size;

    public Batch(@NotNull Table table, int size) {
        this.table = table;
        this.size = size;
        this.columns = table.getNonAutoIncrementColumns();
    }

    @NotNull
    public List<? extends List<?>> rowsToInsert() {
        return transposed(data);
    }

    public void addColumnData(@NotNull List<?> valuesForColumn) {
        data.add(valuesForColumn);
    }

    public int getSize() {
        return size;
    }

    @NotNull
    public List<Column> getColumns() {
        return unmodifiableList(columns);
    }

    @NotNull
    public Table getTable() {
        return table;
    }
}
