package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.InvalidRunnerConfigurationException
import dev.gradleplugins.runnerkit.providers.CommandLineArgumentsProvider
import dev.gradleplugins.runnerkit.providers.LocaleProvider
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.gradleplugins.runnerkit.providers.LocaleProvider.defaultLocale
import static dev.gradleplugins.runnerkit.providers.LocaleProvider.of

@Subject(LocaleProvider)
class LocaleProviderTest extends Specification {
    def "can provide default locale"() {
        expect:
        def subject = defaultLocale()
        subject.isPresent()
        subject.get() == Locale.getDefault()
        subject.asJvmSystemProperties == ['user.language': Locale.getDefault().getLanguage(), 'user.country': Locale.getDefault().getCountry(), 'user.variant': Locale.getDefault().getVariant()]
        subject.asArguments == ["-Duser.language=${Locale.getDefault().getLanguage()}", "-Duser.country=${Locale.getDefault().getCountry()}", "-Duser.variant=${Locale.getDefault().getVariant()}"]*.toString()
    }

    def "can provide specific locale"() {
        expect:
        def subject = of(Locale.CHINESE)
        subject.isPresent()
        subject.get() == Locale.CHINESE
        subject.asJvmSystemProperties == ['user.language': Locale.CHINESE.getLanguage(), 'user.country': Locale.CHINESE.getCountry(), 'user.variant': Locale.CHINESE.getVariant()]
        subject.asArguments == ["-Duser.language=${Locale.CHINESE.getLanguage()}", "-Duser.country=${Locale.CHINESE.getCountry()}", "-Duser.variant=${Locale.CHINESE.getVariant()}"]*.toString()
    }

    @Unroll
    def "throws exception when using local system properties flag in command line arguments"(flag, provider) {
        given:
        def context = Stub(GradleExecutionContext) {
            getArguments() >> CommandLineArgumentsProvider.of([flag])
        }

        when:
        provider.validate(context)

        then:
        def ex = thrown(InvalidRunnerConfigurationException)
        ex.message == 'Please use GradleRunner#withDefaultLocale(Locale) instead of using the command line flags.'

        where:
        [flag, provider] << [["-Duser.language=${Locale.CHINESE.getLanguage()}", "-Duser.country=${Locale.CHINESE.getCountry()}", "-Duser.variant=${Locale.CHINESE.getVariant()}"]*.toString(), [defaultLocale(), of(Locale.CHINESE)]].combinations()
    }
    // TODO: Don't allow any of these flag
}
