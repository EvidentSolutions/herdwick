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
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static fi.evident.herdwick.generators.UniqueConstraintVerifier.createUniqueConstraintVerifiers;
import static java.util.Collections.unmodifiableList;

/**
 * Represents a single batch of data to be generated for insertion into database.
 */
public final class Batch {

    @NotNull
    private final Table table;

    @NotNull
    private final List<List<?>> existingRows;

    @NotNull
    private final List<List<?>> rowsToInsert = new ArrayList<List<?>>();

    @NotNull
    private final List<Column> columns;
    private final int requestedSize;

    @NotNull
    private final List<UniqueConstraintVerifier> uniqueConstraintVerifiers;

    public Batch(@NotNull Table table, @NotNull ResultTable existingData, int requestedSize) {
        this.table = table;
        this.existingRows = resultTableToList(existingData);
        this.requestedSize = requestedSize;
        this.columns = table.getNonAutoIncrementColumns();
        this.uniqueConstraintVerifiers = createUniqueConstraintVerifiers(table, columns);
    }

    @NotNull
    public List<? extends List<?>> rowsToInsert() {
        return rowsToInsert;
    }

    public boolean addRow(@NotNull List<?> row) {
        if (satisfiesUniqueConstraints(row)) {
            rowsToInsert.add(row);
            return true;
        } else {
            return false;
        }
    }

    private boolean satisfiesUniqueConstraints(@NotNull List<?> row) {
        for (UniqueConstraintVerifier verifier : uniqueConstraintVerifiers)
            if (!verifier.satisfies(existingRows, rowsToInsert, row))
                return false;

        return true;
    }

    @NotNull
    private static List<List<?>> resultTableToList(@NotNull ResultTable table) {
        List<List<?>> result = new ArrayList<List<?>>(table.getRowCount());
        for (ResultTable.ResultRow row : table.getRows())
            result.add(row.asList());
        return result;
    }

    public int getRequestedSize() {
        return requestedSize;
    }

    public int getCurrentSize() {
        return rowsToInsert.size();
    }

    public boolean isReady() {
        return rowsToInsert.size() >= requestedSize;
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
