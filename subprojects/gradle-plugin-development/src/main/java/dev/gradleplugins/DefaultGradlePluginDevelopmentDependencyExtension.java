package dev.gradleplugins;

import dev.gradleplugins.internal.DependencyFactory;
import dev.gradleplugins.internal.util.LocalOrRemoteVersionTransformer;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;

final class DefaultGradlePluginDevelopmentDependencyExtension implements GradlePluginDevelopmentDependencyExtension, HasPublicType {
    private final DependencyFactory factory;
    private final Transformer<Dependency, String> gradleApiTransformer;
    private final Transformer<Dependency, String> gradleTestKitTransformer;

    DefaultGradlePluginDevelopmentDependencyExtension(DependencyHandler dependencies) {
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
