package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentDependencyBucket;
import dev.gradleplugins.internal.util.PeekTransformer;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

public final class DefaultDependencyBucket implements GradlePluginDevelopmentDependencyBucket {
    private final String dependencyBucketName;
    private final DependencyFactory dependencyFactory;
    private final DependencyAdder delegate;
    private final Provider<Configuration> configurationProvider;

    public DefaultDependencyBucket(Project project, Provider<SourceSet> sourceSetProvider, String dependencyBucketName) {
        this.dependencyBucketName = dependencyBucketName;
        this.dependencyFactory = DependencyFactory.forProject(project);
        this.delegate = new DefaultDependencyAdder(new FinalizableSourceSet(project, sourceSetProvider), new DependencyBucketConfigure(project));
        this.configurationProvider = sourceSetProvider.map(new Transformer<Configuration, SourceSet>() {
            @Override
            public Configuration transform(SourceSet it) {
                return project.getConfigurations().getByName(bucketConfigurationName(it));
            }

            private String bucketConfigurationName(SourceSet sourceSet) {
                final String sourceSetName = sourceSet.getName();
                if (sourceSet.getName().equals("main")) {
                    return dependencyBucketName;
                } else {
                    return sourceSetName + StringUtils.capitalize(dependencyBucketName);
                }
            }
        });
    }

    @Override
    public String getName() {
        return dependencyBucketName;
    }

    public interface Finalizable<T> {
        void onFinalized(Action<? super T> finalizeAction);
    }

    public static Provider<SourceSet> pluginSourceSet(Project project) {
        return project.provider(() -> ((GradlePluginDevelopmentExtension) project.getExtensions().getByName("gradlePlugin")).getPluginSourceSet());
    }

    private static final class FinalizableSourceSet implements DefaultDependencyBucket.Finalizable<SourceSet> {
        private final Project project;
        private final Provider<SourceSet> sourceSetProvider;

        private FinalizableSourceSet(Project project, Provider<SourceSet> sourceSetProvider) {
            this.project = project;
            this.sourceSetProvider = sourceSetProvider;
        }

        @Override
        public void onFinalized(Action<? super SourceSet> finalizeAction) {
            project.afterEvaluate(__ -> {
                finalizeAction.execute(sourceSetProvider.get());
            });
        }
    }

    public static final class PluginSourceSet implements Finalizable<SourceSet> {
        private final Project project;

        public PluginSourceSet(Project project) {
            this.project = project;
        }

        public void onFinalized(Action<? super SourceSet> finalizeAction) {
            project.afterEvaluate(__ -> {
                gradlePlugin(project, extension -> {
                    finalizeAction.execute(extension.getPluginSourceSet());
                });
            });
        }

        private void gradlePlugin(Project project, Action<? super GradlePluginDevelopmentExtension> configureAction) {
            configureAction.execute((GradlePluginDevelopmentExtension) project.getExtensions().getByName("gradlePlugin"));
        }
    }

    public static final class DependencyBucketConfigure {
        private final Project project;

        public DependencyBucketConfigure(Project project) {
            this.project = project;
        }

        public ForBucketAction forBucket(String name, Action<? super Configuration> configureAction) {
            return new ForBucketAction(project, name, configureAction);
        }
    }

    public static final class ForBucketAction implements Action<SourceSet> {
        private final Project project;
        private final String dependencyBucketName;
        private final Action<? super Configuration> configureAction;

        public ForBucketAction(Project project, String dependencyBucketName, Action<? super Configuration> configureAction) {
            this.project = project;
            this.dependencyBucketName = dependencyBucketName;
            this.configureAction = configureAction;
        }

        @Override
        public void execute(SourceSet sourceSet) {
            configureAction.execute(project.getConfigurations().getByName(bucketConfigurationName(sourceSet)));
        }

        private String bucketConfigurationName(SourceSet sourceSet) {
            final String sourceSetName = sourceSet.getName();
            if (sourceSet.getName().equals("main")) {
                return dependencyBucketName;
            } else {
                return sourceSetName + StringUtils.capitalize(dependencyBucketName);
            }
        }
    }

    public interface DependencyAdder {
        void add(String dependencyBucketName, Dependency dependency);
        void add(String dependencyBucketName, Provider<? extends Dependency> dependencyProvider);
    }

    public static final class DefaultDependencyAdder implements DependencyAdder {
        private final Finalizable<SourceSet> sourceSet;
        private final DependencyBucketConfigure configuration;

        public DefaultDependencyAdder(Finalizable<SourceSet> sourceSet, DependencyBucketConfigure configuration) {
            this.sourceSet = sourceSet;
            this.configuration = configuration;
        }

        @Override
        public void add(String dependencyBucketName, Dependency dependency) {
            sourceSet.onFinalized(configuration.forBucket(dependencyBucketName, configuration -> {
                configuration.getDependencies().add(dependency);
            }));
        }

        @Override
        public void add(String dependencyBucketName, Provider<? extends Dependency> dependencyProvider) {
            sourceSet.onFinalized(configuration.forBucket(dependencyBucketName, configuration -> {
                configuration.getDependencies().addLater(dependencyProvider);
            }));
        }
    }

    @Override
    public void add(Dependency dependency) {
        delegate.add(dependencyBucketName, dependency);
    }

    @Override
    public <DependencyType extends Dependency> void add(DependencyType dependency, Action<? super DependencyType> configureAction) {
        configureAction.execute(dependency);
        delegate.add(dependencyBucketName, dependency);
    }

    @Override
    public <DependencyType extends Dependency> void add(Provider<DependencyType> dependencyProvider) {
        delegate.add(dependencyBucketName, dependencyProvider);
    }

    @Override
    public <DependencyType extends Dependency> void add(Provider<DependencyType> dependencyProvider, Action<? super DependencyType> configureAction) {
        delegate.add(dependencyBucketName, dependencyProvider.map(new PeekTransformer<>(configureAction)));
    }

    @Override
    public void add(FileCollection fileCollection) {
        delegate.add(dependencyBucketName, dependencyFactory.create(fileCollection));
    }

    @Override
    public void add(Project project) {
        delegate.add(dependencyBucketName, dependencyFactory.create(project));
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
