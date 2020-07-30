package dev.gradleplugins.test.fixtures.gradle

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.gradle.executer.GradleDistributionFactory
import dev.gradleplugins.test.fixtures.gradle.executer.internal.*
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

import static dev.gradleplugins.test.fixtures.gradle.executer.internal.WrapperGradleDistributionTest.createDistribution

@Subject(GradleExecuterFactory)
class GradleExecuterFactoryTest extends Specification {
    GradleExecuterFactory factory
    @Rule TemporaryFolder temporaryFolder = new TemporaryFolder()

    def setup() {
        this.factory = new GradleExecuterFactory(temporaryFolder.root)
    }

    def "can create TestKit Gradle executer"() {
        when:
        def executer = factory.testKit()

        then:
        executer instanceof GradleRunnerExecuter
        executer.distribution instanceof CurrentGradleDistribution
    }

    def "can create TestKit Gradle executer for specific distribution"() {
        def distribution = GradleDistributionFactory.distribution('6.5')

        when:
        def executer = factory.testKit(distribution)

        then:
        executer instanceof GradleRunnerExecuter
        executer.distribution == distribution
    }

    def "can create wrapper Gradle executer"() {
        given:
        createDistribution(TestFile.of(temporaryFolder.root))

        when:
        def executer = factory.wrapper(temporaryFolder.root)

        then:
        executer instanceof GradleWrapperExecuter
        executer.distribution instanceof WrapperGradleDistribution
    }

    def "can create forking Gradle executer"() {
        when:
        def executer = factory.forking()

        then:
        executer instanceof ForkingGradleExecuter
        executer.distribution instanceof CurrentGradleDistribution
    }

    def "can create forking Gradle executer for specific distribution"() {
        given:
        def distribution = GradleDistributionFactory.distribution('6.5')

        when:
        def executer = factory.forking(distribution)

        then:
        executer instanceof ForkingGradleExecuter
        executer.distribution == distribution
    }
}
