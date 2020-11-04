package dev.gradleplugins.runnerkit.providers;

import dev.gradleplugins.runnerkit.InvalidPluginMetadataException;
import dev.gradleplugins.runnerkit.utils.PluginUnderTestMetadataReading;

import java.io.File;
import java.util.Collections;
import java.util.List;

public final class InjectedClasspathProvider extends AbstractGradleExecutionProvider<List<File>> {
    public static InjectedClasspathProvider of(List<File> classpath) {
        return fixed(InjectedClasspathProvider.class, classpath);
    }

    public static InjectedClasspathProvider empty() {
        return fixed(InjectedClasspathProvider.class, Collections.emptyList());
    }

    public static InjectedClasspathProvider fromPluginUnderTestMetadata() throws InvalidPluginMetadataException {
        return fixed(InjectedClasspathProvider.class, PluginUnderTestMetadataReading.readImplementationClasspath());
    }
}
