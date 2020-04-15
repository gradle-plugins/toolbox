package dev.gradleplugins.fixtures.sample

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.gradleplugins.test.fixtures.sources.SourceFile

class TestableGradlePluginElement extends GradlePluginElement {
    private final GradlePluginElement main
    private final BasicGradlePluginTestKitFunctionalTest functionalTest

    TestableGradlePluginElement(GradlePluginElement main) {
        this.main = main
        this.functionalTest = new BasicGradlePluginTestKitFunctionalTest()
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
        ofElements(main, functionalTest).writeToProject(projectDir)
    }

    GradlePluginElement withTestingStrategySupport() {
        return new TestableGradlePluginElement(main) {
            @Override
            void writeToProject(TestFile projectDir) {
                ofElements(main, functionalTest.withTestingStrategySupport()).writeToProject(projectDir)
            }

            @Override
            GradlePluginElement withTestingStrategySupport() {
                return this
            }
        }
    }
}
