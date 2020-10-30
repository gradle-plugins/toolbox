package dev.gradleplugins.fixtures.gradle.runner.parameters;

import com.google.common.collect.ImmutableList;

import java.util.List;

public final class CommandLineArguments extends GradleExecutionParameterImpl<List<String>> implements GradleExecutionCommandLineParameter<List<String>>, GradleExecutionCollectionParameter<List<String>> {

    public static CommandLineArguments empty() {
        return fixed(CommandLineArguments.class, ImmutableList.of());
    }

    public static CommandLineArguments of(List<String> arguments) {
        return fixed(CommandLineArguments.class, ImmutableList.copyOf(arguments));
    }

    public CommandLineArguments plus(String argument) {
        return fixed(CommandLineArguments.class, ImmutableList.<String>builder().addAll(get()).add(argument).build());
    }

    @Override
    public List<String> getAsArguments() {
        return get();
    }
}
