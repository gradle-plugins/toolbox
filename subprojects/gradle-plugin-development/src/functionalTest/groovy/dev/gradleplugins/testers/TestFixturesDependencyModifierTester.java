package dev.gradleplugins.testers;

import dev.gradleplugins.buildscript.ast.ExpressionBuilder;
import dev.gradleplugins.buildscript.ast.expressions.Expression;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.buildscript.io.GradleSettingsFile;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.buildscript.ast.expressions.AssignmentExpression.assign;
import static dev.gradleplugins.buildscript.ast.expressions.VariableDeclarationExpression.val;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;
import static dev.gradleplugins.buildscript.syntax.Syntax.string;

public abstract class TestFixturesDependencyModifierTester {
    public abstract GradleRunner runner();

    public abstract ExpressionBuilder<?> modifierDsl();

    public abstract GradleBuildFile buildFile();
    public abstract GradleSettingsFile settingsFile();

    private abstract class Tester {
        public abstract ExpressionBuilder<?> modifierDsl(Expression dsl);

        @Test
        void testTestFixturesDependencyModifierOnExternalModuleDependency() {
            buildFile().append(val("dependencyUnderTest", assign(modifierDsl(groovyDsl("dependencies.create('com.example:foo:1.0')")))));
            buildFile().append(groovyDsl(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert dependencyUnderTest.requestedCapabilities.any {",
                    "      'com.example:foo-test-fixtures' == \"${it.group}:${it.name}\" && it.version == null",
                    "    }",
                    "  }",
                    "}"
            ));

            runner().withTasks("verify").build();
        }

        @Test
        void testTestFixturesDependencyModifierOnGroupArtifactVersionNotation() {
            buildFile().append(val("dependencyUnderTest", assign(modifierDsl(string("com.example:foo:1.0")))));
            buildFile().append(groovyDsl(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert dependencyUnderTest.requestedCapabilities.any {",
                    "      'com.example:foo-test-fixtures' == \"${it.group}:${it.name}\" && it.version == null",
                    "    }",
                    "  }",
                    "}"
            ));

            runner().withTasks("verify").build();
        }

        @Test
        void testTestFixturesDependencyModifierOnProjectDependency() {
            settingsFile().append(groovyDsl("rootProject.name = 'foo'"));
            buildFile().append(val("dependencyUnderTest", assign(modifierDsl(groovyDsl("dependencies.create(project)")))));
            buildFile().append(groovyDsl(
                    "group = 'com.example'",
                    "version = '4.2'",
                    "",
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert dependencyUnderTest.requestedCapabilities.any {",
                    "      'com.example:foo-test-fixtures:4.2' == \"${it.group}:${it.name}:${it.version}\"",
                    "    }",
                    "  }",
                    "}"
            ));

            runner().withTasks("verify").build();
        }

        @Test
        void testTestFixturesDependencyModifierOnProjectNotation() {
            settingsFile().append(groovyDsl("rootProject.name = 'foo'"));
            buildFile().append(val("dependencyUnderTest", assign(modifierDsl(groovyDsl("project")))));
            buildFile().append(groovyDsl(
                    "group = 'com.example'",
                    "version = '4.2'",
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert dependencyUnderTest.requestedCapabilities.any {",
                    "      'com.example:foo-test-fixtures:4.2' == \"${it.group}:${it.name}:${it.version}\"",
                    "    }",
                    "  }",
                    "}"
            ));

            runner().withTasks("verify").build();
        }
    }

    @Nested
    public class GroovyDslTest extends Tester {
        @Override
        public ExpressionBuilder<?> modifierDsl(Expression dsl) {
            return TestFixturesDependencyModifierTester.this.modifierDsl().call(dsl);
        }
    }

    @Nested
    public class KotlinDslTest extends Tester {
        @BeforeEach
        void useKotlinDsl() {
            buildFile().useKotlinDsl();
        }

        @Override
        public ExpressionBuilder<?> modifierDsl(Expression dsl) {
            return TestFixturesDependencyModifierTester.this.modifierDsl().call(dsl);
        }
    }

    @Nested
    public class ModifierTest extends Tester {
        @Override
        public ExpressionBuilder<?> modifierDsl(Expression dsl) {
            return TestFixturesDependencyModifierTester.this.modifierDsl().call("modify", dsl);
        }
    }
}
