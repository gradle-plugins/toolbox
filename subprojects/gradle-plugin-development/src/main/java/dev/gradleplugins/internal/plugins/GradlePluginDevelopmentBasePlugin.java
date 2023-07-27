package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.internal.rules.ConvertGradleApiSelfResolvingDependencyToExternalDependencyRule;
import dev.gradleplugins.internal.rules.RegisterGradlePluginDevelopmentCompatibilityExtensionRule;
import dev.gradleplugins.internal.rules.RegisterGradlePluginDevelopmentDependenciesExtensionRule;
import dev.gradleplugins.internal.rules.WireMinimumGradleVersionWithJvmCompatibilityRule;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.inject.Inject;

abstract /*final*/ class GradlePluginDevelopmentBasePlugin implements Plugin<Project> {
    @Inject
    public GradlePluginDevelopmentBasePlugin() {}

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply(GradlePluginDevelopmentExtensionPlugin.class);
        whenPluginApplied(project, "java-gradle-plugin", new WireMinimumGradleVersionWithJvmCompatibilityRule());
        whenPluginApplied(project, "java-gradle-plugin", new RegisterGradlePluginDevelopmentCompatibilityExtensionRule());
        whenPluginApplied(project, "java-gradle-plugin", new ConvertGradleApiSelfResolvingDependencyToExternalDependencyRule());
        whenPluginApplied(project, "java-gradle-plugin", new RegisterGradlePluginDevelopmentDependenciesExtensionRule());

        project.getPluginManager().withPlugin("java-gradle-plugin", new RemoveTestSourceSets(project));
    }

    private static void whenPluginApplied(Project project, String pluginId, Action<? super Project> action) {
        new WhenPluginAppliedAction(pluginId, action).execute(project);
    }

    private static final class WhenPluginAppliedAction implements Action<Project> {
        private final String pluginId;
        private final Action<? super Project> delegate;

        private WhenPluginAppliedAction(String pluginId, Action<? super Project> delegate) {
            this.pluginId = pluginId;
            this.delegate = delegate;
        }

        @Override
        public void execute(Project project) {
            project.getPluginManager().withPlugin(pluginId, __ -> delegate.execute(project));
        }
    }
}
