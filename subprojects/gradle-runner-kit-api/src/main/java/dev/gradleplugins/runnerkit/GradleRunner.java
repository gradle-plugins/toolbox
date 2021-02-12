package dev.gradleplugins.runnerkit;

import java.io.File;
import java.io.Writer;
import java.net.URI;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.UnaryOperator;

public interface GradleRunner {
    // TODO: Maybe have compatible APIs to TestKit Gradle Runner

    static GradleRunner create(GradleExecutor executor) {
        return ClassUtils.newInstance("dev.gradleplugins.runnerkit.GradleRunnerImpl", new Class[] {GradleExecutor.class}, executor);
    }

    /**
     * Configures the runner to execute the build with the version of Gradle specified.
     * <p>
     * Unless previously downloaded, this method will cause the Gradle runtime for the version specified
     * to be downloaded over the Internet from Gradle's distribution servers.
     * The download will be cached beneath the Gradle User Home directory, the location of which is determined by the following in order of precedence:
     * <ol>
     * <li>The system property {@code "gradle.user.home"}</li>
     * <li>The environment variable {@code "GRADLE_USER_HOME"}</li>
     * </ol>
     * <p>
     * If neither are present, {@code "~/.gradle"} will be used, where {@code "~"} is the value advertised by the JVM's {@code "user.dir"} system property.
     * The system property and environment variable are read in the process using the runner, not the build process.
     * <p>
     * Alternatively, you may use {@link #withGradleInstallation(File)} to use an installation already on the filesystem.
     * <p>
     * To use a non standard Gradle runtime, or to obtain the runtime from an alternative location, use {@link #withGradleDistribution(URI)}.
     *
     * @param versionNumber the version number (e.g. "2.9")
     * @return this
     * @see #withGradleInstallation(File)
     * @see #withGradleDistribution(URI)
     */
    GradleRunner withGradleVersion(String versionNumber);

    /**
     * Configures the runner to execute the build using the installation of Gradle specified.
     * <p>
     * The given file must be a directory containing a valid Gradle installation.
     * <p>
     * Alternatively, you may use {@link #withGradleVersion(String)} to use an automatically installed Gradle version.
     *
     * @param installation a valid Gradle installation
     * @return this
     * @see #withGradleVersion(String)
     * @see #withGradleDistribution(URI)
     */
    GradleRunner withGradleInstallation(File installation);

    /**
     * Configures the runner to execute the build using the distribution of Gradle specified.
     * <p>
     * The given URI must point to a valid Gradle distribution ZIP file.
     * This method is typically used as an alternative to {@link #withGradleVersion(String)},
     * where it is preferable to obtain the Gradle runtime from "local" servers.
     * <p>
     * Unless previously downloaded, this method will cause the Gradle runtime at the given URI to be downloaded.
     * The download will be cached beneath the Gradle User Home directory, the location of which is determined by the following in order of precedence:
     * <ol>
     * <li>The system property {@code "gradle.user.home"}</li>
     * <li>The environment variable {@code "GRADLE_USER_HOME"}</li>
     * </ol>
     * <p>
     * If neither are present, {@code "~/.gradle"} will be used, where {@code "~"} is the value advertised by the JVM's {@code "user.dir"} system property.
     * The system property and environment variable are read in the process using the runner, not the build process.
     *
     * @param distribution a URI pointing at a valid Gradle distribution zip file
     * @return this
     * @see #withGradleVersion(String)
     * @see #withGradleInstallation(File)
     */
    GradleRunner withGradleDistribution(URI distribution);

    /**
     * The injected plugin classpath for the build.
     * <p>
     * The returned list is immutable.
     * Returns an empty list if no classpath was provided with {@link #withPluginClasspath(Iterable)}.
     *
     * @return the classpath of plugins to make available to the build under test
     */
    List<? extends File> getPluginClasspath();

    /**
     * Sets the plugin classpath based on the Gradle plugin development plugin conventions.
     * <p>
     * The 'java-gradle-plugin' generates a file describing the plugin under test and makes it available to the test runtime.
     * This method configures the runner to use this file.
     * Please consult the Gradle documentation of this plugin for more information.
     * <p>
     * This method looks for a file named {@code plugin-under-test-metadata.properties} on the runtime classpath,
     * and uses the {@code implementation-classpath} as the classpath, which is expected to a {@link File#pathSeparatorChar} joined string.
     * If the plugin metadata file cannot be resolved an {@link InvalidPluginMetadataException} is thrown.
     * <p>
     * Plugins from classpath are able to be resolved using the <code>plugins { }</code> syntax in the build under test.
     * Please consult the TestKit Gradle User Manual chapter for more information and usage examples.
     * <p>
     * Calling this method will replace any previous classpath specified via {@link #withPluginClasspath(Iterable)} and vice versa.
     * <p>
     * <b>Note:</b> this method will cause an {@link InvalidRunnerConfigurationException} to be emitted when the build is executed,
     * if the version of Gradle executing the build (i.e. not the version of the runner) is earlier than Gradle 2.8 as those versions do not support this feature.
     * Please consult the TestKit Gradle User Manual chapter alternative strategies that can be used for older Gradle versions.
     *
     * @return this
     * @see #withPluginClasspath(Iterable)
     * @see #getPluginClasspath()
     */
    GradleRunner withPluginClasspath() throws InvalidPluginMetadataException;

    /**
     * Sets the injected plugin classpath for the build.
     * <p>
     * Plugins from the given classpath are able to be resolved using the <code>plugins { }</code> syntax in the build under test.
     * Please consult the TestKit Gradle User Manual chapter for more information and usage examples.
     * <p>
     * <b>Note:</b> this method will cause an {@link InvalidRunnerConfigurationException} to be emitted when the build is executed,
     * if the version of Gradle executing the build (i.e. not the version of the runner) is earlier than Gradle 2.8 as those versions do not support this feature.
     * Please consult the TestKit Gradle User Manual chapter alternative strategies that can be used for older Gradle versions.
     *
     * @param classpath the classpath of plugins to make available to the build under test
     * @return this
     * @see #getPluginClasspath()
     */
    GradleRunner withPluginClasspath(Iterable<? extends File> classpath);

    /**
     * Sets the working directory to use. Defaults to the test's temporary directory.
     *
     * @param workingDirectory the working directory to use
     * @return a new {@link GradleRunner} instance configured with the specified working directory, never null.
     */
    GradleRunner inDirectory(File workingDirectory);

    /**
     * The directory that the build will be executed in.
     * <p>
     * This is analogous to the current directory when executing Gradle from the command line.
     *
     * @return the directory to execute the build in
     * @throws InvalidRunnerConfigurationException if the working directory is not configured
     */
    File getWorkingDirectory() throws InvalidRunnerConfigurationException;

    /**
     * Executes the builds without adding the {@code "--stacktrace"} argument.
     *
     * @return a new {@link GradleRunner} instance configured without stacktrace, never null.
     */
    GradleRunner withStacktraceDisabled();

    /**
     * Activates the build cache. Defaults to disabled.
     *
     * @return a {@link GradleRunner} instance configured with build cache, never null.
     */
    GradleRunner withBuildCacheEnabled();

    /**
     * Sets the task names to execute. Defaults to an empty list.
     *
     * @param tasks the tasks to execute
     * @return a new {@link GradleRunner} instance with the specified tasks to execute, never null.
     */
    default GradleRunner withTasks(String... tasks) {
        return withTasks(Arrays.asList(tasks));
    }

    /**
     * Sets the task names to execute. Defaults to an empty list.
     *
     * @param tasks the tasks to execute
     * @return a new {@link GradleRunner} instance with the specified tasks to execute, never null.
     */
    GradleRunner withTasks(List<String> tasks);

    /**
     * Sets the additional command-line arguments to use when executing the build. Defaults to an empty list.
     *
     * @param args the new arguments to use, the old ones are discarded
     * @return a {@link GradleRunner} instance configured with the specified arguments, never null.
     */
    default GradleRunner withArguments(String... args) {
        return withArguments(Arrays.asList(args));
    }

    /**
     * Sets the additional command-line arguments to use when executing the build. Defaults to an empty list.
     *
     * @param args the new arguments to use, the old ones are discarded
     * @return a new {@link GradleRunner} instance configured with the specified arguments, never null.
     */
    GradleRunner withArguments(List<String> args);

    /**
     * Adds an additional command-line argument to use when executing the build.
     *
     * @param arg a new arguments to append to the old ones
     * @return a new {@link GradleRunner} instance configured with the specified argument, never null.
     */
    GradleRunner withArgument(String arg);

    /**
     * The build arguments.
     * <p>
     * Effectively, the command line arguments to Gradle.
     * This includes all tasks, flags, properties etc.
     * <p>
     * The returned list is immutable.
     *
     * @return the build arguments
     */
    List<String> getAllArguments();

    /**
     * Uses the given settings file by adding {@code "--settings-file"} argument.
     *
     * @param settingsFile the settings file to use
     * @return a new {@link GradleRunner} instance configured with the specified settings file, never null.
     */
    GradleRunner usingSettingsFile(File settingsFile);

    /**
     * Does not create an empty settings file when it's missing before execution.
     *
     * @return a new {@link GradleRunner} instance configured to ignore default behavior when settings file is missing, never null.
     */
    GradleRunner ignoresMissingSettingsFile();

    /**
     * Uses the given build script by adding {@code "--build-file"} argument.
     *
     * @param buildScript the build script file to use
     * @return a new {@link GradleRunner} instance configured with the specified build script file, never null.
     */
    GradleRunner usingBuildScript(File buildScript);

    /**
     * Uses the given init script by adding {@code "--init-script"} argument.
     *
     * @param initScript the init script file to use
     * @return a new {@link GradleRunner} instance configured with the specified init script file, never null.
     */
    GradleRunner usingInitScript(File initScript);

    /**
     * Uses the given project directory by adding the {@code "--project-dir"} argument.
     *
     * @param projectDirectory the project directory to use
     * @return a new {@link GradleRunner} instance configured with the specified project directory, never null.
     */
    GradleRunner usingProjectDirectory(File projectDirectory);

    /**
     * Disable deprecation warning checks.
     *
     * @return a new {@link GradleRunner} without deprecation checking enabled, never null..
     */
    GradleRunner withoutDeprecationChecks();

    /**
     * Sets the default character encoding to use.
     *
     * @param defaultCharacterEncoding the default character encoding to use
     * @return a {@link GradleRunner} instance configured with the specified character encoding, never null.
     */
    GradleRunner withDefaultCharacterEncoding(Charset defaultCharacterEncoding);

    /**
     * Sets the default locale to use.
     *
     * @param defaultLocale the default locale to use
     * @return a new {@link GradleRunner} instance configured with the specified locale, never null.
     */
    GradleRunner withDefaultLocale(Locale defaultLocale);

    /**
     * Renders the welcome message users see upon first invocation of a Gradle distribution with a given Gradle user home directory.
     * By default the message is never rendered.
     *
     * @return a new {@link GradleRunner} instance configured with the welcome message on first invocation, never null.
     */
    GradleRunner withWelcomeMessageEnabled();

    /**
     * Publishes build scans to the public enterprise server for each build ran by this executer.
     * Calling this method implicitly accept the Gradle terms and services.
     *
     * @return a new {@link GradleRunner} instance configured to publish build scans for each builds executed, never null.
     */
    GradleRunner publishBuildScans();

    /**
     * Sets the user's home dir to use when running the build. Implementations are not 100% accurate.
     *
     * @param userHomeDirectory the user home directory to use
     * @return a new {@link GradleRunner} instance configured with the specified user home directory, never null.
     */
    GradleRunner withUserHomeDirectory(File userHomeDirectory);

    /**
     * Sets the <em>Gradle</em> user home dir.
     * Setting to null requests that the executer use the real default Gradle user home dir rather than the default used for testing.
     *
     * <p>Note: does not affect the daemon base dir.</p>
     *
     * @param gradleUserHomeDirectory the Gradle user home directory to use
     * @return a new {@link GradleRunner} instance configured with the specified Gradle user home directory, never null.
     */
    GradleRunner withGradleUserHomeDirectory(File gradleUserHomeDirectory);

    /**
     * Configures a unique Gradle user home directory for the test.
     *
     * The Gradle user home directory used will be underneath the {@link #getWorkingDirectory()} directory.
     *
     * <p>Note: does not affect the daemon base dir.</p>
     *
     * @return a new {@link GradleRunner} instance configured with a unique Gradle user home directory, neverl null.
     */
    GradleRunner requireOwnGradleUserHomeDirectory();

    /**
     * Sets the environment variables to use when executing the build. Defaults to the environment of this process.
     *
     * @param environmentVariables the environment variables to use
     * @return a new {@link GradleRunner} instance configured with the specified environment variables, never null.
     */
    GradleRunner withEnvironmentVariables(Map<String, ?> environmentVariables);

    /**
     * Adds an additional environment variable to use when executing the build.
     *
     * @param key the environment variable key
     * @param value the environment variable value
     * @return a new {@link GradleRunner} instance configured with the specified additional environment variable, never null.
     */
    GradleRunner withEnvironmentVariable(String key, String value);

    /**
     * Sets the environment variables to use when executing the build. Defaults to the environment of this process.
     * <p>
     * Convenience method to allow migration from, older, Gradle Executer API.
     *
     * @param environmentVariables the environment variables to use
     * @return a new {@link GradleRunner} instance configured with the specified environment variables, never null.
     * @see #withEnvironmentVariables(Map)
     */
    @Deprecated
    GradleRunner withEnvironmentVars(Map<String, ?> environmentVariables);

    /**
     * Sets the environment variables to use when executing the build. Defaults to the environment of this process.
     * <p>
     * Convenience method to allow migration from Gradle Test Kit API.
     *
     * @param environmentVariables the environment variables to use
     * @return a new {@link GradleRunner} instance configured with the specified environment variables, never null.
     */
    default GradleRunner withEnvironment(Map<String, String> environmentVariables) {
        return withEnvironmentVariables(environmentVariables);
    }

    /**
     * Forces the rich console output.
     *
     * @return a new {@link GradleRunner} instance configured with the rich console enabled, never null.
     */
    GradleRunner withRichConsoleEnabled();

    /**
     * Configures current runner using the specified operator.
     *
     * @return a the {@link GradleRunner} instance returned by the configuration action, never null.
     */
    GradleRunner configure(UnaryOperator<GradleRunner> action);

    /**
     * Configures the runner to forward standard output from builds to the given writer.
     * <p>
     * The output of the build is always available via {@link BuildResult#getOutput()}.
     * This method can be used to additionally capture the output.
     * <p>
     * The given writer will not be closed by the runner.
     * <p>
     * When executing builds with Gradle versions earlier than 2.9 <b>in debug mode</b> and
     * <b>using a tooling API-based executer</b>, any output produced by the build that was written
     * directly to {@code System.out} or {@code System.err} will not be represented in {@link BuildResult#getOutput()}.
     * This is due to a defect that was fixed in Gradle 2.9.
     *
     * @param writer the writer that build standard output should be forwarded to
     * @return this
     * @see #forwardStandardError(Writer)
     */
    GradleRunner forwardStandardOutput(Writer writer);

    /**
     * Provided method for migration convenience from Gradle TestKit.
     *
     * @param writer the writer that build standard output should be forwarded to
     * @return this
     * @see #forwardStandardOutput(Writer)
     */
    default GradleRunner forwardStdOutput(Writer writer) {
        return forwardStandardOutput(writer);
    }

    /**
     * Configures the runner to forward standard error output from builds to the given writer.
     * <p>
     * The output of the build is always available via {@link BuildResult#getOutput()}.
     * This method can be used to additionally capture the error output.
     * <p>
     * The given writer will not be closed by the runner.
     *
     * @param writer the writer that build standard error output should be forwarded to
     * @return this
     * @see #forwardStandardOutput(Writer)
     */
    GradleRunner forwardStandardError(Writer writer);

    /**
     * Provided method for migration convenience from Gradle TestKit.
     *
     * @param writer the writer that build standard error output should be forwarded to
     * @return this
     * @see #forwardStandardError(Writer)
     */
    default GradleRunner forwardStdError(Writer writer) {
        return forwardStandardError(writer);
    }

    /**
     * Forwards the output of executed builds to the {@link System#out System.out} stream.
     * <p>
     * The output of the build is always available via {@link BuildResult#getOutput()}.
     * This method can be used to additionally forward the output to {@code System.out} of the process using the runner.
     * <p>
     * This method does not separate the standard output and error output.
     * The two streams will be merged as they typically are when using Gradle from a command line interface.
     * If you require separation of the streams, you can use {@link #forwardStandardOutput(Writer)} and {@link #forwardStandardError(Writer)} directly.
     * <p>
     * Calling this method will negate the effect of previously calling {@link #forwardStandardOutput(Writer)} and/or {@link #forwardStandardError(Writer)}.
     * <p>
     * Forwarding the output is the process using the runner is the default behavior for all executor.
     * This method is provided for convenience when migrating from Gradle TestKit.
     *
     * @return this
     * @since 2.9
     * @see #forwardStandardOutput(Writer)
     * @see #forwardStandardError(Writer)
     */
    GradleRunner forwardOutput();

    /**
     * Adds an action to modify the Gradle runner before it is executed.
     * The modification to the Gradle runner only affects the current run.
     *
     * @param action the action that will configure the runner further before execution.
     * @return a new {@link GradleRunner} configured with the specified additional action, never null.
     */
    GradleRunner beforeExecute(UnaryOperator<GradleRunner> action);

    /**
     * Adds an action to assert the outcome based on the execution context right after the execution.
     *
     * @param action the action that will assert based on the execution context after execution.
     * @return a new {@link GradleRunner} configured with the specified additional action, never null.
     */
    GradleRunner afterExecute(Consumer<GradleExecutionContext> action);

    /**
     * Executes a build, expecting it to complete without failure.
     *
     * @throws InvalidRunnerConfigurationException if the configuration of this runner is invalid (e.g. project directory not set)
     * @throws UnexpectedBuildFailure if the build does not succeed
     * @return the build result
     */
    BuildResult build() throws InvalidRunnerConfigurationException, UnexpectedBuildFailure;

    /**
     * Executes a build, expecting it to complete with failure.
     *
     * @throws InvalidRunnerConfigurationException if the configuration of this runner is invalid (e.g. project directory not set)
     * @throws UnexpectedBuildSuccess if the build succeeds
     * @return the build result
     */
    BuildResult buildAndFail() throws InvalidRunnerConfigurationException, UnexpectedBuildSuccess;
}
