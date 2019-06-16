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

package dev.gradleplugins.integtests.fixtures

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.file.TestNameTestDirectoryProvider
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

class AbstractFunctionalSpec extends Specification {
    @Rule
    final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider()

    protected TestFile getProjectDir() {
        return testDirectory
    }

    protected TestFile getBuildFile() {
        return testDirectory.file(getBuildFileName())
    }

    protected File getSettingsFile() {
        return testDirectory.file(getSettingsFileName())
    }

    BuildResult result

    private boolean isBuildCacheEnabled = false

    protected BuildResult build(String... arguments) {
        result = createAndConfigureGradleRunner(arguments).build()
        println(result.output)
        return result
    }

    protected BuildResult succeeds(String... arguments) {
        return build(arguments)
    }

    protected BuildResult run(String... arguments) {
        return build(arguments)
    }

    protected BuildResult buildAndFail(String... arguments) {
        result = createAndConfigureGradleRunner(arguments).buildAndFail()
        println(result.output)
        return result
    }

    private GradleRunner createAndConfigureGradleRunner(String... arguments) {
        def args = arguments.toList();
        args << "-s"
        args << "-i"
        if (isBuildCacheEnabled) {
            args << "--build-cache"
        }
        return GradleRunner.create()
                .withProjectDir(projectDir)
                .withArguments(args)
                .withPluginClasspath()
                .withDebug(true)
    }

    void enableBuildCache() {
        isBuildCacheEnabled = true
    }

    void assertTasksExecutedAndNotSkipped(String... tasks) {
        tasks.each {
            assert result.task(it).outcome in [TaskOutcome.SUCCESS]
        }
    }

    void assertTasksSkipped(String... tasks) {
        tasks.each {
            assert result.task(it).outcome in [TaskOutcome.FROM_CACHE, TaskOutcome.NO_SOURCE, TaskOutcome.SKIPPED, TaskOutcome.UP_TO_DATE]
        }
    }

    protected File file(String relativePath) {
        File file = new File(projectDir, relativePath)
        return file
    }

    protected String getBuildFileName() {
        return "build.gradle"
    }

    protected String getSettingsFileName() {
        return "settings.gradle"
    }

    protected TestFile getTestDirectory() {
        temporaryFolder.testDirectory
    }
}
