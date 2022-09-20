package dev.gradleplugins.internal.rules;

import dev.gradleplugins.internal.RuleGroup;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.SourceSet;

import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.ProviderUtils.finalizeValueOnRead;

@RuleGroup(ApiSourceSetGroup.class)
public final class ApiSourceSet_RegisterApiSourceSetPropertyOnGradlePluginDevelopmentExtensionRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        ((ExtensionAware) gradlePlugin(project)).getExtensions()
                .add(new TypeOf<Property<SourceSet>>() {}, "apiSourceSet",
                        finalizeValueOnRead(project.getObjects().property(SourceSet.class)));
    }
}
