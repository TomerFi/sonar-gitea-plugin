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

import static org.sonar.api.ce.posttask.QualityGate.EvaluationStatus;
import static org.sonar.api.ce.posttask.QualityGate.Status;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import org.sonar.api.ce.posttask.CeTask;
import org.sonar.api.ce.posttask.PostProjectAnalysisTask.ProjectAnalysis;

/**
 * Class for producing a report for pushing to Gitea.
 */
@SuppressWarnings("PMD.DataClass")
public final class SonarReport
{
    private final transient EnumMap<EvaluationStatus, String> evaluationStatusMarks =
        new EnumMap<>(EvaluationStatus.class);
    private final transient EnumMap<Status, String> qualityGateStatusMarks =
        new EnumMap<>(Status.class);
    private final boolean ceTaskSuccess;
    private final String gateId;
    private final String gateName;
    private final Status gateStatus;
    private final transient Map<String, EvaluationStatus> conditionsMap;

    /**
     * Main and only constructor.
     * @param analysis the ProjectAnalysis object for extracting the data from.
    */
    public SonarReport(final ProjectAnalysis analysis)
    {
        qualityGateStatusMarks.put(Status.OK, ":ok_hand:");
        qualityGateStatusMarks.put(Status.ERROR, ":confused:");

        evaluationStatusMarks.put(EvaluationStatus.OK, ":white_check_mark:");
        evaluationStatusMarks.put(EvaluationStatus.ERROR, ":x:");
        evaluationStatusMarks.put(EvaluationStatus.NO_VALUE, ":heavy_minus_sign:");

        ceTaskSuccess = analysis.getCeTask().getStatus().compareTo(CeTask.Status.SUCCESS) == 0;
        gateId = analysis.getQualityGate().getId();
        gateName = analysis.getQualityGate().getName();
        gateStatus = analysis.getQualityGate().getStatus();

        conditionsMap = new HashMap<String, EvaluationStatus>();
        analysis.getQualityGate().getConditions().stream()
            .forEach(
                condition -> conditionsMap.put(condition.getMetricKey(), condition.getStatus()
            )
        );
    }

    public boolean isCeTaskSuccess()
    {
        return ceTaskSuccess;
    }

    public String getGateId()
    {
        return gateId;
    }

    public String getGateName()
    {
        return gateName;
    }

    public Status getGateStatus()
    {
        return gateStatus;
    }

    /**
     * Get a String representation of the report for pushing to Gitea.
     * @return the report in markdown.
     */
    public String getReport()
    {
        final String qgMark = qualityGateStatusMarks.get(getGateStatus());
        final StringBuilder builder = new StringBuilder();
        builder.append(String.format("### %s SonarQube Quality Gate Summary %s%n", qgMark, qgMark));
        for (final Map.Entry<String, EvaluationStatus> entry : conditionsMap.entrySet())
        {
            if (entry.getValue().compareTo(EvaluationStatus.NO_VALUE) != 0)
            {
                builder.append(
                    String.format(
                        "#### %s `%s` `%s`%n",
                        evaluationStatusMarks.get(entry.getValue()),
                        entry.getKey(),
                        entry.getValue().name()
                    )
                );
            }
        }
        return builder.toString();
    }
}
