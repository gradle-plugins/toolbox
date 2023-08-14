package dev.gradleplugins.testers;

import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.buildscript.io.GradleSettingsFile;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;

public abstract class ProjectDependencyTester {
    public abstract GradleRunner runner();

    public abstract GradleBuildFile buildFile();

    public abstract GradleSettingsFile settingsFile();

    public abstract String projectDsl(String projectPath);
    public abstract String projectDsl();

    @Test
    void testProjectWithPathDependency() {
        settingsFile().append(groovyDsl("include 'other-project'"));
        buildFile().append(groovyDsl(
                "def dependencyUnderTest = " + projectDsl(":other-project"),
                "tasks.register('verify') {",
                "  doLast {",
                "    assert dependencyUnderTest instanceof ProjectDependency",
                "    assert dependencyUnderTest.dependencyProject.path == ':other-project'",
                "  }",
                "}"
        ));

        runner().withTasks("verify").build();
    }

    @Test
    void testProjectDependency() {
        buildFile().append(groovyDsl(
                "def dependencyUnderTest = " + projectDsl(),
                "tasks.register('verify') {",
                "  doLast {",
                "    assert dependencyUnderTest instanceof ProjectDependency",
                "    assert dependencyUnderTest.dependencyProject.path == project.path",
                "  }",
                "}"
        ));

        runner().withTasks("verify").build();
    }
}
