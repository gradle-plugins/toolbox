package dev.gradleplugins.integtests.fixtures

import dev.gradleplugins.test.fixtures.archive.JarTestFixture
import dev.gradleplugins.test.fixtures.archive.TarTestFixture
import dev.gradleplugins.test.fixtures.archive.ZipTestFixture
import dev.gradleplugins.test.fixtures.file.TestFile

// TODO: Rename to ArchiveFixtures (important with the s)
trait ArchiveTestFixture {
    abstract TestFile file(String path)

    JarTestFixture jar(String path) {
        return new JarTestFixture(file(path))
    }

    TarTestFixture tar(String path) {
        return new TarTestFixture(file(path))
    }

    ZipTestFixture zip(String path) {
        return new ZipTestFixture(file(path))
    }
}