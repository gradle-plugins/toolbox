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


import dev.gradleplugins.runnerkit.GradleExecutor
import dev.gradleplugins.runnerkit.GradleRunner
import dev.gradleplugins.spock.lang.CleanupTestDirectory
import dev.gradleplugins.spock.lang.TestNameTestDirectoryProvider
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionFailure
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionResult
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution
import dev.gradleplugins.test.fixtures.gradle.executer.internal.ExecutionResultImpl
import dev.gradleplugins.test.fixtures.maven.M2Installation
import groovy.transform.PackageScope
import org.gradle.util.GradleVersion
import org.junit.Rule
import spock.lang.Specification

import java.util.function.UnaryOperator

// TODO: This should be rename to something else... Given it's a Spock specification we could call it GradleSpecification (the fact that it's Functional is putting the wrong spin to this class).
//    This class should only ties things together for fast starting with gradle, however, each pieces should be usable on it's own and compose into something else if the user wants.
@CleanupTestDirectory
class AbstractGradleSpecification extends Specification {
    public static final String DEFAULT_GRADLE_VERSION_SYSPROP_NAME = "dev.gradleplugins.defaultGradleVersion";
    @Rule
    final TestNameTestDirectoryProvider temporaryFolder = new TestNameTestDirectoryProvider(getClass())
    final M2Installation m2 = new M2Installation(TestFile.of(temporaryFolder.testDirectory))
    GradleRunner executer = newRunner()
    private boolean useKotlinDsl = false

    // TODO: I'm hesitant to keep the following here
    ExecutionResult result
    ExecutionFailure failure

    def setup() {
         executer = m2.isolateMavenLocalRepo(executer)
    }

//    def cleanup() {
//        executer.cleanup()
//    }

    protected void useKotlinDsl() {
        // TODO: Detect when configuration already started to happen and either migrate the configuration or crash.
        //   Leaning more toward crashing.
        useKotlinDsl = true
    }

    protected String getGradleDistributionUnderTest() {
//        if (gradleDistribution != null) {
//            return gradleDistribution
//        }
        String defaultGradleVersionUnderTest = System.getProperty(DEFAULT_GRADLE_VERSION_SYSPROP_NAME, null)
        if (defaultGradleVersionUnderTest == null) {
            return GradleVersion.current().version;
        }
        return defaultGradleVersionUnderTest
    }

    private GradleRunner newRunner() {
        return GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(temporaryFolder.testDirectory).withPluginClasspath().withGradleVersion(getGradleDistributionUnderTest())
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
        return (result = new ExecutionResultImpl(executer.withTasks(tasks).build()))
    }

    protected ExecutionFailure fails(String... tasks) {
        return (result = failure = new ExecutionResultImpl(executer.withTasks(tasks).buildAndFail()))
    }

    protected ExecutionResult run(String... arguments) {
        return succeeds(arguments);
    }

    // TODO: Given the comment on the class, this method should be removed
    @Deprecated
    void assertTasksExecutedAndNotSkipped(String... tasks) {
        assertHasResult()
        result.assertTasksExecutedAndNotSkipped(tasks)
    }

    // TODO: Given the comment on the class, this method should be removed
    @Deprecated
    void assertTasksSkipped(String... tasks) {
        assertHasResult()
        result.assertTasksSkipped(tasks)
    }

    /**
     * NOTE: The method is public so it can align with Trait classes like {@link ArchiveTestFixture}.
     */
    TestFile file(Object... path) {
        return getTestDirectory().file(path)
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
        return TestFile.of(temporaryFolder.testDirectory)
    }

    // TODO: Given the comment on the class, this method should be removed
    boolean outputContains(String string) {
        assertHasResult()
        result.assertOutputContains(string) // TODO: It shouldn't assert here...
    }

    // TODO: I'm hesitent to keep the result and failure variable as they hide some of the internal (the fact that succeeds, fails or run needs to be executed first)
    private void assertHasResult() {
        assert result != null: "result is null, you haven't run succeeds(), fails() or run()"
    }

    protected static String configurePluginClasspathAsBuildScriptDependencies() {
        return """buildscript {
    dependencies {
        classpath files(${implementationClassPath.collect { "'${it.absolutePath.replace('\\', '\\\\')}'" }.join(', ')})
    }
}
"""
    }

    private static Iterable<File> getImplementationClassPath() {
        def prop = new Properties()
        prop.load(AbstractGradleSpecification.getResourceAsStream("/plugin-under-test-metadata.properties"))
        return prop.get("implementation-classpath").toString().split(File.pathSeparator).collect { new File(it) }
    }

    protected GradleRunner using(UnaryOperator<GradleRunner> action) {
        executer = action.apply(executer)
        return executer
    }

    protected GradleRunner usingInitScript(File initScript) {
        return executer = executer.usingInitScript(initScript);
    }

//    // Used by GradleCompatibilityTestRunner
//    private static GradleDistribution gradleDistribution = null
    @PackageScope
    static void useGradleDistribution(GradleDistribution gradleDistribution) {
        throw new UnsupportedOperationException("Not implemented");
//        this.gradleDistribution = gradleDistribution
    }
}
