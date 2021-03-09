package dev.gradleplugins.internal

import org.gradle.api.Project
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.artifacts.SelfResolvingDependency
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class GradlePluginDevelopmentDependencyTest extends Specification {
    private final Project project = ProjectBuilder.builder().build()
    private final def dependencies = new GradlePluginDevelopmentDependencyExtensionInternal(project.getDependencies(), project)

    def "can use local Gradle API dependency"() {
        expect:
        def dependency = dependencies.gradleApi('local')
        dependency instanceof SelfResolvingDependency
        dependency.targetComponentId.displayName == 'Gradle API'
    }

    def "can use local Gradle TestKit dependency"() {
        expect:
        def dependency = dependencies.gradleTestKit('local')
        dependency instanceof SelfResolvingDependency
        dependency.targetComponentId.displayName == 'Gradle TestKit'
    }

    def "can use specific Gradle API dependency version"() {
        expect:
        def dependency = dependencies.gradleApi('6.2.1')
        dependency instanceof ExternalDependency
        dependency.group == 'dev.gradleplugins'
        dependency.name == 'gradle-api'
        dependency.version == '6.2.1'
    }

    def "can use specific Gradle TestKit dependency version"() {
        expect:
        def dependency = dependencies.gradleTestKit('6.2.1')
        dependency instanceof ExternalDependency
        dependency.group == 'dev.gradleplugins'
        dependency.name == 'gradle-test-kit'
        dependency.version == '6.2.1'
    }

    def "can use specific Groovy dependency version"() {
        expect:
        def dependency = dependencies.groovy('2.5.14')
        dependency instanceof ExternalDependency
        dependency.group == 'org.codehaus.groovy'
        dependency.name == 'groovy-all'
        dependency.version == '2.5.14'
    }
}
