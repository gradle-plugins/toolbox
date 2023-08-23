package dev.gradleplugins.testers;

import dev.gradleplugins.buildscript.ast.expressions.Expression;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.buildscript.ast.expressions.AssignmentExpression.assign;
import static dev.gradleplugins.buildscript.ast.expressions.VariableDeclarationExpression.val;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;

public abstract class GradleApiDependencyTester {
    public abstract GradleRunner runner();

    public abstract GradleBuildFile buildFile();

    public abstract Expression gradleApiDsl(String version);

    @Test
    void testGradleApiDependency() {
        buildFile().append(val("dependencyUnderTest", assign(gradleApiDsl("6.3"))));
        buildFile().append(groovyDsl(
                "tasks.register('verify') {",
                "  doLast {",
                "    assert dependencyUnderTest instanceof ExternalModuleDependency",
                "    assert dependencyUnderTest.group == 'dev.gradleplugins'",
                "    assert dependencyUnderTest.name == 'gradle-api'",
                "    assert dependencyUnderTest.version == '6.3'",
                "  }",
                "}"
        ));

        runner().withTasks("verify").build();
    }

    @Test
    void testLocalGradleApiDependency() {
        buildFile().append(val("dependencyUnderTest", assign(gradleApiDsl("local"))));
        buildFile().append(groovyDsl(
                "tasks.register('verify') {",
                "  doLast {",
                "    assert dependencyUnderTest instanceof SelfResolvingDependency",
                "    assert dependencyUnderTest.targetComponentId.displayName == 'Gradle API'",
                "  }",
                "}"
        ));

        runner().withTasks("verify").build();
    }
}
