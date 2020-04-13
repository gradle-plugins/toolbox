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
}
