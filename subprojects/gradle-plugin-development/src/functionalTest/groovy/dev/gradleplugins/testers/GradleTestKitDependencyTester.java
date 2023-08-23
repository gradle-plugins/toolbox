package dev.gradleplugins.testers;

import dev.gradleplugins.buildscript.ast.expressions.Expression;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.buildscript.ast.expressions.AssignmentExpression.assign;
import static dev.gradleplugins.buildscript.ast.expressions.VariableDeclarationExpression.val;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;

public abstract class GradleTestKitDependencyTester {
    public abstract GradleRunner runner();

    public abstract GradleBuildFile buildFile();

    public abstract Expression gradleTestKitDsl(String version);
    public abstract Expression gradleTestKitDsl();

    @Test
    void testGradleTestKitDependency() {
        buildFile().append(val("dependencyUnderTest", assign(gradleTestKitDsl("6.3"))));
        buildFile().append(groovyDsl(
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
        buildFile().append(val("dependencyUnderTest", assign(gradleTestKitDsl("local"))));
        buildFile().append(groovyDsl(
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
        buildFile().append(val("dependencyUnderTest", assign(gradleTestKitDsl())));
        buildFile().append(groovyDsl(
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
