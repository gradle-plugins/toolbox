/*
 * Copyright 2012 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter;
import org.gradle.util.GradleVersion;

public class DefaultGradleDistribution extends AbstractGradleDistribution {
    private final GradleVersion version;
    private final TestFile gradleHomeDir;
    private final TestFile binDistribution;

    public DefaultGradleDistribution(GradleVersion gradleVersion, TestFile gradleHomeDir, TestFile binDistribution) {
        this.version = gradleVersion;
        this.gradleHomeDir = gradleHomeDir;
        this.binDistribution = binDistribution;
    }

    @Override
    public String toString() {
        return version.toString();
    }

    @Override
    public TestFile getGradleHomeDirectory() {
        return gradleHomeDir;
    }

    @Override
    public TestFile getBinaryDirectory() {
        return binDistribution;
    }

    @Override
    public GradleVersion getVersion() {
        return version;
    }

    @Override
    public GradleExecuter executer(TestFile testDirectoryProvider/*, IntegrationTestBuildContext buildContext*/) {
//        return new NoDaemonGradleExecuter(this, testDirectoryProvider, version, buildContext).withWarningMode(null);
        return null;
    }
}
