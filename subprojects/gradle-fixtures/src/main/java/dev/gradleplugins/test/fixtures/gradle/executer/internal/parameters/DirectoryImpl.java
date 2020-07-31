package dev.gradleplugins.test.fixtures.gradle.executer.internal.parameters;

import lombok.EqualsAndHashCode;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;

@EqualsAndHashCode
class DirectoryImpl {
    private static final ThreadLocal<File> NEXT_DIRECTORY = new ThreadLocal<>();
    private final File directory;

    public DirectoryImpl() {
        this.directory = NEXT_DIRECTORY.get();
        NEXT_DIRECTORY.remove();
    }

    public File getAsFile() {
        return directory;
    }

    static <T extends Directory> T newInstance(Class<T> type, Object obj) {
        File directory = null;
        if (obj instanceof File) {
            directory = (File) obj;
        } else if (obj instanceof Directory) {
            directory = ((Directory) obj).getAsFile();
        } else if (obj instanceof Path) {
            directory = ((Path) obj).toFile();
        } else {
            throw new IllegalArgumentException(String.format("Unable to convert '%s' to a directory.", obj.toString()));
        }

        NEXT_DIRECTORY.set(directory);
        try {
            return type.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(String.format("Unable to instantiate a '%s'.", type.getSimpleName()), e);
        }
    }
}
