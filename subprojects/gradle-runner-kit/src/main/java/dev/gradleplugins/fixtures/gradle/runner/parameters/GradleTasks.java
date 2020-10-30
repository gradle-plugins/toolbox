package dev.gradleplugins.fixtures.gradle.runner.parameters;

import com.google.common.collect.ImmutableList;

import java.util.List;

public final class GradleTasks extends GradleExecutionParameterImpl<List<String>> implements GradleExecutionCommandLineParameter<List<String>>, GradleExecutionCollectionParameter<List<String>> {

    public static GradleTasks empty() {
        return fixed(GradleTasks.class, ImmutableList.of());
    }

    public static GradleTasks of(List<String> tasks) {
        return fixed(GradleTasks.class, ImmutableList.copyOf(tasks));
    }

    public GradleTasks plus(Iterable<String> tasks) {
        return fixed(GradleTasks.class, ImmutableList.<String>builder().addAll(get()).addAll(tasks).build());
    }

    @Override
    public List<String> getAsArguments() {
        return get();
    }
}
