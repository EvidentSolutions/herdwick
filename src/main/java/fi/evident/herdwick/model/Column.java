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

import fi.evident.herdwick.generators.Generator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Represents a column of a {@link Table}.
 */
public final class Column {

    @NotNull
    private final Table table;

    @NotNull
    private final String name;

    private boolean nullable;
    private int dataType;
    private boolean autoIncrement;

    @NotNull
    private String typeName = "<unknown>";

    private int size;
    private int decimalDigits;

    @Nullable
    private Generator<?> generator;

    Column(@NotNull Table table, @NotNull String name) {
        this.table = table;
        this.name = name;
    }

    @NotNull
    @Override
    public String toString() {
        return table.getName().toString() + '.' + name;
    }

    @NotNull
    public String getName() {
        return name;
    }

    @NotNull
    public Table getTable() {
        return table;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        if (size < 0) throw new IllegalArgumentException("negative size: " + size);
        this.size = size;
    }

    public int getDecimalDigits() {
        return decimalDigits;
    }

    public void setDecimalDigits(int decimalDigits) {
        this.decimalDigits = decimalDigits;
    }

    @NotNull
    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(@NotNull String typeName) {
        this.typeName = typeName;
    }

    public boolean isAutoIncrement() {
        return autoIncrement;
    }

    public void setAutoIncrement(boolean autoIncrement) {
        this.autoIncrement = autoIncrement;
    }

    public int getDataType() {
        return dataType;
    }

    public void setDataType(int dataType) {
        this.dataType = dataType;
    }

    public boolean isNullable() {
        return nullable;
    }

    public void setNullable(boolean nullable) {
        this.nullable = nullable;
    }

    public void setGenerator(@Nullable Generator<?> generator) {
        this.generator = generator;
    }

    @Nullable
    public Generator<?> getGenerator() {
        return generator;
    }
}
