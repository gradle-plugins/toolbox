package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import dev.gradleplugins.internal.FinalizableComponent;
import dev.gradleplugins.internal.GradleCompatibilities;
import dev.gradleplugins.internal.JvmCompatibilities;
import dev.gradleplugins.internal.util.ActionSet;
import dev.gradleplugins.internal.util.Configurable;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Property;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.SourceSet;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;

import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;

/*private*/ abstract /*final*/ class GradlePluginDevelopmentCompatibilityExtensionRule implements Plugin<Project> {
    private static final String EXTENSION_NAME = "compatibility";

    @Inject
    public GradlePluginDevelopmentCompatibilityExtensionRule() {}

    @Override
    public void apply(Project project) {
        project.getPluginManager().withPlugin("java-gradle-plugin", __ -> {
            project.getPluginManager().apply("gradlepluginsdev.rules.gradle-compatibilities");

            final DefaultGradlePluginDevelopmentCompatibilityExtension extension = newCompatibilityExtension(project);

            ((ExtensionAware) gradlePlugin(project)).getExtensions().add(EXTENSION_NAME, extension);

            project.afterEvaluate(finalize(extension));
        });
    }

    private DefaultGradlePluginDevelopmentCompatibilityExtension newCompatibilityExtension(Project project) {
        return project.getObjects().newInstance(DefaultGradlePluginDevelopmentCompatibilityExtension.class, project);
    }

    private static Action<Project> finalize(DefaultGradlePluginDevelopmentCompatibilityExtension extension) {
        return ignored -> extension.finalizeComponent();
    }

    @SuppressWarnings("UnstableApiUsage")
    /*private*/ static abstract /*final*/ class DefaultGradlePluginDevelopmentCompatibilityExtension implements GradlePluginDevelopmentCompatibilityExtension, HasPublicType, FinalizableComponent, Configurable<GradlePluginDevelopmentCompatibilityExtension> {
        private boolean finalized = false;
        private final GradleCompatibilitiesProvider provider;
        private final Runnable finalizeSourceCompatibilityForBackwardCompatibility;
        private final Runnable overrideProjectJvmCompatibilitiesForBackwardCompatibility;
        private final GradlePluginDevelopmentCompatibilityExtensionAdapter.Factory adapterFactory;

        private static final class GradleCompatibilitiesProvider {
            private Project project;
            private ActionSet<GradleCompatibilities> actions = new ActionSet<>();
            private NamedDomainObjectProvider<? extends GradleCompatibilities> provider;

            public GradleCompatibilitiesProvider(Project project) {
                this.project = project;
            }

            public void configure(Action<? super GradleCompatibilities> action) {
                if (provider == null) {
                    actions.add(action);
                } else {
                    provider.configure(action);
                }
            }

            private void finalizeProvider() {
                if (provider == null) {
                    provider = project.getExtensions().getByType(GradleCompatibilities.ForSourceSetExtension.class).forSourceSet(pluginSourceSetOf(project));
                    provider.configure(actions);

                    // Clear some data
                    actions = null;
                    project = null;
                }
            }

            public GradleCompatibilities get() {
                finalizeProvider();
                // WARN: Be careful here, when we resolve, we flush all action to the current `pluginSourceSet`.
                //   This means the `pluginSourceSet` used could misalign with the current `pluginSourceSet`.
                return provider.get();
            }
        }

        private static SourceSet pluginSourceSetOf(Project project) {
            return project.getExtensions().getByType(GradlePluginDevelopmentExtension.class).getPluginSourceSet();
        }

        @Inject
        public DefaultGradlePluginDevelopmentCompatibilityExtension(Project project) {
            this.provider = new GradleCompatibilitiesProvider(project);
            this.finalizeSourceCompatibilityForBackwardCompatibility = () -> {
                project.getExtensions().getByType(JvmCompatibilities.ForSourceSetExtension.class).forSourceSet(pluginSourceSetOf(project)).configure(x -> x.getSourceCompatibility().finalizeValue());
            };
            this.overrideProjectJvmCompatibilitiesForBackwardCompatibility = () -> {
                project.getExtensions().configure(JavaPluginExtension.class, java -> {
                    JvmCompatibilities.ForSourceSetExtension.SourceSetJvmCompatibilities jvmCompatibilities = project.getExtensions().getByType(JvmCompatibilities.ForSourceSetExtension.class).forSourceSet(pluginSourceSetOf(project)).get();
                    java.setTargetCompatibility(jvmCompatibilities.getTargetCompatibility().get());
                    java.setSourceCompatibility(jvmCompatibilities.getSourceCompatibility().get());
                });
            };
            this.adapterFactory = new GradlePluginDevelopmentCompatibilityExtensionAdapter.Factory(project.getObjects());
        }

        @Override
        public TypeOf<?> getPublicType() {
            return TypeOf.typeOf(GradlePluginDevelopmentCompatibilityExtension.class);
        }

        @Override
        public Property<String> getMinimumGradleVersion() {
            return provider.get().getMinimumGradleVersion();
        }

        @Override
        public Property<String> getGradleApiVersion() {
            return provider.get().getGradleApiVersion();
        }

        @Override
        public void finalizeComponent() {
            if (!finalized) {
                provider.finalizeProvider();
                provider.configure(it -> {
                    // For backward compatibility with 1.x series
                    //   Because we force the convention for `minimumGradleVersion` the convention current Gradle,
                    //   we need to finalize the value `sourceCompatibility` so the value resolve in the right order.
                    finalizeSourceCompatibilityForBackwardCompatibility.run();

                    // For backward compatibility with 1.x series
                    //   We used to rely only on the `java` extension for the JVM compatibilities.
                    //   Now, we modeled a middle layer that handle per-source set compatibilities.
                    //   At the moment, we will just back-set the matching source set compatibilities.
                    overrideProjectJvmCompatibilitiesForBackwardCompatibility.run();

                    // For backward compatibility with 1.x series
                    it.getGradleApiVersion().finalizeValue();
                    it.getGradleApiVersion().disallowChanges();

                    // Special case to keep some backward compatibility for the 1.x series
                    it.getMinimumGradleVersion().convention(GradleVersion.current().getVersion());
                    it.getMinimumGradleVersion().disallowChanges();
                });
                finalized = true;
            }
        }

        @Override
        public boolean isFinalized() {
            return finalized;
        }

        public void configure(Action<? super GradlePluginDevelopmentCompatibilityExtension> action) {
            provider.configure(delegate -> action.execute(adapterFactory.create(delegate)));
        }

        /*private*/ static abstract /*final*/ class GradlePluginDevelopmentCompatibilityExtensionAdapter implements GradlePluginDevelopmentCompatibilityExtension {
            private final GradleCompatibilities delegate;

            @Inject
            public GradlePluginDevelopmentCompatibilityExtensionAdapter(GradleCompatibilities delegate) {
                this.delegate = delegate;
            }

            @Override
            public Property<String> getMinimumGradleVersion() {
                return delegate.getMinimumGradleVersion();
            }

            @Override
            public Property<String> getGradleApiVersion() {
                return delegate.getGradleApiVersion();
            }

            static final class Factory {
                private final ObjectFactory objects;

                public Factory(ObjectFactory objects) {
                    this.objects = objects;
                }

                public GradlePluginDevelopmentCompatibilityExtensionAdapter create(GradleCompatibilities delegate) {
                    return objects.newInstance(GradlePluginDevelopmentCompatibilityExtensionAdapter.class, delegate);
                }
            }
        }
    }
}
