package dev.gradleplugins.internal.util;

import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

public final class GradlePluginDevelopmentUtils {
    public static GradlePluginDevelopmentExtension gradlePlugin(Project project) {
        return project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
    }

    @SuppressWarnings("unchecked")
    public static Property<SourceSet> apiSourceSet(GradlePluginDevelopmentExtension extension) {
        return (Property<SourceSet>) ((ExtensionAware) extension).getExtensions().getByName("apiSourceSet");
    }

    public static JavaPluginExtension java(Project project) {
        return project.getExtensions().getByType(JavaPluginExtension.class);
    }
}
