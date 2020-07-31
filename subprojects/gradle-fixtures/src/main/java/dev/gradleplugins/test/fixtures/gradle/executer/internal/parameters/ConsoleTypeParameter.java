package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import dev.gradleplugins.test.fixtures.gradle.logging.ConsoleOutput;
import lombok.EqualsAndHashCode;
import lombok.Value;

import java.util.Arrays;
import java.util.List;

public interface ConsoleTypeParameter extends CommandLineGradleParameter {
    static ConsoleTypeParameter unset() {
        return new UnsetConsoleTypeParameter();
    }

    static ConsoleTypeParameter of(ConsoleOutput output) {
        return new DefaultConsoleTypeParameter(output);
    }

    @Value
    @EqualsAndHashCode(callSuper = false)
    class UnsetConsoleTypeParameter extends UnsetParameter<ConsoleOutput> implements ConsoleTypeParameter {}

    @Value
    class DefaultConsoleTypeParameter implements ConsoleTypeParameter {
        ConsoleOutput value;

        @Override
        public List<String> getAsArguments() {
            return Arrays.asList("--console", value.toString().toLowerCase());
        }
    }
}
