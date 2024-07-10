package dev.gradleplugins.testers;

import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.buildscript.io.GradleSettingsFile;
import dev.gradleplugins.fixtures.sample.GradlePluginApi;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;

public abstract class GradlePluginApiTester {
    GradleBuildFile pluginBuildFile;

    public abstract GradleRunner runner();

    public abstract Path testDirectory();

    public final GradleBuildFile pluginBuildFile() {
        return pluginBuildFile;
    }

    public abstract GradlePluginApi fixture();

    @BeforeEach
    final void givenProject() {
        pluginBuildFile = GradleBuildFile.inDirectory(testDirectory());

        fixture().writeToProject(testDirectory().toFile());

        pluginBuildFile.append(groovyDsl(
                "group = 'com.example'",
                "version = '1.0'",
                "",
                "repositories {",
                "  gradlePluginDevelopment()",
                "}",
                "",
                "gradlePlugin {",
                "  plugins {",
                "    basic {",
                "      id = '" + fixture().getPluginId() + "'",
                "      implementationClass = 'com.example.BasicPlugin'",
                "    }",
                "  }",
                "}",
                "",
                "Set<File> artifactFiles(def publications) {",
                "  return publications.artifacts.files.files",
                "}"
        ));
        GradleSettingsFile.inDirectory(testDirectory());
    }

    @Test
    void rewiresApiElementsToIncludeOnly__ApiJar__ApiClasses() {
        pluginBuildFile.append(groovyDsl(
                "tasks.register('verify') {",
                "  doLast {",
                "    assert artifactFiles(configurations.apiElements.outgoing) == [file('build/libs/my-plugin-1.0-api.jar')] as Set",
                "    assert artifactFiles(configurations.apiElements.outgoing.variants.classes) == [file('build/tmp/syncApiClasses')] as Set",
                "  }",
                "}"
        ));

        runner().withTasks("verify").build();
    }

    // TODO: If we use a custom pluginSourceSet -> the runtimeElements and apiElements won't contain any outgoing information...

    @Test
    void rewiresRuntimeElementsToInclude__ApiAndMainJar__ApiAndMainClasses__ApiAndMainResources() {
        pluginBuildFile.append(groovyDsl(
                "tasks.register('verify') {",
                "  doLast {",
                "    assert artifactFiles(configurations.runtimeElements.outgoing) == [file('build/libs/my-plugin-1.0.jar'), file('build/libs/my-plugin-1.0-api.jar')] as Set",
                "    assert artifactFiles(configurations.runtimeElements.outgoing.variants.classes) == [file('build/classes/java/main'), file('build/tmp/syncApiClasses')] as Set",
                "    assert artifactFiles(configurations.runtimeElements.outgoing.variants.resources) == [file('build/resources/main'), file('build/tmp/syncApiResources')] as Set",
                "  }",
                "}"
        ));

        runner().withTasks("verify").build();
    }
}
