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

import java.util.Random;

import static java.lang.Math.min;

public final class SimpleStringGenerator extends AbstractSimpleGenerator<String> {

    @NotNull
    private final Random random;

    private final int maxLength;

    @NotNull
    private final String alphabet;

    SimpleStringGenerator(@NotNull Random random) {
        this(random, 1000, "abcdefghijklmnopqrstuvwxyz0123456789-_ ");
    }

    SimpleStringGenerator(@NotNull Random random, int maxLength, @NotNull String alphabet) {
        this.random = random;
        this.maxLength = maxLength;
        this.alphabet = alphabet;
    }

    @Nullable
    @Override
    protected String randomValue(@NotNull Column column) {
        int length = random.nextInt(min(column.size, maxLength));

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++)
            sb.append(alphabet.charAt(random.nextInt(alphabet.length())));

        return sb.toString();
    }
}
