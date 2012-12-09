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

import fi.evident.dalesbred.ResultTable;
import fi.evident.herdwick.model.Column;
import fi.evident.herdwick.model.Table;
import fi.evident.herdwick.model.UniqueConstraint;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.herdwick.utils.ObjectUtils.nullSafeEquals;
import static java.util.Collections.unmodifiableList;

/**
 * Represents a single batch of data to be generated for insertion into database.
 */
public final class Batch {

    @NotNull
    private final Table table;

    @NotNull
    private final ResultTable existingData;

    @NotNull
    private final List<List<?>> data = new ArrayList<List<?>>();

    @NotNull
    private final List<Column> columns;
    private final int requestedSize;

    public Batch(@NotNull Table table, @NotNull ResultTable existingData, int requestedSize) {
        this.table = table;
        this.existingData = existingData;
        this.requestedSize = requestedSize;
        this.columns = table.getNonAutoIncrementColumns();
    }

    @NotNull
    public List<? extends List<?>> rowsToInsert() {
        return data;
    }

    public boolean addRow(@NotNull List<?> row) {
        if (satisfiesUniqueConstraints(row)) {
            data.add(row);
            return true;
        } else {
            return false;
        }
    }

    private boolean satisfiesUniqueConstraints(@NotNull List<?> row) {
        for (UniqueConstraint constraint : table.getUniqueConstraints())
            if (!satisfiesUniqueConstraint(constraint, row))
                return false;

        return true;
    }

    private boolean satisfiesUniqueConstraint(@NotNull UniqueConstraint constraint, @NotNull List<?> row) {
        // If this constraint is for a column that we're not generating in this batch
        // (e.g. constraint for auto-generated primary key), we're not interested.
        if (!columns.containsAll(constraint.getColumns()))
            return true;

        int[] columnIndices = columnIndicesFor(constraint);

        for (ResultTable.ResultRow existingRow : existingData.getRows())
            if (matches(existingRow.asList(), row, columnIndices))
                return false;

        for (List<?> existingRow : data)
            if (matches(existingRow, row, columnIndices))
                return false;

        return true;
    }

    private static boolean matches(@NotNull List<?> row1, @NotNull List<?> row2, @NotNull int[] indices) {
        for (int index : indices)
            if (!nullSafeEquals(row1.get(index), row2.get(index)))
                return false;
        return true;
    }

    @NotNull
    private int[] columnIndicesFor(@NotNull UniqueConstraint constraint) {
        List<Column> constraintColumns = constraint.getColumns();
        int[] indices = new int[constraintColumns.size()];

        int i = 0;
        for (Column column : constraintColumns)
            indices[i++] = columns.indexOf(column);

        return indices;
    }

    public int getRequestedSize() {
        return requestedSize;
    }

    public int getCurrentSize() {
        return data.size();
    }

    public boolean isReady() {
        return data.size() >= requestedSize;
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
