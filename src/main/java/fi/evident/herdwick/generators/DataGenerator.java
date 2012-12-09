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
import org.jetbrains.annotations.NotNull;

import java.sql.Types;
import java.util.*;

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

    private static final int MAX_SKIPPED_ROWS = 10000;

    public DataGenerator(@NotNull Database db, @NotNull Dialect dialect) {
        this.db = db;
        this.dialect = dialect;
    }

    public void prepare(@NotNull Batch batch) {
        // TODO: to satisfy possible unique constraints we first need to group the columns in
        int skipped = 0;

        Map<Column, Generator<?>> generators = createGenerators(batch);

        while (!batch.isReady()) {
            if (skipped >= MAX_SKIPPED_ROWS)
                throw new IllegalArgumentException("skipped " + skipped + " rows");
            List<Object> row = new ArrayList<Object>(batch.getColumns().size());

            for (Column column : batch.getColumns()) {
                row.add(generators.get(column).randomValue(random));
            }
            if (!batch.addRow(row))
                skipped++;
        }
    }

    @NotNull
    private Map<Column,Generator<?>> createGenerators(@NotNull Batch batch) {
        Map<Column,Generator<?>> generators = new HashMap<Column, Generator<?>>();

        for (Column column : batch.getColumns())
            generators.put(column, generatorFor(column));

        return generators;
    }

    @NotNull
    private Generator<?> generatorFor(Column column) {
        if (column.getReference() != null)
            return new ReferenceGenerator(db, dialect, column);

        switch (column.dataType) {
            case Types.VARCHAR:
                return new SimpleStringGenerator(min(column.size, 1000));
            case Types.BOOLEAN:
            case Types.BIT:
                return SimpleGenerators.BOOLEAN;
            case Types.INTEGER:
                return SimpleGenerators.INTEGER;
            default:
                throw new IllegalArgumentException("could not find generator for column " + column + " of type: " + column.dataType + " (" + column.typeName + ')');
        }
    }
}
