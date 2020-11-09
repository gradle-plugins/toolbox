/*
 * Copyright 2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.test.fixtures.scan;

import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.util.function.UnaryOperator;

/**
 * Configure build scan in you build under test. It inject `com.gradle.build-scan` plugin version 2.3 inside the plugin block and append the buildScan configuration for agreeing to the public term of service server.
 *
 * <pre>
 * GradleExecuter executer = createExecuter();
 * executer.using(new GradleEnterpriseBuildScan())
 * </pre>
 */
@Deprecated
public class GradleEnterpriseBuildScan implements UnaryOperator<GradleExecuter> {
    @Override
    public GradleExecuter apply(GradleExecuter executer) {
        return executer.beforeExecute(it -> {
            try (PrintWriter out = new PrintWriter(new FileOutputStream(executer.getTestDirectory().file("build-scan.init.gradle")))) {
                out.println("import org.gradle.util.GradleVersion");
                out.println("");
                out.println("def isTopLevelBuild = gradle.getParent() == null");
                out.println("");
                out.println("if (isTopLevelBuild) {");
                out.println("    def gradleVersion = GradleVersion.current().baseVersion");
                out.println("    def atLeastGradle5 = gradleVersion >= GradleVersion.version('5.0')");
                out.println("    def atLeastGradle6 = gradleVersion >= GradleVersion.version('6.0')");
                out.println("");
                out.println("    if (atLeastGradle6) {");
                out.println("        settingsEvaluated {");
                out.println("            if (it.pluginManager.hasPlugin('com.gradle.enterprise')) {");
                out.println("               configureExtension(it.extensions['gradleEnterprise'].buildScan)");
                out.println("            }");
                out.println("        }");
                out.println("    } else if (atLeastGradle5) {");
                out.println("        rootProject {");
                out.println("            if (it.pluginManager.hasPlugin('com.gradle.build-scan')) {");
                out.println("               configureExtension(extensions['buildScan'])");
                out.println("            }");
                out.println("        }");
                out.println("    }");
                out.println("}");
                out.println("");
                out.println("void configureExtension(extension) {");
                out.println("    extension.with {");
                out.println("        termsOfServiceUrl = 'https://gradle.com/terms-of-service'");
                out.println("        termsOfServiceAgree = 'yes'");
                out.println("    }");
                out.println("}");
            } catch (FileNotFoundException e) {
                throw new UncheckedIOException(e);
            }
            return it;
        }).withArgument("--init-script")
                .withArgument(executer.getTestDirectory().file("build-scan.init.gradle").getAbsolutePath())
                .withArgument("--scan");
    }
}
