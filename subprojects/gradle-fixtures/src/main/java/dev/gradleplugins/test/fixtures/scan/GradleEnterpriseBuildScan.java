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
import dev.gradleplugins.test.fixtures.file.TestFile;

import java.util.function.Consumer;

/**
 * Configure build scan in you build under test. It inject `com.gradle.build-scan` plugin version 2.3 inside the plugin block and append the buildScan configuration for agreeing to the public term of service server.
 *
 * <pre>
 * GradleExecuter executer = createExecuter();
 * executer.using(new GradleEnterpriseBuildScan())
 * </pre>
 */
public class GradleEnterpriseBuildScan implements Consumer<GradleExecuter> {
    @Override
    public void accept(GradleExecuter executer) {
        executer.beforeExecute(it -> {
            // TODO: Make sure build scan is not already applied
            // TODO: Support Kotlin DSL

            TestFile buildFile = executer.getTestDirectory().file("build.gradle");
            String content = buildFile.getText();
            // TODO: support multiple space between `plugins` and `{`
            // TODO: It should detect and use the latest build scan version for the distribution to use.
            content = content.replace("plugins {", "plugins {\nid('com.gradle.build-scan') version '2.3'")
                    + "\nbuildScan {\n" +
                    "    termsOfServiceUrl = \"https://gradle.com/terms-of-service\"\n" +
                    "    termsOfServiceAgree = \"yes\"\n" +
                    "}";
            buildFile.setText(content);

            executer.withArgument("--scan");
        });
    }
}
