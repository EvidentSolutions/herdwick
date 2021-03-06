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

import java.util.*;

import static java.util.Collections.*;

/**
 * Represents a table in the database.
 */
public final class Table {

    @NotNull
    private final Name name;

    @NotNull
    private final List<Column> columns = new ArrayList<Column>();

    @NotNull
    private final List<UniqueConstraint> uniqueConstraints = new ArrayList<UniqueConstraint>();

    @NotNull
    private final List<Reference> foreignKeys = new ArrayList<Reference>();

    @NotNull
    private final Map<List<Column>, Generator<List<?>>> generators = new HashMap<List<Column>, Generator<List<?>>>();

    Table(@NotNull Name name) {
        this.name = name;
    }

    @NotNull
    public Name getName() {
        return name;
    }

    @NotNull
    public List<Column> getNonAutoIncrementColumns() {
        List<Column> result = new ArrayList<Column>(columns.size());
        for (Column column : columns)
            if (!column.isAutoIncrement())
                result.add(column);

        return unmodifiableList(result);
    }

    @NotNull
    public Collection<UniqueConstraint> getUniqueConstraints() {
        return unmodifiableCollection(uniqueConstraints);
    }

    @NotNull
    public Collection<Reference> getForeignKeys() {
        return unmodifiableCollection(foreignKeys);
    }

    @Override
    @NotNull
    public String toString() {
        return "Table " + name.toString();
    }

    @NotNull
    public Column addColumn(@NotNull String columnName) {
        if (findColumn(columnName) != null)
            throw new IllegalArgumentException("column " + columnName + " is already present in table " + name);
        Column column = new Column(this, columnName);
        columns.add(column);
        return column;
    }

    @NotNull
    public Column getColumn(@NotNull String columnName) {
        Column column = findColumn(columnName);
        if (column != null)
            return column;
        else
            throw new IllegalArgumentException("no such column: '" + columnName + "' in table " + name);
    }

    @Nullable
    private Column findColumn(@NotNull String columnName) {
        for (Column column : columns)
            if (columnName.equalsIgnoreCase(column.getName()))
                return column;

        return null;
    }

    public void addUniqueConstraint(@NotNull UniqueConstraint constraint) {
        uniqueConstraints.add(constraint);
    }

    public void addForeignKey(@NotNull Reference reference) {
        foreignKeys.add(reference);
    }

    @NotNull
    public Map<List<Column>, Generator<List<?>>> getGenerators() {
        return unmodifiableMap(generators);
    }

    public void registerGenerator(@NotNull List<String> columnNames, @NotNull Generator<List<?>> generator) {
        if (columns.isEmpty()) throw new IllegalArgumentException("no columns");

        // TODO: check for duplicate columns or columns overlapping with existing generators

        List<Column> generatorColumns = new ArrayList<Column>(columnNames.size());
        for (String columnName : columnNames)
            generatorColumns.add(getColumn(columnName));

        generators.put(generatorColumns, generator);
    }
}
