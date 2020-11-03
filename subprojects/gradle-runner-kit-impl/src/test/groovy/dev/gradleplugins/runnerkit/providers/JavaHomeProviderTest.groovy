package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import dev.gradleplugins.runnerkit.providers.EnvironmentVariablesProvider
import dev.gradleplugins.runnerkit.providers.JavaHomeProvider
import org.apache.commons.lang3.SystemUtils
import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.runnerkit.providers.JavaHomeProvider.current

@Subject(JavaHomeProvider)
class JavaHomeProviderTest extends Specification {
    def "can provide current process JAVA_HOME"() {
        expect:
        def subject = current()
        subject.isPresent()
        subject.get() == SystemUtils.javaHome
        subject.asEnvironmentVariables == [JAVA_HOME: SystemUtils.javaHome.absolutePath]
    }

    def "throws exception when using environment variable"() {
        given:
        def context = Stub(GradleExecutionContext) {
            getEnvironmentVariables() >> EnvironmentVariablesProvider.contextDefault().plus([JAVA_HOME: SystemUtils.javaHome])
        }

        when:
        current().validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == "Please use GradleRunner#withJavaHomeDirectory(File) instead of using environment variables."
    }
}
