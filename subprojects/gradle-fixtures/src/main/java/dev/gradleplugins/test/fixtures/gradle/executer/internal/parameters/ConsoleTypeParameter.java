package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import dev.gradleplugins.test.fixtures.gradle.logging.ConsoleOutput;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Deprecated
public final class ConsoleTypeParameter extends GradleExecutionParameterImpl<ConsoleOutput> implements CommandLineGradleExecutionParameter<ConsoleOutput>, GradleExecutionParameter<ConsoleOutput> {
    public static ConsoleTypeParameter unset() {
        return noValue(ConsoleTypeParameter.class);
    }

    public static ConsoleTypeParameter of(ConsoleOutput output) {
        return fixed(ConsoleTypeParameter.class, output);
    }

    @Override
    public List<String> getAsArguments() {
        if (isPresent()) {
            return Arrays.asList("--console", get().toString().toLowerCase());
        }
        return Collections.emptyList();
    }
}
