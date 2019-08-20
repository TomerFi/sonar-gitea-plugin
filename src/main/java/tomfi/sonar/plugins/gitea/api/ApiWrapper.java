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

package tomfi.sonar.plugins.gitea.api;

import static java.nio.charset.StandardCharsets.UTF_8;
import static tomfi.sonar.plugins.gitea.labels.QualityGateLabelBuilder.Labels;

import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

import tomfi.sonar.plugins.gitea.ProjectManager;
import tomfi.sonar.plugins.gitea.SonarReport;
import tomfi.sonar.plugins.gitea.labels.Label;
import tomfi.sonar.plugins.gitea.labels.LabelManager;

/**
 * API Wrapper classes for sending Rest requests to Gitea.
 */
public final class ApiWrapper
{
    /**
     * Contant field holding the default charset name.
     */
    private static final String URL_FMT_REPOS_SEARCH = "%s/repos/search%s";
    private static final String URL_FMT_REPOS_PULL_INFO = "%s/repos/%s/pulls/%d";
    private static final String URL_FMT_REPOS_PULLS = "%s/repos/%s/pulls%s";
    private static final String URL_FMT_ISSUE_COMMENT = "%s/repos/%s/issues/%d/comments";
    private static final String URL_FMT_REPO_LABELS = "%s/repos/%s/labels";

    private static final String URL_PARAM_KEY_QUERY = "q";
    private static final String URL_PARAM_KEY_STATE = "state";

    private static final String URL_PARAM_VALUE_OPEN = "open";

    private static final String URL_BODY_KEY_BODY = "body";
    private static final String URL_BODY_KEY_LABELS = "labels";
    private static final String URL_BODY_KEY_NAME = "name";
    private static final String URL_BODY_KEY_DESC = "description";
    private static final String URL_BODY_KEY_COLOR = "color";
    private static final String URL_BODY_KEY_ID = "id";

    private final transient String apiUrl;

    private final transient ProjectManager projectManager;
    private final transient ResponseHandlers handlers;
    private final transient RequestHelpers helpers;

    /**
     * Main and only constructor.
     * @param baseUrl the Gitea instance URL String.
     * @param token the created Gitea secured token.
     * @param projectMngr the {@link ProjectManager} object with the project metadata.
     */
    public ApiWrapper(final String baseUrl, final String token, final ProjectManager projectMngr)
    {
        apiUrl = UrlUtils.Func_GetBaseApiUrl.apply(baseUrl);
        projectManager = projectMngr;
        helpers = new RequestHelpers(token);
        handlers = new ResponseHandlers(projectMngr);
    }

    /**
     * Validating the repository as a valid Gitea repository.
     * @return true if valid.
     */
    public boolean validateRepository()
    {
        //create param query string
        final Map<String, String> paramMap = new ConcurrentHashMap<String, String>();
        paramMap.put(URL_PARAM_KEY_QUERY, projectManager.getName().split("/")[1]);

        //create url for request
        final String repoSearchUrl = String.format(
            URL_FMT_REPOS_SEARCH, apiUrl, UrlUtils.Func_CreateParamsPart.apply(paramMap)
        );

        //send request and get response
        final Optional<HttpEntity> optResponseEntity = helpers.sendGetRequest(repoSearchUrl);

        //handle response if present
        return optResponseEntity.isPresent()
            ? handlers.repoSearchResponseHandler(optResponseEntity.get())
            : optResponseEntity.isPresent();
    }

    /**
     * Get the pull request number.
     * @return an {@link Optional} object of an {@link Integer} object.
     */
    public Optional<Integer> retreivePullIssueNumber()
    {
        //create param query string
        final Map<String, String> paramMap = new ConcurrentHashMap<String, String>();
        paramMap.put(URL_PARAM_KEY_STATE, URL_PARAM_VALUE_OPEN);

        //create url for request
        final String repoOpenPullsUrl = String.format(
            URL_FMT_REPOS_PULLS,
            apiUrl,
            projectManager.getName(),
            UrlUtils.Func_CreateParamsPart.apply(paramMap)
        );

        //send request and get response
        final Optional<HttpEntity> optResponseEntity = helpers.sendGetRequest(repoOpenPullsUrl);

        //handle response if present
        return optResponseEntity.isPresent()
            ? handlers.repoOpenPullsResponeHandler(optResponseEntity.get()) : Optional.empty();
    }

    /**
     * Push a comment to the pull request.
     * @param pullIssueNumber the pull request number.
     * @param report the {@link SonarReport} object for serializing a String report.
     */
    public void commentOnPullRequest(final int pullIssueNumber, final SonarReport report)
    {
        //create url for request
        final String issueCommentUrl = String.format(
            URL_FMT_ISSUE_COMMENT, apiUrl, projectManager.getName(), pullIssueNumber
        );
        if (report.isCeTaskSuccess())
        {
            //create json body from report
            final JsonObject body = new JsonObject();
            body.addProperty(URL_BODY_KEY_BODY, report.getReport());

            //create http entity from json body
            final StringEntity bodyEntity = new StringEntity(
                body.toString(), ContentType.APPLICATION_JSON.withCharset(UTF_8)
            );

            //send reuqest
            helpers.sendPostRequest(issueCommentUrl, Optional.of(bodyEntity));
        }
    }

    /**
     * Get and instance of {@link LabelManager} with the created {@link Label} objects.
     * @return an {@link Optional} object of a {@link LabelManager} object.
     */
    public Optional<LabelManager> getLabels()
    {
        //create url for request
        final String repoLabelsUrl = String.format(
            URL_FMT_REPO_LABELS, apiUrl, projectManager.getName()
        );

        Optional<LabelManager> retValue = Optional.empty();
        //send request and get response
        final Optional<HttpEntity> optGetResponseEntity = helpers.sendGetRequest(repoLabelsUrl);
        if (optGetResponseEntity.isPresent())
        {
            //extract matching labels from response
            final List<Optional<Label>> labels =
                handlers.getExistingLabels(optGetResponseEntity.get());

            //create new labels in not existing
            final Optional<Label> passed = labels.get(0).isPresent()
                ? labels.get(0) : createNewLabel(repoLabelsUrl, Labels.PASSED);
            final Optional<Label> failed = labels.get(1).isPresent()
                ? labels.get(1) : createNewLabel(repoLabelsUrl, Labels.FAILED);
            retValue = Optional.of(new LabelManager(passed, failed));
        }
        return retValue;
    }

    private Optional<Label> createNewLabel(final String repoLabelsUrl, final Labels labelType)
    {
        //create json body with label details
        final JsonObject body = new JsonObject();
        body.addProperty(URL_BODY_KEY_NAME, labelType.getName());
        body.addProperty(URL_BODY_KEY_DESC, labelType.getDescription());
        body.addProperty(URL_BODY_KEY_COLOR, labelType.getColor());

        //create http entity from json body
        final StringEntity bodyEntity = new StringEntity(
            body.toString(), ContentType.APPLICATION_JSON.withCharset(UTF_8)
        );

        Optional<Label> retValue = Optional.empty();
        //send request and get response
        final Optional<HttpEntity> optPostResponseEntity = helpers.sendPostRequest(
            repoLabelsUrl, Optional.of(bodyEntity)
        );
        if (optPostResponseEntity.isPresent())
        {
            //extract label id from the created label
            retValue = handlers.extractLabelFromEntity(optPostResponseEntity.get());
        }
        return retValue;
    }

    /**
     * Use for updating labels to pull request.
     * @param pullIssueNumber the pull request number.
     * @param addLabel the label to add.
     * @param removeLabels List of labels to remove.
     */
    public void updatePullLabels(
        final int pullIssueNumber, final Label addLabel, final List<Label> removeLabels
    )
    {
        //create url for get info request
        final String getPullRequestInfo = String.format(
            URL_FMT_REPOS_PULL_INFO, apiUrl, projectManager.getName(), pullIssueNumber
        );

        //send request and get response
        final Optional<HttpEntity> optInfoResponseEntity =
            helpers.sendGetRequest(getPullRequestInfo);
        if (optInfoResponseEntity.isPresent())
        {
            //parse response to json
            final Optional<JsonObject> originalBody =
                handlers.getJsonBodyFromEntity(optInfoResponseEntity.get());
            if (originalBody.isPresent())
            {
                pushLabelsToGitea(
                    getPullRequestInfo,
                    originalBody.get().get(URL_BODY_KEY_LABELS).getAsJsonArray(),
                    addLabel,
                    removeLabels
                );
            }
        }
    }

    private void pushLabelsToGitea(
        final String url,
        final JsonArray existingLabels,
        final Label addLabel,
        final List<Label> removeLabels
    )
    {
        //if label exists and needs to be removed
        final List<Integer> labelIds = new ArrayList<Integer>();
        final List<Integer> removeIds = new ArrayList<Integer>(2);
        for (final Label label : removeLabels)
        {
            removeIds.add(label.getId());
        }
        boolean updated = false;
        final Iterator<JsonElement> labelsIterator = existingLabels.iterator();
        while (labelsIterator.hasNext())
        {
            final JsonElement currentElement = labelsIterator.next();
            final int currentId =
                currentElement.getAsJsonObject().get(URL_BODY_KEY_ID).getAsInt();
            if (removeIds.contains(currentId))
            {
                updated = true;
            } else
            {
                labelIds.add(currentId);
            }
        }
        if (!labelIds.contains(addLabel.getId()))
        {
            updated = true;
            labelIds.add(addLabel.getId());
        }

        if (updated)
        {
            //create json body for patching pr labels
            final JsonObject newBody = new JsonObject();
            newBody.add(URL_BODY_KEY_LABELS, new GsonBuilder().create().toJsonTree(labelIds));

            //create http entity from json body
            final StringEntity bodyEntity = new StringEntity(
                newBody.toString(), ContentType.APPLICATION_JSON.withCharset(UTF_8)
            );

            //send reuqest
            helpers.sendPatchRequest(url, Optional.of(bodyEntity));
        }
    }
}
