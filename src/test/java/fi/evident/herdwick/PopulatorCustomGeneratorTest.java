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
import fi.evident.dalesbred.junit.TestDatabaseProvider;
import fi.evident.dalesbred.junit.TransactionalTests;
import fi.evident.herdwick.generators.Generator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PopulatorCustomGeneratorTest {

    private final Database db = TestDatabaseProvider.databaseForProperties("hsqldb-connection.properties");

    private final Populator populator = new Populator(db);

    @Rule
    public final TransactionalTests transactionalTests = new TransactionalTests(db);

    @Test
    public void customGeneratorForColumn() {
        db.update("drop table if exists my_table");
        db.update("create table my_table (id serial primary key, my_column varchar(128) not null)");

        populator.registerGeneratorForColumn("my_table", "my_column", new Generator<String>() {

            private int counter = 0;

            @Nullable
            @Override
            public String randomValue(@NotNull Random random) {
                return "value " + counter++;
            }
        });

        populator.populate("my_table", 10);

        List<String> values = db.findAll(String.class, "select my_column from my_table order by id");

        assertThat(values.size(), is(10));

        for (int i = 0; i < values.size(); i++)
            assertThat(values.get(i), is("value " + i));
    }
}
