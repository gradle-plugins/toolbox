package dev.gradleplugins.fixtures.gradle;

import dev.gradleplugins.fixtures.file.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

public interface GradleScriptFixture {
    File getTestDirectory();

    default File getBuildFile() {
        return FileSystemUtils.file(getTestDirectory(), getBuildFileName());
    }

    default String getBuildFileName() {
        return "build.gradle";
    }

    default File getSettingsFile() {
        return FileSystemUtils.file(getTestDirectory(), getSettingsFileName());
    }

    default String getSettingsFileName() {
        return "settings.gradle";
    }

    static List<File> getImplementationClassPath() {
        Properties properties = new Properties();
        try {
            properties.load(GradleScriptFixture.class.getResourceAsStream("/plugin-under-test-metadata.properties"));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return Arrays.stream(properties.get("implementation-classpath").toString().split(File.pathSeparator)).map(File::new).collect(Collectors.toList());
    }

    static String configurePluginClasspathAsFileCollection() {
        String fileCollectionContent = getImplementationClassPath().stream().map(file -> "\"" + file.getAbsolutePath().replace("\\", "\\\\") + "\"").collect(Collectors.joining(", "));
        return "files(" + fileCollectionContent + ")";
    }
}
