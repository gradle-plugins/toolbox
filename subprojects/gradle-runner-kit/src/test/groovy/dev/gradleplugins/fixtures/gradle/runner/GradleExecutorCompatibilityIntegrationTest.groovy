package dev.gradleplugins.fixtures.gradle.runner

import dev.gradleplugins.fixtures.file.FileSystemFixture
import dev.gradleplugins.fixtures.gradle.GradleScriptFixture
import org.apache.commons.lang3.JavaVersion
import org.apache.commons.lang3.SystemUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.IgnoreIf
import spock.lang.Specification

import static dev.gradleplugins.fixtures.gradle.runner.GradleExecutor.gradleTestKit
import static dev.gradleplugins.fixtures.gradle.runner.GradleExecutor.gradleWrapper
import static dev.gradleplugins.fixtures.gradle.runner.GradleRunner.create
import static dev.gradleplugins.fixtures.gradle.runner.GradleWrapperFixture.writeGradleWrapperTo

class GradleExecutorCompatibilityIntegrationTest extends Specification implements FileSystemFixture, GradleScriptFixture {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    @IgnoreIf({ SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_1_8) })
    def "reuse daemon for compatible executor"() {
        given:
        writeGradleWrapperTo(testDirectory)
        def testKitRunner = create(gradleTestKit()).inDirectory(testDirectory)
        def wrapperRunner = create(gradleWrapper()).inDirectory(testDirectory)

        buildFile << '''
            tasks.register('verify') {
                doLast {
                    file(project.property('pid')) << ProcessHandle.current().pid()
                }
            }
        '''

        when:
        testKitRunner.withArguments('verify', '-Ppid=testkit.txt').build()
        wrapperRunner.withArguments('verify', '-Ppid=wrapper.txt').build()

        then:
        file('testkit.txt').text == file('wrapper.txt').text
    }

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }
}
