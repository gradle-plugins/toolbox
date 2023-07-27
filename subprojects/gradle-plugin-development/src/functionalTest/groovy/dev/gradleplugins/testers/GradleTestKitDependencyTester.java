package dev.gradleplugins.testers;

import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

public abstract class GradleTestKitDependencyTester {
    public abstract GradleRunner runner();

    public abstract Path buildFile();

    public abstract String gradleTestKitDsl(String version);
    public abstract String gradleTestKitDsl();

    @Test
    void testGradleTestKitDependency() throws IOException {
        Files.write(buildFile(), Arrays.asList(
                "def dependencyUnderTest = " + gradleTestKitDsl("6.3"),
                "tasks.register('verify') {",
                "  doLast {",
                "    assert dependencyUnderTest instanceof ExternalModuleDependency",
                "    assert dependencyUnderTest.group == 'dev.gradleplugins'",
                "    assert dependencyUnderTest.name == 'gradle-test-kit'",
                "    assert dependencyUnderTest.version == '6.3'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner().withTasks("verify").build();
    }

    @Test
    void testLocalGradleTestKitDependency() throws IOException {
        Files.write(buildFile(), Arrays.asList(
                "def dependencyUnderTest = " + gradleTestKitDsl("local"),
                "tasks.register('verify') {",
                "  doLast {",
                "    assert dependencyUnderTest instanceof SelfResolvingDependency",
                "    assert dependencyUnderTest.targetComponentId.displayName == 'Gradle TestKit'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner().withTasks("verify").build();
    }

    @Test
    void testGradleTestKitLocalDependency() throws IOException {
        Files.write(buildFile(), Arrays.asList(
                "def dependencyUnderTest = " + gradleTestKitDsl(),
                "tasks.register('verify') {",
                "  doLast {",
                "    assert dependencyUnderTest instanceof SelfResolvingDependency",
                "    assert dependencyUnderTest.targetComponentId.displayName == 'Gradle TestKit'",
                "  }",
                "}"
        ), StandardOpenOption.APPEND);

        runner().withTasks("verify").build();
    }
}
