package dev.gradleplugins.fixtures.file;

import com.google.common.base.Preconditions;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;

import static dev.gradleplugins.fixtures.file.FilePreconditions.checkIsNotNull;
import static java.util.Objects.requireNonNull;

public final class FileSystemUtils {
    public static File createFile(File self) {
        FilePreconditions.checkNotExistingFile(self, "Could not create file");
        self.getParentFile().mkdirs();
        try {
            self.createNewFile();
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Could not create file at '%s'.", self.getAbsolutePath()), e);
        }
        return self;
    }

    public static File createDirectory(File self) {
        FilePreconditions.checkNotExistingDirectory(self, "Could not create directory");
        if (self.mkdirs()) {
            return self;
        }
        if (self.isDirectory()) {
            return self;
        }
        throw new RuntimeException("Could not create directory because some parent directory could not be created.");
    }

    public static File file(File self, Object... paths) {
        return join(checkIsNotNull(self, "Could not join file"), checkObjectPathsAreNotNull(paths));
    }

    private static Object[] checkObjectPathsAreNotNull(Object[] paths) {
        int index = 0;
        for (Object path : paths) {
            if (path == null) {
                throw new IllegalArgumentException(String.format("Could not join file because path object #%d is null.", index));
            }
            ++index;
        }
        return paths;
    }

    private static File join(File self, Object[] paths) {
        File current = self.getAbsoluteFile();
        int i = 0;
        for (Object path : paths) {
            String pathToString = wrapObjectStringifyExceptions(i, () -> requireNonNull(path.toString(), () -> String.format("%s#toString() returns null.", path.getClass().getSimpleName())));
            current = new File(current, pathToString);
            ++i;
        }
        return canonicalizeFileOrThrowException(current);
    }

    private static String wrapObjectStringifyExceptions(int index, Supplier<String> toString) {
        try {
            return toString.get();
        } catch (Throwable e) {
            throw new RuntimeException(String.format("Could not stringify path object #%d.", index), e);
        }
    }

    private static File canonicalizeFileOrThrowException(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Could not canonicalize '%s'.", file), e);
        }
    }

    public static boolean isSelfOrDescendent(File self, File file) {
        checkIsNotNull(self, "Could not check descendent");
        checkIsNotNull(file, "Could not check descendent");
        if (file.getAbsolutePath().equals(self.getAbsolutePath())) {
            return true;
        }
        return file.getAbsolutePath().startsWith(self.getAbsolutePath() + File.separatorChar);
    }

    public static Set<String> getDescendants(File self) {
        FilePreconditions.checkIsDirectory(self, "Could not gather descendants");

        Set<String> actual = new TreeSet<String>();
        visit(actual, "", self);

        return actual;
    }

    private static void visit(Set<String> names, String prefix, File file) {
        File[] files = file.listFiles();
        if (files == null) {
            throw new RuntimeException(String.format("Could not visit folder '%s' because an error occurred.", file.getAbsolutePath()));
        }
        for (File child : file.listFiles()) {
            if (child.isFile()) {
                names.add(prefix + child.getName());
            } else if (child.isDirectory()) {
                visit(names, prefix + child.getName() + "/", child);
            }
        }
    }

    public static boolean isEmptyDirectory(File self) {
        FilePreconditions.checkIsDirectory(self, "Could not check if directory is empty");
        return getDescendants(self).isEmpty();
    }

    /**
     * Recursively delete this directory, reporting all failed paths.
     *
     * @param self the directory to delete.
     * @return self
     */
    public static File forceDeleteDirectory(File self) {
        FilePreconditions.checkIsDirectory(self, "Could not delete directory");
        if (self.isDirectory()) {
            try {
                List<String> errorPaths = new ArrayList<>();
                Files.walkFileTree(self.toPath(), new SimpleFileVisitor<Path>() {
                    @Override
                    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                        if (!file.toFile().delete()) {
                            errorPaths.add(file.toFile().getCanonicalPath());
                        }
                        return FileVisitResult.CONTINUE;
                    }

                    @Override
                    public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                        if (!dir.toFile().delete()) {
                            errorPaths.add(dir.toFile().getCanonicalPath());
                        }
                        return FileVisitResult.CONTINUE;
                    }
                });

                if (!errorPaths.isEmpty()) {
                    StringBuilder builder = new StringBuilder()
                            .append("Unable to recursively delete directory '")
                            .append(self.getCanonicalPath())
                            .append("', failed paths:\n");
                    for (String errorPath : errorPaths) {
                        builder.append("\t- ").append(errorPath).append("\n");
                    }
                    throw new RuntimeException(builder.toString());
                }
            } catch (IOException e) {
                throw new UncheckedIOException(String.format("Could not delete directory '%s' because of an error.", self.getAbsolutePath()), e);
            }
        }
        return self;
    }
}
