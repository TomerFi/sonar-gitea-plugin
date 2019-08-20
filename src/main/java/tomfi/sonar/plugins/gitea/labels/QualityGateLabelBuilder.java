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

/**
 * Builder class for building Label objects.
 */
public final class QualityGateLabelBuilder
{

    /**
     * Enum for generating default lables.
     */
    public enum Labels
    {
        /**Default values for a "passed" label.*/
        PASSED(
            "SonarQube: passed",
            "Label indicating the passing of SonarQube quality gate.",
            "#128A0C"
        ),
        /**Default values for a "failed" label.*/
        FAILED(
            "SonarQube: failed",
            "Label indicating the failing of SonarQube quality gate.",
            "#EE0701"
        );

        private final String name;
        private final String description;
        private final String color;

        /**
         * Main and only constructor for the default labels enum.
         * @param labelName the label name.
         * @param labelDescription the label description.
         * @param labelColor the label color.
         */
        Labels(final String labelName, final String labelDescription, final String labelColor)
        {
            name = labelName;
            description = labelDescription;
            color = labelColor;
        }

        public String getName()
        {
            return name;
        }

        public String getDescription()
        {
            return description;
        }

        public String getColor()
        {
            return color;
        }
    }

    private String name;
    private String description;
    private String color;
    private int ident;
    private String url;

    /**
     * Main constructor for starting the builder.
     */
    @SuppressWarnings("PMD.UncommentedEmptyConstructor")
    public QualityGateLabelBuilder()
    {
    }

    /**
     * Special constructor to start the builder with default values.
     * @param labelType the package {@link Labels} object represnting the default values selected.
     */
    public QualityGateLabelBuilder(final Labels labelType)
    {
        name = labelType.getName();
        description = labelType.getDescription();
        color = labelType.getColor();
    }

    public QualityGateLabelBuilder setName(final String labelName)
    {
        name = labelName;
        return this;
    }

    public QualityGateLabelBuilder setDescription(final String labelDescription)
    {
        description = labelDescription;
        return this;
    }

    public QualityGateLabelBuilder setColor(final String labelColor)
    {
        color = labelColor;
        return this;
    }

    public QualityGateLabelBuilder setId(final int labelId)
    {
        ident = labelId;
        return this;
    }

    public QualityGateLabelBuilder setUrl(final String labelUrl)
    {
        url = labelUrl;
        return this;
    }

    /**
     * Builder final step, build the QualityGateStatusLabel object.
     * @return the QualityGateStatusLabel object.
     */
    public Label build()
    {
        return new QualityGateStatusLabel(name, description, color, ident, url);
    }
}
