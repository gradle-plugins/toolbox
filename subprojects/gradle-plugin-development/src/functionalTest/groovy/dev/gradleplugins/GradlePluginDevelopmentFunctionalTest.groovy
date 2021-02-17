package dev.gradleplugins

import dev.gradleplugins.fixtures.sample.GroovyBasicGradlePlugin
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
                    assert(repositories.withType(${dsl.asClassNotation(MavenArtifactRepository.simpleName)}).find { it.url.toString() == "https://repo.nokee.dev/release" } != null)
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

    @Unroll
    def "shows migration message"() {
        given:
        settingsFile << """
            plugins {
                id 'dev.gradleplugins.gradle-plugin-development'
            }
        """
        buildFile << """
            plugins {
                ${maybeApplyPlugin(useLegacyGradlePluginDevelopment, 'java-gradle-plugin')}
                ${maybeApplyPlugin(hasGroovyLanguageCapability, 'groovy')}
            }
        """
        if (hasGroovySource) {
            file('src/main/groovy/com/example/Foo.groovy') << '''package com.example
class Foo {}
'''
        }
        if (hasJavaSource) {
            file('src/main/java/com/example/Bar.java') << '''package com.example;
public class Bar {}
'''
        }

        when:
        def result = succeeds('tasks')

        then:
        if (messageToExpect == MigrationMessage.NONE) {
            result.assertNotOutput("The Gradle Plugin Development team recommends")
        } else if (messageToExpect == MigrationMessage.USE_GROOVY_GRADLE_PLUGIN) {
            result.assertOutputContains("The Gradle Plugin Development team recommends using 'dev.gradleplugins.groovy-gradle-plugin' instead of 'java-gradle-plugin' and 'groovy'/'groovy-base' in project ':'.")
        } else if (messageToExpect == MigrationMessage.USE_JAVA_GRADLE_PLUGIN) {
            result.assertOutputContains("The Gradle Plugin Development team recommends using 'dev.gradleplugins.java-gradle-plugin' instead of 'java-gradle-plugin' in project ':'.")
        } else {
            false
        }

        where:
        useLegacyGradlePluginDevelopment | hasGroovyLanguageCapability | hasGroovySource | hasJavaSource || messageToExpect
        false                            | false                       | false           | false         || MigrationMessage.NONE
        false                            | false                       | false           | true          || MigrationMessage.NONE
        false                            | false                       | true            | false         || MigrationMessage.NONE
        false                            | false                       | true            | true          || MigrationMessage.NONE
        false                            | true                        | false           | false         || MigrationMessage.NONE
        false                            | true                        | false           | true          || MigrationMessage.NONE
        false                            | true                        | true            | false         || MigrationMessage.NONE
        false                            | true                        | true            | true          || MigrationMessage.NONE
        true                             | false                       | false           | false         || MigrationMessage.USE_JAVA_GRADLE_PLUGIN
        true                             | false                       | false           | true          || MigrationMessage.USE_JAVA_GRADLE_PLUGIN
        true                             | false                       | true            | false         || MigrationMessage.USE_JAVA_GRADLE_PLUGIN
        true                             | false                       | true            | true          || MigrationMessage.USE_JAVA_GRADLE_PLUGIN
        true                             | true                        | false           | false         || MigrationMessage.USE_GROOVY_GRADLE_PLUGIN
        true                             | true                        | false           | true          || MigrationMessage.USE_JAVA_GRADLE_PLUGIN
        true                             | true                        | true            | false         || MigrationMessage.USE_GROOVY_GRADLE_PLUGIN
        true                             | true                        | true            | true          || MigrationMessage.USE_GROOVY_GRADLE_PLUGIN
    }

    private enum MigrationMessage {
        NONE, USE_JAVA_GRADLE_PLUGIN, USE_GROOVY_GRADLE_PLUGIN
    }

    private static String maybeApplyPlugin(boolean condition, String pluginId) {
        if (condition) {
            return "id '$pluginId'"
        }
        return ""
    }

    def "does not warn when using dev.gradleplugins.java-gradle-plugin"() {
        given:
        settingsFile << """
            plugins {
                id 'dev.gradleplugins.gradle-plugin-development'
            }
        """
        buildFile << """
            plugins {
                id 'dev.gradleplugins.java-gradle-plugin'
            }
        """
        new JavaBasicGradlePlugin().writeToProject(testDirectory)

        when:
        def result = succeeds('assemble')

        then:
        result.assertNotOutput('The Gradle Plugin Development team recommends')
    }

    def "does not warn when using dev.gradleplugins.groovy-gradle-plugin"() {
        given:
        settingsFile << """
            plugins {
                id 'dev.gradleplugins.gradle-plugin-development'
            }
        """
        buildFile << """
            plugins {
                id 'dev.gradleplugins.groovy-gradle-plugin'
            }
        """
        new GroovyBasicGradlePlugin().writeToProject(testDirectory)

        when:
        def result = succeeds('assemble')

        then:
        result.assertNotOutput('The Gradle Plugin Development team recommends')
    }

    def "does not warn when using kotlin-dsl plugin"() {
        given:
        settingsFile << """
            plugins {
                id 'dev.gradleplugins.gradle-plugin-development'
            }
        """
        file(GradleScriptDsl.KOTLIN_DSL.buildFileName) << """
            plugins {
                `kotlin-dsl`
            }

            repositories {
                mavenCentral()
            }
        """
        new JavaBasicGradlePlugin().writeToProject(testDirectory)

        when:
        def result = succeeds('assemble')

        then:
        result.assertNotOutput("The Gradle Plugin Development team recommends using")
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

    def "can handle exception with null message"() {
        given:
        settingsFile << """
            plugins {
                id 'dev.gradleplugins.gradle-plugin-development'
            }
            rootProject.name = 'root'
        """
        buildFile << """
            throw new GradleException("Some exception", new Throwable())
        """

        when:
        def failure = fails('tasks')

        then:
        failure.assertHasDescription("A problem occurred evaluating root project 'root'.")
        failure.assertHasCause("Some exception")
        failure.assertNotOutput("java.lang.NullPointerException (no error message)")
    }
}
