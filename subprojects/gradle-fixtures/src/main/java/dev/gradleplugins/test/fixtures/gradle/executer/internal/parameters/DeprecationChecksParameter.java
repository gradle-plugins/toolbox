package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class DeprecationChecksParameter extends GradleExecutionParameterImpl<DeprecationChecksParameter.DeprecationChecks> implements CommandLineGradleExecutionParameter<DeprecationChecksParameter.DeprecationChecks> {

    public enum DeprecationChecks {
        FAILS, IGNORES
    }
    public static DeprecationChecksParameter fails() {
        return fixed(DeprecationChecksParameter.class, DeprecationChecks.FAILS);
    }

    public static DeprecationChecksParameter ignores() {
        return fixed(DeprecationChecksParameter.class, DeprecationChecks.IGNORES);
    }

    @Override
    public List<String> getAsArguments() {
        if (get().equals(DeprecationChecks.FAILS)) {
            return Arrays.asList("--warning-mode", "fail");
        }
        return Collections.emptyList();
    }
}
