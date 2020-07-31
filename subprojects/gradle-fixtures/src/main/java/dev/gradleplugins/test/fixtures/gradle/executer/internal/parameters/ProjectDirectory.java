package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

public final class ProjectDirectory extends DirectoryImpl implements Directory {
    public static ProjectDirectory of(Object directory) {
        return newInstance(ProjectDirectory.class, directory);
    }
}
