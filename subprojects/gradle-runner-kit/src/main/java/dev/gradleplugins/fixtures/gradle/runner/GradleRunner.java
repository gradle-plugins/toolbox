package dev.gradleplugins.fixtures.gradle.runner;

import java.io.File;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public interface GradleRunner {
    // TODO: Maybe have compatible APIs to TestKit Gradle Runner

    static GradleRunner create(GradleExecutor executor) {
        return new GradleRunnerImpl(executor);
    }

    /**
     * Sets the working directory to use. Defaults to the test's temporary directory.
     *
     * @param workingDirectory the working directory to use
     * @return a new {@link GradleRunner} instance configured with the specified working directory, never null.
     */
    GradleRunner inDirectory(File workingDirectory);

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
     * Sets the environment variables to use when executing the build. Defaults to the environment of this process.
     *
     * @param environment the environment variables to use
     * @return a new {@link GradleRunner} instance configured with the specified environment variables, never null.
     */
    GradleRunner withEnvironmentVariables(Map<String, ?> environment);

    /**
     * Forces the rich console output.
     *
     * @return a new {@link GradleRunner} instance configured with the rich console enabled, never null.
     */
    GradleRunner withRichConsoleEnabled();

    /**
     * Configures the runner to forward standard output from builds to the given writer.
     * <p>
     * The output of the build is always available via {@link GradleBuildResult#getOutput()}.
     * This method can be used to additionally capture the output.
     * <p>
     * The given writer will not be closed by the runner.
     * <p>
     * When executing builds with Gradle versions earlier than 2.9 <b>in debug mode</b> and
     * <b>using a tooling API-based executer</b>, any output produced by the build that was written
     * directly to {@code System.out} or {@code System.err} will not be represented in {@link GradleBuildResult#getOutput()}.
     * This is due to a defect that was fixed in Gradle 2.9.
     *
     * @param writer the writer that build standard output should be forwarded to
     * @return this
     * @since 2.9
     * @see #forwardStandardError(Writer)
     */
    GradleRunner forwardStandardOutput(Writer writer);

    default GradleRunner forwardStdOutput(Writer writer) {
        return forwardStandardOutput(writer);
    }

    /**
     * Configures the runner to forward standard error output from builds to the given writer.
     * <p>
     * The output of the build is always available via {@link GradleBuildResult#getOutput()}.
     * This method can be used to additionally capture the error output.
     * <p>
     * The given writer will not be closed by the runner.
     *
     * @param writer the writer that build standard error output should be forwarded to
     * @return this
     * @since 2.9
     * @see #forwardStandardOutput(Writer)
     */
    GradleRunner forwardStandardError(Writer writer);

    default GradleRunner forwardStdError(Writer writer) {
        return forwardStandardError(writer);
    }

    GradleRunner forwardOutput();

//    /**
//     * Sets the plugin classpath based on the Gradle plugin development plugin conventions.
//     * <p>
//     * The 'java-gradle-plugin' generates a file describing the plugin under test and makes it available to the test runtime.
//     * This method configures the runner to use this file.
//     * Please consult the Gradle documentation of this plugin for more information.
//     * <p>
//     * This method looks for a file named {@code plugin-under-test-metadata.properties} on the runtime classpath,
//     * and uses the {@code implementation-classpath} as the classpath, which is expected to a {@link File#pathSeparatorChar} joined string.
//     * If the plugin metadata file cannot be resolved an {@link InvalidPluginMetadataException} is thrown.
//     * <p>
//     * Plugins from classpath are able to be resolved using the <code>plugins { }</code> syntax in the build under test.
//     * Please consult the TestKit Gradle User Manual chapter for more information and usage examples.
//     * <p>
//     * Calling this method will replace any previous classpath specified via {@link #withPluginClasspath(Iterable)} and vice versa.
//     * <p>
//     * <b>Note:</b> this method will cause an {@link InvalidRunnerConfigurationException} to be emitted when the build is executed,
//     * if the version of Gradle executing the build (i.e. not the version of the runner) is earlier than Gradle 2.8 as those versions do not support this feature.
//     * Please consult the TestKit Gradle User Manual chapter alternative strategies that can be used for older Gradle versions.
//     *
//     * @return this
//     * @see #withPluginClasspath(Iterable)
////     * @see #getPluginClasspath()
//     * @since 2.13
//     */
//    public abstract GradleRunner withPluginClasspath();// throws InvalidPluginMetadataException;
//
//    /**
//     * Sets the injected plugin classpath for the build.
//     * <p>
//     * Plugins from the given classpath are able to be resolved using the <code>plugins { }</code> syntax in the build under test.
//     * Please consult the TestKit Gradle User Manual chapter for more information and usage examples.
//     * <p>
//     * <b>Note:</b> this method will cause an {@link InvalidRunnerConfigurationException} to be emitted when the build is executed,
//     * if the version of Gradle executing the build (i.e. not the version of the runner) is earlier than Gradle 2.8 as those versions do not support this feature.
//     * Please consult the TestKit Gradle User Manual chapter alternative strategies that can be used for older Gradle versions.
//     *
//     * @param classpath the classpath of plugins to make available to the build under test
//     * @return this
//     * @see #getPluginClasspath()
//     * @since 2.8
//     */
//    public abstract GradleRunner withPluginClasspath(Iterable<? extends File> classpath);

    /**
     * Executes a build, expecting it to complete without failure.
     *
     * @throws InvalidRunnerConfigurationException if the configuration of this runner is invalid (e.g. project directory not set)
     * @throws UnexpectedBuildFailure if the build does not succeed
     * @return the build result
     */
    GradleBuildResult build() throws InvalidRunnerConfigurationException, UnexpectedBuildFailure;

    /**
     * Executes a build, expecting it to complete with failure.
     *
     * @throws InvalidRunnerConfigurationException if the configuration of this runner is invalid (e.g. project directory not set)
     * @throws UnexpectedBuildSuccess if the build succeeds
     * @return the build result
     */
    GradleBuildResult buildAndFail() throws InvalidRunnerConfigurationException, UnexpectedBuildSuccess;
}
