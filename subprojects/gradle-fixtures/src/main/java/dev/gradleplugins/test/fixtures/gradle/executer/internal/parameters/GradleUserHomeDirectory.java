package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

@Deprecated
public final class GradleUserHomeDirectory extends DirectoryImpl implements Directory {
    public static GradleUserHomeDirectory of(Object directory) {
        return newInstance(GradleUserHomeDirectory.class, directory);
    }
}
