package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentRepositoryExtension;
import dev.gradleplugins.internal.GradlePluginDevelopmentExtensionInternal;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

public final class RegisterLanguageExtensionRule implements Action<Project> {
    private final String languageName;
    private final Class<Object> extensionType;

    @SuppressWarnings("unchecked")
    public <T> RegisterLanguageExtensionRule(String languageName, Class<T> extensionType) {
        this.languageName = languageName;
        this.extensionType = (Class<Object>) extensionType;
    }

    @Override
    public void execute(Project project) {
        final GradlePluginDevelopmentExtension gradlePlugin = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
        final GradlePluginDevelopmentExtensionInternal extension = project.getObjects().newInstance(GradlePluginDevelopmentExtensionInternal.class, project.getExtensions().getByType(JavaPluginExtension.class));
        ((ExtensionAware)gradlePlugin).getExtensions().add(extensionType, languageName, extensionType.cast(extension));

        project.afterEvaluate(proj -> {
            if (!extension.isDefaultRepositoriesDisabled()) {
                GradlePluginDevelopmentRepositoryExtension.from(project.getRepositories()).gradlePluginDevelopment();
            }
        });
    }
}
