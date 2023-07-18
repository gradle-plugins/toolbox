package dev.gradleplugins.internal.rules;

import org.gradle.api.Action;
import org.gradle.api.GradleException;
import org.gradle.api.Project;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class OtherGradlePluginDevelopmentPluginsIncompatibilityRule implements Action<Project> {
    private static final Set<String> GRADLE_PLUGIN_DEVELOPMENT_PLUGINS = new HashSet<String>() {{
        add("dev.gradleplugins.java-gradle-plugin");
        add("dev.gradleplugins.groovy-gradle-plugin");
        add("dev.gradleplugins.kotlin-gradle-plugin");
    }};
    private final String currentPluginId;

    public OtherGradlePluginDevelopmentPluginsIncompatibilityRule(String currentPluginId) {
        this.currentPluginId = currentPluginId;
    }

    @Override
    public void execute(Project project) {
        GRADLE_PLUGIN_DEVELOPMENT_PLUGINS.stream().filter(not(currentPluginId::equals)).forEach(disallowPlugin(project));
    }

    private Consumer<String> disallowPlugin(Project project) {
        return id -> {
            project.getPluginManager().withPlugin(id, __ -> {
                throw new GradleException("The '" + currentPluginId + "' cannot be applied with '" + id + "', please apply just one of them.");
            });
        };
    }

    private static <T> Predicate<T> not(Predicate<T> delegate) {
        return it -> !delegate.test(it);
    }
}
