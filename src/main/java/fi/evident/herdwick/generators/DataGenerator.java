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
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
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
        List<ColumnSetGenerator> result = new ArrayList<ColumnSetGenerator>(columns.size());

        int index = 0;
        for (Column column : columns) {
            Reference reference = getReference(column);
            if (reference != null) {
                result.add(new ReferenceGenerator(db, dialect, reference, index));
            } else {
                result.add(new SingleValueColumnSetGenerator(index, generatorFor(column)));
            }

            index++;
        }

        return result;
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
    public static Reference getReference(@NotNull Column column) {
        for (Reference reference : column.getTable().getForeignKeys())
            if (reference.getSourceColumns().contains(column)) {
                if (reference.getSourceColumns().size() == 1)
                    return reference;
                else
                    throw new UnsupportedOperationException("multi-column foreign keys are not supported");
            }

        return null;
    }
}
