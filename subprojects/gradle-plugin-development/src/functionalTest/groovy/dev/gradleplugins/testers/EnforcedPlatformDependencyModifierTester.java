package dev.gradleplugins.testers;

import dev.gradleplugins.buildscript.ast.ExpressionBuilder;
import dev.gradleplugins.buildscript.ast.expressions.Expression;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static dev.gradleplugins.buildscript.ast.expressions.AssignmentExpression.assign;
import static dev.gradleplugins.buildscript.ast.expressions.MethodCallExpression.call;
import static dev.gradleplugins.buildscript.ast.expressions.VariableDeclarationExpression.val;
import static dev.gradleplugins.buildscript.blocks.GradleBuildScriptBlocks.doLast;
import static dev.gradleplugins.buildscript.blocks.GradleBuildScriptBlocks.registerTask;
import static dev.gradleplugins.buildscript.syntax.Syntax.assertTrue;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.buildscript.syntax.Syntax.string;

public abstract class EnforcedPlatformDependencyModifierTester {
    public abstract GradleRunner runner();

    public abstract ExpressionBuilder<?> modifierDsl();

    public abstract GradleBuildFile buildFile();

    private abstract class Tester {
        public abstract ExpressionBuilder<?> modifierDsl(Expression dsl);

        @Test
        void testEnforcedPlatformDependencyModifierOnExternalModuleDependency() throws IOException {
            buildFile().append(val("dependencyUnderTest", assign(modifierDsl(call("dependencies.create", string("com.example:foo:1.0"))))));
            buildFile().append(registerTask("verify", taskBlock -> {
                taskBlock.add(doLast(doLastBlock -> {
                    doLastBlock.add(assertTrue(call("dependencyUnderTest.isEndorsingStrictVersions").negate()));
                    doLastBlock.add(assertTrue(literal("dependencyUnderTest.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)?.name").equalTo(string("enforced-platform"))));
                }));
            }));

            runner().withTasks("verify").build();
        }

        @Test
        void testEnforcedPlatformDependencyModifierOnGroupArtifactVersionNotation() {
            buildFile().append(val("dependencyUnderTest", assign(modifierDsl(string("com.example:foo:1.0")))));
            buildFile().append(registerTask("verify", taskBlock -> {
                taskBlock.add(doLast(doLastBlock -> {
                    doLastBlock.add(assertTrue(call("dependencyUnderTest.isEndorsingStrictVersions").negate()));
                    doLastBlock.add(assertTrue(literal("dependencyUnderTest.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)?.name").equalTo(string("enforced-platform"))));
                }));
            }));

            runner().withTasks("verify").build();
        }

        @Test
        void testEnforcedPlatformDependencyModifierOnProjectDependency() {
            buildFile().append(val("dependencyUnderTest", assign(modifierDsl(call("dependencies.create", literal("project"))))));
            buildFile().append(registerTask("verify", taskBlock -> {
                taskBlock.add(doLast(doLastBlock -> {
                    doLastBlock.add(assertTrue(call("dependencyUnderTest.isEndorsingStrictVersions").negate()));
                    doLastBlock.add(assertTrue(literal("dependencyUnderTest.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)?.name").equalTo(string("enforced-platform"))));
                }));
            }));

            runner().withTasks("verify").build();
        }

        @Test
        void testEnforcedPlatformDependencyModifierOnProjectNotation() {
            buildFile().append(val("dependencyUnderTest", assign(modifierDsl(literal("project")))));
            buildFile().append(registerTask("verify", taskBlock -> {
                taskBlock.add(doLast(doLastBlock -> {
                    doLastBlock.add(assertTrue(call("dependencyUnderTest.isEndorsingStrictVersions").negate()));
                    doLastBlock.add(assertTrue(literal("dependencyUnderTest.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)?.name").equalTo(string("enforced-platform"))));
                }));
            }));

            runner().withTasks("verify").build();
        }
    }

    @Nested
    public class GroovyDslTest extends Tester {
        @Override
        public ExpressionBuilder<?> modifierDsl(Expression dsl) {
            return EnforcedPlatformDependencyModifierTester.this.modifierDsl().call(dsl);
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
            return EnforcedPlatformDependencyModifierTester.this.modifierDsl().call(dsl);
        }
    }

    @Nested
    public class ModifierTest extends Tester {
        @Override
        public ExpressionBuilder<?> modifierDsl(Expression dsl) {
            return EnforcedPlatformDependencyModifierTester.this.modifierDsl().call("modify", dsl);
        }
    }
}
