package dev.gradleplugins.testers;

import dev.gradleplugins.BuildScriptFile;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public abstract class PlatformDependencyModifierTester {
    public abstract GradleRunner runner();

    public abstract String modifierDsl();

    public abstract BuildScriptFile buildFile();

    private abstract class Tester {
        public abstract String modifierDsl(String dsl);

        @Test
        void testPlatformDependencyModifierOnExternalModuleDependency() throws IOException {
            buildFile().append(
                    "def dependencyUnderTest = " + modifierDsl("dependencies.create(\"com.example:foo:1.0\")"),
                    "",
                    "tasks.register(\"verify\") {",
                    "  doLast {",
                    "    assert(dependencyUnderTest.isEndorsingStrictVersions())",
                    "    assert(dependencyUnderTest.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)?.name == \"platform\")",
                    "  }",
                    "}"
            );

            runner().withTasks("verify").build();
        }

        @Test
        void testPlatformDependencyModifierOnGroupArtifactVersionNotation() throws IOException {
            buildFile().append(
                    "def dependencyUnderTest = " + modifierDsl("\"com.example:foo:1.0\""),
                    "",
                    "tasks.register(\"verify\") {",
                    "  doLast {",
                    "    assert(dependencyUnderTest.isEndorsingStrictVersions())",
                    "    assert(dependencyUnderTest.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)?.name == \"platform\")",
                    "  }",
                    "}"
            );

            runner().withTasks("verify").build();
        }

        @Test
        void testPlatformDependencyModifierOnProjectDependency() throws IOException {
            buildFile().append(
                    "def dependencyUnderTest = " + modifierDsl("dependencies.create(project)"),
                    "",
                    "tasks.register(\"verify\") {",
                    "  doLast {",
                    "    assert(dependencyUnderTest.isEndorsingStrictVersions())",
                    "    assert(dependencyUnderTest.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)?.name == \"platform\")",
                    "  }",
                    "}"
            );

            runner().withTasks("verify").build();
        }

        @Test
        void testPlatformDependencyModifierOnProjectNotation() throws IOException {
            buildFile().append(
                    "def dependencyUnderTest = " + modifierDsl("project"),
                    "",
                    "tasks.register(\"verify\") {",
                    "  doLast {",
                    "    assert(dependencyUnderTest.isEndorsingStrictVersions())",
                    "    assert(dependencyUnderTest.attributes.getAttribute(Category.CATEGORY_ATTRIBUTE)?.name == \"platform\")",
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
            return PlatformDependencyModifierTester.this.modifierDsl() + "(" + dsl + ")";
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
            return PlatformDependencyModifierTester.this.modifierDsl() + "(" + dsl + ")";
        }
    }

    @Nested
    public class ModifierTest extends Tester {
        @Override
        public String modifierDsl(String dsl) {
            return PlatformDependencyModifierTester.this.modifierDsl() + ".modify(" + dsl + ")";
        }
    }
}
