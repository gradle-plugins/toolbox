package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.runnerkit.providers.StandardStreamProvider
import spock.lang.Specification
import spock.lang.Subject

import java.nio.charset.Charset

@Subject(StandardStreamProvider)
class StandardStreamProviderTest extends Specification {
    def "can provide stdout forwarding stream"() {
        expect:
        def subject = StandardStreamProvider.forwardToStandardOutput()
        subject.isPresent()
        subject.get() == System.out
    }

    def "can provide stderr forwarding stream"() {
        expect:
        def subject = StandardStreamProvider.forwardToStandardError()
        subject.isPresent()
        subject.get() == System.err
    }

    def "can provide writer forwarding stream"() {
        given:
        def outStream = new ByteArrayOutputStream()

        expect:
        def subject = StandardStreamProvider.of(new OutputStreamWriter(outStream))
        subject.isPresent()

        def writer = subject.get().newWriter(Charset.defaultCharset().name())
        writer.write("Hello, world!")
        writer.flush()
        outStream.toString() == 'Hello, world!'
    }
}
