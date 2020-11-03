package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import dev.gradleplugins.runnerkit.providers.CharacterEncodingProvider
import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import java.nio.charset.Charset

import static dev.gradleplugins.runnerkit.providers.CharacterEncodingProvider.defaultCharset
import static dev.gradleplugins.runnerkit.providers.CharacterEncodingProvider.of
import static dev.gradleplugins.runnerkit.providers.CharacterEncodingProvider.unset

@Subject(CharacterEncodingProvider)
class CharacterEncodingProviderTest extends Specification {
    def "unset has no value"() {
        expect:
        def subject = unset()
        !subject.isPresent()
        subject.asJvmSystemProperties == [:]
        subject.asArguments == []
    }

    def "default character encoding use charset default"() {
        expect:
        def subject = defaultCharset()
        subject.isPresent()
        subject.get() == Charset.defaultCharset()
        subject.asJvmSystemProperties == ['file.encoding': Charset.defaultCharset().name()]
        subject.asArguments == ["-Dfile.encoding=${Charset.defaultCharset().name()}"]*.toString()
    }

    def "can use any character encoding"() {
        expect:
        def subject = of(Charset.forName("ASCII"))
        subject.isPresent()
        subject.get() == Charset.forName("ASCII")
        subject.asJvmSystemProperties == ['file.encoding': 'US-ASCII']
        subject.asArguments == ['-Dfile.encoding=US-ASCII']
    }

    @Unroll
    def "throws exception when using system property flag in command line arguments"(provider) {
        given:
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.of(['-Dfile.encoding=UTF-8'])
        }

        when:
        provider.validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == 'Please use GradleRunner#withDefaultCharacterEncoding(Charset) instead of using the command line flags.'

        where:
        provider << [unset(), defaultCharset(), of(Charset.defaultCharset())]
    }
}
