package dev.gradleplugins

import dev.gradleplugins.fixtures.sample.JavaBasicGradlePlugin
import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.gradleplugins.test.fixtures.gradle.GradleScriptDsl
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import spock.lang.Unroll

class GradlePluginDevelopmentFunctionalTest extends AbstractGradleSpecification {
    def setup() {
        file('init.gradle') << configurePluginClasspathAsBuildScriptDependencies().replace('buildscript', 'initscript')
        executer = executer.usingInitScript(file('init.gradle'))
    }

    @Unroll
    def "can use extension methods to declare plugin development repository from #dsl"(dsl) {
        given:
        settingsFile << """
            plugins {
                id 'dev.gradleplugins.gradle-plugin-development'
            }
        """
        file(dsl.buildFileName) << """
            repositories {
                gradlePluginDevelopment()
            }

            tasks.register("verify") {
                doLast {
                    assert(repositories.withType(${dsl.asClassNotation(MavenArtifactRepository.simpleName)}).find { it.url.toString() == "https://dl.bintray.com/gradle-plugins/distributions" } != null)
                    assert(repositories.withType(${dsl.asClassNotation(MavenArtifactRepository.simpleName)}).find { it.name == "Gradle Plugin Development" } != null)
                }
            }
        """

        expect:
        succeeds('verify')
        
        where:
        dsl << GradleScriptDsl.values()
    }

    @Unroll
    def "can use extension methods to declare Gradle API dependency from #dsl"(dsl) {
        given:
        settingsFile << """
            plugins {
                id 'dev.gradleplugins.gradle-plugin-development'
            }
        """
        file(dsl.buildFileName) << """
            plugins {
                id("java")
            }

            dependencies {
                implementation(gradleApi("6.2.1"))
            }

            tasks.register("verify") {
                doLast {
                    assert(configurations.implementation.dependencies.first().group == "dev.gradleplugins")
                    assert(configurations.implementation.dependencies.first().name == "gradle-api")
                    assert(configurations.implementation.dependencies.first().version == "6.2.1")
                }
            }
        """

        expect:
        succeeds('verify')

        where:
        dsl << GradleScriptDsl.values()
    }

    def "adds mavenCentral repository only for groovy via repository extension method"() {
        given:
        settingsFile << """
            plugins {
                id 'dev.gradleplugins.gradle-plugin-development'
            }
        """
        buildFile << """
            plugins {
                id 'java'
                id 'application'
            }

            repositories {
                gradlePluginDevelopment()
            }

            mainClassName = 'com.example.Main'
        """
        new JavaBasicGradlePlugin().withFunctionalTest().writeToProject(testDirectory)

        when:
        buildFile << """
            dependencies {
                compileOnly gradleApi('6.2.1') // Compile elements expose org.codehaus.groovy:groovy
            }
        """
        then:
        succeeds('assemble')

        when:
        buildFile << """
            dependencies {
                implementation gradleApi('6.2.1') // Runtime elements expose org.codehaus.groovy:groovy-all
            }
        """
        then:
        def failure = fails('run')
        failure.assertOutputContains('Please verify Gradle API was intentionally declared for runtime usage, see https://nokee.dev/docs/current/manual/gradle-plugin-development.html#sec:gradle-dev-compileonly-vs-implementation.')
        failure.assertOutputContains("If runtime usage of the Gradle API is expected, please declare a repository containing org.codehaus.groovy:groovy-all and org.jetbrains.kotlin:kotlin-stdlib artifacts, i.e. repositories.mavenCentral().")
    }

    def "shows informative message when groovy-all is not found"() {
        given:
        settingsFile << """
            plugins {
                id 'dev.gradleplugins.gradle-plugin-development'
            }
        """
        buildFile << """
            plugins {
                id 'java'
                id 'application'
            }

            repositories {
                gradlePluginDevelopment()
            }

            dependencies {
                implementation gradleApi('6.2.1') // Runtime elements expose org.codehaus.groovy:groovy-all
            }

            mainClassName = 'com.example.Main'
        """
        new JavaBasicGradlePlugin().withFunctionalTest().writeToProject(testDirectory)

        when:
        fails('run')
        then:
        result.assertOutputContains('Please verify Gradle API was intentionally declared for runtime usage, see https://nokee.dev/docs/current/manual/gradle-plugin-development.html#sec:gradle-dev-compileonly-vs-implementation.')
        result.assertOutputContains('If runtime usage of the Gradle API is expected, please declare a repository containing org.codehaus.groovy:groovy-all and org.jetbrains.kotlin:kotlin-stdlib artifacts, i.e. repositories.mavenCentral().')
    }

    def "shows informative message when no repositories are defined"() {
        given:
        settingsFile << """
            plugins {
                id 'dev.gradleplugins.gradle-plugin-development'
            }
        """
        buildFile << """
            plugins {
                id 'java'
            }

            dependencies {
                implementation gradleApi('6.2.1')
            }
        """
        new JavaBasicGradlePlugin().writeToProject(testDirectory)

        when:
        def failure = fails('assemble')

        then:
        failure.assertOutputContains('Please declare a repository using repositories.gradlePluginDevelopment().')
    }

    def "shows informative message when other repositories are defined"() {
        given:
        settingsFile << """
            plugins {
                id 'dev.gradleplugins.gradle-plugin-development'
            }
        """
        buildFile << """
            plugins {
                id 'java'
            }

            repositories {
                mavenCentral()
            }

            dependencies {
                implementation gradleApi('6.2.1')
            }
        """
        new JavaBasicGradlePlugin().writeToProject(testDirectory)

        when:
        def failure = fails('assemble')

        then:
        failure.assertOutputContains('Please declare a repository using repositories.gradlePluginDevelopment().')
    }

    def "warn when using java-gradle-plugin without dev.gradleplugins.java-gradle-plugin"() {
        given:
        settingsFile << """
            plugins {
                id 'dev.gradleplugins.gradle-plugin-development'
            }
        """
        buildFile << """
            plugins {
                id 'java-gradle-plugin'
            }
        """
        new JavaBasicGradlePlugin().writeToProject(testDirectory)

        when:
        def result = succeeds('assemble')

        then:
        result.assertOutputContains("The Gradle Plugin Development team recommends using 'dev.gradleplugins.java-gradle-plugin' instead of 'java-gradle-plugin' in project ':'.")
    }

    @Unroll
    def "decorates multiple projects [#dsl]"(dsl) {
        given:
        settingsFile << """
            plugins {
                id 'dev.gradleplugins.gradle-plugin-development'
            }

            include 'foo'
        """
        file('foo', dsl.buildFileName) << """
            plugins {
                id("java")
            }

            repositories {
                gradlePluginDevelopment()
            }

            dependencies {
                implementation(gradleApi("6.2.1"))
            }
        """

        expect:
        succeeds('assemble')

        where:
        dsl << GradleScriptDsl.values()
    }
}
