package dev.gradleplugins.runnerkit.providers;

import java.util.List;

public interface GradleExecutionCommandLineProvider {
    List<String> getAsArguments();
}
