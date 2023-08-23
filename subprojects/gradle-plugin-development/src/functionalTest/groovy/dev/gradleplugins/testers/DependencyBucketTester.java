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
            buildFile().append(val("bucketUnderTest", assign(DependencyBucketTester.this.bucketDsl())));
            buildFile().append(groovyDsl(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert bucketUnderTest.asConfiguration.get().dependencies.any {",
                    "      it instanceof ExternalModuleDependency && 'com.example:foo:1.0' == \"${it.group}:${it.name}:${it.version}\"",
                    "    }",
                    "  }",
                    "}"
            ));

            runner().withTasks("verify").build();
        }

        @Test
        void testProjectNotation() {
            settingsFile().append(groovyDsl("include 'other-project'"));
            buildFile().append(bucketDsl(groovyDsl("project(':other-project')")));
            buildFile().append(val("bucketUnderTest", assign(DependencyBucketTester.this.bucketDsl())));
            buildFile().append(groovyDsl(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert bucketUnderTest.asConfiguration.get().dependencies.any {",
                    "      it instanceof ProjectDependency && ':other-project' == it.dependencyProject.path",
                    "    }",
                    "  }",
                    "}"
            ));

            runner().withTasks("verify").build();
        }

        @Test
        void testLocalProjectNotation() {
            buildFile().append(bucketDsl(groovyDsl("project")));
            buildFile().append(val("bucketUnderTest", assign(DependencyBucketTester.this.bucketDsl())));
            buildFile().append(groovyDsl(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert bucketUnderTest.asConfiguration.get().dependencies.any {",
                    "      it instanceof ProjectDependency && project.path == it.dependencyProject.path",
                    "    }",
                    "  }",
                    "}"
            ));

            runner().withTasks("verify").build();
        }

        @Test
        void testFileCollectionNotation() {
            buildFile().append(bucketDsl(groovyDsl("files('my-path')")));
            buildFile().append(val("bucketUnderTest", assign(DependencyBucketTester.this.bucketDsl())));
            buildFile().append(groovyDsl(
                    "tasks.register('verify') {",
                    "  doLast {",
                    "    assert bucketUnderTest.asConfiguration.get().dependencies.any {",
                    "      it instanceof FileCollectionDependency && [project.file('my-path')] as Set == it.files.files",
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
