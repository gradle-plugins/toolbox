package dev.gradleplugins;

import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin;
import dev.gradleplugins.runnerkit.GradleExecutor;
import dev.gradleplugins.runnerkit.GradleRunner;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;

class GradlePluginDevelopmentBasePluginFunctionalTests {
    @TempDir Path testDirectory;
    GradleRunner runner = GradleRunner.create(GradleExecutor.gradleTestKit()).withGradleVersion(System.getProperty("dev.gradleplugins.defaultGradleVersion")).withPluginClasspath();

    @BeforeEach
    void givenProject() throws IOException {
        new JavaBasicGradlePlugin().writeToProject(testDirectory.toFile());
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
            "plugins {",
            "  id 'dev.gradleplugins.gradle-plugin-base'",
            "}",
            "",
            "tasks.register('verify')"
        ));

        runner = runner.inDirectory(testDirectory).withTasks("verify");
    }

    @Test
    void whenJavaPluginApplied_registerCompatibilityExtension() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
            "apply plugin: 'java-gradle-plugin'",
            "",
            "import " + GradlePluginDevelopmentCompatibilityExtension.class.getCanonicalName(),
            "tasks.named('verify') {",
            "  doLast {",
            "    assert gradlePlugin.extensions.findByName('compatibility') instanceof " + GradlePluginDevelopmentCompatibilityExtension.class.getSimpleName(),
            "  }",
            "}"
        ), StandardOpenOption.APPEND);

        runner.build();
    }

    @Test
    void whenJavaPluginApplied_replaceLocalGradleApiWithRemoteGradleApi() throws IOException {
        Files.write(testDirectory.resolve("build.gradle"), Arrays.asList(
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
        ), StandardOpenOption.APPEND);

        runner.build();
    }
}
