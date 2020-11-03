package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import dev.gradleplugins.runnerkit.providers.BuildScanProvider
import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider
import org.apache.commons.lang3.SystemUtils
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.gradleplugins.runnerkit.GradleExecutionContext.BuildScan.DISABLED
import static dev.gradleplugins.runnerkit.GradleExecutionContext.BuildScan.ENABLED
import static dev.gradleplugins.runnerkit.providers.BuildScanProvider.disabled
import static dev.gradleplugins.runnerkit.providers.BuildScanProvider.enabled

@Subject(BuildScanProvider)
class BuildScanProviderTest extends Specification {
    def "ensures all possible values are accounted for"() {
        expect:
        GradleExecutionContext.BuildScan.values() as Set == [ENABLED, DISABLED] as Set
    }

    def "can enable build scan"() {
        expect:
        enabled().isPresent()
        enabled().get() == ENABLED
        enabled().asArguments[0] == '--init-script'
        enabled().asArguments[1].startsWith(SystemUtils.JAVA_IO_TMPDIR)
        enabled().asArguments[1].endsWith('.init.gradle')
        enabled().asArguments[2] == '--scan'
    }

    def "can disable build scan"() {
        expect:
        disabled().isPresent()
        disabled().get() == DISABLED
        disabled().asArguments == []
    }

    def "keep the same init script path"() {
        expect:
        def subject = enabled()
        subject.asArguments == subject.asArguments
    }

    def "creates the init script"() {
        expect:
        new File(enabled().asArguments[1]).exists()
    }

    @Unroll
    def "throws exception when using command line flag"() {
        given:
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.of([flag])
        }

        when:
        provider.validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == message

        where:
        provider    | flag           | message
        enabled()   | '--scan'       | 'Please remove command line flag enabling build scan as it was already enabled via GradleRunner#publishBuildScans().'
        enabled()   | '--no-scan'    | 'Please remove command line flag disabling build scan and any call to GradleRunner#publishBuildScans() for this runner as it is disabled by default for all toolbox runner.'
        disabled()  | '--scan'       | 'Please use GradleRunner#publishBuildScans() instead of using flag in command line arguments.'
        disabled()  | '--no-scan'    | 'Please remove command line flag disabling build scan as it is disabled by default for all toolbox runner.'
    }

    def "does not throw exceptions when scan flag is not in the command line arguments"() {
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.empty()
        }

        when:
        enabled().validate(context)
        then:
        noExceptionThrown()

        when:
        disabled().validate(context)
        then:
        noExceptionThrown()
    }
}
