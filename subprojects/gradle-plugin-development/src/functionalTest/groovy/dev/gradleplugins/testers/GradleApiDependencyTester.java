package dev.gradleplugins.testers;

import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public abstract class GradleApiDependencyTester {
    public abstract GradleRunner runner();

    public abstract Path buildFile();

    public abstract String gradleApiDsl(String version);

    @Test
    void testGradleApiDependency() throws IOException {
        Files.write(buildFile(), Arrays.asList(
                "def dependencyUnderTest = " + gradleApiDsl("6.3"),
                "tasks.register('verify') {",
                "  doLast {",
                "    assert dependencyUnderTest instanceof ExternalModuleDependency",
                "    assert dependencyUnderTest.group == 'dev.gradleplugins'",
                "    assert dependencyUnderTest.name == 'gradle-api'",
                "    assert dependencyUnderTest.version == '6.3'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner().withTasks("verify").build();
    }

    @Test
    void testLocalGradleApiDependency() throws IOException {
        Files.write(buildFile(), Arrays.asList(
                "def dependencyUnderTest = " + gradleApiDsl("local"),
                "tasks.register('verify') {",
                "  doLast {",
                "    assert dependencyUnderTest instanceof SelfResolvingDependency",
                "    assert dependencyUnderTest.targetComponentId.displayName == 'Gradle API'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner().withTasks("verify").build();
    }
}
