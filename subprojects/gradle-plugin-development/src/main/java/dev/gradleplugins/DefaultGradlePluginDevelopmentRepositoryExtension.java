package dev.gradleplugins;

import org.gradle.api.Action;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.internal.Actions;

final class DefaultGradlePluginDevelopmentRepositoryExtension implements GradlePluginDevelopmentRepositoryExtension, HasPublicType {
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
