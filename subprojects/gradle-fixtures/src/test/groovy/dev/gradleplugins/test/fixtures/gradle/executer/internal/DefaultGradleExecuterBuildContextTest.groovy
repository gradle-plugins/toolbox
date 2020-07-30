package dev.gradleplugins.test.fixtures.gradle.executer.internal

import spock.lang.Specification

class DefaultGradleExecuterBuildContextTest extends Specification {
    def "ensures TEST_DIR is canonical"() {
        expect:
        TestKitGradleExecuterBuildContext.TEST_DIR.absolutePath == DefaultGradleExecuterBuildContext.TEST_DIR.canonicalPath
    }

    def "ensures TEST_DIR is pointing at the proper working directory"() {
        expect:
        DefaultGradleExecuterBuildContext.TEST_DIR.absolutePath == cwd
    }

    def "has default daemon base directory"() {
        expect:
        DefaultGradleExecuterBuildContext.INSTANCE.daemonBaseDirectory.absolutePath == "${cwd}/build/daemon"
    }

    def "has default temporary directory"() {
        expect:
        DefaultGradleExecuterBuildContext.INSTANCE.temporaryDirectory.absolutePath == "${cwd}/build/tmp"
    }

    def "defaults Gradle user home directory to TestKit location"() {
        expect:
        DefaultGradleExecuterBuildContext.INSTANCE.gradleUserHomeDirectory.absolutePath == "${cwd}/intTestHomeDir/worker-1"
    }

    protected String getCwd() {
        return Optional.ofNullable(System.getProperty('dev.gradleplugins.cwd')).orElseThrow { new AssertionError() }
    }
}
