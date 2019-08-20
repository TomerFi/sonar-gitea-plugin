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

package tomfi.sonar.plugins.gitea;

import java.util.Optional;

import org.sonar.api.ce.posttask.Analysis;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask.ProjectAnalysis;

/**
 * Object reprenting the project metadata.
 */
@SuppressWarnings("PMD.DataClass")
public final class ProjectManager
{
    private final Optional<Analysis> optAnalysis;
    private final String key;
    private final String name;
    private final String uuid;
    private final Optional<String> revision;

    /**
     * Main and only constructor.
     * @param analysis the ProjectAnalysis object for gathering the data from.
     */
    public ProjectManager(final ProjectAnalysis analysis)
    {
        optAnalysis = analysis.getAnalysis();
        key = analysis.getProject().getKey();
        name = analysis.getProject().getName();
        uuid = analysis.getProject().getUuid();
        revision = optAnalysis.isPresent() ? optAnalysis.get().getRevision() : Optional.empty();
    }

    public boolean isAnalyzed()
    {
        return optAnalysis.isPresent();
    }

    public Optional<String> getRevision()
    {
        return revision;
    }

    public String getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    public String getUuid()
    {
        return uuid;
    }


}
