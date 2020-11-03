package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.runnerkit.providers.EnvironmentVariablesProvider
import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.runnerkit.providers.EnvironmentVariablesProvider.contextDefault

@Subject(EnvironmentVariablesProvider)
class EnvironmentVariablesProviderTest extends Specification {
    def "can use context default value"() {
        expect:
        def subject = contextDefault()
        !subject.isPresent() // not present, it's up to the context to decide
    }

    def "can add environement variables to the context default values"() {
        expect:
        def subject = contextDefault().plus([A:'a'])
        subject.isPresent()
        subject.get() == [A: 'a']
    }
}
