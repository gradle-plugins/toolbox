package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.internal.rules.RegisterTestSuiteFactoryServiceRule;
import dev.gradleplugins.internal.rules.RegisterTestingExtensionOnGradleDevelExtensionRule;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.inject.Inject;

abstract class GradlePluginDevelopmentTestingBasePlugin implements Plugin<Project> {
    @Inject
    public GradlePluginDevelopmentTestingBasePlugin() {}

    @Override
    public void apply(Project project) {
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-base");
        whenPluginApplied(project, "java-gradle-plugin", new RegisterTestingExtensionOnGradleDevelExtensionRule());
        new RegisterTestSuiteFactoryServiceRule().execute(project);
        project.afterEvaluate(new FinalizeComponents());
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
