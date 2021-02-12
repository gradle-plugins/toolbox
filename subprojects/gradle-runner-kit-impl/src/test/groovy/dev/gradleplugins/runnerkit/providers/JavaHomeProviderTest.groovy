package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import org.apache.commons.lang3.SystemUtils
import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.runnerkit.providers.JavaHomeProvider.current
import static dev.gradleplugins.runnerkit.providers.JavaHomeProvider.inherited

@Subject(JavaHomeProvider)
class JavaHomeProviderTest extends Specification {
    def "can provide current process JAVA_HOME"() {
        expect:
        def subject = current()
        subject.isPresent()
        subject.get() == SystemUtils.javaHome
        subject.asEnvironmentVariables == [JAVA_HOME: SystemUtils.javaHome.absolutePath]
    }

    def "can provide JAVA_HOME from environment variables"() {
        expect:
        def subject = inherited()
        !subject.isPresent()
        subject.asEnvironmentVariables == [:]
    }

    def "throws exception when using environment variable"() {
        given:
        def context = Stub(GradleExecutionContext) {
            getEnvironmentVariables() >> EnvironmentVariablesProvider.inherited().plus([JAVA_HOME: SystemUtils.javaHome])
        }

        when:
        current().validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == "Please use GradleRunner#withJavaHomeDirectory(File) instead of using environment variables."
    }
}
