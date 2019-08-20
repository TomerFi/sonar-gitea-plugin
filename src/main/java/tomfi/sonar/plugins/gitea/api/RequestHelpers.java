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

import static org.apache.http.HttpStatus.SC_CREATED;
import static org.apache.http.HttpStatus.SC_OK;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicHeader;

import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

/**
 * Helper class for sending Rest requests.
 */
public final class RequestHelpers
{

    private static final Logger LOG = Loggers.get(RequestHelpers.class);

    private static final List<Integer> HTTP_SUCCESS_STATUS = Arrays.asList(SC_CREATED, SC_OK);

    private final HttpClient httpClient;

    /**
     * Main and only constructor.
     * @param token the secured token to use as Bearer.
     */
    public RequestHelpers(final String token)
    {
        final List<Header> headers = Arrays.asList(
            new BasicHeader(HttpHeaders.AUTHORIZATION, String.join(" ", "Bearer", token)),
            new BasicHeader(HttpHeaders.ACCEPT, ContentType.APPLICATION_JSON.toString())
        );

        httpClient = HttpClients.custom().setDefaultHeaders(headers).build();
    }

    /**
     * Helper method for sending a Rest Get request.
     * @param url url to send the request to.
     * @return an Optional object of the HttpEntity object as the response.
     */
    public Optional<HttpEntity> sendGetRequest(final String url)
    {
        final HttpGet request = new HttpGet(url);
        return getResponse(request);
    }

    /**
     * Helper method for sending a Rest Post request.
     * @param url url to send the request to.
     * @param optBody optional body to attach to the request.
     * @return an Optional object of the HttpEntity object as the response.
     */
    public Optional<HttpEntity> sendPostRequest(
        final String url, final Optional<HttpEntity> optBody
    )
    {
        final HttpPost request = new HttpPost(url);
        if (optBody.isPresent())
        {
            request.setEntity(optBody.get());
        }
        return getResponse(request);
    }

    /**
     * Helper method for sending a Rest Patch request.
     * @param url url to send the request to.
     * @param optBody optional body to attach to the request.
     * @return an Optional object of the HttpEntity object as the response.
     */
    public Optional<HttpEntity> sendPatchRequest(
        final String url, final Optional<HttpEntity> optBody
    )
    {
        final HttpPatch request = new HttpPatch(url);
        if (optBody.isPresent())
        {
            request.setEntity(optBody.get());
        }
        return getResponse(request);
    }

    private Optional<HttpEntity> getResponse(final HttpRequestBase request)
    {
        Optional<HttpEntity> retValue = Optional.empty();
        try
        {
            final HttpResponse response = httpClient.execute(request);
            if (HTTP_SUCCESS_STATUS.contains(response.getStatusLine().getStatusCode()))
            {
                retValue = Optional.ofNullable(response.getEntity());
            } else
            {
                LOG.error(response.getStatusLine().getReasonPhrase());
            }
        } catch (IOException exc)
        {
            LOG.error(exc.getMessage());
        }
        return retValue;
    }
}
