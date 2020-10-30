package dev.gradleplugins.fixtures.gradle.runner;

import dev.gradleplugins.fixtures.gradle.runner.parameters.*;

import java.util.List;

public interface GradleExecutionContext {
    GradleExecutorType getExecutorType();
    GradleDistribution getDistribution();
    StandardOutput getStandardOutput();
    StandardOutput getStandardError();

    // JVM arguments
    CharacterEncoding getDefaultCharacterEncoding();
    Locale getDefaultLocale();
    WelcomeMessage getWelcomeMessageRendering();
    UserHomeDirectory getUserHomeDirectory();
    DaemonIdleTimeout getDaemonIdleTimeout();
    DaemonBaseDirectory getDaemonBaseDirectory();

    // Command line arguments
    Stacktrace getStacktrace();
    CommandLineArguments getArguments();
    BuildCache getBuildCache();
    SettingsFile getSettingsFile();
    GradleUserHomeDirectory getGradleUserHomeDirectory();
    BuildScript getBuildScript();
    InitScripts getInitScripts();
    ProjectDirectory getProjectDirectory();
    WorkingDirectory getWorkingDirectory();
    ConsoleType getConsoleType();
    DeprecationChecks getDeprecationChecks();
    GradleTasks getTasks();

    BuildScan getBuildScan();
    MissingSettingsFilePolicy getMissingSettingsFilePolicy();
    EnvironmentVariables getEnvironmentVariables();
    JavaHome getJavaHome();

    List<String> getAllArguments();

    List<GradleExecutionParameter<?>> getExecutionParameters();
}
