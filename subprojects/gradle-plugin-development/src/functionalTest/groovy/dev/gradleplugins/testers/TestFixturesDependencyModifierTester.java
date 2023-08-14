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
import static dev.gradleplugins.buildscript.ast.expressions.ItExpression.it;
import static dev.gradleplugins.buildscript.ast.expressions.MethodCallExpression.call;
import static dev.gradleplugins.buildscript.ast.expressions.PropertyAccessExpression.plainProperty;
import static dev.gradleplugins.buildscript.ast.expressions.VariableDeclarationExpression.val;
import static dev.gradleplugins.buildscript.blocks.GradleBuildScriptBlocks.doLast;
import static dev.gradleplugins.buildscript.blocks.GradleBuildScriptBlocks.registerTask;
import static dev.gradleplugins.buildscript.syntax.Syntax.assertTrue;
import static dev.gradleplugins.buildscript.syntax.Syntax.lambda;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.buildscript.syntax.Syntax.nul;
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
            buildFile().append(val("dependencyUnderTest", assign(modifierDsl(call("dependencies.create", string("com.example:foo:1.0"))))));
            buildFile().append(registerTask("verify", taskBlock -> {
                taskBlock.add(doLast(it -> {
                    it.add(assertTrue(call("dependencyUnderTest.requestedCapabilities.any", lambda(anyBlock -> {
                        anyBlock.add(string("com.example:foo-test-fixtures").equalTo(literal("\"${it.group}:${it.name}\"")).and(it().plainProperty("version").equalTo(nul())));
                    }))));
                }));
            }));

            runner().withTasks("verify").build();
        }

        @Test
        void testTestFixturesDependencyModifierOnGroupArtifactVersionNotation() {
            buildFile().append(val("dependencyUnderTest", assign(modifierDsl(string("com.example:foo:1.0")))));
            buildFile().append(registerTask("verify", taskBlock -> {
                taskBlock.add(doLast(it -> {
                    it.add(assertTrue(call("dependencyUnderTest.requestedCapabilities.any", lambda(anyBlock -> {
                        anyBlock.add(string("com.example:foo-test-fixtures").equalTo(literal("\"${it.group}:${it.name}\"")).and(it().plainProperty("version").equalTo(nul())));
                    }))));
                }));
            }));

            runner().withTasks("verify").build();
        }

        @Test
        void testTestFixturesDependencyModifierOnProjectDependency() {
            settingsFile().append(setRootProjectName("foo"));
            buildFile().append(val("dependencyUnderTest", assign(modifierDsl(call("dependencies.create", project())))));
            buildFile().append(setGroup("com.example"));
            buildFile().append(setVersion("4.2"));
            buildFile().append(registerTask("verify", taskBlock -> {
                taskBlock.add(doLast(it -> {
                    it.add(assertTrue(call("dependencyUnderTest.requestedCapabilities.any", lambda(anyBlock -> {
                        anyBlock.add(string("com.example:foo-test-fixtures:4.2").equalTo(literal("\"${it.group}:${it.name}:${it.version}\"")));
                    }))));
                }));
            }));

            runner().withTasks("verify").build();
        }

        @Test
        void testTestFixturesDependencyModifierOnProjectNotation() {
            settingsFile().append(setRootProjectName("foo"));
            buildFile().append(val("dependencyUnderTest", assign(modifierDsl(project()))));
            buildFile().append(setGroup("com.example"));
            buildFile().append(setVersion("4.2"));
            buildFile().append(registerTask("verify", taskBlock -> {
                taskBlock.add(doLast(it -> {
                    it.add(assertTrue(call("dependencyUnderTest.requestedCapabilities.any", lambda(anyBlock -> {
                        anyBlock.add(string("com.example:foo-test-fixtures:4.2").equalTo(literal("\"${it.group}:${it.name}:${it.version}\"")));
                    }))));
                }));
            }));

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

    static Expression setRootProjectName(String name) {
        return plainProperty("rootProject").plainProperty("name").assign(string(name));
    }

    static Expression setGroup(String group) {
        return plainProperty("group").assign(string(group));
    }

    static Expression setVersion(String version) {
        return plainProperty("version").assign(string(version));
    }

    static Expression project() {
        return literal("project");
    }
}
