package dev.gradleplugins.runnerkit.providers;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface GradleExecutionJvmSystemPropertyProvider extends GradleExecutionCommandLineProvider {
    default List<String> getAsArguments() {
        return getAsJvmSystemProperties().entrySet().stream().map(it -> "-D" + it.getKey() + "=" + it.getValue()).collect(Collectors.toList());
    }

    Map<String, String> getAsJvmSystemProperties();
}
