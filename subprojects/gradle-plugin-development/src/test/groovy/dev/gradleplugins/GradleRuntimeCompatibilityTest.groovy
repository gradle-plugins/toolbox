package dev.gradleplugins

import org.gradle.api.JavaVersion
import spock.lang.Specification
import spock.lang.Subject

@Subject(GradleRuntimeCompatibility)
class GradleRuntimeCompatibilityTest extends Specification {
    def "can query minimum groovy version for specific Gradle version"() {
        expect:
        GradleRuntimeCompatibility.groovyVersionOf('6.2.1') == '2.5.8'
        GradleRuntimeCompatibility.groovyVersionOf('2.14') == '2.4.4'
    }

    def "can query minimum Java version for specific Gradle version"() {
        expect:
        GradleRuntimeCompatibility.minimumJavaVersionFor('6.2.1') == JavaVersion.VERSION_1_8
        GradleRuntimeCompatibility.minimumJavaVersionFor('2.14') == JavaVersion.VERSION_1_6
    }

    def "can query minimum Kotlin version for specific Gradle version"() {
        expect:
        GradleRuntimeCompatibility.kotlinVersionOf('6.2.1').get() == '1.3.61'
        !GradleRuntimeCompatibility.kotlinVersionOf('2.14').isPresent()
    }
}
