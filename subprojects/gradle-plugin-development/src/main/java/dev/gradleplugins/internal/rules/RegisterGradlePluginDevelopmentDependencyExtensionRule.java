package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension;
import dev.gradleplugins.internal.DependencyFactory;
import dev.gradleplugins.internal.runtime.dsl.DefaultGradleExtensionMixInService;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

import java.util.Objects;

public final class RegisterGradlePluginDevelopmentDependencyExtensionRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        new DefaultGradleExtensionMixInService(project.getObjects())
                .forExtension(GradlePluginDevelopmentDependencyExtension.class)
                .useInstance(new DefaultGradlePluginDevelopmentDependencyExtension(DependencyFactory.forProject(project)))
                .build()
                .mixInto(project.getDependencies());
    }

    private static final class DefaultGradlePluginDevelopmentDependencyExtension implements GradlePluginDevelopmentDependencyExtension, HasPublicType {
        private final DependencyFactory dependencyFactory;

        DefaultGradlePluginDevelopmentDependencyExtension(DependencyFactory dependencyFactory) {
            this.dependencyFactory = dependencyFactory;
        }

        @Override
        public Dependency gradleApi(String version) {
            Objects.requireNonNull(version, "'version' must not be null");
            if ("local".equals(version)) {
                return dependencyFactory.localGradleApi();
            }
            return dependencyFactory.gradleApi(version);
        }

        @Override
        public Dependency gradleTestKit(String version) {
            Objects.requireNonNull(version, "'version' must not be null");
            if ("local".equals(version)) {
                return dependencyFactory.localGradleTestKit();
            }
            return dependencyFactory.gradleTestKit(version);
        }

        @Override
        @SuppressWarnings("deprecation")
        public Dependency gradleFixtures() {
            return dependencyFactory.gradleFixtures();
        }

        @Override
        public Dependency gradleRunnerKit() {
            return dependencyFactory.gradleRunnerKit();
        }

        @Override
        public TypeOf<?> getPublicType() {
            return TypeOf.typeOf(GradlePluginDevelopmentDependencyExtension.class);
        }
    }
}
