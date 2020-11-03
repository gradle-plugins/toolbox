package dev.gradleplugins.runnerkit;

import dev.gradleplugins.runnerkit.providers.*;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution;
import lombok.val;
import org.gradle.internal.classloader.ClasspathUtil;
import org.gradle.internal.classpath.ClassPath;
import org.gradle.internal.installation.CurrentGradleInstallation;
import org.gradle.internal.installation.GradleInstallation;
import org.gradle.testkit.runner.internal.GradleProvider;
import org.gradle.testkit.runner.internal.ToolingApiGradleExecutor;

import java.io.File;
import java.util.*;
import java.util.stream.Stream;

import static dev.gradleplugins.fixtures.file.FileSystemUtils.file;
import static dev.gradleplugins.runnerkit.providers.DaemonBaseDirectoryProvider.TEST_KIT_DAEMON_DIR_NAME;
import static dev.gradleplugins.runnerkit.providers.DaemonIdleTimeoutProvider.DEFAULT_TEST_KIT_DAEMON_IDLE_TIMEOUT;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

final class GradleExecutorGradleTestKitImpl extends AbstractGradleExecutor {
    private final ToolingApiGradleExecutor delegate = new ToolingApiGradleExecutor();

    @Override
    protected GradleExecutionResult doRun(GradleExecutionContext parameters) {
        // TODO: TEst
        if (!parameters.getDaemonIdleTimeout().get().equals(DEFAULT_TEST_KIT_DAEMON_IDLE_TIMEOUT)) {
            throw new InvalidRunnerConfigurationException("Custom daemon idle timeout not supported for Gradle TestKit executor");
        }

        // TODO: Test
        if (!parameters.getDaemonBaseDirectory().get().equals(file(parameters.getGradleUserHomeDirectory().get(), TEST_KIT_DAEMON_DIR_NAME))) {
            throw new InvalidRunnerConfigurationException("Custom daemon directory not supported for Gradle TestKit executor");
        }

//        System.out.println("Starting with " + parameters.getAllArguments());

        return new GradleExecutionResultTestKitImpl(delegate.run(asTestKitParameters(parameters)));
    }

    private static org.gradle.testkit.runner.internal.GradleExecutionParameters asTestKitParameters(GradleExecutionContext parameters) {
        return new org.gradle.testkit.runner.internal.GradleExecutionParameters(
                gradleProvider(parameters.getDistribution()),
                parameters.getGradleUserHomeDirectory().get(),
                parameters.getProjectDirectory().orElseGet(parameters.getWorkingDirectory()::get),
                buildArguments(parameters),
                jvmArguments(parameters),
                ClassPath.EMPTY,
                false,
                parameters.getStandardOutput().get(),
                parameters.getStandardError().get(),
                null,
                environmentVariables(parameters));
    }

    //region Gradle provider
    private static GradleProvider gradleProvider(GradleExecutionProvider<GradleDistribution> parameter) {
        return parameter.map(it -> GradleProvider.installation(it.getGradleHomeDirectory())).orElseGet(GradleExecutorGradleTestKitImpl::findGradleInstallFromGradleRunner);
    }

    private static GradleProvider findGradleInstallFromGradleRunner() {
        GradleInstallation gradleInstallation = CurrentGradleInstallation.get();
        if (gradleInstallation == null) {
            String messagePrefix = "Could not find a Gradle installation to use based on the location of the GradleRunner class";
            try {
                File classpathForClass = ClasspathUtil.getClasspathForClass(GradleRunner.class);
                messagePrefix += ": " + classpathForClass.getAbsolutePath();
            } catch (Exception ignore) {
                // ignore
            }
            throw new InvalidRunnerConfigurationException(messagePrefix + ". Please specify a Gradle runtime to use via GradleRunner.withGradleVersion() or similar.");
        }
        return GradleProvider.installation(gradleInstallation.getGradleHome());
    }
    //endregion

    private static List<String> buildArguments(GradleExecutionContext parameters) {
        return parameters.getExecutionParameters().stream().filter(it -> it instanceof GradleExecutionCommandLineProvider && !(it instanceof GradleExecutionJvmSystemPropertyProvider || it instanceof GradleUserHomeDirectoryProvider)).flatMap(GradleExecutorGradleTestKitImpl::asArguments).collect(toList());
    }

    private static List<String> jvmArguments(GradleExecutionContext parameters) {
        val result = new ArrayList<String>();
        result.addAll(parameters.getExecutionParameters().stream().filter(it -> it instanceof GradleExecutionJvmSystemPropertyProvider && !(it instanceof DaemonBaseDirectoryProvider || it instanceof DaemonIdleTimeoutProvider)).flatMap(GradleExecutorGradleTestKitImpl::asArguments).collect(toList()));
        result.addAll(getImplicitBuildJvmArgs());
        return result;
    }

    private static Stream<String> asArguments(GradleExecutionProvider<?> parameter) {
        return ((GradleExecutionCommandLineProvider) parameter).getAsArguments().stream();
    }

    //region Environment variables
    private static Map<String, String> environmentVariables(GradleExecutionContext parameters) {
        return parameters.getEnvironmentVariables().map(GradleExecutorGradleTestKitImpl::toStringValues).orElseGet(Collections::emptyMap);
    }

    private static Map<String, String> toStringValues(Map<String, ?> environmentVariables) {
        return environmentVariables.entrySet().stream().map(it -> new AbstractMap.SimpleImmutableEntry<>(it.getKey(), it.getValue().toString())).collect(toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    //endregion
}
