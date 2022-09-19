package dev.gradleplugins.internal.util;

import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

public final class GradlePluginDevelopmentUtils {
    public static GradlePluginDevelopmentExtension gradlePlugin(Project project) {
        return project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
    }

    public static JavaPluginExtension java(Project project) {
        return project.getExtensions().getByType(JavaPluginExtension.class);
    }
}
