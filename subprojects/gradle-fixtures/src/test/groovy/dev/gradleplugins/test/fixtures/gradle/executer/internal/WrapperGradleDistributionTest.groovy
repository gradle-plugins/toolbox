package dev.gradleplugins.test.fixtures.gradle.executer.internal

import dev.gradleplugins.test.fixtures.file.TestFile
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@Subject(WrapperGradleDistribution)
class WrapperGradleDistributionTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    protected TestFile getTestDirectory() {
        return TestFile.of(temporaryFolder.root)
    }

    static void createDistribution(TestFile testDirectory) {
        testDirectory.file('gradle/wrapper/gradle-wrapper.properties').text = """
            |distributionBase=GRADLE_USER_HOME
            |distributionPath=wrapper/dists
            |distributionUrl=https\\://services.gradle.org/distributions/gradle-6.5-bin.zip
            |zipStoreBase=GRADLE_USER_HOME
            |zipStorePath=wrapper/dists
            |""".stripMargin()
        testDirectory.file('gradle/wrapper/gradle-wrapper.jar').createFile()
        testDirectory.file('gradlew').createFile()
        testDirectory.file('gradlew.bat').createFile()
    }

    @Unroll
    def "can retrieve wrapper #displayName version"(displayName, version) {
        given:
        createDistribution(testDirectory)
        testDirectory.file('gradle/wrapper/gradle-wrapper.properties').text = """
            |distributionBase=GRADLE_USER_HOME
            |distributionPath=wrapper/dists
            |distributionUrl=https\\://services.gradle.org/distributions/gradle-${version}-bin.zip
            |zipStoreBase=GRADLE_USER_HOME
            |zipStorePath=wrapper/dists
            |""".stripMargin()

        and:
        def distribution = new WrapperGradleDistribution(testDirectory)

        expect:
        distribution.version.version == version

        where:
        displayName         | version
        'released'          | '6.5'
        'patched released'  | '6.5.1'
        'release candidate' | '6.6-rc-1'
    }

    def "throws exception when retrieving distribution version if distributionUrl is not present"() {
        given:
        createDistribution(testDirectory)
        testDirectory.file('gradle/wrapper/gradle-wrapper.properties').text = """
            |distributionBase=GRADLE_USER_HOME
            |distributionPath=wrapper/dists
            |zipStoreBase=GRADLE_USER_HOME
            |zipStorePath=wrapper/dists
            |""".stripMargin()

        and:
        def distribution = new WrapperGradleDistribution(testDirectory)

        when:
        distribution.version

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Unable to retrive 'distributionUrl' property from '${testDirectory.absolutePath}/gradle/wrapper/gradle-wrapper.properties'."
    }

    @Unroll
    def "throws exception when wrapper distribution is invalid"(wrapperFileToDelete) {
        given:
        createDistribution(testDirectory)
        testDirectory.file(wrapperFileToDelete).delete()

        when:
        new WrapperGradleDistribution(testDirectory)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Invalid wrapper distribution at '${testDirectory.absolutePath}'."

        where:
        wrapperFileToDelete << ['gradlew', 'gradlew.bat', 'gradle/wrapper/gradle-wrapper.properties', 'gradle/wrapper/gradle-wrapper.jar']
    }

    def "throws exception when root project directory is null"() {
        when:
        new WrapperGradleDistribution(null)

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Invalid project directory, was null."
    }

    def "throws exception when root project directory does not exists"() {
        when:
        new WrapperGradleDistribution(testDirectory.file('does/not/exists'))

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Invalid project directory, path '${testDirectory.absolutePath}/does/not/exists' does not exists."
    }

    def "throws exception when root project directory is a file"() {
        when:
        new WrapperGradleDistribution(testDirectory.file('some-file').createFile())

        then:
        def ex = thrown(IllegalArgumentException)
        ex.message == "Invalid project directory, path '${testDirectory.absolutePath}/some-file' is a file."
    }
}
