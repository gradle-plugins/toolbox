package dev.gradleplugins.runnerkit

import dev.gradleplugins.runnerkit.fixtures.PluginUnderTest
import org.gradle.util.GradleVersion

import static dev.gradleplugins.fixtures.runnerkit.BuildResultMatchers.hasFailureDescription
import static dev.gradleplugins.runnerkit.utils.PluginUnderTestMetadataReading.IMPLEMENTATION_CLASSPATH_PROP_KEY
import static dev.gradleplugins.runnerkit.utils.PluginUnderTestMetadataReading.PLUGIN_METADATA_FILE_NAME
import static org.junit.Assume.assumeFalse
import static spock.util.matcher.HamcrestSupport.expect

abstract class GradleRunnerConventionalPluginClasspathInjectionIntegrationTest extends BaseGradleRunnerIntegrationTest {
    private PluginUnderTest pluginUnderTest

    def setup() {
        pluginUnderTest = new PluginUnderTest(file("pluginProject"))
        buildFile << pluginUnderTest.useDeclaration
    }

    def "uses conventional plugin classpath if requested and is available"() {
        assumeFalse(this.class.name.contains('Wrapper'))
        expect:
        pluginUnderTest.build().exposeMetadata {
            runner('helloWorld')
                    .withPluginClasspath()
                    .build()
        }
    }

//    @InspectsBuildOutput
    def "does not use conventional plugin classpath if not requested"() {
        when:
        def result = pluginUnderTest.build().exposeMetadata {
            runner('helloWorld')
                    .buildAndFail()
        }

        then:
        expect result, hasFailureDescription("""
            |Plugin [id: 'com.company.helloworld'] was not found in any of the following sources:
            |
            |- Gradle Core Plugins (plugin is not in 'org.gradle' namespace)
            |- $pluginRepositoriesDisplayName (plugin dependency must include a version number for this source)
        """.stripMargin().trim())
    }

//    @InspectsBuildOutput
    def "explicit classpath takes precedence over conventional classpath"() {
        assumeFalse(this.class.name.contains('Wrapper'))
        given:
        def explicitClasspath = [file('does/not/exist')]

        when:
        def result = pluginUnderTest.exposeMetadata {
            runner('helloWorld')
                    .withPluginClasspath()
                    .withPluginClasspath(explicitClasspath)
                    .buildAndFail()
        }

        then:
        expect result, hasFailureDescription("""
            |Plugin [id: 'com.company.helloworld'] was not found in any of the following sources:
            |
            |- Gradle Core Plugins (plugin is not in 'org.gradle' namespace)
            |- Gradle TestKit (classpath: ${explicitClasspath*.absolutePath.join(File.pathSeparator)})
            |- $pluginRepositoriesDisplayName (plugin dependency must include a version number for this source)
        """.stripMargin().trim())
    }

    def "throws if conventional classpath is requested and metadata cannot be found"() {
        when:
        runner('helloWorld')
                .withPluginClasspath()
                .buildAndFail()

        then:
        def t = thrown(InvalidPluginMetadataException)
        t.message == "Test runtime classpath does not contain plugin metadata file '$PLUGIN_METADATA_FILE_NAME'".toString()
    }

    def "throws if metadata contains an empty classpath"() {
        when:
        pluginUnderTest.implClasspath().exposeMetadata {
            runner('helloWorld')
                    .withPluginClasspath()
                    .buildAndFail()
        }

        then:
        def t = thrown(InvalidPluginMetadataException)
        t.message == "Plugin metadata file '${pluginUnderTest.metadataFile.toURI().toURL()}' has empty value for property named '$IMPLEMENTATION_CLASSPATH_PROP_KEY'".toString()
    }

    def "throws if metadata has no implementation classpath"() {
        when:
        pluginUnderTest.noImplClasspath().exposeMetadata {
            runner('helloWorld')
                    .withPluginClasspath()
                    .build()
        }

        then:
        def t = thrown(InvalidPluginMetadataException)
        t.message == "Plugin metadata file '${pluginUnderTest.metadataFile.toURI().toURL()}' does not contain expected property named '$IMPLEMENTATION_CLASSPATH_PROP_KEY'".toString()
    }

    private static String getPluginRepositoriesDisplayName() {
        return gradleVersion >= GradleVersion.version("4.4")
                ? "Plugin Repositories"
                : "Gradle Central Plugin Repository"
    }
}

class TestKitGradleRunnerConventionalPluginClasspathInjectionIntegrationTest extends GradleRunnerConventionalPluginClasspathInjectionIntegrationTest {

    @Override
    protected GradleRunner runner(String... arguments) {
        return GradleRunner.create(GradleExecutor.gradleTestKit()).inDirectory(testDirectory).withArguments(arguments)
    }
}

class WrapperGradleRunnerConventionalPluginClasspathInjectionIntegrationTest extends GradleRunnerConventionalPluginClasspathInjectionIntegrationTest implements GradleWrapperFixture {
    @Override
    protected GradleRunner runner(String... arguments) {
        writeGradleWrapperToTestDirectory(gradleVersion.version)
        return GradleRunner.create(GradleExecutor.gradleWrapper()).inDirectory(testDirectory).withArguments(arguments)
    }
}
