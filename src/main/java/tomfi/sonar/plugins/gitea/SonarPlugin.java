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

import java.util.Arrays;

import org.sonar.api.Plugin;
import org.sonar.api.PropertyType;
import org.sonar.api.config.PropertyDefinition;
import org.sonar.api.resources.Qualifiers;

/**
 * SonarQube plugin implementation.
 */
@SuppressWarnings("PMD.AtLeastOneConstructor")
public final class SonarPlugin implements Plugin
{

    private static final String PROP_GITEA_CATEGORY = "Gitea";
    private static final String EMPTY_STRING = "";

    /**
     * SonarQube property name for Gitea instance url.
    */
    public static final String PROP_GITEA_URL = "sonar.gitea.url";
    private static final String PROP_GITEA_URL_NAME = "Instance URL";
    private static final String PROP_GITEA_URL_DESC = "URL for accessing the Gitea instance.";

    /**
     * SonarQube property name for Gitea secured token.
    */
    public static final String PROP_GITEA_TOKEN = "sonar.gitea.token.secured";
    private static final String PROP_GITEA_TOKEN_NAME = "Bearer Token";
    private static final String PROP_GITEA_TOKEN_DESC =
        "Bearer token for the user for commenting on behalf of SonarQube.";

    /**
     * SonarQube property name for a flag indicating whether or not to push lables.
     */
    public static final String PROP_GITEA_LABELS = "sonar.gitea.labels";
    private static final String PROP_GITEA_LABELS_NAME = "Label PR as Passed/Failed";
    private static final String PROP_GITEA_LABELS_DESC =
        "Update Passed/Failed label on the PR, will create the label if needed.";
    private static final boolean PROP_GITEA_LABELS_DEFAULT = true;

    @Override
    public void define(final Context context)
    {
        context.addExtensions(
            Arrays.asList(

                PostProjectAnalysis.class,

                PropertyDefinition.builder(PROP_GITEA_URL)
                    .name(PROP_GITEA_URL_NAME)
                    .description(PROP_GITEA_URL_DESC)
                    .category(PROP_GITEA_CATEGORY)
                    .defaultValue(EMPTY_STRING)
                    .type(PropertyType.STRING)
                    .onQualifiers(Qualifiers.PROJECT)
                    .build(),

                PropertyDefinition.builder(PROP_GITEA_TOKEN)
                    .name(PROP_GITEA_TOKEN_NAME)
                    .description(PROP_GITEA_TOKEN_DESC)
                    .category(PROP_GITEA_CATEGORY)
                    .defaultValue(EMPTY_STRING)
                    .type(PropertyType.PASSWORD)
                    .onQualifiers(Qualifiers.PROJECT)
                    .build(),

                PropertyDefinition.builder(PROP_GITEA_LABELS)
                    .name(PROP_GITEA_LABELS_NAME)
                    .description(PROP_GITEA_LABELS_DESC)
                    .category(PROP_GITEA_CATEGORY)
                    .defaultValue(String.valueOf(PROP_GITEA_LABELS_DEFAULT))
                    .type(PropertyType.BOOLEAN)
                    .onQualifiers(Qualifiers.PROJECT)
                    .build()
            )
        );
    }
}
