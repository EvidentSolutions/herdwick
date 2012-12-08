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

import fi.evident.dalesbred.Database;
import fi.evident.dalesbred.TransactionCallback;
import fi.evident.dalesbred.TransactionContext;
import fi.evident.herdwick.dialects.DefaultDialect;
import fi.evident.herdwick.dialects.Dialect;
import fi.evident.herdwick.dialects.JdbcMetadataProvider;
import fi.evident.herdwick.dialects.MetadataProvider;
import fi.evident.herdwick.generators.Batch;
import fi.evident.herdwick.generators.DataGenerator;
import fi.evident.herdwick.model.Name;
import fi.evident.herdwick.model.Table;
import fi.evident.herdwick.model.TableCollection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;

public final class Populator {

    @NotNull
    private final Database db;

    @Nullable
    private final String defaultSchema;

    @NotNull
    private final DataGenerator dataGenerator;

    @NotNull
    private final MetadataProvider metadataProvider = new JdbcMetadataProvider();

    @NotNull
    private final Dialect dialect = new DefaultDialect();

    @NotNull
    private final TableCollection tables;

    public Populator(@NotNull Database db) {
        this(db, "public");
    }

    public Populator(@NotNull Database db, @Nullable String defaultSchema) {
        this.db = db;
        this.dataGenerator = new DataGenerator(db);
        this.defaultSchema = defaultSchema;
        this.tables = db.withTransaction(new TransactionCallback<TableCollection>() {
            @Override
            public TableCollection execute(@NotNull TransactionContext tx) throws SQLException {
                return metadataProvider.loadTables(tx.getConnection());
            }
        });
    }

    public void populate(@NotNull String table, int count) {
        populate(defaultSchema, table, count);
    }

    public void populate(@Nullable String schema, @NotNull String table, int count) {
        populate(new Name(schema, table), count);
    }

    public void populate(@NotNull Name tableName, int count) {
        Batch batch = createBatch(tableName, count);

        db.updateBatch(dialect.createInsert(batch.getTable().getName(), batch.getColumns()), batch.rowsToInsert());
    }

    @NotNull
    private Batch createBatch(@NotNull Name tableName, int size) {
        Table table = tables.getTable(tableName);
        Batch batch = new Batch(table, size);

        dataGenerator.prepare(batch);

        return batch;
    }
}
