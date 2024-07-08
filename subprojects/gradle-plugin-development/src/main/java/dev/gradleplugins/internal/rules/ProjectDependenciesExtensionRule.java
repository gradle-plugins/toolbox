package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension;
import dev.gradleplugins.internal.DependencyFactory;
import dev.gradleplugins.internal.util.LocalOrRemoteVersionTransformer;
import groovy.lang.Closure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/*private*/ abstract /*final*/ class ProjectDependenciesExtensionRule implements Plugin<Project> {
    private static final Logger LOGGER = Logging.getLogger(ProjectDependenciesExtensionRule.class);

    @Inject
    public ProjectDependenciesExtensionRule() {}

    @Override
    public void apply(Project project) {
        final DependencyHandler dependencies = project.getDependencies();
        dependencies.getExtensions().add("gradlePluginDevelopment", new DefaultGradlePluginDevelopmentDependencyExtension(project.getDependencies()));
        try {
            Method target = Class.forName("dev.gradleplugins.internal.dsl.groovy.GroovyDslRuntimeExtensions").getMethod("extendWithMethod", Object.class, String.class, Closure.class);
            target.invoke(null, dependencies, "gradleApi", new GradleApiClosure(dependencies));
            target.invoke(null, dependencies, "gradleTestKit", new GradleTestKitClosure(dependencies));
            target.invoke(null, dependencies, "gradleFixtures", new GradleFixturesClosure(dependencies));
            target.invoke(null, dependencies, "gradleRunnerKit", new GradleRunnerKitClosure(dependencies));
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            LOGGER.info("Unable to extend DependencyHandler with gradleApi(String) and gradleFixtures().");
        }
    }

    private static class GradleApiClosure extends Closure<Dependency> {
        public GradleApiClosure(DependencyHandler handler) {
            super(handler);
        }

        public Dependency doCall(String version) {
            return ((ExtensionAware) getOwner()).getExtensions().getByType(GradlePluginDevelopmentDependencyExtension.class).gradleApi(version);
        }
    }

    private static class GradleTestKitClosure extends Closure<Dependency> {
        public GradleTestKitClosure(DependencyHandler handler) {
            super(handler);
        }

        public Dependency doCall(String version) {
            return ((ExtensionAware) getOwner()).getExtensions().getByType(GradlePluginDevelopmentDependencyExtension.class).gradleTestKit(version);
        }
    }

    private static class GradleFixturesClosure extends Closure<Dependency> {
        public GradleFixturesClosure(DependencyHandler handler) {
            super(handler);
        }

        public Dependency doCall() {
            return ((ExtensionAware) getOwner()).getExtensions().getByType(GradlePluginDevelopmentDependencyExtension.class).gradleFixtures();
        }
    }

    private static class GradleRunnerKitClosure extends Closure<Dependency> {
        public GradleRunnerKitClosure(DependencyHandler handler) {
            super(handler);
        }

        public Dependency doCall() {
            return ((ExtensionAware) getOwner()).getExtensions().getByType(GradlePluginDevelopmentDependencyExtension.class).gradleRunnerKit();
        }
    }

    private static final class DefaultGradlePluginDevelopmentDependencyExtension implements GradlePluginDevelopmentDependencyExtension, HasPublicType {
        private final DependencyFactory factory;
        private final Transformer<Dependency, String> gradleApiTransformer;
        private final Transformer<Dependency, String> gradleTestKitTransformer;

        public DefaultGradlePluginDevelopmentDependencyExtension(DependencyHandler dependencies) {
            this.factory = new DependencyFactory(dependencies);
            this.gradleApiTransformer = new LocalOrRemoteVersionTransformer<>(factory::localGradleApi, factory::gradleApi);
            this.gradleTestKitTransformer = new LocalOrRemoteVersionTransformer<>(factory::localGradleTestKit, factory::gradleTestKit);
        }

        @Override
        public Dependency gradleApi(String version) {
            return gradleApiTransformer.transform(version);
        }

        @Override
        public Dependency gradleTestKit(String version) {
            return gradleTestKitTransformer.transform(version);
        }

        @Override
        public Dependency gradleFixtures() {
            return factory.gradleFixtures();
        }

        @Override
        public Dependency gradleRunnerKit() {
            return factory.gradleRunnerKit();
        }

        @Override
        public TypeOf<?> getPublicType() {
            return TypeOf.typeOf(GradlePluginDevelopmentDependencyExtension.class);
        }
    }
}
