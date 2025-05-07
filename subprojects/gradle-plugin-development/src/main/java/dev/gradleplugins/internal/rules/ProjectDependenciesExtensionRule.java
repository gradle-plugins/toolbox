package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension;
import dev.gradleplugins.internal.DependencyFactory;
import dev.gradleplugins.internal.runtime.dsl.GroovyHelper;
import dev.gradleplugins.internal.util.LocalOrRemoteVersionTransformer;
import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import javax.inject.Inject;

/*private*/ abstract /*final*/ class ProjectDependenciesExtensionRule implements Plugin<Project> {
    private static final Logger LOGGER = Logging.getLogger(ProjectDependenciesExtensionRule.class);

    @Inject
    public ProjectDependenciesExtensionRule() {}

    @Override
    public void apply(Project project) {
        final DependencyHandler dependencies = project.getDependencies();
        final GradlePluginDevelopmentDependencyExtension extension = new DefaultGradlePluginDevelopmentDependencyExtension(dependencies);
        dependencies.getExtensions().add("gradlePluginDevelopment", extension);

        GroovyHelper.instance().addNewInstanceMethod(dependencies, "gradleApi", new MethodClosure(extension, "gradleApi"));
        GroovyHelper.instance().addNewInstanceMethod(dependencies, "gradleTestKit", new MethodClosure(extension, "gradleTestKit"));
        GroovyHelper.instance().addNewInstanceMethod(dependencies, "gradleFixtures", new MethodClosure(extension, "gradleFixtures"));
        GroovyHelper.instance().addNewInstanceMethod(dependencies, "gradleRunnerKit", new MethodClosure(extension, "gradleRunnerKit"));
        GroovyHelper.instance().addNewInstanceMethod(dependencies, "gradlePlugin", new MethodClosure(extension, "gradlePlugin"));
    }

    // NOTE: The class MUST NOT BE a Gradle type because of the Groovy method injection
    private static final class DefaultGradlePluginDevelopmentDependencyExtension implements GradlePluginDevelopmentDependencyExtension, HasPublicType {
        private final DependencyFactory factory;
        private final Transformer<Dependency, String> gradleApiTransformer;
        private final Transformer<Dependency, String> gradleTestKitTransformer;

        @Inject
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
        public ExternalModuleDependency gradlePlugin(String pluginNotation) {
            return factory.gradlePlugin(pluginNotation);
        }

        @Override
        public TypeOf<?> getPublicType() {
            return TypeOf.typeOf(GradlePluginDevelopmentDependencyExtension.class);
        }
    }
}
