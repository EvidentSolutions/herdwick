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
import fi.evident.dalesbred.ResultTable;
import fi.evident.dalesbred.SQL;
import fi.evident.herdwick.dialects.Dialect;
import fi.evident.herdwick.model.Reference;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

/**
 * Generates random foreign key references.
 */
final class ReferenceGenerator implements ColumnSetGenerator {

    @NotNull
    private final int[] indices;

    @NotNull
    private final List<ResultTable.ResultRow> ids;

    ReferenceGenerator(@NotNull Database db, @NotNull Dialect dialect, @NotNull Reference reference, @NotNull int[] indices) {
        assert indices.length == reference.getColumnCount();

        this.indices = indices;

        @SQL
        String sql = dialect.selectAll(reference.getTargetColumns(), reference.getTargetTable());

        ids = db.findTable(sql).getRows();

        if (ids.isEmpty())
            throw new IllegalStateException("Can't construct a generator for columns " + reference.getSourceColumns() + ", because the referenced table " + reference.getTargetTable().getName() + " contains no rows.");
    }

    @Override
    public void generate(@NotNull Object[] row, @NotNull Random random) {
        ResultTable.ResultRow id = ids.get(random.nextInt(ids.size()));

        for (int i = 0; i < indices.length; i++)
            row[indices[i]] = id.get(i);
    }
}
