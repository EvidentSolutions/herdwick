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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class NameTest {

    @Test
    public void nameComparisons() {
        assertThat(new Name(null, "foo"), is(new Name(null, "foo")));
        assertThat(new Name(null, "foo"), is(not(new Name(null, "bar"))));

        assertThat(new Name("foo", "bar"), is(new Name("foo", "bar")));
        assertThat(new Name("foo", "bar"), is(not(new Name("foo", "baz"))));
        assertThat(new Name("foo", "bar"), is(not(new Name("baz", "bar"))));
    }

    @Test
    public void nameComparisonsAreCaseInsensitive() {
        assertThat(new Name(null, "foo"), is(new Name(null, "Foo")));
        assertThat(new Name(null, "foo"), is(new Name(null, "FOO")));
        assertThat(new Name("foo", "bar"), is(new Name("FOO", "BAR")));
    }

    @Test
    public void stringRepresentationsOfName() {
        assertThat(new Name(null, "foo").toString(), is("foo"));
        assertThat(new Name("foo", "bar").toString(), is("foo.bar"));
    }
}
