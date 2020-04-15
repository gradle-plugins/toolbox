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

package dev.gradleplugins.test.fixtures.gradle.executer;

import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.logging.ConsoleOutput;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public interface GradleExecuter {
    /**
     * Sets the working directory to use. Defaults to the test's temporary directory.
     */
    GradleExecuter inDirectory(File directory);

    File getWorkingDirectory();

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
     *
     * @param args the new arguments to use, the old ones are discarded
     * @return a new {@link GradleExecuter} instance with the new configuration.
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

    /**
     * Activates the plugin classpath from the plugins under test.
     */
    GradleExecuter withPluginClasspath();

    /**
     * Sets the user's home dir to use when running the build. Implementations are not 100% accurate.
     */
    GradleExecuter withUserHomeDirectory(File userHomeDirectory);

    /**
     * Sets the <em>Gradle</em> user home dir.
     * Setting to null requests that the executer use the real default Gradle user home dir rather than the default used for testing.
     *
     * <p>Note: does not affect the daemon base dir.</p>
     */
    GradleExecuter withGradleUserHomeDirectory(File gradleUserHomeDirectory);

    /**
     * Sets the environment variables to use when executing the build. Defaults to the environment of this process.
     */
    GradleExecuter withEnvironmentVars(Map<String, ?> environment);

    /**
     * Uses the given settings file by adding {@code "--settings-file"} argument.
     */
    GradleExecuter usingSettingsFile(File settingsFile);

    /**
     * Uses the given build script by adding {@code "--build-file"} argument.
     */
    GradleExecuter usingBuildScript(File buildScript);

    /**
     * Uses the given init script by adding {@code "--init-script"} argument.
     */
    GradleExecuter usingInitScript(File initScript);

    /**
     * Uses the given project directory by adding the {@code "--project-dir"} argument.
     */
    GradleExecuter usingProjectDirectory(File projectDirectory);

    /**
     * The directory that the executer will use for any test specific storage.
     *
     * May or may not be the same directory as the build to be run.
     */
    TestFile getTestDirectory();

    /**
     * Executes the requested build, asserting that the build succeeds. Resets the configuration of this executor.
     *
     * @return The result.
     */
    ExecutionResult run();

    /**
     * Executes the requested build, asserting that the build fails. Resets the configuration of this executer.
     *
     * @return The result.
     */
    ExecutionFailure runWithFailure();

    /**
     * Adds an action to be called immediately before execution, to allow extra configuration to be injected.
     */
    GradleExecuter beforeExecute(Consumer<? super GradleExecuter> action);

    /**
     * Adds an action to be called immediately after execution.
     */
    GradleExecuter afterExecute(Consumer<? super GradleExecuter> action);

    /**
     * Executes the build with {@code "--console=rich, auto, verbose"} argument.
     */
    GradleExecuter withConsole(ConsoleOutput consoleOutput);

    /**
     * Configures a unique gradle user home dir for the test.
     *
     * The gradle user home dir used will be underneath the {@link #getTestDirectory()} directory.
     *
     * <p>Note: does not affect the daemon base dir.</p>
     */
    GradleExecuter requireOwnGradleUserHomeDirectory();

}
