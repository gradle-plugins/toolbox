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

package dev.gradleplugins.integtests.fixtures.executer;

import dev.gradleplugins.test.fixtures.file.TestDirectoryProvider;
import org.gradle.testkit.runner.BuildResult;

import java.io.File;
import java.util.List;

public interface GradleExecuter {
    /**
     * Sets the working directory to use. Defaults to the test's temporary directory.
     */
    GradleExecuter inDirectory(File directory);

    /**
     * Allows to trigger on breakpoints inside the Gradle build when the debugger is attached to the caller.
     */
    GradleExecuter withDebuggerAttached();

    /**
     * Sets the task names to execute. Defaults to an empty list.
     */
    GradleExecuter withTasks(String... tasks);

    /**
     * Sets the task names to execute. Defaults to an empty list.
     */
    GradleExecuter withTasks(List<String> tasks);

    /**
     * Sets the additional command-line arguments to use when executing the build. Defaults to an empty list.
     */
    GradleExecuter withArguments(String... args);

    /**
     * Sets the additional command-line arguments to use when executing the build. Defaults to an empty list.
     */
    GradleExecuter withArguments(List<String> args);

    /**
     * Adds an additional command-line argument to use when executing the build.
     */
    GradleExecuter withArgument(String arg);

    /**
     * Executes the builds without adding the {@code "--stacktrace"} argument.
     */
    GradleExecuter withStacktraceDisabled();

    /**
     * Activates the build cache. Defaults to false.
     */
    GradleExecuter withBuildCacheEnabled();

    GradleExecuter usingSettingsFile(File settingsFile);

    /**
     * The directory that the executer will use for any test specific storage.
     *
     * May or may not be the same directory as the build to be run.
     */
    TestDirectoryProvider getTestDirectoryProvider();

    /**
     * Executes the requested build, asserting that the build succeeds. Resets the configuration of this executor.
     *
     * @return The result.
     */
    BuildResult run();

    /**
     * Executes the requested build, asserting that the build fails. Resets the configuration of this executer.
     *
     * @return The result.
     */
    BuildResult runWithFailure();
}
