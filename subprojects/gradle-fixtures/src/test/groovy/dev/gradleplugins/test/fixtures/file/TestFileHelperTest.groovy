package dev.gradleplugins.test.fixtures.file

import org.apache.commons.lang3.SystemUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.IgnoreIf
import spock.lang.Specification
import dev.gradleplugins.test.fixtures.archive.ZipTestFixture

class TestFileHelperTest extends Specification {
    @Rule
    TemporaryFolder temporaryFolder = new TemporaryFolder()

    @IgnoreIf({ SystemUtils.IS_OS_WINDOWS })
    def "can restore executable permission when unzip files"() {
        given:
        def file = new File(temporaryFolder.root, 'file-with-executable-permission')
        try {
            file.createNewFile()
            file.setExecutable(true, false)

            def process = "zip -r file.zip file-with-executable-permission".execute(null, temporaryFolder.root)
            process.consumeProcessOutput(System.out, System.err)
            assert process.waitFor() == 0
        } finally {
            file.delete()
        }

        and:
        def zipFile = new File(temporaryFolder.root, 'file.zip')
        assert new ZipTestFixture(zipFile).hasDescendants('file-with-executable-permission')

        when:
        TestFile.of(zipFile).unzipTo(temporaryFolder.root)

        then:
        file.exists()
        file.canExecute()
    }

    @IgnoreIf({ SystemUtils.IS_OS_WINDOWS }) // Because I'm lazy and it's good enough for now
    def "can execute commands with environment variables without value"() {
        given:
        def envVar = 'GRADLE_OPTS='

        when:
        def result = TestFile.of(findInPath('ls')).execute([], [envVar])

        then:
        noExceptionThrown()

        and:
        result.exitCode == 0
    }

    File findInPath(String command) {
        return System.getenv('PATH').split(File.pathSeparator).collect { new File("$it/$command") }.find { it.exists() }
    }
}
