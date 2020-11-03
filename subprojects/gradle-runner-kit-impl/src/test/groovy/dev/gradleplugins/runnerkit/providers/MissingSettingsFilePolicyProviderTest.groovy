package dev.gradleplugins.runnerkit.providers

import dev.gradleplugins.fixtures.file.FileSystemFixture
import dev.gradleplugins.runnerkit.GradleExecutionContext
import dev.gradleplugins.runnerkit.providers.MissingSettingsFilePolicyProvider
import dev.gradleplugins.runnerkit.providers.WorkingDirectoryProvider
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.runnerkit.GradleExecutionContext.MissingSettingsFilePolicy.CREATE_WHEN_MISSING
import static dev.gradleplugins.runnerkit.GradleExecutionContext.MissingSettingsFilePolicy.IGNORES_WHEN_MISSING

@Subject(MissingSettingsFilePolicyProvider)
class MissingSettingsFilePolicyProviderTest extends Specification implements FileSystemFixture {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    @Override
    File getTestDirectory() {
        return temporaryFolder.root
    }

    def "ensures all possible values are accounted for"() {
        expect:
        GradleExecutionContext.MissingSettingsFilePolicy.values() as Set == [CREATE_WHEN_MISSING, IGNORES_WHEN_MISSING] as Set
    }

    def "can provide create on missing policy"() {
        given:
        def context = Stub(GradleExecutionContext) {
            getWorkingDirectory() >> WorkingDirectoryProvider.of(testDirectory)
        }

        expect:
        def subject = MissingSettingsFilePolicyProvider.createWhenMissing()
        subject.isPresent()
        subject.get() == CREATE_WHEN_MISSING

        and:
        subject.accept(context)
        file('settings.gradle').exists()
    }

    def "can provide ignores on missing policy"() {
        given:
        def context = Stub(GradleExecutionContext) {
            getWorkingDirectory() >> WorkingDirectoryProvider.of(testDirectory)
        }

        expect:
        def subject = MissingSettingsFilePolicyProvider.ignoresWhenMissing()
        subject.isPresent()
        subject.get() == IGNORES_WHEN_MISSING

        and:
        subject.accept(context)
        !file('settings.gradle').exists()
    }
}
