package dev.gradleplugins.testers;

import dev.gradleplugins.buildscript.ast.ExpressionBuilder;
import dev.gradleplugins.buildscript.ast.expressions.Expression;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.buildscript.ast.expressions.AssignmentExpression.assign;
import static dev.gradleplugins.buildscript.ast.expressions.VariableDeclarationExpression.val;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;
import static dev.gradleplugins.buildscript.syntax.Syntax.string;

public abstract class PlatformDependencyModifierTester {
    public abstract GradleRunner runner();

    public abstract ExpressionBuilder<?> modifierDsl();

    public abstract GradleBuildFile buildFile();

    private abstract class Tester {
        public abstract ExpressionBuilder<?> modifierDsl(Expression dsl);

        @Test
        void testPlatformDependencyModifierOnExternalModuleDependency() {
            buildFile().append(val("dependencyUnderTest", assign(modifierDsl(groovyDsl("dependencies.create('com.example:foo:1.0')")))));
            buildFile().append(groovyDsl(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert dependencyUnderTest.isEndorsingStrictVersions()",
                    "    assert dependencyUnderTest.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)?.name == 'platform'",
                    "  }",
                    "}"
            ));

            runner().withTasks("verify").build();
        }

        @Test
        void testPlatformDependencyModifierOnGroupArtifactVersionNotation() {
            buildFile().append(val("dependencyUnderTest", assign(modifierDsl(string("com.example:foo:1.0")))));
            buildFile().append(groovyDsl(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert dependencyUnderTest.isEndorsingStrictVersions()",
                    "    assert dependencyUnderTest.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)?.name == 'platform'",
                    "  }",
                    "}"
            ));

            runner().withTasks("verify").build();
        }

        @Test
        void testPlatformDependencyModifierOnProjectDependency() {
            buildFile().append(val("dependencyUnderTest", assign(modifierDsl(groovyDsl("dependencies.create(project)")))));
            buildFile().append(groovyDsl(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert dependencyUnderTest.isEndorsingStrictVersions()",
                    "    assert dependencyUnderTest.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)?.name == 'platform'",
                    "  }",
                    "}"
            ));

            runner().withTasks("verify").build();
        }

        @Test
        void testPlatformDependencyModifierOnProjectNotation() {
            buildFile().append(val("dependencyUnderTest", assign(modifierDsl(groovyDsl("project")))));
            buildFile().append(groovyDsl(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert dependencyUnderTest.isEndorsingStrictVersions()",
                    "    assert dependencyUnderTest.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)?.name == 'platform'",
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
            return PlatformDependencyModifierTester.this.modifierDsl().call(dsl);
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
            return PlatformDependencyModifierTester.this.modifierDsl().call(dsl);
        }
    }

    @Nested
    public class ModifierTest extends Tester {
        @Override
        public ExpressionBuilder<?> modifierDsl(Expression dsl) {
            return PlatformDependencyModifierTester.this.modifierDsl().call("modify", dsl);
        }
    }
}
