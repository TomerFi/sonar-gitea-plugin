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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.http.HttpEntity;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

import tomfi.sonar.plugins.gitea.ProjectManager;
import tomfi.sonar.plugins.gitea.labels.Label;
import tomfi.sonar.plugins.gitea.labels.QualityGateLabelBuilder;
import tomfi.sonar.plugins.gitea.labels.QualityGateLabelBuilder.Labels;

/**
 * Helper class for handling responses from Gitea.
 */
public final class ResponseHandlers
{
    private static final Logger LOG = Loggers.get(ResponseHandlers.class);

    private static final String GITEA_RESP_KEY_OK = "ok";
    private static final String GITEA_RESP_KEY_DATA = "data";
    private static final String GITEA_RESP_KEY_FULL_NAME = "full_name";

    private static final String GITEA_RESP_KEY_HEAD = "head";
    private static final String GITEA_RESP_KEY_SHA = "sha";
    private static final String GITEA_RESP_KEY_NUMBER = "number";
    private static final String GITEA_RESP_KEY_NAME = "name";
    private static final String GITEA_RESP_KEY_ID = "id";
    private static final String GITEA_RESP_KEY_DESC = "description";
    private static final String GITEA_RESP_KEY_URL = "url";
    private static final String GITEA_RESP_KEY_COLOR = "color";

    private final transient JsonParser jsonParser;
    private final transient ProjectManager projectManager;

    /**
     * Main and only constructor.
     * @param projectMngr the {@link ProjectManager} object holding the project metadata.
     */
    public ResponseHandlers(final ProjectManager projectMngr)
    {
        jsonParser = new JsonParser();
        projectManager = projectMngr;
    }

    /**
     * Helper for checking if the repository exists on the Gitea instance.
     * @param entity the HttpEntity object to check.
     * @return true if exists.
     */
    public boolean repoSearchResponseHandler(final HttpEntity entity)
    {
        boolean retValue = false;
        try
        (
            JsonReader jreader = new JsonReader(new InputStreamReader(entity.getContent(), UTF_8));
        )
        {
            final JsonObject jobject = jsonParser.parse(jreader).getAsJsonObject();
            if (jobject.get(GITEA_RESP_KEY_OK).getAsBoolean())
            {
                final JsonArray jarray = jobject.get(GITEA_RESP_KEY_DATA).getAsJsonArray();
                for (final JsonElement element : jarray)
                {
                    final String repoFullName =
                        element.getAsJsonObject().get(GITEA_RESP_KEY_FULL_NAME).getAsString();
                    if (projectManager.getName().equals(repoFullName))
                    {
                        retValue = true;
                    }
                }
            }
        } catch (IOException exc)
        {
            LOG.error(exc.getMessage());
        }
        return retValue;
    }

    /**
     * Helper for extracting the pull request number object from the HttpEntity object.
     * @param entity the HttpEntity object to extract from.
     * @return and Optional object of an Integer object.
     */
    public Optional<Integer> repoOpenPullsResponeHandler(final HttpEntity entity)
    {
        Optional<Integer> retValue = Optional.empty();
        if (projectManager.getRevision().isPresent())
        {
            try
            (
                JsonReader jreader = new JsonReader(new InputStreamReader(entity.getContent(), UTF_8));
            )
            {
                final JsonArray jarray = jsonParser.parse(jreader).getAsJsonArray();
                for (final JsonElement element : jarray)
                {
                    final String currentRevision = element
                        .getAsJsonObject()
                        .get(GITEA_RESP_KEY_HEAD)
                        .getAsJsonObject()
                        .get(GITEA_RESP_KEY_SHA)
                        .getAsString();
                    if (currentRevision.equals(projectManager.getRevision().get()))
                    {
                        retValue = Optional.ofNullable(
                            element.getAsJsonObject().get(GITEA_RESP_KEY_NUMBER).getAsInt()
                        );
                    }
                }
            } catch (IOException exc)
            {
                LOG.error(exc.getMessage());
            }
        }
        return retValue;
    }

    /**
     * Helper for extracting a List of labels from an HttpEntity object.
     * @param entity the HttpEntity object to extract from.
     * @return a List object of Optional objects of Label objects.
     */
    public List<Optional<Label>> getExistingLabels(final HttpEntity entity)
    {
        Optional<Label> passed = Optional.empty();
        Optional<Label> failed = Optional.empty();
        try
        (
            JsonReader jreader = new JsonReader(new InputStreamReader(entity.getContent(), UTF_8));
        )
        {
            final JsonArray jarray = jsonParser.parse(jreader).getAsJsonArray();
            for (final JsonElement element : jarray)
            {
                final String currentName =
                    element.getAsJsonObject().get(GITEA_RESP_KEY_NAME).getAsString();
                if (currentName.equals(Labels.PASSED.getName()))
                {
                    passed = Optional.of(parseLabelFromJson(element.getAsJsonObject()));
                }
                if (currentName.equals(Labels.FAILED.getName()))
                {
                    failed = Optional.of(parseLabelFromJson(element.getAsJsonObject()));
                }

            }
        } catch (IOException exc)
        {
            LOG.error(exc.getMessage());
        }
        final List<Optional<Label>> labelList = new ArrayList<>(2);
        labelList.add(passed);
        labelList.add(failed);
        return labelList;
    }

    /**
     * Helper for extracting a Label object from an HttpEntity object.
     * @param entity the HttpEntity object to extract from.
     * @return an Optional object of the extracted Label object.
     */
    public Optional<Label> extractLabelFromEntity(final HttpEntity entity)
    {
        Optional<Label> retValue = Optional.empty();
        try
        (
            JsonReader jreader = new JsonReader(new InputStreamReader(entity.getContent(), UTF_8));
        )
        {
            final JsonObject jobject = jsonParser.parse(jreader).getAsJsonObject();
            retValue = Optional.of(parseLabelFromJson(jobject));
        } catch (IOException exc)
        {
            LOG.error(exc.getMessage());
        }
        return retValue;
    }

    private Label parseLabelFromJson(final JsonObject jobject)
    {
        return new QualityGateLabelBuilder()
            .setName(jobject.get(GITEA_RESP_KEY_NAME).getAsString())
            .setColor(jobject.get(GITEA_RESP_KEY_COLOR).getAsString())
            .setDescription(jobject.get(GITEA_RESP_KEY_DESC).getAsString())
            .setUrl(jobject.get(GITEA_RESP_KEY_URL).getAsString())
            .setId(jobject.get(GITEA_RESP_KEY_ID).getAsInt())
            .build();
    }

    /**
     * Helper for extracting a JsonObject from an HttpEntity object.
     * @param entity the HttpEntity object to extract from.
     * @return an Optional object of the extracted JsonObject object.
     */
    public Optional<JsonObject> getJsonBodyFromEntity(final HttpEntity entity)
    {
        Optional<JsonObject> retValue = Optional.empty();
        try
        (
            JsonReader jreader = new JsonReader(new InputStreamReader(entity.getContent(), UTF_8));
        )
        {
            retValue = Optional.of(jsonParser.parse(jreader).getAsJsonObject());
        } catch (IOException exc)
        {
            LOG.error(exc.getMessage());
        }
        return retValue;
    }
}
