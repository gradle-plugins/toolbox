package dev.gradleplugins.test.fixtures.gradle.executer;

import dev.gradleplugins.test.fixtures.file.TestFile;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class GradleWrapperExecuter extends AbstractGradleExecuter {
    private Map<String, Object> environment = null;

    public GradleWrapperExecuter(TestFile testDirectory) {
        super(testDirectory);
    }

    @Override
    protected ExecutionResult doRun() {
        try {
            List<String> command = new ArrayList<>();
            command.add("./gradlew");
            command.addAll(getAllArguments());
            ProcessBuilder processBuilder = new ProcessBuilder().command(command).directory(getWorkingDirectory());
            if (environment != null) {
                processBuilder.environment().putAll(environment.entrySet().stream().map(it -> new HashMap.SimpleEntry<>(it.getKey(), it.getValue().toString())).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
            }
            OutputCapturer standardOutputCapturer = outputCapturerFor(System.out, Charset.defaultCharset());
            OutputCapturer errorOutputCapturer = outputCapturerFor(System.err, Charset.defaultCharset());
            Process process = processBuilder.start();
            Thread outStream = new Thread(new StreamRunner(process.getInputStream(), standardOutputCapturer.getOutputStream()));
            Thread inStream = new Thread(new StreamRunner(process.getErrorStream(), errorOutputCapturer.getOutputStream()));
            outStream.start();
            inStream.start();
            if (0 != process.waitFor()) {
                throw new RuntimeException("Build failure");
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

    @Override
    public GradleExecuter withEnvironmentVars(Map<String, ?> environment) {
        this.environment = new HashMap<>(environment);
        return this;
    }
}
