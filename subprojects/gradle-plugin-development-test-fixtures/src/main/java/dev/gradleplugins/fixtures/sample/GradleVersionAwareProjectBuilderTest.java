package dev.gradleplugins.fixtures.sample;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

@SourceFileLocation(file = "java-gradle-plugin/src/test/java/com/example/VersionAwareTest.java")
public final class GradleVersionAwareProjectBuilderTest extends RegularFileContent {
    @Override
    protected String getPath() {
        return "java/com/example";
    }

    @Override
    public String getSourceSetName() {
        return "test";
    }
}
