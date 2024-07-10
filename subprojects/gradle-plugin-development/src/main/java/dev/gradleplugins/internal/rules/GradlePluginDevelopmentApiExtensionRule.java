package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentApiExtension;
import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import dev.gradleplugins.internal.DependencyBucketFactory;
import dev.gradleplugins.internal.DependencyFactory;
import dev.gradleplugins.internal.ExportedApiExtension;
import dev.gradleplugins.internal.util.ActionSet;
import dev.gradleplugins.internal.util.Configurable;
import dev.gradleplugins.internal.util.LocalOrRemoteVersionTransformer;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.bundling.Jar;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import javax.inject.Inject;
import java.util.Collections;
import java.util.function.Predicate;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;

/*private*/ abstract /*final*/ class GradlePluginDevelopmentApiExtensionRule implements Plugin<Project> {
    @Inject
    public GradlePluginDevelopmentApiExtensionRule() {}

    @Override
    public void apply(Project project) {
        project.getPluginManager().withPlugin("java-gradle-plugin", __ -> {
            project.getPluginManager().apply("gradlepluginsdev.rules.exported-apis");

            final DefaultGradlePluginDevelopmentApiExtension extension = newApiExtension(project);

            ((ExtensionAware) gradlePlugin(project)).getExtensions().add("api", extension);

            // Add Gradle API to API source set
            final DependencyFactory factory = DependencyFactory.forProject(project);
            new DependencyBucketFactory(project, extension.getSourceSet()).create("compileOnly").add(project.provider(() -> ((ExtensionAware) gradlePlugin(project)).getExtensions().findByType(GradlePluginDevelopmentCompatibilityExtension.class))
                    .flatMap(it -> it.getGradleApiVersion())
                    .orElse("local")
                    .map(new LocalOrRemoteVersionTransformer<>(factory::localGradleApi, factory::gradleApi)));

            // TODO: avoid this train wreck
            project.afterEvaluate(___ -> extension.provider.finalizeValue());
        });
    }

    public static GradlePluginDevelopmentExtension gradlePlugin(Project project) {
        return project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
    }

    private static DefaultGradlePluginDevelopmentApiExtension newApiExtension(Project project) {
        return project.getObjects().newInstance(DefaultGradlePluginDevelopmentApiExtension.class, project);
    }

    /*private*/ static abstract /*final*/ class DefaultGradlePluginDevelopmentApiExtension implements GradlePluginDevelopmentApiExtension, Configurable<GradlePluginDevelopmentApiExtension> {
        private final ExportedApiProvider provider;

        private static final class ExportedApiProvider {
            private Project project;
            private ActionSet<ExportedApiExtension.ExportedApi> actions = new ActionSet<>();
            private NamedDomainObjectProvider<? extends ExportedApiExtension.ExportedApi> provider;

            public ExportedApiProvider(Project project) {
                this.project = project;
            }

            public void configure(Action<? super ExportedApiExtension.ExportedApi> action) {
                if (provider == null) {
                    actions.add(action);
                } else {
                    provider.configure(action);
                }
            }

            private void finalizeValue() {
                if (provider == null) {
                    provider = project.getExtensions().getByType(ExportedApiExtension.class).forSourceSet(pluginSourceSetOf(project));
                    provider.configure(actions);

                    // Clear some data
                    actions = null;
                    project = null;
                }
            }

            public ExportedApiExtension.ExportedApi get() {
                finalizeValue();
                // WARN: Be careful here, when we resolve, we flush all action to the current `pluginSourceSet`.
                //   This means the `pluginSourceSet` used could misalign with the current `pluginSourceSet`.
                return provider.get();
            }
        }

        private static SourceSet pluginSourceSetOf(Project project) {
            return project.getExtensions().getByType(GradlePluginDevelopmentExtension.class).getPluginSourceSet();
        }

        private final GradlePluginDevelopmentApiExtensionAdapter.Factory adapterFactory;

        @Inject
        public DefaultGradlePluginDevelopmentApiExtension(Project project) {
            this.provider = new ExportedApiProvider(project);
            this.adapterFactory = new GradlePluginDevelopmentApiExtensionAdapter.Factory(project.getObjects());
        }

        @Override
        public Property<SourceSet> getSourceSet() {
            return provider.get().getApiSourceSet();
        }

        @Override
        public TaskProvider<Jar> getJarTask() {
            return provider.get().getJarTask().getAsProvider();
        }

        @Override
        public void setJarTask(TaskProvider<Jar> jarTaskProvider) {
            provider.configure(it -> it.getJarTask().set(jarTaskProvider));
        }

        @Override
        public void configure(Action<? super GradlePluginDevelopmentApiExtension> action) {
            provider.configure(exportedApi -> action.execute(adapterFactory.create(exportedApi)));
        }

        /*private*/ static abstract /*final*/ class GradlePluginDevelopmentApiExtensionAdapter implements GradlePluginDevelopmentApiExtension {
            private final ExportedApiExtension.ExportedApi delegate;

            @Inject
            public GradlePluginDevelopmentApiExtensionAdapter(ExportedApiExtension.ExportedApi delegate) {
                this.delegate = delegate;
            }

            @Override
            public Property<SourceSet> getSourceSet() {
                return delegate.getApiSourceSet();
            }

            @Override
            public TaskProvider<Jar> getJarTask() {
                return delegate.getJarTask().getAsProvider();
            }

            @Override
            public void setJarTask(TaskProvider<Jar> jarTaskProvider) {
                delegate.getJarTask().set(jarTaskProvider);
            }

            static final class Factory {
                private final ObjectFactory objects;

                public Factory(ObjectFactory objects) {
                    this.objects = objects;
                }

                public GradlePluginDevelopmentApiExtensionAdapter create(ExportedApiExtension.ExportedApi delegate) {
                    return objects.newInstance(GradlePluginDevelopmentApiExtensionAdapter.class, delegate);
                }
            }
        }
    }
}
