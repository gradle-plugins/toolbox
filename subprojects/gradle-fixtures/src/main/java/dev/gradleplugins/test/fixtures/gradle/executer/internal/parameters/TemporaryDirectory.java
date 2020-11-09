package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

@Deprecated
public final class TemporaryDirectory extends DirectoryImpl implements Directory {
    public static TemporaryDirectory of(Object directory) {
        return newInstance(TemporaryDirectory.class, directory);
    }
}
