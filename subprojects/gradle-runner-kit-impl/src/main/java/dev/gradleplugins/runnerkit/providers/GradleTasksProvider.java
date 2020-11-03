package dev.gradleplugins.runnerkit.providers;

import lombok.val;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public final class GradleTasksProvider extends AbstractGradleExecutionProvider<List<String>> implements GradleExecutionCommandLineProvider {

    public static GradleTasksProvider empty() {
        return fixed(GradleTasksProvider.class, emptyList());
    }

    public static GradleTasksProvider of(List<String> tasks) {
        return fixed(GradleTasksProvider.class, unmodifiableList(tasks));
    }

    public GradleTasksProvider plus(Iterable<String> tasks) {
        val result = new ArrayList<String>();
        result.addAll(get());
        tasks.forEach(result::add);
        return fixed(GradleTasksProvider.class, unmodifiableList(result));
    }

    @Override
    public List<String> getAsArguments() {
        return get();
    }
}
