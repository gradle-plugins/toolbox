package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.fixtures.file.FileSystemUtils;
import dev.gradleplugins.runnerkit.GradleExecutionContext;

import java.io.File;
import java.util.function.Function;

public final class WorkingDirectoryProvider extends AbstractGradleExecutionProvider<File> {
    public static WorkingDirectoryProvider unset() {
        return noValue(WorkingDirectoryProvider.class);
    }

    public static WorkingDirectoryProvider of(File workingDirectory) {
        return fixed(WorkingDirectoryProvider.class, workingDirectory);
    }

    public static Function<GradleExecutionContext, File> relativeToWorkingDirectory(String path) {
        return context -> {
            ((GradleExecutionProviderInternal<File>) context.getWorkingDirectory()).calculateValue(context);
            return FileSystemUtils.file(context.getWorkingDirectory().get(), path);
        };
    }
}
