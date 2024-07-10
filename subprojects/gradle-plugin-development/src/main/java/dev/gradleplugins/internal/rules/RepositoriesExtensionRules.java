package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentRepositoryExtension;
import dev.gradleplugins.internal.runtime.dsl.GroovyHelper;
import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.internal.Actions;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/*private*/ final class RepositoriesExtensionRules {
    private static final Logger LOGGER = Logging.getLogger(RepositoriesExtensionRules.class);

    private RepositoriesExtensionRules() {}

    /*private*/ static abstract /*final*/ class ForProject implements Plugin<Project> {
        @Inject
        public ForProject() {}

        public void apply(Project project) {
            decorate(project.getRepositories());
        }
    }

    /*private*/ static abstract /*final*/ class ForSettings implements Plugin<Settings> {
        @Inject
        public ForSettings() {}

        @Override
        public void apply(Settings settings) {
            try {
                Method Settings__getDependencyResolutionManagement = settings.getClass().getDeclaredMethod("getDependencyResolutionManagement");
                Object dependencyResolutionManagement = Settings__getDependencyResolutionManagement.invoke(settings);
                Method DependencyResolutionManagement__getRepositories = dependencyResolutionManagement.getClass().getDeclaredMethod("getRepositories");
                RepositoryHandler repositories = (RepositoryHandler) DependencyResolutionManagement__getRepositories.invoke(dependencyResolutionManagement);

                decorate(repositories);
            } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                // ignore, lower Gradle
            }
        }
    }

    private static void decorate(RepositoryHandler repositories) {
        final GradlePluginDevelopmentRepositoryExtension extension = ((ExtensionAware) repositories).getExtensions().create("gradlePluginDevelopment", DefaultGradlePluginDevelopmentRepositoryExtension.class, repositories);

        GroovyHelper.instance().addNewInstanceMethod(repositories, "gradlePluginDevelopment", new MethodClosure(extension, "gradlePluginDevelopment"));
    }

    /*private*/ static abstract /*final*/ class DefaultGradlePluginDevelopmentRepositoryExtension implements GradlePluginDevelopmentRepositoryExtension, HasPublicType {
        private final RepositoryHandler repositories;

        @Inject
        public DefaultGradlePluginDevelopmentRepositoryExtension(RepositoryHandler repositories) {
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
