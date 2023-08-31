package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentPublication;
import dev.gradleplugins.GradlePluginDevelopmentPublishingExtension;
import dev.gradleplugins.internal.SoftwareComponentFactoryProvider;
import org.gradle.api.Action;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.NamedDomainObjectSet;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.capabilities.Capability;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;
import org.gradle.api.publish.Publication;
import org.gradle.api.publish.PublicationContainer;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.ivy.IvyPublication;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.maven.internal.publication.MavenPublicationInternal;
import org.gradle.api.publish.plugins.PublishingPlugin;
import org.gradle.api.publish.tasks.GenerateModuleMetadata;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.plugin.devel.PluginDeclaration;
import org.jetbrains.annotations.Nullable;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;

import static org.gradle.api.attributes.Usage.JAVA_RUNTIME;
import static org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE;

public final class RegisterGradlePluginDevelopmentPublishingExtensionRule implements Action<Project> {
    private static final String EXTENSION_NAME = "publishing";
    private static final String BECAUSE_DIFFERENT_CONTAINER = "the provider is created by another container which is in no way connected to the destination container thus resolving the element will not trigger all actions as needed";

    @Override
    public void execute(Project project) {
        // Register publishing extension on Gradle Plugin extension
        project.getPlugins().withType(PublishingPlugin.class, __ -> {
            final DefaultGradlePluginDevelopmentPublishingExtension publishingExtension = project.getObjects().newInstance(DefaultGradlePluginDevelopmentPublishingExtension.class, project);

            publishingExtension.getPublications().configureEach(spec -> {
                spec.getVersion().convention(project.provider(() -> project.getVersion().toString()));
                spec.getGroup().convention(project.provider(() -> project.getGroup().toString()));
                spec.getStatus().convention(project.provider(() -> project.getStatus().toString()));
                spec.getExcluded().convention(project.provider(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        // TODO: This logic may not work in composite build
                        if (project.getGradle().getStartParameter().getTaskNames().stream().anyMatch(it -> it.startsWith(publishTaskNamePrefix()) || it.startsWith(publishTaskPathPrefix()))) {
                            return false;
                        } else {
                            return null;
                        }
                    }

                    private String publishTaskNamePrefix() {
                        return "publish" + capitalize(spec.getName());
                    }

                    private String publishTaskPathPrefix() {
                        String result = ":" + publishTaskNamePrefix();
                        if (project.getParent() != null) {
                            result = project.getPath() + result;
                        }
                        return result;
                    }
                }).orElse(true));
            });

            gradlePlugin(project, developmentExtension -> {
                ((ExtensionAware) developmentExtension).getExtensions().add(EXTENSION_NAME, publishingExtension);

                project.afterEvaluate(___ -> {
                    if (!publishingExtension.getPublications().isEmpty() && developmentExtension.isAutomatedPublishing()) {
                        throw new UnsupportedOperationException("Please disable gradlePlugin.automatedPublishing!");
                    }

                    final List<GradlePluginDevelopmentPublication<?>> publicationRequests = publishingExtension.getPublications().stream().filter(it -> !it.getExcluded().get()).collect(Collectors.toList());
                    if (publicationRequests.size() > 1) {
                        throw new UnsupportedOperationException("Gradle impose a single publication per-project limit during an execution. For this execution, the user requested the following publications: " + publicationRequests.stream().map(Object::toString).collect(Collectors.joining(",")) + ". Please select only one publication to publish.");
                    }
                });
            });
        });

        // Register publications rules
        project.getPluginManager().withPlugin("maven-publish", __ -> {
            final GradlePluginDevelopmentPublishingExtension publishingExtension = GradlePluginDevelopmentPublishingExtension.publishing(((GradlePluginDevelopmentExtension) project.getExtensions().getByName("gradlePlugin")));

            publishingExtension.getPublications().configureEach(mavenPublication(spec -> {
                // Register publish to MavenLocal lifecycle task
                project.getTasks().register("publish" + capitalize(spec.getName()) + "ToMavenLocal", task -> {
                    task.setGroup(PublishingPlugin.PUBLISH_TASK_GROUP);
                    task.setDescription("Publishes all Maven publications produced by " + spec + " to the local Maven cache.");
                    task.dependsOn((Callable<Object>) () -> spec.getAllPublications().stream().map(it -> "publish" + capitalize(it.getName()) + "PublicationToMavenLocal").collect(Collectors.toList()));
                });

                // Register publish to <repository> lifecycle task
                ((PublishingExtension) project.getExtensions().getByName("publishing")).getRepositories().configureEach(publishRepository -> {
                    project.getTasks().register("publish" + capitalize(spec.getName()) + "To" + capitalize(publishRepository.getName() + "Repository"), task -> {
                        task.setGroup(PublishingPlugin.PUBLISH_TASK_GROUP);
                        task.setDescription("Publishes all Maven publications produced by " + spec + " to " + publishRepository + ".");
                        task.dependsOn((Callable<Object>) () -> spec.getAllPublications().stream().map(it -> "publish" + capitalize(it.getName()) + "PublicationTo" + capitalize(publishRepository.getName()) + "Repository").collect(Collectors.toList()));
                    });
                });
            }));

            project.afterEvaluate(ignored -> {
                publishingExtension.getPublications().configureEach(mavenPublication(spec -> {
                    if (spec.getExcluded().get()) {
                        return;
                    }

                    final PublicationContainer publications = ((PublishingExtension) project.getExtensions().getByName("publishing")).getPublications();

                    { /* creates plugin publication */
                        final NamedDomainObjectProvider<MavenPublication> pluginPublication = publications.register(spec.getName() + "Plugin" + capitalize(simpleName(spec.getPublicationType())), spec.getPublicationType());
                        pluginPublication.configure(it -> it.setGroupId(spec.getGroup().get()));
                        pluginPublication.configure(it -> it.setVersion(spec.getVersion().get()));
                        pluginPublication.configure(it -> it.setArtifactId(project.getName()));
                        pluginPublication.configure(it -> it.from(project.getComponents().getByName("java")));
                        spec.getAllPublications().add(mustRealize(pluginPublication, BECAUSE_DIFFERENT_CONTAINER));
                        ((GradlePluginDevelopmentPublicationInternal<MavenPublication>) spec).useAsPluginPublication(pluginPublication);
                    }

                    { /* creates plugin marker publications */
                        final GradlePluginDevelopmentExtension developmentExtension = (GradlePluginDevelopmentExtension) project.getExtensions().getByName("gradlePlugin");
                        developmentExtension.getPlugins().configureEach(pluginDeclaration -> {
                            Configuration apiElements = project.getConfigurations().create(spec.getName() + capitalize(pluginDeclaration.getName()) + "ApiElements");
                            apiElements.getDependencies().add(project.getDependencies().create(project));
                            apiElements.setCanBeConsumed(true);
                            apiElements.setCanBeResolved(false);
                            apiElements.attributes(it -> {
                                it.attribute(USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_API));
                            });
                            apiElements.outgoing(it -> it.capability(PluginMarkerCapability.of(project, spec, pluginDeclaration)));

                            Configuration runtimeElements = project.getConfigurations().create(spec.getName() + capitalize(pluginDeclaration.getName()) + "RuntimeElements");
                            runtimeElements.getDependencies().add(project.getDependencies().create(project));
                            runtimeElements.setCanBeConsumed(true);
                            runtimeElements.setCanBeResolved(false);
                            runtimeElements.attributes(it -> {
                                it.attribute(USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, JAVA_RUNTIME));
                            });
                            runtimeElements.outgoing(it -> it.capability(PluginMarkerCapability.of(project, spec, pluginDeclaration)));

                            // Note that it's normal for the api/runtime elements to fold onto each other when they both point to the same dependency (aka no API dependencies).
                            final AdhocComponentWithVariants component = project.getObjects().newInstance(SoftwareComponentFactoryProvider.class).get().adhoc(pluginDeclaration.getName());
                            component.addVariantsFromConfiguration(apiElements, it -> it.mapToMavenScope("compile"));
                            component.addVariantsFromConfiguration(runtimeElements, it -> it.mapToMavenScope("runtime"));

                            final NamedDomainObjectProvider<MavenPublication> publication = publications.register(spec.getName() + capitalize(pluginDeclaration.getName()) + "PluginMarker" + capitalize(simpleName(spec.getPublicationType())), spec.getPublicationType());
                            publication.configure(it -> it.from(component));
                            publication.configure(it -> it.pom(mavenPom -> {
                                mavenPom.getDescription().set(pluginDeclaration.getDescription());
                                mavenPom.getName().set(pluginDeclaration.getDisplayName());
                            }));
                            publication.configure(it -> {
                                it.suppressPomMetadataWarningsFor(apiElements.getName());
                                it.suppressPomMetadataWarningsFor(runtimeElements.getName());
                            });
                            publication.configure(it -> {
                                ((MavenPublicationInternal) it).setAlias(true); // ignore the publications
                            });
                            publication.configure(it -> it.setGroupId(pluginDeclaration.getId()));
                            publication.configure(it -> it.setVersion(spec.getVersion().get()));
                            publication.configure(it -> it.setArtifactId(pluginDeclaration.getId() + ".gradle.plugin"));

                            // Ignore Gradle metadata because Gradle resolve plugins without Usage attributes
                            project.getTasks().named("generateMetadataFileFor" + capitalize(publication.getName()) + "Publication", GenerateModuleMetadata.class, task -> {
                                task.setEnabled(false);
                            });

                            spec.getAllPublications().add(mustRealize(publication, BECAUSE_DIFFERENT_CONTAINER));
                            spec.getPluginMarkerPublications().add(mustRealize(publication, BECAUSE_DIFFERENT_CONTAINER));
                        });
                    }
                }));
            });
        });

        // TODO: Add ivy publishing
    }

    @SuppressWarnings("unchecked")
    private static Action<GradlePluginDevelopmentPublication<?>> mavenPublication(Action<? super GradlePluginDevelopmentPublication<MavenPublication>> configureAction) {
        return it -> {
            if (it.getPublicationType().equals(MavenPublication.class)) {
                configureAction.execute((GradlePluginDevelopmentPublication<MavenPublication>) it);
            }
        };
    }

    private static void gradlePlugin(Project project, Action<? super GradlePluginDevelopmentExtension> action) {
        action.execute((GradlePluginDevelopmentExtension) project.getExtensions().getByName("gradlePlugin"));
    }

    public static /*final*/ class DefaultGradlePluginDevelopmentPublishingExtension implements GradlePluginDevelopmentPublishingExtension, HasPublicType {
        private final Project project;
        private final NamedDomainObjectSet<GradlePluginDevelopmentPublication<? extends Publication>> publications;

        @Inject
        public DefaultGradlePluginDevelopmentPublishingExtension(Project project) {
            this.project = project;
            this.publications = project.getObjects().namedDomainObjectSet(new TypeOf<GradlePluginDevelopmentPublication<? extends Publication>>() {}.getConcreteClass());
        }

        @Override
        public <T extends Publication> void registerPublication(String name, Class<T> type) {
            createPublication(name, type);
        }

        @Override
        public <T extends Publication> void registerPublication(String name, Class<T> type, Action<? super GradlePluginDevelopmentPublication<T>> configureAction) {
            configureAction.execute(createPublication(name, type));
        }

        private <T extends Publication> GradlePluginDevelopmentPublication<T> createPublication(String name, Class<T> type) {
            if (project.getExtensions().getByType(GradlePluginDevelopmentExtension.class).isAutomatedPublishing()) {
                throw new UnsupportedOperationException("Please disable gradlePlugin.automatedPublishing!");
            }

            @SuppressWarnings("unchecked")
            final GradlePluginDevelopmentPublication<T> newPublication = (GradlePluginDevelopmentPublication<T>) project.getObjects().newInstance(DefaultGradlePluginDevelopmentPublication.class, name, type);
            publications.add(newPublication);
            return newPublication;
        }

        @Override
        public NamedDomainObjectSet<GradlePluginDevelopmentPublication<?>> getPublications() {
            return publications;
        }

        @Override
        public void publications(Action<? super NamedDomainObjectSet<GradlePluginDevelopmentPublication<?>>> configureAction) {
            configureAction.execute(publications);
        }

        @Override
        public TypeOf<?> getPublicType() {
            return TypeOf.typeOf(GradlePluginDevelopmentPublishingExtension.class);
        }
    }

    public interface GradlePluginDevelopmentPublicationInternal<T extends Publication> extends GradlePluginDevelopmentPublication<T> {
        void useAsPluginPublication(NamedDomainObjectProvider<T> publicationProvider);
    }

    public static /*final*/ abstract class DefaultGradlePluginDevelopmentPublication implements GradlePluginDevelopmentPublicationInternal<Publication>, HasPublicType {
        private final String name;
        private final Class<Publication> publicationType;
        private final DomainObjectSet<Publication> allPublications;
        private final DomainObjectSet<Publication> pluginMarkerPublications;
        private NamedDomainObjectProvider<Publication> pluginPublication;
        private List<Action<? super Publication>> pluginPublicationActions = new ArrayList<>();

        @Inject
        @SuppressWarnings("unchecked")
        public DefaultGradlePluginDevelopmentPublication(String name, Class<? extends Publication> publicationType, ObjectFactory objects) {
            this.name = name;
            this.publicationType = (Class<Publication>) publicationType;
            this.allPublications = (DomainObjectSet<Publication>) objects.domainObjectSet(publicationType);
            this.pluginMarkerPublications = (DomainObjectSet<Publication>) objects.domainObjectSet(publicationType);
        }

        @Override
        public DomainObjectSet<Publication> getAllPublications() {
            return allPublications;
        }

        @Override
        public DomainObjectSet<Publication> getPluginMarkerPublications() {
            return pluginMarkerPublications;
        }

        @Override
        public Provider<Publication> getPluginPublication() {
            return pluginPublication;
        }

        @Override
        public void pluginPublication(Action<? super Publication> configureAction) {
            if (pluginPublication == null) {
                pluginPublicationActions.add(configureAction);
            } else {
                pluginPublication.configure(configureAction);
            }
        }

        public void useAsPluginPublication(NamedDomainObjectProvider<Publication> publicationProvider) {
            this.pluginPublication = publicationProvider;
            pluginPublicationActions.forEach(publicationProvider::configure);
            pluginPublicationActions = null;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Class<Publication> getPublicationType() {
            return publicationType;
        }

        @Override
        public TypeOf<?> getPublicType() {
            return new TypeOf<GradlePluginDevelopmentPublication<? extends Publication>>() {};
        }

        @Override
        public String toString() {
            return "publication '" + name + "'";
        }
    }

    private static String capitalize(String s) {
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private static String simpleName(Class<? extends Publication> publicationType) {
        if (publicationType.equals(MavenPublication.class)) {
            return "maven";
        } else if (publicationType.equals(IvyPublication.class)) {
            return "ivy";
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private static <T> T mustRealize(Provider<T> providerToRealize, String because) {
        return providerToRealize.get();
    }

    public static final class PluginMarkerCapability implements Capability {
        private final Provider<String> groupProvider;
        private final String name;
        private final Provider<String> versionProvider;

        public PluginMarkerCapability(Provider<String> groupProvider, String name, Provider<String> versionProvider) {
            this.groupProvider = groupProvider;
            this.name = name;
            this.versionProvider = versionProvider;
        }

        public static Capability of(Project project, GradlePluginDevelopmentPublication<?> spec, PluginDeclaration pluginDeclaration) {
            return new PluginMarkerCapability(spec.getGroup(), capabilityName(project, pluginDeclaration), spec.getVersion());
        }

        @Override
        public String getGroup() {
            return groupProvider.get();
        }

        @Override
        public String getName() {
            return name;
        }

        @Nullable
        @Override
        public String getVersion() {
            return versionProvider.get();
        }

        public static String capabilityName(Project project, PluginDeclaration pluginDeclaration) {
            return project.getName() + "-" + pluginDeclaration.getName();
        }
    }
}
