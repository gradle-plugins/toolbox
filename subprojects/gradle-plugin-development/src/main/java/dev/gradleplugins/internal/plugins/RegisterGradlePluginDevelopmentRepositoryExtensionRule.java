package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentRepositoryExtension;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

final class RegisterGradlePluginDevelopmentRepositoryExtensionRule implements Action<Project> {
    private static final Logger LOGGER = Logging.getLogger(RegisterGradlePluginDevelopmentRepositoryExtensionRule.class);

    public void execute(Project project) {
        RepositoryHandler repositories = project.getRepositories();
        try {
            Method target = Class.forName("dev.gradleplugins.internal.dsl.groovy.GroovyDslRuntimeExtensions").getMethod("extendWithMethod", Object.class, String.class, Closure.class);
            target.invoke(null, repositories, "gradlePluginDevelopment", new GradlePluginDevelopmentClosure(repositories));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.info("Unable to extend RepositoryHandler with gradlePluginDevelopment().");
        }
    }

    private static class GradlePluginDevelopmentClosure extends Closure<Dependency> {
        private final GradlePluginDevelopmentRepositoryExtension extension;

        public GradlePluginDevelopmentClosure(RepositoryHandler repositories) {
            super(repositories);
            this.extension = GradlePluginDevelopmentRepositoryExtension.from(repositories);
        }

        public MavenArtifactRepository doCall() {
            return extension.gradlePluginDevelopment();
        }
    }
}
