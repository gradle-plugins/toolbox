package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentRepositoryExtension;
import dev.gradleplugins.internal.GradleDistributionRepositories;
import dev.gradleplugins.internal.runtime.dsl.GroovyHelper;
import dev.gradleplugins.internal.util.ClosureWrappedConfigureAction;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.artifacts.repositories.MavenArtifactRepository;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.internal.Actions;

import javax.inject.Inject;
import java.util.function.Supplier;

import static dev.gradleplugins.internal.util.DoNothingAction.doNothing;
import static dev.gradleplugins.internal.util.MinimumDependencyResolutionManagement.dependencyResolutionManagement;

/*private*/ final class RepositoriesExtensionRules {
    private static final Logger LOGGER = Logging.getLogger(RepositoriesExtensionRules.class);

    private RepositoriesExtensionRules() {}

    /*private*/ static abstract /*final*/ class ForProject implements Plugin<Project> {
        @Inject
        public ForProject() {}

        public void apply(Project project) {
            decorate(project.getRepositories(), () -> project.getExtensions().getByType(GradleDistributionRepositories.Factory.class));
        }
    }

    /*private*/ static abstract /*final*/ class ForSettings implements Plugin<Settings> {
        @Inject
        public ForSettings() {}

        @Override
        public void apply(Settings settings) {
            dependencyResolutionManagement(settings, it -> {
                it.repositories(repositories -> {
                    decorate(repositories, () -> settings.getExtensions().getByType(GradleDistributionRepositories.Factory.class));
                });
            });
        }
    }

    private static void decorate(RepositoryHandler repositories, Supplier<GradleDistributionRepositories.Factory>
            factory) {
        final GradlePluginDevelopmentRepositoryExtension extension = new DefaultGradlePluginDevelopmentRepositoryExtension(repositories, factory);
        ((ExtensionAware) repositories).getExtensions().add("gradlePluginDevelopment", extension);

        GroovyHelper.instance().addNewInstanceMethod(repositories, "gradlePluginDevelopment", new MethodClosure(extension, "gradlePluginDevelopment"));
        GroovyHelper.instance().addNewInstanceMethod(repositories, "gradleDistributions", new MethodClosure(extension, "gradleDistributions"));
        GroovyHelper.instance().addNewInstanceMethod(repositories, "gradleDistributionsSnapshots", new MethodClosure(extension, "gradleDistributionsSnapshots"));
    }

    // NOTE: The class MUST NOT BE a Gradle type because of the Groovy method injection
    private static final class DefaultGradlePluginDevelopmentRepositoryExtension implements GradlePluginDevelopmentRepositoryExtension, HasPublicType {
        private final RepositoryHandler repositories;
        private final Supplier<GradleDistributionRepositories.Factory> factory;

        @Inject
        public DefaultGradlePluginDevelopmentRepositoryExtension(RepositoryHandler repositories, Supplier<GradleDistributionRepositories.Factory> factory) {
            this.repositories = repositories;
            this.factory = factory;
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

        public MavenArtifactRepository gradlePluginDevelopment(@DelegatesTo(MavenArtifactRepository.class) @SuppressWarnings("rawtypes") Closure action) {
            return gradlePluginDevelopment(new ClosureWrappedConfigureAction<>(action));
        }

        @Override
        public ArtifactRepository gradleDistributions() {
            return factory.get().gradleDistributions(doNothing());
        }

        @Override
        public ArtifactRepository gradleDistributionsSnapshots() {
            return factory.get().gradleDistributionsSnapshots(doNothing());
        }

        @Override
        public TypeOf<?> getPublicType() {
            return TypeOf.typeOf(GradlePluginDevelopmentRepositoryExtension.class);
        }
    }
}
