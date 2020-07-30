package dev.gradleplugins.test.fixtures.gradle.executer.internal;

import dev.gradleplugins.test.fixtures.file.TestFile;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionFailure;
import dev.gradleplugins.test.fixtures.gradle.executer.ExecutionResult;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistribution;
import dev.gradleplugins.test.fixtures.gradle.executer.GradleExecuter;
import lombok.RequiredArgsConstructor;
import lombok.val;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OutOfProcessGradleExecuter extends AbstractGradleExecuter {
    public OutOfProcessGradleExecuter(GradleDistribution distribution, TestFile testDirectory, GradleExecuterBuildContext buildContext) {
        super(distribution, testDirectory, buildContext);
    }

    OutOfProcessGradleExecuter(TestFile testDirectory, GradleExecuterConfiguration configuration) {
        super(testDirectory, configuration);
    }

    @Override
    protected GradleExecuter newInstance(TestFile testDirectory, GradleExecuterConfiguration configuration) {
        return new OutOfProcessGradleExecuter(testDirectory, configuration);
    }

    @Override
    public GradleExecuter requireGradleDistribution() {
        return this;
    }

    @Override
    public boolean usesGradleDistribution() {
        return true;
    }

    @RequiredArgsConstructor
    private static class GradleHandle {
        private final Process process;
        private final Thread stdout;
        private final Thread stderr;
        private final Supplier<String> output;
        private final Supplier<String> error;

        public int waitFor() {
            try {
                return process.waitFor();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    protected GradleHandle createHandle() {
        try {
            List<String> command = new ArrayList<>();
            if (SystemUtils.IS_OS_WINDOWS) {
                command.addAll(Arrays.asList("cmd", "/c", getDistribution().getGradleHomeDirectory().file("bin/gradle.bat").getAbsolutePath()));
            } else {
                command.add(getDistribution().getGradleHomeDirectory().file("bin/gradle").getAbsolutePath());
            }
            command.addAll(getAllArguments());
            ProcessBuilder processBuilder = new ProcessBuilder().command(command).directory(getWorkingDirectory());
            if (!configuration.getEnvironment().isEmpty()) {
                processBuilder.environment().putAll(configuration.getEnvironment().entrySet().stream().map(it -> new HashMap.SimpleEntry<>(it.getKey(), it.getValue().toString())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            }
            OutputCapturer standardOutputCapturer = outputCapturerFor(System.out, Charset.defaultCharset());
            OutputCapturer errorOutputCapturer = outputCapturerFor(System.err, Charset.defaultCharset());
            Process process = processBuilder.start();
            Thread outStream = new Thread(new StreamRunner(process.getInputStream(), standardOutputCapturer.getOutputStream()));
            Thread errStream = new Thread(new StreamRunner(process.getErrorStream(), errorOutputCapturer.getOutputStream()));
            outStream.start();
            errStream.start();
            return new GradleHandle(process, outStream, errStream, standardOutputCapturer::getOutputAsString, errorOutputCapturer::getOutputAsString);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected ExecutionResult doRun() {
        try {
            val handle = createHandle();
            if (0 != handle.waitFor()) {
                throw new RuntimeException("Build failure (" + handle.process.exitValue() + ")");
            }
            handle.stdout.join();
            handle.stderr.join();
            return new OutputScrapingExecutionResult(LogContent.of(handle.output.get()), LogContent.of(handle.error.get()), true);
        } catch (InterruptedException e) {
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
        try {
            val handle = createHandle();
            if (0 == handle.waitFor()) {
                throw new RuntimeException("Build succeeded (" + handle.process.exitValue() + ")");
            }
            handle.stdout.join();
            handle.stderr.join();
            return new OutputScrapingExecutionFailure(handle.output.get(), handle.error.get(), true);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public GradleExecuter withDebuggerAttached() {
        throw new UnsupportedOperationException(); // Not at the moment
    }

    @Override
    public GradleExecuter withPluginClasspath() {
        throw new UnsupportedOperationException(); // Should pass in classpath to Gradle via batch script
    }
}
