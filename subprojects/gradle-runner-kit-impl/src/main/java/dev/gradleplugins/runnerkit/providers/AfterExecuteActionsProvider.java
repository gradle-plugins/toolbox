package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.GradleExecutionContext;
import lombok.val;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

public final class AfterExecuteActionsProvider extends AbstractGradleExecutionProvider<List<Consumer<GradleExecutionContext>>> {
    public static AfterExecuteActionsProvider empty() {
        return fixed(AfterExecuteActionsProvider.class, Collections.emptyList());
    }

    public AfterExecuteActionsProvider plus(Consumer<GradleExecutionContext> action) {
        val result = new ArrayList<Consumer<GradleExecutionContext>>();
        result.addAll(get());
        result.add(action);
        return fixed(AfterExecuteActionsProvider.class, result);
    }
}
