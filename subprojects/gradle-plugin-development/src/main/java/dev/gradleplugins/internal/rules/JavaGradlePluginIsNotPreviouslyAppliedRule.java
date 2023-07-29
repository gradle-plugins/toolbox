package dev.gradleplugins.internal.rules;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

public final class JavaGradlePluginIsNotPreviouslyAppliedRule implements Action<Project> {
    private final String currentPluginId;

    public JavaGradlePluginIsNotPreviouslyAppliedRule(String currentPluginId) {
        this.currentPluginId = currentPluginId;
    }

    @Override
    public void execute(Project project) {
        // The kotlin-dsl plugin gets special treatment in Gradle and gets moved to the front of the plugin list to apply. The kotlin-dsl internally apply the java-gradle-plugin plugin as well. Because of this, we are going to ignore the specific case of the kotlin-dsl applied and have a separate assertion.
        if (project.getPluginManager().hasPlugin("java-gradle-plugin") && !project.getPluginManager().hasPlugin("org.gradle.kotlin.kotlin-dsl")) {
            throw new GradleException("The Gradle core plugin 'java-gradle-plugin' should not be applied within your build when using '" + currentPluginId + "'.");
        }
    }
}
