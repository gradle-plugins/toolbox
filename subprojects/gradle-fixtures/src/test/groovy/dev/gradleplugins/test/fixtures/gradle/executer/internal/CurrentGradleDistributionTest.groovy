package dev.gradleplugins.test.fixtures.gradle.executer.internal

import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Subject

@Subject(CurrentGradleDistribution)
class CurrentGradleDistributionTest extends Specification {
    @Ignore
    def "refers to current Gradle"() {
        given:
        def distribution = new CurrentGradleDistribution()

        expect:
        distribution.gradleHomeDirectory.absolutePath.startsWith("${System.getProperty('user.home')}/.gradle/wrapper/dists/gradle-")
    }
}
