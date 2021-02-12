package dev.gradleplugins.runnerkit;

import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Consumer;

import static dev.gradleplugins.fixtures.file.FileSystemUtils.file;

public interface GradleWrapperFixture {
    File getTestDirectory();

    default void writeGradleWrapperToTestDirectory() {
        writeGradleWrapperTo(getTestDirectory());
    }

    default void writeGradleWrapperToTestDirectory(String version) {
        writeGradleWrapperTo(getTestDirectory(), configuration -> configuration.setVersion(version));
    }

    static void writeGradleWrapperTo(File workingDirectory) {
        try {
            Files.copy(GradleWrapperFixture.class.getResourceAsStream("/dev/gradleplugins/fixtures/runnerkit/wrapper/gradlew"), file(workingDirectory, "gradlew").toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(GradleWrapperFixture.class.getResourceAsStream("/dev/gradleplugins/fixtures/runnerkit/wrapper/gradlew.bat"), file(workingDirectory, "gradlew.bat").toPath(), StandardCopyOption.REPLACE_EXISTING);

            file(workingDirectory, "gradlew").setExecutable(true);
            file(workingDirectory, "gradle/wrapper").mkdirs();

            Files.copy(GradleWrapperFixture.class.getResourceAsStream("/dev/gradleplugins/fixtures/runnerkit/wrapper/gradle-wrapper.jar"), file(workingDirectory, "gradle/wrapper/gradle-wrapper.jar").toPath(), StandardCopyOption.REPLACE_EXISTING);
            Files.copy(GradleWrapperFixture.class.getResourceAsStream("/dev/gradleplugins/fixtures/runnerkit/wrapper/gradle-wrapper.properties"), file(workingDirectory, "gradle/wrapper/gradle-wrapper.properties").toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    static void writeGradleWrapperTo(File workingDirectory, Consumer<GradleWrapperConfiguration> action) {
        writeGradleWrapperTo(workingDirectory);

        GradleWrapperConfiguration configuration = new GradleWrapperConfiguration();
        action.accept(configuration);

        Properties wrapperProperties = new Properties();
        File wrapperPropertiesFile = new File(workingDirectory, "gradle/wrapper/gradle-wrapper.properties");
        try (InputStream inStream = new FileInputStream(wrapperPropertiesFile)) {
            wrapperProperties.load(inStream);
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Could not read '%s' because of an error.", wrapperPropertiesFile.getAbsolutePath()), e);
        }
        configuration.getVersion().ifPresent(version -> wrapperProperties.compute("distributionUrl", (key, oldValue) -> {
            if (version.contains("-")) {
                return "https://services.gradle.org/distributions-snapshots/gradle-" + version + "-bin.zip";
            }
            return "https://services.gradle.org/distributions/gradle-" + version + "-bin.zip";
        }));

        try (OutputStream outStream = new FileOutputStream(wrapperPropertiesFile)) {
            wrapperProperties.store(outStream, null);
        } catch (IOException e) {
            throw new UncheckedIOException(String.format("Could not write '%s' because of an error.", wrapperPropertiesFile.getAbsolutePath()), e);
        }
    }

    final class GradleWrapperConfiguration {
        private String version;

        public Optional<String> getVersion() {
            return Optional.ofNullable(version);
        }

        public void setVersion(String version) {
            this.version = version;
        }
    }
}
