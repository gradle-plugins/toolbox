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

import dev.gradleplugins.integtests.fixtures.executer.ExecutionFailure
import dev.gradleplugins.integtests.fixtures.executer.ExecutionResult
import dev.gradleplugins.integtests.fixtures.executer.GradleExecuter
import dev.gradleplugins.integtests.fixtures.executer.GradleRunnerExecuter
import dev.gradleplugins.test.fixtures.file.CleanupTestDirectory
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.file.TestNameTestDirectoryProvider
import dev.gradleplugins.test.fixtures.maven.M2Installation
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Rule
import spock.lang.Specification

import java.util.function.Consumer

@CleanupTestDirectory
class AbstractFunctionalSpec extends Specification {
    @Rule
    final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider()
    final M2Installation m2 = new M2Installation(temporaryFolder)
    GradleExecuter executer = createExecuter()
    ExecutionResult result
    ExecutionFailure failure
    private boolean useKotlinDsl = false

    def setup() {
         m2.isolateMavenLocalRepo(executer)
    }

    protected void useKotlinDsl() {
        // TODO: Detect when configuration already started to happen and either migrate the configuration or crash.
        //   Leaning more toward crashing.
        useKotlinDsl = true
    }

    private GradleExecuter createExecuter() {
        return new GradleRunnerExecuter(temporaryFolder).withPluginClasspath()
    }

    protected TestFile getProjectDir() {
        return testDirectory
    }

    protected TestFile getBuildFile() {
        return testDirectory.file(getBuildFileName())
    }

    protected TestFile getSettingsFile() {
        return testDirectory.file(getSettingsFileName())
    }

    protected ExecutionResult succeeds(String... tasks) {
        return (result = executer.withTasks(tasks).run())
    }

    protected ExecutionFailure fails(String... tasks) {
        return (result = failure = executer.withTasks(tasks).runWithFailure())
    }

    protected ExecutionResult run(String... arguments) {
        return succeeds(arguments);
    }

    void assertTasksExecutedAndNotSkipped(String... tasks) {
        assertHasResult()
        result.assertTasksExecutedAndNotSkipped(tasks)
    }

    void assertTasksSkipped(String... tasks) {
        assertHasResult()
        result.assertTasksSkipped(tasks)
    }

    protected TestFile file(String relativePath) {
        return getTestDirectory().file(relativePath)
    }

    protected String getBuildFileName() {
        if (useKotlinDsl) {
            return "build.gradle.kts"
        }
        return "build.gradle"
    }

    protected String getSettingsFileName() {
        if (useKotlinDsl) {
            return "settings.gradle.kts"
        }
        return "settings.gradle"
    }

    protected TestFile getTestDirectory() {
        temporaryFolder.testDirectory
    }

    boolean outputContains(String string) {
        assertHasResult()
        result.assertOutputContains(string)
    }

    private void assertHasResult() {
        assert result != null: "result is null, you haven't run succeeds(), fails() or run()"
    }

    protected static String configurePluginClasspathAsBuildScriptDependencies() {
        return """buildscript {
    dependencies {
        classpath files(${implementationClassPath.collect { "'$it'" }.join(', ')})
    }
}
"""
    }

    private static Iterable<File> getImplementationClassPath() {
        def prop = new Properties()
        prop.load(AbstractFunctionalSpec.getResourceAsStream("/plugin-under-test-metadata.properties"))
        return prop.get("implementation-classpath").toString().split(File.pathSeparator).collect { new File(it) }
    }

    protected GradleExecuter using(Consumer<GradleExecuter> action) {
        action.accept(executer)
        return executer
    }
}
