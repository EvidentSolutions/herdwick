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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

abstract class AbstractSimpleGenerator<T> implements Generator<T> {

    @NotNull
    @Override
    public List<T> createValuesForColumn(int count, @NotNull Column column) {
        List<T> values = new ArrayList<T>(count);
        for (int i = 0; i < count; i++)
            values.add(allowedRandomValue(column, values));
        return values;
    }

    @Nullable
    public T allowedRandomValue(@NotNull Column column, @NotNull Collection<?> existing) {
        int retries = 10;

        for (int i = 0; i < retries; i++) {
            T value = randomValue(column);

            if (!column.unique || !existing.contains(value))
                return value;
        }

        throw new RuntimeException("tried " + retries + " times but couldn't come up with unique random value for " + column);
    }

    @Nullable
    protected abstract T randomValue(@NotNull Column column);
}
