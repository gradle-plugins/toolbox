package dev.gradleplugins.fixtures.gradle.runner;

import dev.gradleplugins.fixtures.gradle.GradleScriptFixture;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.Charset;
import java.nio.file.Files;

import static dev.gradleplugins.fixtures.file.FileSystemUtils.file;

public interface GradleWrapperFixture {
    static void writeGradleWrapperTo(File workingDirectory) {
        try {
            Files.copy(GradleScriptFixture.class.getResourceAsStream("/dev/gradleplugins/fixtures/gradle/wrapper/gradlew"), file(workingDirectory, "gradlew").toPath());
            Files.copy(GradleScriptFixture.class.getResourceAsStream("/dev/gradleplugins/fixtures/gradle/wrapper/gradlew.bat"), file(workingDirectory, "gradlew.bat").toPath());

            file(workingDirectory, "gradlew").setExecutable(true);
            file(workingDirectory, "gradle/wrapper").mkdirs();

            Files.copy(GradleScriptFixture.class.getResourceAsStream("/dev/gradleplugins/fixtures/gradle/wrapper/gradle-wrapper.jar"), file(workingDirectory, "gradle/wrapper/gradle-wrapper.jar").toPath());
            Files.copy(GradleScriptFixture.class.getResourceAsStream("/dev/gradleplugins/fixtures/gradle/wrapper/gradle-wrapper.properties"), file(workingDirectory, "gradle/wrapper/gradle-wrapper.properties").toPath());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
