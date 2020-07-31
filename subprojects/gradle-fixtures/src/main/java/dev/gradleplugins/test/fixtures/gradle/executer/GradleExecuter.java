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
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public interface GradleExecuter {
    /**
     * Return the Gradle distribution used by this executer.
     *
     * @return a {@link GradleDistribution} instance used by this executer, never null.
     */
    GradleDistribution getDistribution();

    /**
     * Sets the working directory to use. Defaults to the test's temporary directory.
     */
    GradleExecuter inDirectory(File directory);

    /**
     * Returns the working directory of this executer.
     *
     * @return a directory representing the working directory of this executer.
     */
    File getWorkingDirectory();

    /**
     * Allows to trigger on breakpoints inside the Gradle build when the debugger is attached to the caller.
     *
     * @return a {@link GradleExecuter} instance that will attach the debugger, never null.
     */
    GradleExecuter withDebuggerAttached();

    /**
     * Sets the task names to execute. Defaults to an empty list.
     *
     * @return a {@link GradleExecuter} instance with the specified tasks to execute, never null.
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
    GradleExecuter beforeExecute(Function<? super GradleExecuter, GradleExecuter> action);

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

    /**
     * Disable deprecation warning checks.
     *
     * @return a {@link GradleExecuter} with the new configuration.
     */
    GradleExecuter withoutDeprecationChecks();

    /**
     * Requires that there is a real gradle distribution for the execution, which in-process or tooling API-based execution does not.
     *
     * <p>Note: try to avoid using this method. It has some major drawbacks when it comes to development: 1. It requires a Gradle distribution or installation, and this will need to be rebuilt after
     * each change in order to use the test, and 2. it requires that the build run in a different JVM, which makes it very difficult to debug.</p>
     */
    GradleExecuter requireGradleDistribution();

    /**
     * Returns true if this executer uses a real Gradle distribution for execution.
     *
     * @return {@code true} if this executer uses a real Gradle distribution for execution or {@code false} otherwise.
     */
    boolean usesGradleDistribution();

    /**
     * Configures that any daemons used by the execution are unique to the test.
     *
     * This value is persistent across executions by this executer.
     *
     * <p>Note: this does not affect the Gradle user home directory.</p>
     */
    GradleExecuter requireIsolatedDaemons();

    /**
     * Requires that the build run in a separate daemon process.
     *
     * @return a {@link GradleExecuter} instance configured to run build in a separate daemon process, never null.
     */
    GradleExecuter requireDaemon();

    /**
     * Returns true if this executer uses a daemon.
     *
     * @return {@code true} if the executer uses a daemon or {@code false} otherwise.
     */
    boolean usesDaemon();

    /**
     * Returns true if this executer will share daemons with other executers.
     *
     * @return {@code true} if the executer share daemons with other executers or {@code false} otherwise.
     */
    boolean usesSharedDaemons();

    /**
     * Set the working space for any daemons used by the builds.
     *
     * <p>Note: this does not affect the Gradle user home directory.</p>
     *
     * @return a {@link GradleExecuter} instance configured for the specified daemon base directory, never null.
     */
    GradleExecuter withDaemonBaseDirectory(File daemonBaseDirectory);

    /**
     * Set the the idle time a daemon should live for.
     *
     * @return a {@link GradleExecuter} instance configured for the specified daemon idle time, never null.
     */
    GradleExecuter withDaemonIdleTimeout(Duration daemonIdleTimeout);

    /**
     * Disable crash daemon checks.
     *
     * @return a {@link GradleExecuter} instance configured without checking for daemon crash, never null.
     */
    GradleExecuter withoutDaemonCrashChecks();

    /**
     * Don't set temporary folder explicitly.
     *
     * @return a {@link GradleExecuter} instance configured without explicit temporary directory value, never null.
     */
    GradleExecuter withoutExplicitTemporaryDirectory();

    /**
     * Sets the default character encoding to use.
     *
     * @return a {@link GradleExecuter} instance configured with the specified character encoding, never null.
     * @throws UnsupportedOperationException on non-forking executers
     */
    GradleExecuter withDefaultCharacterEncoding(Charset defaultCharacterEncoding);

    /**
     * Sets the default locale to use.
     *
     * Only makes sense for forking executers.
     *
     * @return a {@link GradleExecuter} instance configured with the specified locale, never null.
     * @throws UnsupportedOperationException on non-forking executers
     */
    GradleExecuter withDefaultLocale(Locale defaultLocale);

    /**
     * Renders the welcome message users see upon first invocation of a Gradle distribution with a given Gradle user home directory.
     * By default the message is never rendered.
     *
     * @return a {@link GradleExecuter} instance configured with the welcome message on first invocation, never null.
     */
    GradleExecuter withWelcomeMessageEnabled();

    /**
     * Publishes build scans to the public enterprise server for each build ran by this executer.
     * Calling this method implicitly accept the Gradle terms and services.
     *
     * @return a {@link GradleExecuter} instance configured to publish build scans for each builds executed, never null.
     */
    GradleExecuter withBuildScanEnabled();
}
