package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

@Deprecated
public final class UserHomeDirectory extends DirectoryImpl implements Directory {
    public static UserHomeDirectory of(Object directory) {
        return newInstance(UserHomeDirectory.class, directory);
    }
}
