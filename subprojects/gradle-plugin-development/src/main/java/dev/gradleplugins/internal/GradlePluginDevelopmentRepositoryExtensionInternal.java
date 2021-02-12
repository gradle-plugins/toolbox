package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentRepositoryExtension;
import groovy.lang.Closure;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionAware;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@RequiredArgsConstructor
public class GradlePluginDevelopmentRepositoryExtensionInternal implements GradlePluginDevelopmentRepositoryExtension {
    private static final Logger LOGGER = Logging.getLogger(GradlePluginDevelopmentRepositoryExtensionInternal.class);
    @Getter(AccessLevel.PROTECTED) private final RepositoryHandler repositories;

    @Override
    public MavenArtifactRepository gradlePluginDevelopment() {
        getRepositories().mavenCentral(repo -> {
            repo.setName("Gradle Plugin Development - Groovy");
            repo.mavenContent(content -> content.includeModule("org.codehaus.groovy", "groovy"));
        });
        return getRepositories().maven(repository -> {
            repository.setName("Gradle Plugin Development");
            repository.setUrl("https://repo-release.nokeedev.net");
            repository.mavenContent(content -> {
                content.includeGroup("dev.gradleplugins");
            });
        });
    }

    public static GradlePluginDevelopmentRepositoryExtensionInternal of(RepositoryHandler repositories) {
        return (GradlePluginDevelopmentRepositoryExtensionInternal) ExtensionAware.class.cast(repositories).getExtensions().getByType(GradlePluginDevelopmentRepositoryExtension.class);
    }

    public void applyTo(RepositoryHandler repositories) {
        ExtensionAware.class.cast(repositories).getExtensions().add(GradlePluginDevelopmentRepositoryExtension.class, "gradlePluginDevelopment", this);
        try {
            Method target = Class.forName("dev.gradleplugins.internal.dsl.groovy.GroovyDslRuntimeExtensions").getMethod("extendWithMethod", Object.class, String.class, Closure.class);
            target.invoke(null, repositories, "gradlePluginDevelopment", new GradlePluginDevelopmentClosure(repositories));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.info("Unable to extend RepositoryHandler with gradlePluginDevelopment().");
        }
    }

    private class GradlePluginDevelopmentClosure extends Closure<Dependency> {
        public GradlePluginDevelopmentClosure(Object owner) {
            super(owner);
        }

        public MavenArtifactRepository doCall() {
            return GradlePluginDevelopmentRepositoryExtensionInternal.this.gradlePluginDevelopment();
        }
    }
}
