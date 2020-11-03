package dev.gradleplugins.runnerkit;

import dev.gradleplugins.runnerkit.providers.GradleExecutionProvider;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters.GradleExecutionParameter;

import java.io.File;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;

public interface GradleExecutionContext {
    Class<? extends GradleExecutor> getExecutorType();
    GradleExecutionProvider<GradleDistribution> getDistribution();
    GradleExecutionProvider<OutputStream> getStandardOutput();
    GradleExecutionProvider<OutputStream> getStandardError();

    // JVM arguments
    GradleExecutionProvider<Charset> getDefaultCharacterEncoding();
    GradleExecutionProvider<Locale> getDefaultLocale();
    GradleExecutionProvider<WelcomeMessage> getWelcomeMessageRendering();
    GradleExecutionProvider<File> getUserHomeDirectory();
    GradleExecutionProvider<Duration> getDaemonIdleTimeout();
    GradleExecutionProvider<File> getDaemonBaseDirectory();

    // Command line arguments
    GradleExecutionProvider<Stacktrace> getStacktrace();
    GradleExecutionProvider<List<String>> getArguments();
    GradleExecutionProvider<BuildCache> getBuildCache();
    GradleExecutionProvider<File> getSettingsFile();
    GradleExecutionProvider<File> getGradleUserHomeDirectory();
    GradleExecutionProvider<File> getBuildScript();
    GradleExecutionProvider<List<File>> getInitScripts();
    GradleExecutionProvider<File> getProjectDirectory();
    GradleExecutionProvider<File> getWorkingDirectory();
    GradleExecutionProvider<ConsoleType> getConsoleType();
    GradleExecutionProvider<DeprecationChecks> getDeprecationChecks();
    GradleExecutionProvider<List<String>> getTasks();

    GradleExecutionProvider<BuildScan> getBuildScan();
    GradleExecutionProvider<MissingSettingsFilePolicy> getMissingSettingsFilePolicy();
    GradleExecutionProvider<Map<String, ?>> getEnvironmentVariables();
    GradleExecutionProvider<File> getJavaHome();

    List<String> getAllArguments();

    List<GradleExecutionProvider<?>> getExecutionParameters();

    enum ExecutorType {
        UNKNOWN,
        GRADLE_TEST_KIT,
        GRADLE_WRAPPER;

        boolean isGradleTestKit() {
            return equals(GRADLE_TEST_KIT);
        }

        boolean isGradleWrapper() {
            return equals(GRADLE_WRAPPER);
        }
    }

    enum WelcomeMessage {
        ENABLED,
        DISABLED
    }

    enum BuildCache {
        ENABLED, DISABLED
    }

    enum BuildScan {
        ENABLED, DISABLED
    }

    enum Stacktrace {
        SHOW, HIDE
    }

    enum ConsoleType {
        /**
         * Enable color and rich output, regardless of whether the current process is attached to a console or not.
         * When not attached to a console, the color and rich output is encoded using ANSI control characters.
         */
        RICH
    }

    enum DeprecationChecks {
        FAILS
    }

    enum MissingSettingsFilePolicy {
        IGNORES_WHEN_MISSING,
        CREATE_WHEN_MISSING;
    }
}
