/*
 * Copyright (c) 2012 Evident Solution
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

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

final class MultiColumnColumnSetGenerator implements ColumnSetGenerator {

    @NotNull
    private final int[] indices;

    @NotNull
    private final Generator<List<?>> generator;

    MultiColumnColumnSetGenerator(@NotNull int[] indices, @NotNull Generator<List<?>> generator) {
        this.indices = indices;
        this.generator = generator;
    }

    @Override
    public void generate(@NotNull Object[] row, @NotNull Random random) {
        List<?> values = generator.randomValue(random);

        if (values == null)
            throw new NullPointerException("got null values from generator " + generator);

        if (values.size() != indices.length)
            throw new IllegalStateException("expected " + indices.length + " values from generator " + generator + ", but got " + values.size());

        for (int i = 0; i < indices.length; i++)
            row[indices[i]] = values.get(i);
    }
}
