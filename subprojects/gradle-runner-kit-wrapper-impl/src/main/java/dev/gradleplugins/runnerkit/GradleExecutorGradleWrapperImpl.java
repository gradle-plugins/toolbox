package dev.gradleplugins.runnerkit;

import dev.gradleplugins.fixtures.file.FileSystemUtils;
import dev.gradleplugins.runnerkit.distributions.WrapperAwareGradleDistribution;
import dev.gradleplugins.runnerkit.providers.GradleExecutionCommandLineProvider;
import dev.gradleplugins.runnerkit.providers.GradleExecutionEnvironmentVariableProvider;
import dev.gradleplugins.runnerkit.providers.GradleExecutionProvider;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.lang3.SystemUtils;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Arrays.asList;

final class GradleExecutorGradleWrapperImpl extends AbstractGradleExecutor {
    @Override
    protected GradleExecutionResult doRun(GradleExecutionContext parameters) {
        // TODO: Should also check the wrapper distribution is pointing at the right directory?
        //   But what is the right directory?
        if (!(parameters.getDistribution().get() instanceof WrapperAwareGradleDistribution)) {
            throw new InvalidRunnerConfigurationException("The Gradle wrapper executor doesn't support customizing the distribution.");
        }

        if (!parameters.getInjectedClasspath().get().isEmpty()) {
            throw new InvalidRunnerConfigurationException("The Gradle wrapper executor doesn't support injected classpath.");
        }

//        System.out.println("Starting with " + parameters.getAllArguments());

        // Try to reuse the current user wrapper cache.
        val workingDirectory = parameters.getProjectDirectory().orElseGet(parameters.getWorkingDirectory()::get);
        val relativePath = parameters.getGradleUserHomeDirectory().get().toPath().relativize(GradleUserHomeLookup.gradleUserHome().toPath());
        val wrapperProperties = new Properties();
        try (val inStream = new FileInputStream(FileSystemUtils.file(workingDirectory, "gradle/wrapper/gradle-wrapper.properties"))) {
            wrapperProperties.load(inStream);
            wrapperProperties.setProperty("distributionPath", relativePath.toString() + "/wrapper/dists");
            wrapperProperties.setProperty("zipStorePath", relativePath.toString() + "/wrapper/dists");
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        try (val outStream = new FileOutputStream(FileSystemUtils.file(workingDirectory, "gradle/wrapper/gradle-wrapper.properties"))) {
            wrapperProperties.store(outStream, null);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        final ProcessBuilder builder = new ProcessBuilder();
        if (SystemUtils.IS_OS_WINDOWS) {
            builder.command().addAll(asList("cmd", "/c", "gradlew.bat"));
            builder.command().addAll(allArguments(parameters));
        } else {
            builder.command().add("./gradlew");
            builder.command().addAll(allArguments(parameters));
        }
        builder.environment().putAll(environmentVariables(parameters));
        builder.directory(parameters.getWorkingDirectory().get());
        try {
            final Process process = builder.start();
            val output = new ByteArrayOutputStream();
            val stdoutStream = new TeeOutputStream(output, parameters.getStandardOutput().get());
            val stderrStream = new TeeOutputStream(output, parameters.getStandardError().get());
            val outputThreads = new Thread[] {
                    new Thread(copy(process.getInputStream(), stdoutStream)),
                    new Thread(copy(process.getErrorStream(), stderrStream))
            };
            for (Thread thread : outputThreads) {
                thread.start();
            }
            try {
                process.waitFor();
                stdoutStream.flush();
                stderrStream.flush();
                return new GradleExecutionResultProcessImpl(process.exitValue(), output.toString());
            } catch (InterruptedException e) {
                // forcefully interrupt the threads so they naturally exit
                //   we don't try to join the thread here because we were already interrupted
                for (Thread thread : outputThreads) {
                    thread.interrupt();
                }
                throw new RuntimeException(e);
            } finally {
                // tries to join the threads, they should naturally exit after the process finish
                //   given the process's output stream should close.
                for (Thread thread : outputThreads) {
                    thread.join();
                }
            }
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static Runnable copy(InputStream inStream, OutputStream outStream) {
        return () -> {
            try {
                IOUtils.copy(inStream, outStream);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        };
    }

    private static List<String> allArguments(GradleExecutionContext parameters) {
        val result = new ArrayList<String>();
        result.add("-Dorg.gradle.jvmargs=" + getImplicitBuildJvmArgs().stream().map(it -> "'" + it + "'").collect(Collectors.joining(" ")));
        parameters.getExecutionParameters().stream().filter(GradleExecutionCommandLineProvider.class::isInstance).flatMap(GradleExecutorGradleWrapperImpl::asArguments).forEach(result::add);
        return result;
    }

    private static Stream<String> asArguments(GradleExecutionProvider<?> parameter) {
        return ((GradleExecutionCommandLineProvider) parameter).getAsArguments().stream();
    }

    private static Map<String, String> environmentVariables(GradleExecutionContext parameters) {
        val result = new HashMap<String, String>();
        parameters.getEnvironmentVariables().orElse(Collections.emptyMap()).forEach((key, value) -> {
            result.put(key, value.toString());
        });
        result.putAll(((GradleExecutionEnvironmentVariableProvider) parameters.getJavaHome()).getAsEnvironmentVariables());
        result.put("GRADLE_USER_HOME", parameters.getGradleUserHomeDirectory().get().getAbsolutePath());
        return result;
    }
}
