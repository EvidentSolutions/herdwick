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

import fi.evident.herdwick.metadata.Column;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import static java.lang.Math.min;

public final class DataGenerator {

    private static final int MAX_VARCHAR_LENGTH = 10000;

    private static final String VARCHAR_ALPHABET = "abcdefghijklmnopqrstuvwxyz0123456789-_ ";

    @NotNull
    private final Random random = new Random();

    @NotNull
    public List<Object> createValuesForColumn(@NotNull Column column, int count) {
        List<Object> values = new ArrayList<Object>(count);
        for (int i = 0; i < count; i++)
            values.add(distinctRandomValue(column, values));
        return values;
    }

    @Nullable
    public Object distinctRandomValue(@NotNull Column column, Collection<?> existing) {
        int retries = 10;

        for (int i = 0; i < retries; i++) {
            Object value = randomValue(column);

            if (!existing.contains(value))
                return value;
        }

        throw new RuntimeException("tried " + retries + " times but couldn't come up with unique random value for " + column.name);
    }

    @Nullable
    private Object randomValue(@NotNull Column column) {
        switch (column.dataType) {
            case Types.VARCHAR:
                return randomString(min(column.size, MAX_VARCHAR_LENGTH));
            case Types.INTEGER:
                return random.nextInt();
            default:
                throw new IllegalArgumentException("unknown sql-type: " + column.dataType + " (" + column.typeName + ')');
        }
    }

    private String randomString(int size) {
        int length = random.nextInt(size);

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++)
            sb.append(VARCHAR_ALPHABET.charAt(random.nextInt(VARCHAR_ALPHABET.length())));

        return sb.toString();
    }
}
