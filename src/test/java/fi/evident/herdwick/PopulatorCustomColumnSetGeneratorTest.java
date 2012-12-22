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
import org.junit.Rule;
import org.junit.Test;

import java.util.List;
import java.util.Random;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class PopulatorCustomColumnSetGeneratorTest {

    private final Database db = TestDatabaseProvider.databaseForProperties("hsqldb-connection.properties");

    private final Populator populator = new Populator(db);

    @Rule
    public final TransactionalTests transactionalTests = new TransactionalTests(db);

    @Test
    public void customGeneratorForColumnSet() {
        db.update("drop table if exists my_table");
        db.update("create table my_table (id serial primary key, smaller int not null, larger int not null)");

        populator.registerGeneratorForColumns("my_table", asList("smaller", "larger"), new Generator<List<?>>() {
            @Override
            @NotNull
            public List<?> randomValue(@NotNull Random random) {
                int first = random.nextInt();
                int second = random.nextInt();

                return asList(min(first, second), max(first, second));
            }
        });

        populator.populate("my_table", 100);

        List<OrderedPair> rows = db.findAll(OrderedPair.class, "select smaller, larger from my_table");

        assertThat(rows.size(), is(100));

        for (OrderedPair row : rows)
            assertTrue(row.smaller + " <= " + row.larger, row.smaller <= row.larger);
    }

    public static class OrderedPair {
        public final int smaller;
        public final int larger;

        public OrderedPair(int smaller, int larger) {
            this.smaller = smaller;
            this.larger = larger;
        }
    }
}
