package dev.gradleplugins.internal.util;

import org.gradle.api.Project;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

public final class GradlePluginDevelopmentUtils {
    public static GradlePluginDevelopmentExtension gradlePlugin(Project project) {
        return project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
    }
}
