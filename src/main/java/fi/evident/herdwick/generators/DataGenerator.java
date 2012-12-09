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
import fi.evident.herdwick.dialects.Dialect;
import fi.evident.herdwick.model.Column;
import fi.evident.herdwick.model.Reference;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Types;
import java.util.*;
import java.util.logging.Logger;

import static java.lang.Math.min;

/**
 * Facade responsible for populating {@link Batch} with generated data.
 */
public final class DataGenerator {

    @NotNull
    private final Database db;

    @NotNull
    private final Dialect dialect;

    @NotNull
    private final Random random = new Random();

    @NotNull
    private static final Logger log = Logger.getLogger(DataGenerator.class.getName());

    private static final int MAX_DISCARDED_ROWS = 10000;

    public DataGenerator(@NotNull Database db, @NotNull Dialect dialect) {
        this.db = db;
        this.dialect = dialect;
    }

    public void prepare(@NotNull Batch batch) {
        int discarded = 0;

        RowGenerator rowGenerator = createRowGenerator(batch.getColumns());

        while (!batch.isReady() && discarded < MAX_DISCARDED_ROWS) {
            List<Object> row = rowGenerator.createRow(random);

            if (!batch.addRow(row))
                discarded++;
        }

        if (!batch.isReady())
            log.warning("Caller requested " + batch.getRequestedSize() + " rows to be generated for " + batch.getTable().getName() + ", but could only produce " + batch.getCurrentSize() + " rows satisfying unique constraints. (Discarded " + discarded + " random rows.)");
    }

    @NotNull
    private RowGenerator createRowGenerator(@NotNull List<Column> columns) {
        List<ColumnSetGenerator> generators = createGenerators(columns);

        return new RowGenerator(columns.size(), generators);
    }

    @NotNull
    private List<ColumnSetGenerator> createGenerators(@NotNull List<Column> columns) {
        WorkList workList = new WorkList(columns);

        List<ColumnSetGenerator> generators = new ArrayList<ColumnSetGenerator>(columns.size());

        // First go through all the foreign keys and try to build generators for them
        while (true) {
            ColumnSetGenerator referenceGenerator = extractReferenceGenerator(workList);
            if (referenceGenerator != null)
                generators.add(referenceGenerator);
            else
                break;
        }

        // Then go through the remaining items
        for (IndexedColumn column : workList)
            generators.add(new SingleValueColumnSetGenerator(column.index, generatorFor(column.column)));

        return generators;
    }

    @Nullable
    private ReferenceGenerator extractReferenceGenerator(@NotNull WorkList workList) {
        for (IndexedColumn column : workList) {
            Reference reference = findReferenceWithSourceColumn(column.column);
            if (reference != null) {
                int[] indices = workList.removeColumnsAndReturnIndices(reference.getSourceColumns());
                return new ReferenceGenerator(db, dialect, reference, indices);
            }
        }

        return null;
    }

    @NotNull
    private static Generator<?> generatorFor(@NotNull Column column) {
        switch (column.getDataType()) {
            case Types.VARCHAR:
                return new SimpleStringGenerator(min(column.getSize(), 1000));
            case Types.BOOLEAN:
            case Types.BIT:
                return SimpleGenerators.BOOLEAN;
            case Types.INTEGER:
                return SimpleGenerators.INTEGER;
            default:
                throw new IllegalArgumentException("could not find generator for column " + column + " of type: " + column.getDataType() + " (" + column.getTypeName() + ')');
        }
    }

    @Nullable
    public static Reference findReferenceWithSourceColumn(@NotNull Column column) {
        for (Reference reference : column.getTable().getForeignKeys())
            if (reference.getSourceColumns().contains(column))
                return reference;

        return null;
    }


    private static final class WorkList implements Iterable<IndexedColumn> {

        @NotNull
        private final LinkedList<IndexedColumn> workList;

        WorkList(@NotNull List<Column> columns) {
            this.workList = new LinkedList<IndexedColumn>();
            for (int i = 0; i < columns.size(); i++)
                workList.add(new IndexedColumn(i, columns.get(i)));
        }

        @NotNull
        int[] removeColumnsAndReturnIndices(@NotNull List<Column> removedColumns) {
            int[] indices = new int[removedColumns.size()];
            int index = 0;

            for (Column removedColumn : removedColumns) {
                for (Iterator<IndexedColumn> it = workList.iterator(); it.hasNext(); ) {
                    IndexedColumn column = it.next();
                    if (column.column.equals(removedColumn)) {
                        indices[index++] = column.index;
                        it.remove();
                    }
                }
            }

            return indices;
        }

        @NotNull
        @Override
        public Iterator<IndexedColumn> iterator() {
            return workList.iterator();
        }
    }
}
