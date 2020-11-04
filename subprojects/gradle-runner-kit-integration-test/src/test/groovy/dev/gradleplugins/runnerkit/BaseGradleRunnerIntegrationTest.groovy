package dev.gradleplugins.runnerkit

import dev.gradleplugins.fixtures.file.FileSystemFixture
import dev.gradleplugins.fixtures.runnerkit.GradleScriptFixture
import org.gradle.util.GradleVersion
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

abstract class BaseGradleRunnerIntegrationTest extends Specification implements FileSystemFixture, GradleScriptFixture {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    public static GradleVersion gradleVersion = GradleVersion.current()

    protected abstract GradleRunner runner(String... arguments)

    protected static String helloWorldTask() {
        """
        task helloWorld {
            doLast {
                println 'Hello world!'
            }
        }
        """
    }

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }
}
