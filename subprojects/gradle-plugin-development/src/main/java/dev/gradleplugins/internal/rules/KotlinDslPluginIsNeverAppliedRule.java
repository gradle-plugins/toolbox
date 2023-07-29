package dev.gradleplugins.internal.rules;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

public final class KotlinDslPluginIsNeverAppliedRule implements Action<Project> {
    private final String currentPluginId;

    public KotlinDslPluginIsNeverAppliedRule(String currentPluginId) {
        this.currentPluginId = currentPluginId;
    }

    @Override
    public void execute(Project project) {
        project.getPluginManager().withPlugin("org.gradle.kotlin.kotlin-dsl", appliedPlugin -> {
            throw new GradleException("The Gradle plugin 'kotlin-dsl' should not be applied within your build when using '" + currentPluginId + "'.");
        });
    }
}
