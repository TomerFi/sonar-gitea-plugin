/**
 * MIT License
 *
 * Copyright (c) 2019 Tomer Figenblat
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package tomfi.sonar.plugins.gitea.labels;

import java.util.Optional;

/**
 * Manager object for holding the Optional {@link Label} objects.
 */
public final class LabelManager
{
    private final Optional<Label> passed;
    private final Optional<Label> failed;

    /**
     * Main and only constructor.
     * @param passedLabel the Optional passing {@link Label} object.
     * @param failedLabel the Optional failing {@link Label} object.
     */
    public LabelManager(final Optional<Label> passedLabel, final Optional<Label> failedLabel)
    {
        passed = passedLabel;
        failed = failedLabel;
    }

    public Label getPassed()
    {
        return passed.get();
    }

    public Label getFailed()
    {
        return failed.get();
    }

    public boolean gotLabels()
    {
        return passed.isPresent() && failed.isPresent();
    }
}
