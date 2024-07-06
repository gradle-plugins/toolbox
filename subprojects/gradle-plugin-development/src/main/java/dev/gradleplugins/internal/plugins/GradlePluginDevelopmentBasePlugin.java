package dev.gradleplugins.internal.plugins;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.inject.Inject;

abstract /*final*/ class GradlePluginDevelopmentBasePlugin implements Plugin<Project> {
    @Inject
    public GradlePluginDevelopmentBasePlugin() {}

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("gradlepluginsdev.rules.gradle-jvm-compatibilities");
        project.getPluginManager().apply(GradlePluginDevelopmentExtensionPlugin.class);
        project.getPluginManager().apply("gradlepluginsdev.rules.gradle-plugin-compatibility-extension");
        whenPluginApplied("java-gradle-plugin", new RemoveGradleApiProjectDependency()).execute(project);
        whenPluginApplied("java-gradle-plugin", new AddGradleApiDependencyToCompileOnlyApiConfiguration()).execute(project);
        whenPluginApplied("java-gradle-plugin", new RemoveTestSourceSets()).execute(project);
    }

    private static Action<Project> whenPluginApplied(String pluginId, Action<? super Project> action) {
        return new WhenPluginAppliedAction(pluginId, action);
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
