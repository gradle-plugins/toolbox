package dev.gradleplugins.testers;

import dev.gradleplugins.buildscript.ast.expressions.Expression;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.buildscript.ast.expressions.AssignmentExpression.assign;
import static dev.gradleplugins.buildscript.ast.expressions.VariableDeclarationExpression.val;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;

public abstract class GradlePluginDependencyTester {
    public abstract GradleRunner runner();

    public abstract GradleBuildFile buildFile();

    public abstract Expression gradlePluginDsl(String pluginNotation);

    @Test
    void testGradlePluginDependency() {
        buildFile().append(val("dependencyUnderTest", assign(gradlePluginDsl("dev.gradleplugins.java-gradle-plugin:1.6.1"))));
        buildFile().append(groovyDsl(
                "tasks.register('verify') {",
                "  doLast {",
                "    assert dependencyUnderTest instanceof ExternalModuleDependency",
                "    assert dependencyUnderTest.group == 'dev.gradleplugins.java-gradle-plugin'",
                "    assert dependencyUnderTest.name == 'dev.gradleplugins.java-gradle-plugin.gradle.plugin'",
                "    assert dependencyUnderTest.version == '1.6.1'",
                "  }",
                "}"
        ));

        runner().withTasks("verify").build();
    }
}
