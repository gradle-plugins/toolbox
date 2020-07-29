package dev.gradleplugins.test.fixtures.gradle.executer

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.gradle.executer.internal.CurrentGradleDistribution
import dev.gradleplugins.test.fixtures.gradle.executer.internal.DownloadableGradleDistribution
import dev.gradleplugins.test.fixtures.gradle.executer.internal.WrapperGradleDistribution
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.test.fixtures.gradle.executer.internal.WrapperGradleDistributionTest.createDistribution

@Subject(GradleDistributionFactory)
class GradleDistributionFactoryTest extends Specification {
    @Rule TemporaryFolder temporaryFolder = new TemporaryFolder()

    def "can create current Gradle distribution"() {
        when:
        def distribution = GradleDistributionFactory.current()

        then:
        distribution instanceof CurrentGradleDistribution
    }

    def "can create wrapper Gradle distribution"() {
        given:
        createDistribution(TestFile.of(temporaryFolder.root))

        when:
        def distribution = GradleDistributionFactory.wrapper(temporaryFolder.root)

        then:
        distribution instanceof WrapperGradleDistribution
    }

    def "can create downloadable Gradle distribution"() {
        when:
        def distribution = GradleDistributionFactory.distribution('6.5')

        then:
        distribution instanceof DownloadableGradleDistribution
    }
}
