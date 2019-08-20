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

import static org.sonar.api.ce.posttask.QualityGate.Status;

import static tomfi.sonar.plugins.gitea.SonarPlugin.PROP_GITEA_LABELS;
import static tomfi.sonar.plugins.gitea.SonarPlugin.PROP_GITEA_TOKEN;
import static tomfi.sonar.plugins.gitea.SonarPlugin.PROP_GITEA_URL;

import java.util.Arrays;
import java.util.Optional;

import org.sonar.api.ce.ComputeEngineSide;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask;
import org.sonar.api.config.Configuration;

import tomfi.sonar.plugins.gitea.api.ApiWrapper;
import tomfi.sonar.plugins.gitea.labels.Label;
import tomfi.sonar.plugins.gitea.labels.LabelManager;

/**
 * SonarQube extension for creating a report and pushing it to Gitea.
 */
@ComputeEngineSide
public final class PostProjectAnalysis implements PostProjectAnalysisTask
{
    private final Configuration config;

    /**
     * Main and only constructor, used with dependency injection.
     * @param configuration the injected Configuration object.
     */
    public PostProjectAnalysis(final Configuration configuration)
    {
        config = configuration;
    }

    @Override
    public void finished(final ProjectAnalysis analysis)
    {
        //create the project manager and api wrapper
        final ProjectManager projectManager = new ProjectManager(analysis);
        final Optional<String> optUrl = config.get(PROP_GITEA_URL);
        final Optional<String> optToken = config.get(PROP_GITEA_TOKEN);
        if (optUrl.isPresent() && optToken.isPresent())
        {
            startApiJob(analysis, projectManager, optUrl.get(), optToken.get());
        }
    }

    private void startApiJob(
        final ProjectAnalysis analysis,
        final ProjectManager projectManager,
        final String url,
        final String token
    )
    {
        final ApiWrapper api = new ApiWrapper(url, token, projectManager);
        //break if not analyzed or not gitea repository
        if (projectManager.isAnalyzed() && api.validateRepository())
        {
            //break if not a pull request
            final Optional<Integer> pullIssueNumber = api.retreivePullIssueNumber();
            if (pullIssueNumber.isPresent())
            {
                pushToGitea(analysis, api, pullIssueNumber.get());
            }
        }
    }

    private void pushToGitea(
        final ProjectAnalysis analysis, final ApiWrapper api, final int pullIssueNumber
    )
    {
        //create a report
        final SonarReport report = new SonarReport(analysis);

        //add a comment to the pr conversation
        api.commentOnPullRequest(pullIssueNumber, report);

        //if configured, label the pr as passed or failed
        if (
            config.get(PROP_GITEA_LABELS).isPresent()
            && Boolean.valueOf(config.get(PROP_GITEA_LABELS).get())
        )
        {
            final Optional<LabelManager> labels = api.getLabels();
            if (labels.isPresent())
            {
                final LabelManager labelManager = labels.get();
                final Label addLabel = report.getGateStatus().compareTo(Status.OK) == 0
                    ? labelManager.getPassed() : labelManager.getFailed();
                final Label removeLabel = report.getGateStatus().compareTo(Status.OK) == 0
                    ? labelManager.getFailed() : labelManager.getPassed();
                api.updatePullLabels(pullIssueNumber, addLabel, Arrays.asList(removeLabel));
            }
        }
    }
}
