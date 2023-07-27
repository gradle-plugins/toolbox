package dev.gradleplugins.testers;

import dev.gradleplugins.BuildScriptFile;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Arrays;

public abstract class TestFixturesDependencyModifierTester {
    public abstract GradleRunner runner();

    public abstract String modifierDsl();

    public abstract BuildScriptFile buildFile();

    private abstract class Tester {
        public abstract String modifierDsl(String dsl);

        @Test
        void testTestFixturesDependencyModifierOnExternalModuleDependency() throws IOException {
            buildFile().append(
                    "def dependencyUnderTest = " + modifierDsl("dependencies.create(\"com.example:foo:1.0\")"),
                    "",
                    "tasks.register(\"verify\") {",
                    "  doLast {",
                    "    assert(dependencyUnderTest.requestedCapabilities.any { \"com.example:foo-test-fixtures\" == \"${it.group}:${it.name}\" && it.version == null })",
                    "  }",
                    "}"
            );

            runner().withTasks("verify").build();
        }

        @Test
        void testTestFixturesDependencyModifierOnGroupArtifactVersionNotation() throws IOException {
            buildFile().append(
                    "def dependencyUnderTest = " + modifierDsl("\"com.example:foo:1.0\""),
                    "",
                    "tasks.register(\"verify\") {",
                    "  doLast {",
                    "    assert(dependencyUnderTest.requestedCapabilities.any { \"com.example:foo-test-fixtures\" == \"${it.group}:${it.name}\" && it.version == null })",
                    "  }",
                    "}"
            );

            runner().withTasks("verify").build();
        }

        @Test
        void testTestFixturesDependencyModifierOnProjectDependency() throws IOException {
            Files.write(buildFile().getLocation().getParent().resolve("settings.gradle"), Arrays.asList(
                    "rootProject.name = 'foo'"
            ));
            buildFile().append(
                    "def dependencyUnderTest = " + modifierDsl("dependencies.create(project)"),
                    "group = \"com.example\"",
                    "version = \"4.2\"",
                    "",
                    "tasks.register(\"verify\") {",
                    "  doLast {",
                    "    assert(dependencyUnderTest.requestedCapabilities.any { \"com.example:foo-test-fixtures:4.2\" == \"${it.group}:${it.name}:${it.version}\" })",
                    "  }",
                    "}"
            );

            runner().withTasks("verify").build();
        }

        @Test
        void testTestFixturesDependencyModifierOnProjectNotation() throws IOException {
            Files.write(buildFile().getLocation().getParent().resolve("settings.gradle"), Arrays.asList(
                    "rootProject.name = 'foo'"
            ));
            buildFile().append(
                    "def dependencyUnderTest = " + modifierDsl("project"),
                    "group = \"com.example\"",
                    "version = \"4.2\"",
                    "",
                    "tasks.register(\"verify\") {",
                    "  doLast {",
                    "    assert(dependencyUnderTest.requestedCapabilities.any { \"com.example:foo-test-fixtures:4.2\" == \"${it.group}:${it.name}:${it.version}\" })",
                    "  }",
                    "}"
            );

            runner().withTasks("verify").build();
        }
    }

    @Nested
    public class GroovyDslTest extends Tester {
        @Override
        public String modifierDsl(String dsl) {
            return TestFixturesDependencyModifierTester.this.modifierDsl() + "(" + dsl + ")";
        }
    }

    @Nested
    public class KotlinDslTest extends Tester {
        @BeforeEach
        void useKotlinDsl() throws IOException {
            buildFile().useKotlinDsl();
        }

        @Override
        public String modifierDsl(String dsl) {
            return TestFixturesDependencyModifierTester.this.modifierDsl() + "(" + dsl + ")";
        }
    }

    @Nested
    public class ModifierTest extends Tester {
        @Override
        public String modifierDsl(String dsl) {
            return TestFixturesDependencyModifierTester.this.modifierDsl() + ".modify(" + dsl + ")";
        }
    }
}
