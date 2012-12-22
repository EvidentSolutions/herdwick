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

package fi.evident.herdwick;

import fi.evident.dalesbred.*;
import fi.evident.herdwick.dialects.Dialect;
import fi.evident.herdwick.dialects.MetadataProvider;
import fi.evident.herdwick.generators.Batch;
import fi.evident.herdwick.generators.DataGenerator;
import fi.evident.herdwick.generators.Generator;
import fi.evident.herdwick.model.Name;
import fi.evident.herdwick.model.Table;
import fi.evident.herdwick.model.TableCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.List;

import static fi.evident.dalesbred.SqlQuery.query;
import static fi.evident.herdwick.utils.ObjectUtils.requireNonNull;

/**
 * Facade for the functionality of the application.
 */
public final class Populator {

    @NotNull
    private final Database db;

    @Nullable
    private final String defaultSchema;

    @NotNull
    private final DataGenerator dataGenerator;

    @NotNull
    private final Dialect dialect;

    @NotNull
    private final MetadataProvider metadataProvider;

    @Nullable
    private TableCollection tables;

    private boolean batchMode = true;

    /**
     * Constructs new Populator for given database.
     */
    public Populator(@NotNull Database db) {
        this(db, "public");
    }

    /**
     * Constructs new Populator for given database and schema.
     */
    public Populator(@NotNull Database db, @Nullable String defaultSchema) {
        this.db = requireNonNull(db);
        this.dialect = Dialect.detect(db);
        this.metadataProvider = dialect.getMetadataProvider();
        this.dataGenerator = new DataGenerator(db, dialect);
        this.defaultSchema = defaultSchema;
    }

    /**
     * Constructs populator that connects database with given credentials.
     */
    @NotNull
    public static Populator forUrlAndCredentials(@NotNull String url, @Nullable String username, @Nullable String password) {
        return new Populator(Database.forUrlAndCredentials(url, username, password));
    }

    /**
     * @see #populate(fi.evident.herdwick.model.Name, int)
     */
    public int populate(@NotNull String table, int count) {
        return populate(new Name(defaultSchema, table), count);
    }

    /**
     * Tries to insert {@code count} rows into {@code table}. Due to uniqueness constraints,
     * it might not be possible to create the requested amount of rows in which case a warning
     * is logged and less rows are inserted. Returns the amount of rows actually inserted.
     *
     * @param  table to populate
     * @param  count of rows to insert
     * @return amount of rows actually inserted
     */
    public int populate(@NotNull Name table, int count) {
        Batch batch = createBatch(requireNonNull(table), count);

        @SQL
        String insert = dialect.createInsert(batch.getTable().getName(), batch.getColumns());

        if (batchMode) {
            db.updateBatch(insert, batch.rowsToInsert());
        } else {
            for (List<?> row : batch.rowsToInsert())
                db.update(query(insert, row));
        }

        return batch.getCurrentSize();
    }

    @NotNull
    private Batch createBatch(@NotNull Name tableName, int size) {
        Table table = getTables().getTable(tableName);
        ResultTable existingData = db.findTable(dialect.selectAll(table.getNonAutoIncrementColumns(), table));
        Batch batch = new Batch(table, existingData, size);

        dataGenerator.prepare(batch);

        return batch;
    }

    /**
     * @see #registerGeneratorForColumn(fi.evident.herdwick.model.Name, String, fi.evident.herdwick.generators.Generator)
     */
    public void registerGeneratorForColumn(@NotNull String table, @NotNull String column, @NotNull Generator<?> generator) {
        registerGeneratorForColumn(new Name(defaultSchema, table), column, generator);
    }

    /**
     * Registers generator that will be used for given column instead of the automatically determined generator.
     */
    public void registerGeneratorForColumn(@NotNull Name table, @NotNull String column, @NotNull Generator<?> generator) {
        getTables().getTable(table).getColumn(column).setGenerator(generator);
    }

    /**
     * @see #registerGeneratorForColumns(fi.evident.herdwick.model.Name, java.util.List, fi.evident.herdwick.generators.Generator)
     */
    public void registerGeneratorForColumns(@NotNull String table, @NotNull List<String> columns, @NotNull Generator<List<?>> generator) {
        registerGeneratorForColumns(new Name(defaultSchema, table), columns, generator);
    }

    /**
     * Registers a generator that will be used to generate value for the given set of columns.
     */
    public void registerGeneratorForColumns(@NotNull Name table, @NotNull List<String> columns, @NotNull Generator<List<?>> generator) {
        getTables().getTable(table).registerGenerator(columns, generator);
    }

    /**
     * Returns whether batch mode is used.
     *
     * @see #setBatchMode(boolean)
     */
    public boolean isBatchMode() {
        return batchMode;
    }

    /**
     * By default, the populator tries to insert all rows of a populate-call} in a single batch.
     * This is efficient, but produces less helpful error messages when operation fails. This
     * method can be used to disable the match mode, in which case individual inserts are performed
     * for all rows.
     */
    public void setBatchMode(boolean batchMode) {
        this.batchMode = batchMode;
    }

    @NotNull
    private TableCollection getTables() {
        if (tables == null) {
            tables = db.withTransaction(new TransactionCallback<TableCollection>() {
                @NotNull
                @Override
                public TableCollection execute(@NotNull TransactionContext tx) throws SQLException {
                    return metadataProvider.loadTables(tx.getConnection());
                }
            });
        }

        return tables;
    }
}
