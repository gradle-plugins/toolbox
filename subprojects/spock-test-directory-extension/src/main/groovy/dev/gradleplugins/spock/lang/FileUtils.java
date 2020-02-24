package dev.gradleplugins.spock.lang;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;

// TODO: Share implementation with TestFile
public class FileUtils {
    public static void forceDeleteDirectory(File directory) throws IOException {
        if (directory.isDirectory()) {

            if (Files.isSymbolicLink(directory.toPath())) {
                if (!directory.delete()) {
                    throw new IOException("Unable to delete symlink: " + directory.getCanonicalPath());
                }
            } else {
                List<String> errorPaths = new ArrayList<>();
                Files.walkFileTree(directory.toPath(), new SimpleFileVisitor<Path>() {

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
                            .append("Unable to recursively delete directory ")
                            .append(directory.getCanonicalPath())
                            .append(", failed paths:\n");
                    for (String errorPath : errorPaths) {
                        builder.append("\t- ").append(errorPath).append("\n");
                    }
                    throw new IOException(builder.toString());
                }
            }

        // TODO: This may be wrong, if it's a file, the method should not delete it (because it's not a directory)
        } else if (directory.exists()) {
            if (!directory.delete()) {
                throw new IOException("Unable to delete file: " + directory.getCanonicalPath());
            }
        }
    }

    public static File createDirectory(File directory) {
        if (directory.mkdirs()) {
            return directory;
        }
        if (directory.isDirectory()) {
            return directory;
        }
        throw new AssertionError("Problems creating dir: " + directory
                + ". Diagnostics: exists=" + directory.exists() + ", isFile=" + directory.isFile() + ", isDirectory=" + directory.isDirectory() + ", isSymbolicLink=" + Files.isSymbolicLink(directory.toPath()));
    }

    public static File createFile(File file) {
        createDirectory(file.getParentFile());
        try {
            assertTrue(file.isFile() || file.createNewFile());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return file;
    }

    /**
     * Construct a file from the set of name elements.
     *
     * @param directory the parent directory
     * @param names the name elements
     * @return the file
     */
    public static File file(final File directory, final String... names) {
        if (directory == null) {
            throw new NullPointerException("directory must not be null");
        }
        if (names == null) {
            throw new NullPointerException("names must not be null");
        }
        File file = directory;
        for (final String name : names) {
            file = new File(file, name);
        }
        return file;
    }
}
