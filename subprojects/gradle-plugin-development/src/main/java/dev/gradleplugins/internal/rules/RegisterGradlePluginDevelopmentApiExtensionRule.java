package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentApiExtension;
import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationPublications;
import org.gradle.api.artifacts.ConfigurationVariant;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.PublishArtifactSet;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.util.GradleVersion;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class RegisterGradlePluginDevelopmentApiExtensionRule implements Action<Project> {
    private static final String EXTENSION_NAME = "api";

    @Override
    public void execute(Project project) {
        final GradlePluginDevelopmentApiExtensionInternal apiExtension = project.getObjects().newInstance(GradlePluginDevelopmentApiExtensionInternal.class);

        gradlePlugin(project, developmentExtension -> {
            ((ExtensionAware) developmentExtension).getExtensions().add(EXTENSION_NAME, apiExtension);
        });

        // Add Gradle API to API source set
        sourceSets(project, sourceSets -> {
            sourceSets.configureEach(sourceSet -> {
                project.getConfigurations().configureEach(configuration -> {
                    // TODO: Should we use compileOnlyApi? In theory... yes
                    if (compileOnlyConfigurationOf(sourceSet).test(configuration)) { // should be compileOnlyApi only for libraries
                        configuration.getDependencies().addAllLater(newCollectionProvider(project, Dependency.class,
                                whenPluginApiSourceSet(project, sourceSet, gradleApiDependency(project))));
                    }
                });
            });
        });

        apiExtension.getSourceSet().finalizeValueOnRead();
        apiExtension.getSourceSet().convention(project.provider(() -> {
            final GradlePluginDevelopmentExtension developmentExtension = (GradlePluginDevelopmentExtension) project.getExtensions().findByName("gradlePlugin");
            if (developmentExtension != null) {
                return developmentExtension.getPluginSourceSet();
            }
            return null;
        }));

        apiExtension.getJarTaskName().finalizeValueOnRead();
        apiExtension.getJarTaskName().convention(apiExtension.getSourceSet().map(sourceSet -> {
            // Create JAR task with sensible default when it doesn't exist
            if (!project.getTasks().getNames().contains(sourceSet.getJarTaskName())) {
                project.getTasks().register(sourceSet.getJarTaskName(), Jar.class, task -> {
                    task.from(sourceSet.getOutput());
                    task.getArchiveClassifier().set(sourceSet.getName());
                });
            }
            return sourceSet.getJarTaskName();
        }));

        project.afterEvaluate(__ -> {
            wirePluginApiSourceSetIntoPluginSourceSetIfDifferent(project);
            wirePluginApiIntoExportedElements(project);
        });
    }

    public static /*final*/ abstract class GradlePluginDevelopmentApiExtensionInternal implements GradlePluginDevelopmentApiExtension, HasPublicType {
        private final TaskContainer tasks;

        @Inject
        public GradlePluginDevelopmentApiExtensionInternal(TaskContainer tasks) {
            this.tasks = tasks;
        }

        @Override
        public TaskProvider<Jar> getJarTask() {
            return tasks.named(getJarTaskName().get(), Jar.class);
        }

        @Override
        public void setJarTask(TaskProvider<Jar> jarTaskProvider) {
            getJarTaskName().set(jarTaskProvider.getName());
        }

        public abstract Property<String> getJarTaskName();

        @Override
        public TypeOf<?> getPublicType() {
            return TypeOf.typeOf(GradlePluginDevelopmentApiExtension.class);
        }
    }

    private static void gradlePlugin(Project project, Action<? super GradlePluginDevelopmentExtension> action) {
        action.execute((GradlePluginDevelopmentExtension) project.getExtensions().getByName("gradlePlugin"));
    }

    private static void wirePluginApiSourceSetIntoPluginSourceSetIfDifferent(Project project) {
        final SourceSet pluginSourceSet = Objects.requireNonNull(pluginSourceSet(project), sourceSetMustNotBeNull("plugin"));
        final SourceSet pluginApiSourceSet = Objects.requireNonNull(pluginApiSourceSet(project), sourceSetMustNotBeNull("plugin API"));

        if (!pluginSourceSet.equals(pluginApiSourceSet)) {
            pluginSourceSet.setCompileClasspath(pluginSourceSet.getCompileClasspath().plus(pluginApiSourceSet.getRuntimeClasspath()));
            pluginSourceSet.setRuntimeClasspath(pluginSourceSet.getRuntimeClasspath().plus(pluginApiSourceSet.getRuntimeClasspath()));

            project.getConfigurations().named(pluginSourceSet.getApiConfigurationName(), extendsFrom(project.getConfigurations().findByName(pluginApiSourceSet.getApiConfigurationName())));
            project.getConfigurations().named(pluginSourceSet.getImplementationConfigurationName(), extendsFrom(project.getConfigurations().getByName(pluginApiSourceSet.getImplementationConfigurationName())));
            project.getConfigurations().named(pluginSourceSet.getCompileOnlyConfigurationName(), extendsFrom(project.getConfigurations().getByName(pluginApiSourceSet.getCompileOnlyConfigurationName())));
            project.getConfigurations().named(pluginSourceSet.getRuntimeOnlyConfigurationName(), extendsFrom(project.getConfigurations().getByName(pluginApiSourceSet.getRuntimeOnlyConfigurationName())));
            if (project.getConfigurations().getNames().contains(compileOnlyApiConfigurationName(pluginSourceSet))) {
                project.getConfigurations().named(compileOnlyApiConfigurationName(pluginSourceSet), extendsFrom(project.getConfigurations().findByName(compileOnlyApiConfigurationName(pluginApiSourceSet))));
            }
        }
    }

    private static String compileOnlyApiConfigurationName(SourceSet sourceSet) {
        if (sourceSet.getName().equals("main")) {
            return "compileOnlyApi";
        }
        return sourceSet.getName() + "CompileOnlyApi";
    }

    private static Action<Configuration> extendsFrom(@Nullable Configuration configuration) {
        return it -> {
            if (configuration != null) {
                it.extendsFrom(configuration);
            }
        };
    }

    @Nullable
    private static SourceSet pluginApiSourceSet(Project project) {
        final GradlePluginDevelopmentExtension extension = (GradlePluginDevelopmentExtension) project.getExtensions().findByName("gradlePlugin");
        if (extension == null) {
            return null; // not a Gradle plugin development project
        } else {
            return GradlePluginDevelopmentApiExtension.api(extension).getSourceSet().getOrNull(); // the API should enforce non-null values (not the case in the code), so we assume non-null
        }
    }

    @Nullable
    private static SourceSet pluginSourceSet(Project project) {
        final GradlePluginDevelopmentExtension extension = (GradlePluginDevelopmentExtension) project.getExtensions().findByName("gradlePlugin");
        if (extension == null) {
            return null; // not a Gradle plugin development project
        } else {
            return extension.getPluginSourceSet(); // the API should enforce non-null values (not the case in the code), so we assume non-null
        }
    }

    private static String sourceSetMustNotBeNull(String sourceSetIntent) {
        return "The " + sourceSetIntent + " source set must not be null.";
    }

    //region API elements

    private static void wirePluginApiIntoExportedElements(Project project) {
        final SourceSet pluginSourceSet = Objects.requireNonNull(pluginSourceSet(project), sourceSetMustNotBeNull("plugin"));
        final GradlePluginDevelopmentExtension developmentExtension = (GradlePluginDevelopmentExtension) project.getExtensions().getByName("gradlePlugin");

        final TaskProvider<Jar> jar = GradlePluginDevelopmentApiExtension.api(developmentExtension).getJarTask();
        final PublishArtifact jarArtifact = new JarPublishArtifact(jar);


        if (!developmentExtension.getPluginSourceSet().getJarTaskName().equals(jar.getName())) {
            final TaskProvider<Sync> classes = project.getTasks().register("syncApiClasses", Sync.class, task -> {
                task.setDestinationDir(project.file(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName())));
                task.from(jar.flatMap(it -> it.getInputs().getSourceFiles().getElements().map(__ -> it.getInputs().getSourceFiles())).map(it -> it.getAsFileTree().matching(p -> p.include("**/*.class"))));
            });

            final TaskProvider<Sync> resources = project.getTasks().register("syncApiResources", Sync.class, task -> {
                task.setDestinationDir(project.file(project.getLayout().getBuildDirectory().dir("tmp/" + task.getName())));
                task.from(jar.flatMap(it -> it.getInputs().getSourceFiles().getElements().map(__ -> it.getInputs().getSourceFiles())).map(it -> it.getAsFileTree().matching(p -> p.exclude("**/*.class"))));
            });

            project.getConfigurations().named(pluginSourceSet.getApiElementsConfigurationName(), apiElements -> {
                apiElements.outgoing(artifacts(PublishArtifactSet::clear));
                apiElements.outgoing(artifacts(add(jarArtifact)));
                apiElements.getOutgoing().getVariants().named("classes", artifacts(PublishArtifactSet::clear));
                apiElements.getOutgoing().getVariants().named("classes", artifacts(add(new DirectoryPublishArtifact(classes))));
                // Note: there is no such thing as 'resources' for Java API
            });

            project.getConfigurations().named(pluginSourceSet.getRuntimeElementsConfigurationName(), runtimeElements -> {
                runtimeElements.outgoing(artifacts(add(jarArtifact)));
                runtimeElements.getOutgoing().getVariants().named("classes", artifacts(add(new DirectoryPublishArtifact(classes))));
                runtimeElements.getOutgoing().getVariants().named("resources", artifacts(add(new DirectoryPublishArtifact(resources))));
            });
        }
    }

    private static <T> Action<T> artifacts(Consumer<? super PublishArtifactSet> action) {
        return t -> {
            if (t instanceof ConfigurationPublications) {
                action.accept(((ConfigurationPublications) t).getArtifacts());
            } else if (t instanceof ConfigurationVariant) {
                action.accept(((ConfigurationVariant) t).getArtifacts());
            } else {
                throw new UnsupportedOperationException("Cannot get artifacts on '" + t + "'");
            }
        };
    }

    private static Consumer<PublishArtifactSet> add(PublishArtifact artifact) {
        return t -> t.add(artifact);
    }

    private static <OUT, IN> Transformer<Iterable<OUT>, Iterable<IN>> transformEach(Transformer<OUT, IN> transformer) {
        return elements -> new ArrayList<OUT>() {{
            for (IN item : elements) {
                add(transformer.transform(item));
            }
        }};
    }

    private static final class JarPublishArtifact implements PublishArtifact {
        private final TaskProvider<Jar> jarTaskProvider;

        private JarPublishArtifact(TaskProvider<Jar> jarTaskProvider) {
            this.jarTaskProvider = jarTaskProvider;
        }

        @Override
        public String getName() {
            return jarTaskProvider.getName();
        }

        @Override
        public String getExtension() {
            return "jar";
        }

        @Override
        public String getType() {
            return ArtifactTypeDefinition.JAR_TYPE;
        }

        @Nullable
        @Override
        public String getClassifier() {
            return jarTaskProvider.get().getArchiveClassifier().getOrNull();
        }

        @Override
        public File getFile() {
            return jarTaskProvider.get().getArchiveFile().get().getAsFile();
        }

        @Nullable
        @Override
        public Date getDate() {
            return null; // use current date as-per Javadoc
        }

        @Override
        public TaskDependency getBuildDependencies() {
            return new TaskDependency() {
                @Override
                public Set<? extends Task> getDependencies(@Nullable Task task) {
                    return Collections.singleton(jarTaskProvider.get());
                }
            };
        }
    }

    private static final class DirectoryPublishArtifact implements PublishArtifact {
        private final TaskProvider<Sync> file;

        private DirectoryPublishArtifact(TaskProvider<Sync> file) {
            this.file = file;
        }

        @Override
        public String getName() {
            return file.getName();
        }

        @Override
        public String getExtension() {
            return ""; // assume no extension
        }

        @Override
        public String getType() {
            return ArtifactTypeDefinition.DIRECTORY_TYPE;
        }

        @Nullable
        @Override
        public String getClassifier() {
            return null;
        }

        @Override
        public File getFile() {
            return file.get().getDestinationDir();
        }

        @Nullable
        @Override
        public Date getDate() {
            return null; // use current date as-per Javadoc
        }

        @Override
        public TaskDependency getBuildDependencies() {
            return new TaskDependency() {
                @Override
                public Set<? extends Task> getDependencies(@Nullable Task task) {
                    return Collections.singleton(file.get());
                }
            };
        }
    }
    //endregion

    private static <T> Provider<? extends Iterable<T>> newCollectionProvider(Project project, Class<T> elementType, Provider<? extends Iterable<? extends T>> value) {
        return project.getObjects().listProperty(elementType).value(value);
    }

    private static void sourceSets(Project project, Action<? super SourceSetContainer> action) {
        action.execute((SourceSetContainer) project.getExtensions().getByName("sourceSets"));
    }

    private static Provider<? extends Iterable<? extends Dependency>> whenPluginApiSourceSet(Project project, SourceSet sourceSet, Provider<? extends Dependency> delegate) {
        return project.provider(() -> {
            if (sourceSet.equals(pluginApiSourceSet(project))) {
                return new Object(); // mark this case is valid
            } else {
                return null;
            }
        }).flatMap(__ -> delegate.map(Collections::singletonList)).orElse(Collections.emptyList());
    }

    private static Predicate<Configuration> compileOnlyConfigurationOf(SourceSet sourceSet) {
        return configuration -> configuration.getName().equals(sourceSet.getCompileOnlyConfigurationName());
    }

    private static Provider<Dependency> gradleApiDependency(Project project) {
        // TODO: I believe this won't work in a configuration cache context because we use Project instance to create Dependency instance
        return gradleApiVersion(project).map(version -> GradlePluginDevelopmentDependencyExtension.from(project.getDependencies()).gradleApi(version));
    }

    private static Provider<String> gradleApiVersion(Project project) {
        return project.provider(() -> {
            final GradlePluginDevelopmentExtension developmentExtension = (GradlePluginDevelopmentExtension) project.getExtensions().getByName("gradlePlugin");
            final GradlePluginDevelopmentCompatibilityExtension result = (GradlePluginDevelopmentCompatibilityExtension) ((ExtensionAware) developmentExtension).getExtensions().findByName("compatibility");
            return result;
        }).flatMap(GradlePluginDevelopmentCompatibilityExtension::getGradleApiVersion).orElse(GradleVersion.current().getVersion());
    }
}
