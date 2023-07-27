package dev.gradleplugins.testers;

import dev.gradleplugins.BuildScriptFile;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public abstract class DependencyBucketTester {
    public abstract GradleRunner runner();

    public abstract String bucketDsl();

    public abstract BuildScriptFile buildFile();

    public BuildScriptFile settingsFile() {
        return new BuildScriptFile(buildFile().getLocation().getParent().resolve("settings.gradle"));
    }

    private abstract class Tester {
        public abstract String bucketDsl(String dsl);

        public String instanceofOperator() {
            return "instanceof";
        }

        public String setOf(String elements) {
            return "[" + elements + "] as Set";
        }

        @Test
        void testGroupArtifactVersionNotation() throws IOException {
            buildFile().append(
                    bucketDsl("\"com.example:foo:1.0\""),
                    "",
                    "tasks.register(\"verify\") {",
                    "  doLast {",
                    "    assert(" + DependencyBucketTester.this.bucketDsl() + ".asConfiguration.get().dependencies.any {",
                    "      it " + instanceofOperator() + " ExternalModuleDependency && \"com.example:foo:1.0\" == \"${it.group}:${it.name}:${it.version}\"",
                    "    })",
                    "  }",
                    "}"
            );

            runner().withTasks("verify").build();
        }

        @Test
        void testProjectNotation() throws IOException {
            settingsFile().append("include 'other-project'");
            buildFile().append(
                    bucketDsl("project(\":other-project\")"),
                    "",
                    "tasks.register(\"verify\") {",
                    "  doLast {",
                    "    assert(" + DependencyBucketTester.this.bucketDsl() + ".asConfiguration.get().dependencies.any {",
                    "      it " + instanceofOperator() + " ProjectDependency && \":other-project\" == it.dependencyProject.path",
                    "    })",
                    "  }",
                    "}"
            );

            runner().withTasks("verify").build();
        }

        @Test
        void testLocalProjectNotation() throws IOException {
            buildFile().append(
                    bucketDsl("project"),
                    "",
                    "tasks.register(\"verify\") {",
                    "  doLast {",
                    "    assert(" + DependencyBucketTester.this.bucketDsl() + ".asConfiguration.get().dependencies.any {",
                    "      it " + instanceofOperator() + " ProjectDependency && project.path == it.dependencyProject.path",
                    "    })",
                    "  }",
                    "}"
            );

            runner().withTasks("verify").build();
        }

        @Test
        void testFileCollectionNotation() throws IOException {
            buildFile().append(
                    bucketDsl("files(\"my-path\")"),
                    "",
                    "tasks.register(\"verify\") {",
                    "  doLast {",
                    "    assert(" + DependencyBucketTester.this.bucketDsl() + ".asConfiguration.get().dependencies.any {",
                    "      it " + instanceofOperator() + " FileCollectionDependency && " +  setOf("project.file(\"my-path\")") + " == it.files.files",
                    "    })",
                    "  }",
                    "}"
            );

            runner().withTasks("verify").build();
        }
    }

    @Nested
    public class GroovyDslTest extends Tester {
        @Override
        public String bucketDsl(String dsl) {
            return DependencyBucketTester.this.bucketDsl() + "(" + dsl + ")";
        }
    }

    @Nested
    public class KotlinDslTest extends Tester {
        @BeforeEach
        void useKotlinDsl() throws IOException {
            buildFile().useKotlinDsl();
        }

        @Override
        public String bucketDsl(String dsl) {
            return DependencyBucketTester.this.bucketDsl() + "(" + dsl + ")";
        }

        @Override
        public String instanceofOperator() {
            return "is";
        }

        @Override
        public String setOf(String elements) {
            return "setOf(" + elements + ")";
        }
    }

    @Nested
    public class AdderTest extends Tester {
        @Override
        public String bucketDsl(String dsl) {
            return DependencyBucketTester.this.bucketDsl() + ".add(" + dsl + ")";
        }
    }
}
