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

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * A foreign key reference.
 */
public final class Reference {

    @NotNull
    private final List<Column> sourceColumns;

    @NotNull
    private final List<Column> targetColumns;

    private Reference(@NotNull List<Column> sourceColumns, @NotNull List<Column> targetColumns) {
        assert !sourceColumns.isEmpty() && !targetColumns.isEmpty() && sourceColumns.size() == targetColumns.size();

        this.sourceColumns = sourceColumns;
        this.targetColumns = targetColumns;
    }

    public int getColumnCount() {
        return sourceColumns.size();
    }

    @NotNull
    public Table getSourceTable() {
        return sourceColumns.get(0).getTable();
    }

    @NotNull
    public List<Column> getSourceColumns() {
        return unmodifiableList(sourceColumns);
    }

    @NotNull
    public Table getTargetTable() {
        return targetColumns.get(0).getTable();
    }

    @NotNull
    public List<Column> getTargetColumns() {
        return unmodifiableList(targetColumns);
    }

    @Override
    @NotNull
    public String toString() {
        StringBuilder sb = new StringBuilder(100);

        sb.append(getSourceTable().getName());
        sb.append(" (");
        appendColumns(sb, sourceColumns);
        sb.append(") references ");
        sb.append(getTargetTable().getName());
        sb.append(" (");
        appendColumns(sb, targetColumns);
        sb.append(')');

        return sb.toString();
    }

    private static void appendColumns(@NotNull StringBuilder sb, @NotNull List<Column> columns) {
        for (int i = 0; i < columns.size(); i++) {
            if (i != 0)
                sb.append(',');
            sb.append(columns.get(i).getName());
        }
    }

    @NotNull
    public static Builder builder(@NotNull Column source, @NotNull Column target) {
        return new Builder(source, target);
    }

    public static final class Builder {

        @NotNull
        private final List<Column> sources = new ArrayList<Column>();

        @NotNull
        private final List<Column> targets = new ArrayList<Column>();

        private Builder(@NotNull Column source, @NotNull Column target) {
            addColumn(source, target);
        }

        public void addColumn(@NotNull Column source, @NotNull Column target) {
            sources.add(source);
            targets.add(target);
        }

        @NotNull
        public Reference build() {
            return new Reference(sources, targets);
        }
    }
}
