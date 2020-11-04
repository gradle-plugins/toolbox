package dev.gradleplugins.runnerkit

import dev.gradleplugins.fixtures.file.FileSystemFixture
import dev.gradleplugins.fixtures.runnerkit.GradleScriptFixture
import dev.gradleplugins.runnerkit.GradleWrapperFixture
import org.apache.commons.lang3.JavaVersion
import org.apache.commons.lang3.SystemUtils
import org.gradle.util.GradleVersion
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.IgnoreIf
import spock.lang.Specification

class GradleExecutorCompatibilityIntegrationTest extends Specification implements FileSystemFixture, GradleScriptFixture, GradleWrapperFixture {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    @IgnoreIf({ SystemUtils.isJavaVersionAtMost(JavaVersion.JAVA_1_8) })
    def "reuse daemon for compatible executor"() {
        given:
        writeGradleWrapperToTestDirectory(GradleVersion.current().version)
        def testKitRunner = GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(testDirectory)
        def wrapperRunner = GradleRunner.create(GradleExecutor.gradleWrapper()).inDirectory(testDirectory)

        buildFile << '''
            tasks.register('verify') {
                doLast {
                    println "=== Runtime information of ${ProcessHandle.current().pid()} ==="
                    System.getenv().each { k, v -> println "ENV -> $k = $v" }
                    System.properties.each { k, v -> println "PRO -> $k = $v" }
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
