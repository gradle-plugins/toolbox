package dev.gradleplugins.fixtures.sample;

import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;

import java.io.File;
import java.util.List;

public final class TestableGradlePluginElement extends GradlePluginElement {
    private final GradlePluginElement main;
    private final SourceElement functionalTest;

    public TestableGradlePluginElement(GradlePluginElement main, SourceElement functionalTest) {
        this.main = main;
        this.functionalTest = functionalTest;
    }

    @Override
    public String getPluginId() {
        return main.getPluginId();
    }

    @Override
    public TestableGradlePluginElement withFunctionalTest() {
        return this;
    }

    @Override
    public List<SourceFile> getFiles() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void writeToProject(File projectDir) {
        ofElements(main, functionalTest).writeToProject(projectDir);
    }
}
