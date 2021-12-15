package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentRepositoryExtension;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Action;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.plugins.ExtensionAware;

@RequiredArgsConstructor
public class GradlePluginDevelopmentRepositoryExtensionInternal implements GradlePluginDevelopmentRepositoryExtension {
    @Getter(AccessLevel.PROTECTED) private final RepositoryHandler repositories;
    private final GradlePluginDevelopmentRepositoryExtension extension;

    @Override
    public MavenArtifactRepository gradlePluginDevelopment() {
        return extension.gradlePluginDevelopment();
    }

    @Override
    public MavenArtifactRepository gradlePluginDevelopment(Action<? super MavenArtifactRepository> action) {
        return extension.gradlePluginDevelopment(action);
    }

    public static GradlePluginDevelopmentRepositoryExtensionInternal of(RepositoryHandler repositories) {
        return (GradlePluginDevelopmentRepositoryExtensionInternal) ExtensionAware.class.cast(repositories).getExtensions().getByType(GradlePluginDevelopmentRepositoryExtension.class);
    }

    public void applyTo(RepositoryHandler repositories) {
        ExtensionAware.class.cast(repositories).getExtensions().add(GradlePluginDevelopmentRepositoryExtension.class, "gradlePluginDevelopment", this);
    }
}
