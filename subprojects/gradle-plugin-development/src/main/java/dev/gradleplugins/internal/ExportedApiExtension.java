package dev.gradleplugins.internal;

import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.ConfigurablePublishArtifact;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationPublications;
import org.gradle.api.artifacts.ConfigurationVariant;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.file.Directory;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Nested;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskCollection;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;

import javax.inject.Inject;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

import static dev.gradleplugins.internal.util.FilterTransformer.filter;
import static dev.gradleplugins.internal.util.NegateSpec.negate;

public abstract class ExportedApiExtension {
    private final ObjectFactory objects;
    private final NamedDomainObjectSet<ExportedApi> apis;

    @Inject
    public ExportedApiExtension(ObjectFactory objects) {
        this.objects = objects;
        this.apis = objects.namedDomainObjectSet(ExportedApi.class);
    }

    public NamedDomainObjectProvider<ExportedApi> forSourceSet(SourceSet sourceSet) {
        if (apis.findByName(sourceSet.getName()) == null) {
            apis.add(objects.newInstance(ExportedApi.class, sourceSet));
        }
        return apis.named(sourceSet.getName());
    }

    public static abstract class ExportedApi implements Named {
        private final SourceSet sourceSet;

        @Inject
        public ExportedApi(SourceSet sourceSet) {
            this.sourceSet = sourceSet;
        }

        @Override
        public String getName() {
            return sourceSet.getName();
        }

        public SourceSet getSourceSet() {
            return sourceSet;
        }

        @Nested
        public abstract ConfigurableJarTaskProvider getJarTask();

        public abstract Property<SourceSet> getApiSourceSet();

        public static abstract /*final*/ class ConfigurableJarTaskProvider implements Named {
            private final TaskCollection<Task> tasks;
            private final Property<String> nameProvider;
            private final Property<Jar> taskProvider;

            @Inject
            public ConfigurableJarTaskProvider(TaskContainer tasks, ObjectFactory objects) {
                this.tasks = tasks;
                this.nameProvider = objects.property(String.class);
                this.nameProvider.finalizeValueOnRead();
                this.taskProvider = objects.property(Jar.class);
                this.taskProvider.finalizeValueOnRead();
            }

            public String getName() {
                return nameProvider.get();
            }

            public void set(TaskProvider<? extends Jar> taskProvider) {
                this.nameProvider.set(taskProvider.getName());
                this.taskProvider.set(taskProvider);
            }

            public void set(Provider<? extends Jar> taskProvider) {
                this.nameProvider.set(taskProvider.map(Jar::getName));
                this.taskProvider.set(taskProvider);
            }

            public ConfigurableJarTaskProvider convention(Provider<? extends Jar> taskProvider) {
                this.nameProvider.convention(taskProvider.map(Jar::getName));
                this.taskProvider.convention(taskProvider);
                return this;
            }

            public TaskProvider<Jar> getAsProvider() {
                return tasks.named(getName(), Jar.class);
            }

            public Provider<Jar> toProvider() {
                return taskProvider;
            }
        }
    }

    /*private*/ static abstract /*final*/ class Rule implements Plugin<Project> {
        @Inject
        public Rule() {}

        @Override
        public void apply(Project project) {
            final ExportedApiExtension extension = project.getExtensions().create("$exportedApis", ExportedApiExtension.class);

            extension.apis.configureEach(it -> {
                it.getApiSourceSet().finalizeValueOnRead();
                it.getApiSourceSet().convention(it.getSourceSet());

                it.getJarTask().convention(it.getApiSourceSet().flatMap(sourceSet -> {
                    // Create JAR task with sensible default when it doesn't exist
                    if (!project.getTasks().getNames().contains(sourceSet.getJarTaskName())) {
                        project.getTasks().register(sourceSet.getJarTaskName(), Jar.class, task -> {
                            task.setGroup("Build");
                            task.setDescription(String.format("Assemble an API jar archive for the %s classes.", sourceSet.getName()));
                            task.from(sourceSet.getOutput());
                            task.getArchiveClassifier().set(sourceSet.getName());
                        });
                    }
                    return project.getTasks().named(sourceSet.getJarTaskName(), Jar.class);
                }));
            });

            wirePluginApiSourceSetIntoPluginSourceSetIfDifferent(extension).execute(project);
            wirePluginApiIntoExportedElements(extension).execute(project);
        }

        private static Action<Project> wirePluginApiSourceSetIntoPluginSourceSetIfDifferent(ExportedApiExtension extension) {
            return new Action<Project>() {
                @Override
                public void execute(Project project) {
                    extension.apis.configureEach(new Action<ExportedApi>() {
                        @Override
                        public void execute(ExportedApi exportedApi) {
                            final SourceSet implSourceSet = exportedApi.getSourceSet();

                            implSourceSet.setCompileClasspath(implSourceSet.getCompileClasspath().plus(project.getObjects().fileCollection().from((Callable<?>) () -> {
                                final SourceSet apiSourceSet = exportedApi.getApiSourceSet().get();
                                if (!implSourceSet.equals(apiSourceSet)) {
                                    return apiSourceSet.getRuntimeClasspath();
                                } else {
                                    return Collections.emptyList();
                                }
                            })));
                            implSourceSet.setRuntimeClasspath(implSourceSet.getRuntimeClasspath().plus(project.getObjects().fileCollection().from((Callable<?>) () -> {
                                final SourceSet apiSourceSet = exportedApi.getApiSourceSet().get();
                                if (!implSourceSet.equals(apiSourceSet)) {
                                    return apiSourceSet.getRuntimeClasspath();
                                } else {
                                    return Collections.emptyList();
                                }
                            })));

                            Provider<SourceSet> apiSourceSet = exportedApi.getApiSourceSet().map(filter(negate(implSourceSet::equals)));
                            project.getConfigurations().named(implSourceSet.getApiConfigurationName())
                                    .configure(extendsFrom(named(apiSourceSet.map(SourceSet::getApiConfigurationName))));
                            project.getConfigurations().named(implSourceSet.getImplementationConfigurationName())
                                    .configure(extendsFrom(named(apiSourceSet.map(SourceSet::getImplementationConfigurationName))));
                            project.getConfigurations().named(implSourceSet.getCompileOnlyConfigurationName())
                                    .configure(extendsFrom(named(apiSourceSet.map(SourceSet::getCompileOnlyConfigurationName))));
                            project.getConfigurations().named(implSourceSet.getRuntimeOnlyConfigurationName())
                                    .configure(extendsFrom(named(apiSourceSet.map(SourceSet::getRuntimeOnlyConfigurationName))));
                            if (project.getConfigurations().getNames().contains(compileOnlyApiConfigurationName(implSourceSet))) {
                                project.getConfigurations().named(compileOnlyApiConfigurationName(implSourceSet))
                                        .configure(extendsFrom(named(apiSourceSet.map(this::compileOnlyApiConfigurationName))));
                            } else {
                                // Try to be lenient catch future existence of compileOnlyApi
                                project.getConfigurations().configureEach(configuration -> {
                                    if (compileOnlyApiConfigurationName(implSourceSet).equals(configuration.getName())) {
                                        extendsFrom(named(apiSourceSet.map(this::compileOnlyApiConfigurationName))).execute(configuration);
                                    }
                                });
                            }
                        }

                        private /*static*/ Provider<Configuration> named(Provider<String> configurationNameProvider) {
                            // Caution: This mapping will return null if the configuration name is wrong.
                            //   It's not exactly what we want in all cases, but it's good enough for what we do.
                            return configurationNameProvider.map(project.getConfigurations()::findByName);
                        }

                        private /*static*/ String compileOnlyApiConfigurationName(SourceSet sourceSet) {
                            if (sourceSet.getName().equals("main")) {
                                return "compileOnlyApi";
                            }
                            return sourceSet.getName() + "CompileOnlyApi";
                        }

                        // Poor man implementation of deferred Configuration#extendsFrom
                        private /*static*/ Action<Configuration> extendsFrom(Provider<Configuration> configuration) {
                            return deferDependenciesConfiguration(new BiConsumer<Configuration, DependencySet>() {
                                @Override
                                public void accept(Configuration it, DependencySet dependencies) {
                                    dependencies.addAllLater(asCollectionProvider(configuration.map(t -> t.getIncoming().getDependencies())));
                                }

                                @SuppressWarnings("unchecked")
                                private /*static*/ <T> Provider<? extends Iterable<T>> asCollectionProvider(Provider<Iterable<? extends T>> provider) {
                                    provider = provider.orElse(Collections.emptyList());
                                    return (Provider<? extends Iterable<T>>) project.getObjects().listProperty(Object.class).value(provider);
                                }
                            });
                        }

                        private /*static*/ Action<Configuration> deferDependenciesConfiguration(BiConsumer<? super Configuration, ? super DependencySet> action) {
                            return it -> it.withDependencies(dependencies -> action.accept(it, dependencies));
                        }
                    });
                }
            };
        }

        private static Action<Project> wirePluginApiIntoExportedElements(ExportedApiExtension extension) {
            return new Action<Project>() {
                @Override
                public void execute(Project project) {
                    extension.apis.configureEach(exportedApi -> {
                        final SourceSet implSourceSet = exportedApi.getSourceSet();
                        final Provider<Jar> jarTaskProvider = exportedApi.getJarTask().toProvider();
                        final Provider<Directory> classesDirectory = jarTaskProvider.flatMap(jarTask -> {
                                if (implSourceSet.getJarTaskName().equals(jarTask.getName())) {
                                    return implSourceSet.getJava().getClassesDirectory();
                                } else {
                                    final String taskName = implSourceSet.getTaskName("sync", "apiClasses");
                                    TaskProvider<Sync> classes = null;
                                    if (project.getTasks().getNames().contains(taskName)) {
                                        classes = project.getTasks().named(taskName, Sync.class);
                                    } else {
                                        classes = project.getTasks().register(taskName, Sync.class, task -> {
                                            task.setDescription(String.format("Assemble the API classes for %s.", implSourceSet));
                                            task.setDestinationDir(project.file(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName())));
                                            task.from(jarTask.getInputs().getSourceFiles().getElements().map(__ -> jarTask.getInputs().getSourceFiles()).map(it -> it.getAsFileTree().matching(p -> p.include("**/*.class"))));
                                        });
                                    }
                                    return project.getLayout().dir(classes.map(task -> task.getDestinationDir()));
                                }
                            });

                        final Provider<Directory> resourcesDirectory = exportedApi.getJarTask().toProvider().flatMap(jarTask -> {
                                if (implSourceSet.getJarTaskName().equals(jarTask.getName())) {
                                    return implSourceSet.getResources().getClassesDirectory()
                                            .orElse(project.getObjects().directoryProperty().fileValue(implSourceSet.getOutput().getResourcesDir()));
                                } else {
                                    final String taskName = implSourceSet.getTaskName("sync", "apiResources");
                                    TaskProvider<Sync> resources = null;
                                    if (project.getTasks().getNames().contains(taskName)) {
                                        resources = project.getTasks().named(taskName, Sync.class);
                                    } else {
                                        resources = project.getTasks().register(implSourceSet.getTaskName("sync", "apiResources"), Sync.class, task -> {
                                            task.setDescription(String.format("Assemble the API resources for %s.", implSourceSet));
                                            task.setDestinationDir(project.file(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName())));
                                            task.from(jarTask.getInputs().getSourceFiles().getElements().map(__ -> jarTask.getInputs().getSourceFiles()).map(it -> it.getAsFileTree().matching(p -> p.exclude("**/*.class"))));
                                        });
                                    }
                                    return project.getLayout().dir(resources.map(task -> task.getDestinationDir()));
                                }
                            });

                        project.getConfigurations().named(implSourceSet.getApiElementsConfigurationName(), apiElements -> {
                            apiElements.outgoing(clearArtifacts());
                            apiElements.outgoing(artifacts(jarTaskProvider, it -> {
                                it.setName(implSourceSet.getName() + "-api-jar");
                                it.setType(ArtifactTypeDefinition.JAR_TYPE);
                            }));
                            apiElements.getOutgoing().getVariants().named("classes", clearArtifacts());
                            apiElements.getOutgoing().getVariants().named("classes", artifacts(classesDirectory, it -> {
                                it.setName(implSourceSet.getName() + "-api-classes");
                                it.setType(ArtifactTypeDefinition.JVM_CLASS_DIRECTORY);
                            }));
                            // Note: there is no such thing as 'resources' for Java API
                        });

                        project.getConfigurations().named(implSourceSet.getRuntimeElementsConfigurationName(), runtimeElements -> {
                            runtimeElements.outgoing(artifacts(jarTaskProvider, it -> {
                                it.setName(implSourceSet.getName() + "-api-jar");
                                it.setType(ArtifactTypeDefinition.JAR_TYPE);
                            }));
                            runtimeElements.getOutgoing().getVariants().named("classes", artifacts(classesDirectory, it -> {
                                it.setName(implSourceSet.getName() + "-api-classes");
                                it.setType(ArtifactTypeDefinition.JVM_CLASS_DIRECTORY);
                            }));
                            runtimeElements.getOutgoing().getVariants().named("resources", artifacts(resourcesDirectory, it -> {
                                it.setName(implSourceSet.getName() + "-api-resources");
                                it.setType(ArtifactTypeDefinition.JVM_RESOURCES_DIRECTORY);
                            }));
                        });
                    });
                }

                private /*static*/ <T> Action<T> clearArtifacts() {
                    return t -> {
                        if (t instanceof ConfigurationPublications) {
                            ((ConfigurationPublications) t).getArtifacts().clear();
                        } else if (t instanceof ConfigurationVariant) {
                            ((ConfigurationVariant) t).getArtifacts().clear();
                        } else {
                            throw new UnsupportedOperationException("Cannot get artifacts on '" + t + "'");
                        }
                    };
                }

                private /*static*/ <T> Action<T> artifacts(Object notation, Action<? super ConfigurablePublishArtifact> action) {
                    return t -> {
                        if (t instanceof ConfigurationPublications) {
                            ((ConfigurationPublications) t).artifact(notation, action);
                        } else if (t instanceof ConfigurationVariant) {
                            ((ConfigurationVariant) t).artifact(notation, action);
                        } else {
                            throw new UnsupportedOperationException("Cannot get artifacts on '" + t + "'");
                        }
                    };
                }
            };
        }
    }
}
