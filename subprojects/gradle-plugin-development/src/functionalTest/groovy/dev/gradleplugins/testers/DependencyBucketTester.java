package dev.gradleplugins.testers;

import dev.gradleplugins.buildscript.ast.ExpressionBuilder;
import dev.gradleplugins.buildscript.ast.expressions.Expression;
import dev.gradleplugins.buildscript.ast.type.ReferenceType;
import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.buildscript.io.GradleSettingsFile;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.FileCollectionDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.buildscript.ast.expressions.ItExpression.it;
import static dev.gradleplugins.buildscript.ast.expressions.MethodCallExpression.call;
import static dev.gradleplugins.buildscript.ast.type.UnknownType.unknownType;
import static dev.gradleplugins.buildscript.blocks.GradleBuildScriptBlocks.doLast;
import static dev.gradleplugins.buildscript.blocks.GradleBuildScriptBlocks.registerTask;
import static dev.gradleplugins.buildscript.syntax.Syntax.assertTrue;
import static dev.gradleplugins.buildscript.syntax.Syntax.lambda;
import static dev.gradleplugins.buildscript.syntax.Syntax.literal;
import static dev.gradleplugins.buildscript.syntax.Syntax.setOf;
import static dev.gradleplugins.buildscript.syntax.Syntax.string;

public abstract class DependencyBucketTester {
    public abstract GradleRunner runner();

    public abstract ExpressionBuilder<?> bucketDsl();

    public abstract GradleBuildFile buildFile();

    public abstract GradleSettingsFile settingsFile();

    private abstract class Tester {
        public abstract Expression bucketDsl(Expression dsl);

        @Test
        void testGroupArtifactVersionNotation() {
            buildFile().append(bucketDsl(string("com.example:foo:1.0")));
            buildFile().append(registerTask("verify", taskBlock -> {
                taskBlock.add(doLast(it -> {
                    it.add(assertTrue(DependencyBucketTester.this.bucketDsl().call("asConfiguration.get().dependencies.any", lambda(anyBlock -> {
                        anyBlock.add(it().instanceOf(ExternalModuleDependency.class).and(string("com.example:foo:1.0").equalTo(literal("\"${it.group}:${it.name}:${it.version}\""))));
                    }))));
                }));
            }));

            runner().withTasks("verify").build();
        }

        @Test
        void testProjectNotation() {
            settingsFile().append(call("include", string("other-project")));
            buildFile().append(bucketDsl(call("project", string(":other-project"))));
            buildFile().append(registerTask("verify", taskBlock -> {
                taskBlock.add(doLast(it ->{
                    it.add(assertTrue(DependencyBucketTester.this.bucketDsl().call("asConfiguration.get().dependencies.any", lambda(anyBlock -> {
                        anyBlock.add(it().instanceOf(ProjectDependency.class).and(string(":other-project").equalTo(it().dot("dependencyProject.path"))));
                    }))));
                }));
            }));

            runner().withTasks("verify").build();
        }

        @Test
        void testLocalProjectNotation() {
            buildFile().append(bucketDsl(literal("project")));
            buildFile().append(registerTask("verify", taskBlock -> {
                taskBlock.add(doLast(doLastBlock -> {
                    doLastBlock.add(assertTrue(DependencyBucketTester.this.bucketDsl().call("asConfiguration.get().dependencies.any", lambda(anyBlock -> {
                        anyBlock.add(it().instanceOf(ProjectDependency.class).and(literal("project.path").equalTo(it().dot("dependencyProject.path"))));
                    }))));
                }));
            }));

            runner().withTasks("verify").build();
        }

        @Test
        void testFileCollectionNotation() {
            buildFile().append(bucketDsl(call("files", string("my-path"))));
            buildFile().append(registerTask("verify", taskBlock -> {
                taskBlock.add(doLast(doLastBlock -> {
                    doLastBlock.add(assertTrue(DependencyBucketTester.this.bucketDsl().call("asConfiguration.get().dependencies.any", lambda(anyBlock -> {
                        anyBlock.add(it().instanceOf(FileCollectionDependency.class).and(setOf(call("project.file", string("my-path"))).equalTo(it().dot("files.files"))));
                    }))));
                }));
            }));

            runner().withTasks("verify").build();
        }
    }

    @Nested
    public class GroovyDslTest extends Tester {
        @Override
        public Expression bucketDsl(Expression dsl) {
            return DependencyBucketTester.this.bucketDsl().call(dsl);
        }
    }

    @Nested
    public class KotlinDslTest extends Tester {
        @BeforeEach
        void useKotlinDsl() {
            buildFile().useKotlinDsl();
        }

        @Override
        public Expression bucketDsl(Expression dsl) {
            return DependencyBucketTester.this.bucketDsl().call(dsl);
        }
    }

    @Nested
    public class AdderTest extends Tester {
        @Override
        public Expression bucketDsl(Expression dsl) {
            return DependencyBucketTester.this.bucketDsl().call("add", dsl);
        }
    }
}
