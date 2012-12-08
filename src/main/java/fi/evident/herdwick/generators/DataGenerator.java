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

package fi.evident.herdwick.generators;

import fi.evident.herdwick.model.Column;
import org.jetbrains.annotations.NotNull;

import java.sql.Types;
import java.util.List;
import java.util.Random;

public final class DataGenerator {

    @NotNull
    private final Random random = new Random();

    @NotNull
    public List<?> createValuesForColumn(@NotNull Column column, int count) {
        Generator<?> generator = generatorFor(column);
        return generator.createValuesForColumn(count, column);
    }

    @NotNull
    private Generator<?> generatorFor(Column column) {
        switch (column.dataType) {
            case Types.VARCHAR:
                return new SimpleStringGenerator(random);
            case Types.BOOLEAN:
            case Types.BIT:
                return new SimpleBooleanGenerator(random);
            case Types.INTEGER:
                return new SimpleIntegerGenerator(random);
            default:
                throw new IllegalArgumentException("unknown sql-type: " + column.dataType + " (" + column.typeName + ')');
        }
    }
}
