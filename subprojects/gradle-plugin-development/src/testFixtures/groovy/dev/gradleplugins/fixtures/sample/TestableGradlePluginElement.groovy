package dev.gradleplugins.fixtures.sample

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.sources.SourceFile

class TestableGradlePluginElement extends GradlePluginElement {
    private final GradlePluginElement main
    private final SourceElement functionalTest

    TestableGradlePluginElement(GradlePluginElement main, SourceElement functionalTest) {
        this.main = main
        this.functionalTest = functionalTest
    }

    @Override
    String getPluginId() {
        return main.pluginId
    }

    @Override
    TestableGradlePluginElement withFunctionalTest() {
        return this
    }

    @Override
    List<SourceFile> getFiles() {
        throw new UnsupportedOperationException()
    }

    @Override
    void writeToProject(TestFile projectDir) {
        dev.gradleplugins.test.fixtures.sources.SourceElement.ofElements(main, functionalTest).writeToProject(projectDir)
    }

    GradlePluginElement withTestingStrategySupport() {
        return new TestableGradlePluginElement(main, functionalTest.withTestingStrategySupport())
    }
}
