package dev.gradleplugins.fixtures.sample;

import dev.gradleplugins.fixtures.sources.Element;
import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class GradlePluginApi extends GradlePluginElement {
    private final GradlePluginElement gradlePluginElement;
    private final String sourceSetName;
    private final SourceElement apiElement = new SourceElement() {
        @Override
        public List<SourceFile> getFiles() {
            return Arrays.asList(sourceFile("java", "com/example/api/MyExtension.java", Stream.of(
                    "package com.example.api;",
                    "",
                    "import org.gradle.api.provider.Property;",
                    "",
                    "public interface MyExtension {",
                    "    Property<String> getValue();",
                    "}",
                    ""
            ).collect(Collectors.joining("\n"))));
        }

        @Override
        public String getSourceSetName() {
            return sourceSetName;
        }
    };

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
