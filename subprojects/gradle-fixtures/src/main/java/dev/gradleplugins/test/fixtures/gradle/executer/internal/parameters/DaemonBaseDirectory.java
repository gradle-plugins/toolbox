package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

@Deprecated
public final class DaemonBaseDirectory extends DirectoryImpl implements Directory {
    public static DaemonBaseDirectory of(Object directory) {
        return newInstance(DaemonBaseDirectory.class, directory);
    }
}
