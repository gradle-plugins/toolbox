package dev.gradleplugins.runnerkit.providers;

import java.io.File;

public final class WorkingDirectoryProvider extends AbstractGradleExecutionProvider<File> {
    public static WorkingDirectoryProvider unset() {
        return noValue(WorkingDirectoryProvider.class);
    }

    public static WorkingDirectoryProvider of(File workingDirectory) {
        return fixed(WorkingDirectoryProvider.class, workingDirectory);
    }
}
