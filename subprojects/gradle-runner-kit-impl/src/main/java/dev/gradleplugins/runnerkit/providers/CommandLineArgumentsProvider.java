package dev.gradleplugins.runnerkit.providers;

import lombok.val;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;

public final class CommandLineArgumentsProvider extends AbstractGradleExecutionProvider<List<String>> implements GradleExecutionCommandLineProvider {

    public static CommandLineArgumentsProvider empty() {
        return fixed(CommandLineArgumentsProvider.class, emptyList());
    }

    public static CommandLineArgumentsProvider of(List<String> arguments) {
        return fixed(CommandLineArgumentsProvider.class, unmodifiableList(arguments));
    }

    public CommandLineArgumentsProvider plus(String argument) {
        val result = new ArrayList<String>(get());
        result.add(argument);
        return fixed(CommandLineArgumentsProvider.class, unmodifiableList(result));
    }

    @Override
    public List<String> getAsArguments() {
        return get();
    }
}
