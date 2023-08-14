package dev.gradleplugins.testers;

import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;

public abstract class GradleTestKitDependencyTester {
    public abstract GradleRunner runner();

    public abstract GradleBuildFile buildFile();

    public abstract String gradleTestKitDsl(String version);
    public abstract String gradleTestKitDsl();

    @Test
    void testGradleTestKitDependency() {
        buildFile().append(groovyDsl(
                "def dependencyUnderTest = " + gradleTestKitDsl("6.3"),
                "tasks.register('verify') {",
                "  doLast {",
                "    assert dependencyUnderTest instanceof ExternalModuleDependency",
                "    assert dependencyUnderTest.group == 'dev.gradleplugins'",
                "    assert dependencyUnderTest.name == 'gradle-test-kit'",
                "    assert dependencyUnderTest.version == '6.3'",
                "  }",
                "}"
        ));

        runner().withTasks("verify").build();
    }

    @Test
    void testLocalGradleTestKitDependency() {
        buildFile().append(groovyDsl(
                "def dependencyUnderTest = " + gradleTestKitDsl("local"),
                "tasks.register('verify') {",
                "  doLast {",
                "    assert dependencyUnderTest instanceof SelfResolvingDependency",
                "    assert dependencyUnderTest.targetComponentId.displayName == 'Gradle TestKit'",
                "  }",
                "}"
        ));

        runner().withTasks("verify").build();
    }

    @Test
    void testGradleTestKitLocalDependency() {
        buildFile().append(groovyDsl(
                "def dependencyUnderTest = " + gradleTestKitDsl(),
                "tasks.register('verify') {",
                "  doLast {",
                "    assert dependencyUnderTest instanceof SelfResolvingDependency",
                "    assert dependencyUnderTest.targetComponentId.displayName == 'Gradle TestKit'",
                "  }",
                "}"
        ));

        runner().withTasks("verify").build();
    }
}
