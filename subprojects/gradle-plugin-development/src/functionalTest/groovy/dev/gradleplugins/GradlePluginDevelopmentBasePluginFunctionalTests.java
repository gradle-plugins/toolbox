package dev.gradleplugins;

import dev.gradleplugins.buildscript.io.GradleBuildFile;
import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Path;

import static dev.gradleplugins.buildscript.blocks.ApplyStatement.Notation.plugin;
import static dev.gradleplugins.buildscript.blocks.ApplyStatement.apply;
import static dev.gradleplugins.buildscript.syntax.Syntax.groovyDsl;
import static dev.gradleplugins.buildscript.syntax.Syntax.importClass;

class GradlePluginDevelopmentBasePluginFunctionalTests {
    @TempDir Path testDirectory;
    GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(() -> testDirectory).withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).withPluginClasspath();
    GradleBuildFile buildFile;

    @BeforeEach
    void givenProject() throws IOException {
        new JavaBasicGradlePlugin().writeToProject(testDirectory.toFile());

        buildFile = GradleBuildFile.inDirectory(testDirectory);
        buildFile.plugins(it -> it.id("dev.gradleplugins.gradle-plugin-base"));
        buildFile.append(groovyDsl("tasks.register('verify')"));

        runner = runner.withTasks("verify");
    }

    @Test
    void whenJavaPluginApplied_registerCompatibilityExtension() {
        buildFile.append(apply(plugin("java-gradle-plugin")));
        buildFile.append(importClass(GradlePluginDevelopmentCompatibilityExtension.class));
        buildFile.append(groovyDsl(
            "tasks.named('verify') {",
            "  doLast {",
            "    assert gradlePlugin.extensions.findByName('compatibility') instanceof " + GradlePluginDevelopmentCompatibilityExtension.class.getSimpleName(),
            "  }",
            "}"
        ));

        runner.build();
    }

    @Test
    void whenJavaPluginApplied_replaceLocalGradleApiWithRemoteGradleApi() throws IOException {
        buildFile.append(groovyDsl(
                "apply plugin: 'java-gradle-plugin'",
                "",
                "gradlePlugin {",
                "  compatibility {",
                "    gradleApiVersion = '6.5'",
                "  }",
                "}",
                "",
                "tasks.named('verify') {",
                "  doLast {",
                "    assert !configurations.detachedConfiguration().extendsFrom(configurations.api).with {",
                "      canBeResolved = true",
                "      canBeConsumed = false",
                "      resolve()",
                "      return it",
                "    }.allDependencies.any { it instanceof SelfResolvingDependency }",
                "",
                "    assert configurations.detachedConfiguration().extendsFrom(configurations.compileOnly).with {",
                "      canBeResolved = true",
                "      canBeConsumed = false",
                "      resolve()",
                "      return it",
                "    }.allDependencies.any { it instanceof ExternalDependency && 'dev.gradleplugins:gradle-api:6.5'.equals(\"${it.group}:${it.name}:${it.version}\".toString()) }",
                "  }",
                "}"
        ));

        runner.build();
    }
}
