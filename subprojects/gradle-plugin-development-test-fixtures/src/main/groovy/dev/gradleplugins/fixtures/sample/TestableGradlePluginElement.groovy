package dev.gradleplugins.fixtures.sample

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.fixtures.sources.SourceFile

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
    void writeToProject(File projectDir) {
        ofElements(main, functionalTest).writeToProject(projectDir)
    }
}
