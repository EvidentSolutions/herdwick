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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Possibly qualified database name.
 */
public final class Name {

    @Nullable
    private final String schema;

    @NotNull
    private final String name;

    public Name(@Nullable String schema, @NotNull String name) {
        this.schema = schema;
        this.name = name;
    }

    @Nullable
    public String getSchema() {
        return schema;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        if (schema != null)
            return schema + '.' + name;
        else
            return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o instanceof Name) {
            Name rhs = (Name) o;

            return name.equalsIgnoreCase(rhs.name)
                && (schema != null ? schema.equalsIgnoreCase(rhs.schema) : rhs.schema == null);
        }

        return false;
    }

    @Override
    public int hashCode() {
        return 31 * (schema != null ? schema.toLowerCase().hashCode() : 0) + name.toLowerCase().hashCode();
    }
}
