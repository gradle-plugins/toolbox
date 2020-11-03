package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import dev.gradleplugins.runnerkit.providers.BuildCacheProvider
import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.gradleplugins.runnerkit.GradleExecutionContext.BuildCache.DISABLED
import static dev.gradleplugins.runnerkit.GradleExecutionContext.BuildCache.ENABLED
import static dev.gradleplugins.runnerkit.providers.BuildCacheProvider.disabled
import static dev.gradleplugins.runnerkit.providers.BuildCacheProvider.enabled

@Subject(BuildCacheProvider)
class BuildCacheProviderTest extends Specification {
    def "ensures all possible values are accounted for"() {
        expect:
        GradleExecutionContext.BuildCache.values() as Set == [ENABLED, DISABLED] as Set
    }

    def "can enable build cache"() {
        expect:
        enabled().isPresent()
        enabled().get() == ENABLED
        enabled().asArguments == ['--build-cache']
    }

    def "can disable build cache"() {
        expect:
        disabled().isPresent()
        disabled().get() == DISABLED
        disabled().asArguments == []
    }

    def "can validate"() {
        expect:
        enabled().validate(Stub(GradleExecutionContext))
        disabled().validate(Stub(GradleExecutionContext))
    }

    def "can calculate values"() {
        expect:
        enabled().calculateValue(Stub(GradleExecutionContext))
        disabled().calculateValue(Stub(GradleExecutionContext))
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
        provider    | flag                  | message
        enabled()   | '--build-cache'       | 'Please remove command line flag enabling build cache as it was already enabled via GradleRunner#withBuildCacheEnabled().'
        enabled()   | '--no-build-cache'    | 'Please remove command line flag disabling build cache and any call to GradleRunner#withBuildCacheEnabled() for this runner as it is disabled by default for all toolbox runner.'
        disabled()  | '--build-cache'       | 'Please use GradleRunner#withBuildCacheEnabled() instead of using flag in command line arguments.'
        disabled()  | '--no-build-cache'    | 'Please remove command line flag disabling build cache as it is disabled by default for all toolbox runner.'
    }

    def "does not throw exceptions when build cache flag is not in the command line arguments"() {
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
