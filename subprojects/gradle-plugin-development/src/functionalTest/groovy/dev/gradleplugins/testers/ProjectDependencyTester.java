package dev.gradleplugins.testers;

import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public abstract class ProjectDependencyTester {
    public abstract GradleRunner runner();

    public abstract Path buildFile();

    public Path settingsFile() {
        return buildFile().getParent().resolve("settings.gradle");
    }

    public abstract String projectDsl(String projectPath);
    public abstract String projectDsl();

    @Test
    void testProjectWithPathDependency() throws IOException {
        Files.write(settingsFile(), Arrays.asList("include 'other-project'"));
        Files.write(buildFile(), Arrays.asList(
                "def dependencyUnderTest = " + projectDsl(":other-project"),
                "tasks.register('verify') {",
                "  doLast {",
                "    assert dependencyUnderTest instanceof ProjectDependency",
                "    assert dependencyUnderTest.dependencyProject.path == ':other-project'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner().withTasks("verify").build();
    }

    @Test
    void testProjectDependency() throws IOException {
        Files.write(buildFile(), Arrays.asList(
                "def dependencyUnderTest = " + projectDsl(),
                "tasks.register('verify') {",
                "  doLast {",
                "    assert dependencyUnderTest instanceof ProjectDependency",
                "    assert dependencyUnderTest.dependencyProject.path == project.path",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner().withTasks("verify").build();
    }
}
