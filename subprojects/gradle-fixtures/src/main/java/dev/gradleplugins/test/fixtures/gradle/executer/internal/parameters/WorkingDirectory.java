package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

@Deprecated
public final class WorkingDirectory extends DirectoryImpl implements Directory {
    public static WorkingDirectory of(Object directory) {
        return newInstance(WorkingDirectory.class, directory);
    }
}
