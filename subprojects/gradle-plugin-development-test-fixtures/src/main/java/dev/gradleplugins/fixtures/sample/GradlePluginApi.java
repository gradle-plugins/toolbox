package dev.gradleplugins.fixtures.sample;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

import java.io.File;
import java.util.List;

public final class GradlePluginApi extends GradlePluginElement {
    private final GradlePluginElement gradlePluginElement;
    private final String sourceSetName;
    private final SourceElement apiElement = new MyExtensionElement();

    @SourceFileLocation(file = "java-gradle-plugin/src/main/java/com/example/api/MyExtension.java")
    private class MyExtensionElement extends RegularFileContent {
        @Override
        protected String getPath() {
            return "java/com/example/api";
        }

        @Override
        public String getSourceSetName() {
            return sourceSetName;
        }
    }

    public GradlePluginApi(GradlePluginElement gradlePluginElement) {
        this(gradlePluginElement, "main");
    }

    private GradlePluginApi(GradlePluginElement gradlePluginElement, String sourceSetName) {
        this.gradlePluginElement = gradlePluginElement;
        this.sourceSetName = sourceSetName;
    }

    @Override
    public List<SourceFile> getFiles() {
        throw new UnsupportedOperationException();
    }

    public void writeToProject(File projectDir) {
        gradlePluginElement.writeToProject(projectDir);
        apiElement.writeToProject(projectDir);
    }

    public GradlePluginApi withSourceSetName(String sourceSetName) {
        return new GradlePluginApi(gradlePluginElement, sourceSetName);
    }


    @Override
    public String getPluginId() {
        return gradlePluginElement.getPluginId();
    }

    @Override
    public TestableGradlePluginElement withFunctionalTest() {
        throw new UnsupportedOperationException();
    }
}
