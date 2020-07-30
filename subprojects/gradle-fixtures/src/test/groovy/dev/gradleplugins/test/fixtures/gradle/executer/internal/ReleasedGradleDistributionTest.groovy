package dev.gradleplugins.test.fixtures.gradle.executer.internal

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification

class ReleasedGradleDistributionTest extends Specification {
    @Rule TemporaryFolder temporaryFolder = new TemporaryFolder()

    def "can access binaries directory"() {
        given:
        def distribution = new ReleasedGradleDistribution('6.5.1', temporaryFolder.root)

        expect:
        distribution.binDistribution.absolutePath == "${temporaryFolder.root.canonicalPath}/gradle-6.5.1-bin.zip"
        distribution.binDistribution.assertIsFile()
        distribution.gradleHomeDirectory.absolutePath == "${temporaryFolder.root.canonicalPath}/gradle-6.5.1"
        distribution.gradleHomeDirectory.assertIsDirectory()
    }
}
