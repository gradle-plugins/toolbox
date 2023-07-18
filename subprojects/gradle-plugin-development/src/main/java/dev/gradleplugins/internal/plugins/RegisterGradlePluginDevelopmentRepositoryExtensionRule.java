package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentRepositoryExtension;
import dev.gradleplugins.internal.runtime.dsl.DefaultGradleExtensionMixInService;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.internal.Actions;

final class RegisterGradlePluginDevelopmentRepositoryExtensionRule implements Action<Project> {
    public void execute(Project project) {
        RepositoryHandler repositories = project.getRepositories();

        new DefaultGradleExtensionMixInService(project.getObjects())
                .forExtension(GradlePluginDevelopmentRepositoryExtension.class)
                .useInstance(new DefaultGradlePluginDevelopmentRepositoryExtension(repositories))
                .build()
                .mixInto((ExtensionAware) repositories);
    }

    private static final class DefaultGradlePluginDevelopmentRepositoryExtension implements GradlePluginDevelopmentRepositoryExtension, HasPublicType {
        private final RepositoryHandler repositories;

        DefaultGradlePluginDevelopmentRepositoryExtension(RepositoryHandler repositories) {
            this.repositories = repositories;
        }

        @Override
        public MavenArtifactRepository gradlePluginDevelopment() {
            return gradlePluginDevelopment(Actions.doNothing());
        }

        @Override
        public MavenArtifactRepository gradlePluginDevelopment(Action<? super MavenArtifactRepository> action) {
            return repositories.mavenCentral(repository -> {
                repository.setName("Gradle Plugin Development");
                repository.mavenContent(content -> {
                    content.includeGroup("dev.gradleplugins");
                    content.includeModule("org.codehaus.groovy", "groovy");

                    // Groovy 3+ transitive dependencies
                    content.includeModule("com.github.javaparser", "javaparser-core");
                    content.includeModule("com.github.javaparser", "javaparser-parent");
                    content.includeModule("org.junit", "junit-bom");
                });
                action.execute(repository);
            });
        }

        @Override
        public TypeOf<?> getPublicType() {
            return TypeOf.typeOf(GradlePluginDevelopmentRepositoryExtension.class);
        }
    }
}
