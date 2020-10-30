package dev.gradleplugins.fixtures.gradle.runner;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import dev.gradleplugins.fixtures.gradle.runner.parameters.GradleExecutionCommandLineParameter;
import dev.gradleplugins.fixtures.gradle.runner.parameters.GradleExecutionParameter;
import dev.gradleplugins.test.fixtures.gradle.executer.internal.WrapperGradleDistribution;
import dev.nokee.core.exec.*;
import lombok.val;
import org.apache.commons.lang3.SystemUtils;
import org.gradle.wrapper.GradleUserHomeLookup;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.gradleplugins.fixtures.file.FileSystemUtils.file;
import static dev.nokee.core.exec.CommandLineToolInvocationEnvironmentVariables.inherit;

final class GradleExecutorGradleWrapperImpl extends AbstractGradleExecutor {
    @Override
    protected GradleExecutionResult doRun(GradleExecutionContext parameters) {
        if (!(parameters.getDistribution().get() instanceof WrapperGradleDistribution)) {
            throw new InvalidRunnerConfigurationException("The Gradle wrapper executor doesn't support customizing the distribution");
        }

//        System.out.println("Starting with " + parameters.getAllArguments());

        val workingDirectory = parameters.getProjectDirectory().orElseGet(parameters.getWorkingDirectory()::get);
        val relativePath = parameters.getGradleUserHomeDirectory().get().toPath().relativize(GradleUserHomeLookup.gradleUserHome().toPath());
        val wrapperProperties = new Properties();
        try (val inStream = new FileInputStream(file(workingDirectory, "gradle/wrapper/gradle-wrapper.properties"))) {
            wrapperProperties.load(inStream);
            wrapperProperties.setProperty("distributionPath", relativePath.toString() + "/wrapper/dists");
            wrapperProperties.setProperty("zipStorePath", relativePath.toString() + "/wrapper/dists");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try (val outStream = new FileOutputStream(file(workingDirectory, "gradle/wrapper/gradle-wrapper.properties"))) {
            wrapperProperties.store(outStream, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        CommandLine command = null;
        if (SystemUtils.IS_OS_WINDOWS) {
            command = CommandLine.of(ImmutableList.<String>builder().add("cmd", "/c", "gradlew.bat").addAll(allArguments(parameters)).build());
        } else {
            command = CommandLine.of(ImmutableList.<String>builder().add("./gradlew").addAll(allArguments(parameters)).build());
        }
        val result = command.newInvocation()
                .withEnvironmentVariables(environmentVariables(parameters))
                .workingDirectory(parameters.getWorkingDirectory().get())
                .redirectStandardOutput(CommandLineToolInvocationStandardOutputRedirect.forwardTo(parameters.getStandardOutput().get()))
                .redirectErrorOutput(CommandLineToolInvocationErrorOutputRedirect.forwardTo(parameters.getStandardError().get()))
                .buildAndSubmit(new ProcessBuilderEngine())
                .waitFor();

        return new GradleExecutionResultNokeeExecImpl(result);
    }

    private static List<String> allArguments(GradleExecutionContext parameters) {
        return ImmutableList.copyOf(Iterables.concat(ImmutableList.of("-Dorg.gradle.jvmargs=" + String.join(" ", getImplicitBuildJvmArgs().stream().map(it -> "'" + it + "'").collect(Collectors.toList()))), parameters.getExecutionParameters().stream().filter(GradleExecutionCommandLineParameter.class::isInstance).flatMap(GradleExecutorGradleWrapperImpl::asArguments).collect(Collectors.toList())));
    }

    private static Stream<String> asArguments(GradleExecutionParameter<?> parameter) {
        return ((GradleExecutionCommandLineParameter<?>) parameter).getAsArguments().stream();
    }

    private static CommandLineToolInvocationEnvironmentVariables environmentVariables(GradleExecutionContext parameters) {
        return parameters.getEnvironmentVariables().map(CommandLineToolInvocationEnvironmentVariables::from).orElse(inherit()).plus(CommandLineToolInvocationEnvironmentVariables.from(parameters.getJavaHome().getAsEnvironmentVariables()));
    }

//    public GradleHandle start(GradleExecutionParameters parameters) {
//
//    }
}
