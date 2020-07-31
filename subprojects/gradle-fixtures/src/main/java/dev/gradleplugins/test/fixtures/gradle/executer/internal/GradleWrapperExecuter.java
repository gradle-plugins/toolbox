package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.executer.*;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

public class GradleWrapperExecuter extends AbstractGradleExecuter {
    public GradleWrapperExecuter(GradleDistribution distribution, TestFile testDirectory, GradleExecuterBuildContext buildContext) {
        super(distribution, testDirectory, buildContext);
    }

    private GradleWrapperExecuter(TestFile testDirectory, GradleExecutionParameters configuration) {
        super(testDirectory, configuration);
    }

    @Override
    protected GradleExecuter newInstance(TestFile testDirectory, GradleExecutionParameters configuration) {
        return new GradleWrapperExecuter(testDirectory, configuration);
    }

    @Override
    public GradleExecuter requireGradleDistribution() {
        return this; // already downloading its own distribution via Gradle wrapper
    }

    @Override
    public boolean usesGradleDistribution() {
        return true;
    }

    @Override
    protected ExecutionResult doRun() {
        try {
            List<String> command = new ArrayList<>();
            if (SystemUtils.IS_OS_WINDOWS) {
                command.addAll(Arrays.asList("cmd", "/c", "gradlew.bat"));
            } else {
                command.add("./gradlew");
            }
            command.addAll(getAllArguments());
            ProcessBuilder processBuilder = new ProcessBuilder().command(command).directory(getWorkingDirectory());
            processBuilder.environment().putAll(configuration.getEnvironment().getAsMap());
            OutputCapturer standardOutputCapturer = outputCapturerFor(System.out, Charset.defaultCharset());
            OutputCapturer errorOutputCapturer = outputCapturerFor(System.err, Charset.defaultCharset());
            Process process = processBuilder.start();
            Thread outStream = new Thread(new StreamRunner(process.getInputStream(), standardOutputCapturer.getOutputStream()));
            Thread inStream = new Thread(new StreamRunner(process.getErrorStream(), errorOutputCapturer.getOutputStream()));
            outStream.start();
            inStream.start();
            if (0 != process.waitFor()) {
                throw new RuntimeException("Build failure (" + process.exitValue() + ")");
            }
            outStream.join();
            inStream.join();
            return new OutputScrapingExecutionResult(LogContent.of(standardOutputCapturer.getOutputAsString()), LogContent.of(errorOutputCapturer.getOutputAsString()), true);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private static class StreamRunner implements Runnable {
        private final InputStream inputStream;
        private final OutputStream outputStream;

        public StreamRunner(InputStream inputStream, OutputStream outputStream) {
            this.inputStream = inputStream;
            this.outputStream = outputStream;
        }

        @Override
        public void run() {
            try {
                IOUtils.copy(inputStream, outputStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static OutputCapturer outputCapturerFor(PrintStream stream, Charset outputEncoding) {
        return new OutputCapturer(stream, outputEncoding);
    }

    @Override
    protected ExecutionFailure doRunWithFailure() {
        return null;
    }

    @Override
    public GradleExecuter withDebuggerAttached() {
        return null;
    }

    @Override
    public GradleExecuter withPluginClasspath() {
        return null;
    }
}
