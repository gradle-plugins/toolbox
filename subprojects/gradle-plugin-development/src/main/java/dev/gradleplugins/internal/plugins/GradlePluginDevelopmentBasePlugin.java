package dev.gradleplugins.internal.plugins;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.inject.Inject;

/*private*/ abstract /*final*/ class GradlePluginDevelopmentBasePlugin implements Plugin<Project> {
    @Inject
    public GradlePluginDevelopmentBasePlugin() {}

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("gradlepluginsdev.rules.gradle-distribution-repositories");
        project.getPluginManager().apply("gradlepluginsdev.rules.gradle-repositories-extension");
        project.getPluginManager().apply("gradlepluginsdev.rules.project-dependencies-extension");
        project.getPluginManager().apply("gradlepluginsdev.rules.gradle-jvm-compatibilities");
        project.getPluginManager().apply("gradlepluginsdev.rules.gradle-plugin-compatibility-extension");
        project.getPluginManager().apply("gradlepluginsdev.rules.gradle-plugin-api-extension");
        project.getPluginManager().apply("gradlepluginsdev.rules.gradle-plugin-dependencies");
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
