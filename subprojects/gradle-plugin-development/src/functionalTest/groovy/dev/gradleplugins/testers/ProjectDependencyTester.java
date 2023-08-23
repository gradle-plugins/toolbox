package dev.gradleplugins.testers;

import dev.gradleplugins.buildscript.ast.expressions.Expression;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.buildscript.io.GradleSettingsFile;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.buildscript.ast.expressions.AssignmentExpression.assign;
import static dev.gradleplugins.buildscript.ast.expressions.VariableDeclarationExpression.val;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;

public abstract class ProjectDependencyTester {
    public abstract GradleRunner runner();

    public abstract GradleBuildFile buildFile();

    public abstract GradleSettingsFile settingsFile();

    public abstract Expression projectDsl(String projectPath);
    public abstract Expression projectDsl();

    @Test
    void testProjectWithPathDependency() {
        settingsFile().append(groovyDsl("include 'other-project'"));
        buildFile().append(val("dependencyUnderTest", assign(projectDsl(":other-project"))));
        buildFile().append(groovyDsl(
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
        buildFile().append(val("dependencyUnderTest", assign(projectDsl())));
        buildFile().append(groovyDsl(
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
