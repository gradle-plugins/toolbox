package dev.gradleplugins.fixtures.sample;

import dev.gradleplugins.fixtures.sources.RegularFileContent;
import dev.gradleplugins.fixtures.sources.annotations.SourceFileLocation;

@SourceFileLocation(file = "groovy-gradle-plugin/src/test/groovy/com/example/BasicPluginTest.groovy")
public final class BasicGradlePluginProjectBuilderTest extends RegularFileContent {
    @Override
    public String getSourceSetName() {
        return "test";
    }

    @Override
    protected String getPath() {
        return "groovy/com/example";
    }
}
