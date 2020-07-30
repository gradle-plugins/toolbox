package dev.gradleplugins.test.fixtures.gradle.executer.internal

import spock.lang.Specification

class TestKitGradleExecuterBuildContextTest extends Specification {
    def "ensures TEST_DIR is canonical"() {
        expect:
        TestKitGradleExecuterBuildContext.TEST_DIR.absolutePath == TestKitGradleExecuterBuildContext.TEST_DIR.canonicalPath
    }

    def "ensures TEST_DIR is pointing at the proper working directory"() {
        expect:
        TestKitGradleExecuterBuildContext.TEST_DIR.absolutePath == cwd
    }

    def "has default daemon base directory"() {
        expect:
        TestKitGradleExecuterBuildContext.INSTANCE.daemonBaseDirectory.absolutePath == "${System.getProperty("java.io.tmpdir")}.gradle-test-kit-${System.getProperty("user.name")}/daemon"
    }

    def "has default temporary directory"() {
        expect:
        TestKitGradleExecuterBuildContext.INSTANCE.temporaryDirectory.absolutePath == "${cwd}/build/tmp"
    }

    def "defaults Gradle user home directory to TestKit location"() {
        expect:
        TestKitGradleExecuterBuildContext.INSTANCE.gradleUserHomeDirectory.absolutePath == "${System.getProperty("java.io.tmpdir")}.gradle-test-kit-${System.getProperty("user.name")}"
    }

    protected String getCwd() {
        return Optional.ofNullable(System.getProperty('dev.gradleplugins.cwd')).orElseThrow { new AssertionError() }
    }
}
