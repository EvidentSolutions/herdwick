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
import fi.evident.herdwick.model.UniqueConstraint;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static fi.evident.herdwick.utils.ObjectUtils.nullSafeEquals;

final class UniqueConstraintVerifier {

    @NotNull
    private final int[] indices;

    UniqueConstraintVerifier(@NotNull int[] indices) {
        this.indices = indices;
    }

    boolean satisfies(@NotNull Iterable<List<?>> existingRows, @NotNull Iterable<List<?>> rowsToInsert, @NotNull List<?> candidateRow) {
        return satisfies(existingRows, candidateRow) && satisfies(rowsToInsert, candidateRow);
    }

    boolean satisfies(@NotNull Iterable<? extends List<?>> existingRows, @NotNull List<?> candidate) {
        for (List<?> existingRow : existingRows)
            if (matches(existingRow, candidate))
                return false;
        return true;
    }

    private boolean matches(@NotNull List<?> row1, @NotNull List<?> row2) {
        for (int index : indices)
            if (!nullSafeEquals(row1.get(index), row2.get(index)))
                return false;
        return true;
    }

    static List<UniqueConstraintVerifier> createUniqueConstraintVerifiers(@NotNull Table table, @NotNull List<Column> columns) {
        Collection<UniqueConstraint> uniqueConstraints = table.getUniqueConstraints();
        List<UniqueConstraintVerifier> result = new ArrayList<UniqueConstraintVerifier>(uniqueConstraints.size());
        for (UniqueConstraint constraint : uniqueConstraints) {
            // If this constraint is for a column that we're not generating in this batch
            // (e.g. constraint for auto-generated primary key), we're not interested.
            if (columns.containsAll(constraint.getColumns()))
                result.add(new UniqueConstraintVerifier(columnIndicesFor(constraint, columns)));
        }
        return result;
    }

    @NotNull
    private static int[] columnIndicesFor(@NotNull UniqueConstraint constraint, @NotNull List<Column> columns) {
        List<Column> constraintColumns = constraint.getColumns();
        int[] indices = new int[constraintColumns.size()];

        int i = 0;
        for (Column column : constraintColumns)
            indices[i++] = columns.indexOf(column);

        return indices;
    }
}
