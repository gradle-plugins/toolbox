package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentDependencyBucket;
import dev.gradleplugins.internal.util.FilterTransformer;
import dev.gradleplugins.internal.util.PeekTransformer;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;

import java.util.Collections;
import java.util.function.BiConsumer;

public final class DependencyBucketFactory {
    private final Project project;
    private final Provider<SourceSet> sourceSetProvider;

    public DependencyBucketFactory(Project project, Provider<SourceSet> sourceSetProvider) {
        this.project = project;
        this.sourceSetProvider = sourceSetProvider;
    }

    public GradlePluginDevelopmentDependencyBucket create(String bucketName) {
        return new DefaultDependencyBucket(bucketName);
    }

    public final class DefaultDependencyBucket implements GradlePluginDevelopmentDependencyBucket {
        private final String dependencyBucketName;
        private final DependencyFactory dependencyFactory;
        private final Provider<Configuration> configurationProvider;
        private final Provider<String> configurationNameProvider;

        public DefaultDependencyBucket(String dependencyBucketName) {
            this.dependencyBucketName = dependencyBucketName;
            this.dependencyFactory = DependencyFactory.forProject(project);
            this.configurationNameProvider = sourceSetProvider.map(sourceSet -> {
                final String sourceSetName = sourceSet.getName();
                if (sourceSet.getName().equals("main")) {
                    return dependencyBucketName;
                } else {
                    return sourceSetName + StringUtils.capitalize(dependencyBucketName);
                }
            });
            this.configurationProvider = configurationNameProvider.map(project.getConfigurations()::getByName);
        }

        @Override
        public String getName() {
            return dependencyBucketName;
        }

        @SuppressWarnings("unchecked")
        private <T> Provider<? extends Iterable<T>> asCollectionProvider(Provider<? extends Iterable<? extends T>> provider) {
            return (Provider<? extends Iterable<T>>) project.getObjects().listProperty(Object.class).value(provider);
        }

        private /*static*/ <T> Provider<? extends Iterable<T>> asList(Provider<T> provider) {
            return provider.map(Collections::singletonList).orElse(Collections.emptyList());
        }

        private Provider<String> ifThisBucket(Configuration configuration) {
            return configurationNameProvider.map(new FilterTransformer<>(it -> it.equals(configuration.getName())));
        }

        @Override
        public void add(Dependency dependency) {
            project.getConfigurations().configureEach(usingWorkaroundForBadlyCodedKotlinPlugin((it, dependencies) -> {
                dependencies.addAllLater(asCollectionProvider(asList(ifThisBucket(it).map(__ -> dependency))));
            }));
        }

        @Override
        public <DependencyType extends Dependency> void add(DependencyType dependency, Action<? super DependencyType> configureAction) {
            configureAction.execute(dependency);
            add(dependency);
        }

        @Override
        public <DependencyType extends Dependency> void add(Provider<DependencyType> dependencyProvider) {
            project.getConfigurations().configureEach(usingWorkaroundForBadlyCodedKotlinPlugin((it, dependencies) -> {
                dependencies.addAllLater(asCollectionProvider(asList(ifThisBucket(it).flatMap(__ -> dependencyProvider))));
            }));
        }

        private /*static*/ Action<Configuration> usingWorkaroundForBadlyCodedKotlinPlugin(BiConsumer<? super Configuration, ? super DependencySet> action) {
            return it -> {
                // Kotlin plugin is poorly coded causing lots of very strange errors.
                //   The plugin eagerly resolve the Configuration#dependencies container.
                it.defaultDependencies(dependencies -> action.accept(it, dependencies));
            };
        }

        @Override
        public <DependencyType extends Dependency> void add(Provider<DependencyType> dependencyProvider, Action<? super DependencyType> configureAction) {
            add(dependencyProvider.map(new PeekTransformer<>(configureAction)));
        }

        @Override
        public void add(FileCollection fileCollection) {
            add(dependencyFactory.create(fileCollection));
        }

        @Override
        public void add(Project project) {
            add(dependencyFactory.create(project));
        }

        @Override
        public void add(CharSequence dependencyNotation) {
            add(dependencyFactory.create(dependencyNotation));
        }

        @Override
        public void add(CharSequence dependencyNotation, Action<? super ExternalModuleDependency> configureAction) {
            add(dependencyFactory.create(dependencyNotation), configureAction);
        }

        @Override
        public Provider<Configuration> getAsConfiguration() {
            return configurationProvider;
        }
    }
}
