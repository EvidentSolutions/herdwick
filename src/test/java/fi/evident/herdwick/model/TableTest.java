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

package fi.evident.herdwick.model;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

public class TableTest {

    private final Table table = new Table(new Name(null, "foo"));

    @Test
    public void columnLookup() {
        Column column = table.addColumn("bar");

        assertThat(table.getColumn("bar"), is(sameInstance(column)));
    }

    @Test
    public void columnLookupIsCaseInsensitive() {
        Column column = table.addColumn("bar");

        assertThat(table.getColumn("BAR"), is(sameInstance(column)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void columnLookupForNonexistentColumnThrowsException() {
        table.getColumn("bar");
    }

    @Test
    public void addingUniqueConstraints() {
        Column column1 = table.addColumn("column1");
        Column column2 = table.addColumn("column2");

        UniqueConstraint constraint = new UniqueConstraint("my-constraint", asList(column1, column2));

        table.addUniqueConstraint(constraint);

        assertThat(new ArrayList<UniqueConstraint>(table.getUniqueConstraints()), is(Collections.singletonList(constraint)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void cantAddSameColumnMultipleTimes() {
        table.addColumn("foo");
        table.addColumn("foo");
    }
}
