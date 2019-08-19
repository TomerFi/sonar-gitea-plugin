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

import static org.apache.http.HttpHeaders.AUTHORIZATION;
import static org.apache.http.HttpHeaders.ACCEPT;
import static org.apache.http.entity.ContentType.APPLICATION_JSON;

import static org.assertj.core.api.Assertions.assertThat;

import static tomfi.sonar.plugins.gitea.SonarPlugin.PROP_GITEA_LABELS;
import static tomfi.sonar.plugins.gitea.SonarPlugin.PROP_GITEA_TOKEN;
import static tomfi.sonar.plugins.gitea.SonarPlugin.PROP_GITEA_URL;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URISyntaxException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.mockito.Mockito;

import org.mockserver.client.MockServerClient;
import org.mockserver.integration.ClientAndServer;
import org.mockserver.model.Header;
import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.JsonBody;
import org.mockserver.model.RegexBody;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask.ProjectAnalysis;
import org.sonar.api.ce.posttask.QualityGate.Condition;
import org.sonar.api.ce.posttask.Analysis;
import org.sonar.api.ce.posttask.CeTask;
import org.sonar.api.ce.posttask.PostProjectAnalysisTaskTester;
import org.sonar.api.ce.posttask.Project;
import org.sonar.api.ce.posttask.QualityGate;
import org.sonar.api.config.Configuration;

public final class PostProjectAnalysisTest
{
    //the following fields are identical to the values in testing files, do not change.
    private static final String FAKE_BEARER_TOKEN = "thisisafaketokenforaccessingafakegitea12";
    private static final String FAKE_REPO_FULL_NAME = "repoOwner/fake-gitea-repo";
    private static final String FAKE_SCM_REVISION = "afakescmrevisionidntafakescmrevisionidnt";
    private static final int FAKE_PR_ISSUE_NUMBER = 35;
    private static final String FAKE_METRIC_KEY = "fakeconditionmetrikkey";

    private static final int MOCK_SERVER_PORT = 1234;

    private static final List<Header> CUSTOM_HEADERS = Arrays.asList(
        new Header(AUTHORIZATION, String.join(" ", "Bearer", FAKE_BEARER_TOKEN)),
        new Header(ACCEPT, APPLICATION_JSON.toString())
    );

    private static ClientAndServer mockServer;
    private static MockServerClient getRepoSearchClient;
    private static MockServerClient getRepoPullsClient;
    private static MockServerClient postIssueCommentClient;
    private static MockServerClient getRepoLabelsClient;
    private static MockServerClient postCreateRepoLabelClient;
    private static MockServerClient getRepoPullInfoClient;
    private static MockServerClient patchLabelsOnPrClient;

    private static Configuration mockConfiguration;
    private static ProjectAnalysis mockProjectAnalysis;

    private enum Method
    {
        GET,
        POST,
        PATCH
    }

    private static final Function<String, String> Func_ResourceFileToString = filename ->
    {
        try
        (
            BufferedReader breader =
                Files.newBufferedReader(Paths.get(ClassLoader.getSystemResource(filename).toURI()))
        )
        {
            return breader.lines().collect(Collectors.joining(System.lineSeparator()));
        } catch (IOException | URISyntaxException exc)
        {
            throw new RuntimeException(exc);
        }
    };

    private static MockServerClient registerClientWithResponseJson(
        final String serverAddress, final Method method, final String path, final String bodyFile
    )
    {
        final MockServerClient retObj = new MockServerClient(
            serverAddress, mockServer.getLocalPort()
        );
        retObj
            .when(
                HttpRequest.request()
                    .withHeaders(CUSTOM_HEADERS)
                    .withMethod(method.toString())
                    .withPath(path)
            )
            .respond(
                HttpResponse.response()
                    .withStatusCode(200)
                    .withBody(JsonBody.json(Func_ResourceFileToString.apply(bodyFile)))
            );
        return retObj;
    }

    private static MockServerClient registerClientWithRequestRegex(
        final String serverAddress, final Method method, final String path, final String regexFile
    )
    {
        final MockServerClient retObj = new MockServerClient(
            serverAddress, mockServer.getLocalPort()
        );
        retObj
            .when(
                HttpRequest.request()
                    .withHeaders(CUSTOM_HEADERS)
                    .withMethod(method.toString())
                    .withBody(RegexBody.regex(Func_ResourceFileToString.apply(regexFile)))
            )
            .respond(
                HttpResponse.response()
                    .withStatusCode(200)
            );
        return retObj;
    }

    @BeforeAll
    @DisplayName("Start the mock server and mock clients for mocking the Gitea api.")
    public static void startMockServerClient() throws FileNotFoundException, UnknownHostException
    {
        final String serverAddress = InetAddress.getLocalHost().getHostAddress();
        mockServer = ClientAndServer.startClientAndServer(MOCK_SERVER_PORT);
        getRepoSearchClient = registerClientWithResponseJson(
            serverAddress,
            Method.GET,
            "/api/v1/repos/search",
            "search_repos_response.json"
        );
        getRepoPullsClient = registerClientWithResponseJson(
            serverAddress,
            Method.GET,
            String.format("/api/v1/repos/%s/pulls", FAKE_REPO_FULL_NAME),
            "get_pulls_response.json"
        );
        postIssueCommentClient = registerClientWithRequestRegex(
            serverAddress,
            Method.POST,
            String.format(
                "/api/v1/repos/%s/issues/%d/comments",
                FAKE_REPO_FULL_NAME,
                FAKE_PR_ISSUE_NUMBER
            ),
            "comment_on_issue_request_regex.txt"
        );
        getRepoLabelsClient = registerClientWithResponseJson(
            serverAddress,
            Method.GET,
            String.format("/api/v1/repos/%s/labels", FAKE_REPO_FULL_NAME),
            "get_repo_labels_result.json"
        );
        postCreateRepoLabelClient = registerClientWithResponseJson(
            serverAddress,
            Method.POST,
            String.format("/api/v1/repos/%s/labels", FAKE_REPO_FULL_NAME),
            "create_repo_label_result.json"
        );
        getRepoPullInfoClient = registerClientWithResponseJson(
            serverAddress,
            Method.GET,
            String.format("/api/v1/repos/%s/pulls/%d", FAKE_REPO_FULL_NAME, FAKE_PR_ISSUE_NUMBER),
            "get_repo_pull_info.json"
        );
        patchLabelsOnPrClient = registerClientWithRequestRegex(
            serverAddress,
            Method.PATCH,
            String.format("/api/v1/repos/%s/pulls/%d", FAKE_REPO_FULL_NAME, FAKE_PR_ISSUE_NUMBER),
            "patch_labels_on_pr_result.txt"
        );
        initConfiguration(serverAddress);
    }

    @AfterAll
    @DisplayName("Stop the mock server and mock clients for mocking the Gitea api.")
    public static void stopMockServerClients()
    {
        getRepoSearchClient.close();
        getRepoPullsClient.close();
        postIssueCommentClient.close();
        getRepoLabelsClient.close();
        postCreateRepoLabelClient.close();
        getRepoPullInfoClient.close();
        patchLabelsOnPrClient.close();
        mockServer.stop();
    }

    private static void initConfiguration(final String serverAddress)
    {
        //Mock the Configuration class for configuring the plugin.
        mockConfiguration = Mockito.mock(Configuration.class);
        Mockito.when(
            mockConfiguration.get(PROP_GITEA_LABELS)
        ).thenReturn(Optional.of("true"));
        Mockito.when(
            mockConfiguration.get(PROP_GITEA_TOKEN)
        ).thenReturn(Optional.of(FAKE_BEARER_TOKEN));
        Mockito.when(
            mockConfiguration.get(PROP_GITEA_URL)
        ).thenReturn(
            Optional.of(
                String.join("", "http://", serverAddress, ":", String.valueOf(MOCK_SERVER_PORT))
            )
        );
    }

    @BeforeAll
    @DisplayName("Create mock for injecting the project as analysis.")
    public static void initProjectAnalysis()
    {
        //Create a fake Project for attributing the ProjectAnalysis object.
        final Project fakeProject = PostProjectAnalysisTaskTester.newProjectBuilder()
            .setKey("fakeprojectkey")
            .setName(FAKE_REPO_FULL_NAME)
            .setUuid("fakeprojectuuid")
            .build();

        //Mock the Analysis class for attributing the ProjectAnalysis object.
        final Analysis mockAnalysis = Mockito.mock(Analysis.class);
        Mockito
            .when(mockAnalysis.getRevision())
            .thenReturn(Optional.of(FAKE_SCM_REVISION));

        //Mock the Analysis class for attributing the ProjectAnalysis object.
        final CeTask mockCeTask = Mockito.mock(CeTask.class);
        Mockito.when(mockCeTask.getStatus()).thenReturn(CeTask.Status.SUCCESS);

        //Mock the Condition class for attributing the QualityGate object.
        final Condition mockCondition = Mockito.mock(Condition.class);
        Mockito.when(mockCondition.getMetricKey()).thenReturn(FAKE_METRIC_KEY);
        Mockito.when(mockCondition.getValue()).thenReturn("12");
        Mockito.when(mockCondition.getStatus()).thenReturn(QualityGate.EvaluationStatus.OK);

        //Create a fake QualityGate for attributing the ProjectAnalysis object.
        final QualityGate fakeQualityGate = PostProjectAnalysisTaskTester.newQualityGateBuilder()
            .setStatus(QualityGate.Status.OK)
            .setId("fakequalitygateid")
            .setName("fakequalitygatename")
            .add(mockCondition)
            .build();

        //Mock the ProjectAnalysis class for injecting the PostProjectAnalysis object.
        mockProjectAnalysis =  Mockito.mock(ProjectAnalysis.class);
        Mockito.when(mockProjectAnalysis.getProject()).thenReturn(fakeProject);
        Mockito.when(mockProjectAnalysis.getAnalysis()).thenReturn(Optional.of(mockAnalysis));
        Mockito.when(mockProjectAnalysis.getCeTask()).thenReturn(mockCeTask);
        Mockito.when(mockProjectAnalysis.getQualityGate()).thenReturn(fakeQualityGate);
    }

    @Test
    public void test() throws IOException
    {
        final PrintStream stdout = System.out;
        final ByteArrayOutputStream tempout = new ByteArrayOutputStream();

        System.setOut(new PrintStream(tempout));

        final PostProjectAnalysis testSensor = new PostProjectAnalysis(mockConfiguration);
        testSensor.finished(mockProjectAnalysis);

        System.setOut(stdout);

        assertThat(tempout.toString()).doesNotContain("ERROR", "Exception");
    }
}
