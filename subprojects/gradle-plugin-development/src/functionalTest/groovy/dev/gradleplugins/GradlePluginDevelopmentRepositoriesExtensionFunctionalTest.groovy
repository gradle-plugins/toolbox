package dev.gradleplugins

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import org.gradle.util.GradleVersion
import org.junit.Assume

class GradlePluginDevelopmentRepositoriesExtensionFunctionalTest extends AbstractGradleSpecification {
    def setup() {
        buildFile << '''
            def configuration = configurations.create('toResolve') {
                canBeConsumed = false
                canBeResolved = true
                attributes {
                    attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage, 'java-runtime'))
                }
            }
            
            dependencies {
                toResolve 'dev.gradleplugins:gradle-api:6.8.2'
            }

            tasks.register('verify') {
                doLast {
                    assert configuration.incoming.artifactView { lenient(true) }.artifacts*.id*.displayName == ['gradle-api-6.8.2.jar (dev.gradleplugins:gradle-api:6.8.2)']
                }
            }
        '''
    }

    def "can declare Gradle plugin development repository inside project repositories"() {
        given:
        settingsFile << configurePluginClasspathAsBuildScriptDependencies()
        settingsFile << '''
            apply plugin: 'dev.gradleplugins.gradle-plugin-development'
        '''
        buildFile << '''
            repositories {
                gradlePluginDevelopment()
            }
        '''

        expect:
        succeeds('verify')
    }

    def "can declare Gradle plugin development repository inside dependency resolution management"() {
        Assume.assumeTrue(GradleVersion.version(gradleDistributionUnderTest) >= GradleVersion.version("6.7"))

        given:
        settingsFile << configurePluginClasspathAsBuildScriptDependencies()
        settingsFile << '''
            apply plugin: 'dev.gradleplugins.gradle-plugin-development'
            
            dependencyResolutionManagement {
                repositories {
                    gradlePluginDevelopment()
                }
                repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
            }
            
            assert dependencyResolutionManagement.repositories*.name == ['Gradle Plugin Development']
        '''

        expect:
        succeeds('verify')
    }
}
