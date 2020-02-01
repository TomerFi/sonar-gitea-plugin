/**
 * MIT License
 *
 * Copyright (c) 2019 Tomer Figenblat
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software
 * and associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING
 * BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NON-INFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package tomfi.sonar.plugins.gitea;

import static org.assertj.core.api.Assertions.assertThat;
import static tomfi.sonar.plugins.gitea.SonarPlugin.PROP_GITEA_LABELS;
import static tomfi.sonar.plugins.gitea.SonarPlugin.PROP_GITEA_TOKEN;
import static tomfi.sonar.plugins.gitea.SonarPlugin.PROP_GITEA_URL;

import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.sonar.api.Plugin.Context;
import org.sonar.api.SonarRuntime;
import org.sonar.api.config.PropertyDefinition;

public final class SonarPluginTest {
  private static Context mockContext;
  private static SonarPlugin testPlugin;

  @BeforeAll
  public static void testPluginConfiguration() {
    mockContext = new Context(Mockito.mock(SonarRuntime.class));
    testPlugin = new SonarPlugin();
    testPlugin.define(mockContext);
  }

  @Test
  @DisplayName("Test plugin extenions.")
  @SuppressWarnings("unchecked")
  public void testExtensions() {
    assertThat(mockContext.getExtensions()).contains(PostProjectAnalysis.class);
  }

  @Test
  @DisplayName("Test plugin configuration.")
  @SuppressWarnings("unchecked")
  public void testConfiguration() {
    final List<String> properties =
        ((List<Object>) mockContext.getExtensions())
            .stream()
                .filter(obj -> obj instanceof PropertyDefinition)
                .map(obj -> obj.toString())
                .collect(Collectors.toList());

    assertThat(properties)
        .containsExactlyInAnyOrder(PROP_GITEA_LABELS, PROP_GITEA_TOKEN, PROP_GITEA_URL);
  }
}
