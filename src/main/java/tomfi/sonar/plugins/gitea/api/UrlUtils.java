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

import java.util.Map;
import java.util.function.Function;
import java.util.function.UnaryOperator;

/**
 * Url tools class.
*/
public final class UrlUtils
{

    private static final String GITEA_URL_FMT_BASE_API = "%s/api/v1";

    /**
     * Function concatinating SonarQube V1 api uri to the provided base uri.
     */
    public static final UnaryOperator<String> Func_GetBaseApiUrl = origin ->
    {
        final int lastIndex = origin.length() - 1;
        final String result =
            origin.charAt(lastIndex) == '/' ? origin.substring(0, lastIndex) : origin;
        return String.format(GITEA_URL_FMT_BASE_API, result);
    };

    /**
     * Function converting a Map with String keys and String values to a valid uri query string.
     */
    public static final Function<Map<String, String>, String> Func_CreateParamsPart = queryMap ->
    {
        final StringBuilder retUrlBuilder = new StringBuilder("?");
        for (final Map.Entry<String, String> entry : queryMap.entrySet())
        {
            retUrlBuilder.append(String.join("=", entry.getKey(), entry.getValue()));
            retUrlBuilder.append('&');
        }
        return retUrlBuilder.toString().substring(0, retUrlBuilder.length() - 1);
    };

    /**
     * Private constructor for a utility class.
     */
    private UrlUtils()
    {
    }
}
