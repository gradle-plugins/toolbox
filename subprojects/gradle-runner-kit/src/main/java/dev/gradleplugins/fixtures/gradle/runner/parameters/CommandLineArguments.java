package dev.gradleplugins.fixtures.gradle.runner.parameters;

import lombok.val;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public final class CommandLineArguments extends GradleExecutionParameterImpl<List<String>> implements GradleExecutionCommandLineParameter<List<String>>, GradleExecutionCollectionParameter<List<String>> {

    public static CommandLineArguments empty() {
        return fixed(CommandLineArguments.class, emptyList());
    }

    public static CommandLineArguments of(List<String> arguments) {
        return fixed(CommandLineArguments.class, unmodifiableList(arguments));
    }

    public CommandLineArguments plus(String argument) {
        val result = new ArrayList<String>(get());
        result.add(argument);
        return fixed(CommandLineArguments.class, unmodifiableList(result));
    }

    @Override
    public List<String> getAsArguments() {
        return get();
    }
}
