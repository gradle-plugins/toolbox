package dev.gradleplugins.fixtures.gradle.runner.parameters;

import lombok.val;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public final class GradleTasks extends GradleExecutionParameterImpl<List<String>> implements GradleExecutionCommandLineParameter<List<String>>, GradleExecutionCollectionParameter<List<String>> {

    public static GradleTasks empty() {
        return fixed(GradleTasks.class, emptyList());
    }

    public static GradleTasks of(List<String> tasks) {
        return fixed(GradleTasks.class, unmodifiableList(tasks));
    }

    public GradleTasks plus(Iterable<String> tasks) {
        val result = new ArrayList<String>();
        result.addAll(get());
        tasks.forEach(result::add);
        return fixed(GradleTasks.class, unmodifiableList(result));
    }

    @Override
    public List<String> getAsArguments() {
        return get();
    }
}
