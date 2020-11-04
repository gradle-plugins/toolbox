package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleRunner;
import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

public final class BeforeExecuteActionsProvider extends AbstractGradleExecutionProvider<List<UnaryOperator<GradleRunner>>> {
    public static BeforeExecuteActionsProvider empty() {
        return fixed(BeforeExecuteActionsProvider.class, Collections.emptyList());
    }

    public BeforeExecuteActionsProvider plus(UnaryOperator<GradleRunner> action) {
        val result = new ArrayList<UnaryOperator<GradleRunner>>();
        result.addAll(get());
        result.add(action);
        return fixed(BeforeExecuteActionsProvider.class, result);
    }
}
