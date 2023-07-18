package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension;
import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.artifacts.component.ComponentIdentifier;
import org.gradle.api.internal.artifacts.dependencies.SelfResolvingDependencyInternal;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;
import org.gradle.util.GradleVersion;

import java.util.Collections;
import java.util.function.Predicate;

public final class ConvertGradleApiSelfResolvingDependencyToExternalDependencyRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        // Surgical procedure of removing the Gradle API and replacing it with dev.gradleplugins:gradle-api
        // Why so complicated?
        //   The GradlePluginDevelopment extension allow to modify the pluginSourceSet at any point in time.
        //   For this reason, we can't assume the "main" source set or even the pluginSourceSet at the time this code execute is correct.
        //   So we inject the configuration code into "all" source set and their respective configuration which defer the decision to when the pluginSourceSet
        //   should be immutable. There is nothing enforcing the immutability, so it's a best effort.
        sourceSets(project, sourceSets -> {
            sourceSets.configureEach(sourceSet -> {
                // We have to go through all configurations as API configuration are only created for java-library and java features
                project.getConfigurations().configureEach(configuration -> {
                    if (apiConfigurationOf(sourceSet).test(configuration)) {
                        configuration.withDependencies(ifPluginSourceSet(project, sourceSet, removeSelfResolvingGradleApiDependency()));
                    } else if (compileOnlyConfigurationOf(sourceSet).test(configuration)) { // should be compileOnlyApi only for libraries
                        configuration.getDependencies().addAllLater(newCollectionProvider(project, Dependency.class,
                                whenPluginSourceSet(project, sourceSet, gradleApiDependency(project))));
                    }
                });
            });
        });
    }

    private static <T> Provider<? extends Iterable<T>> newCollectionProvider(Project project, Class<T> elementType, Provider<? extends Iterable<? extends T>> value) {
        return project.getObjects().listProperty(elementType).value(value);
    }

    private static Provider<Dependency> gradleApiDependency(Project project) {
        // TODO: I believe this won't work in a configuration cache context because we use Project instance to create Dependency instance
        return gradleApiVersion(project).map(version -> GradlePluginDevelopmentDependencyExtension.from(project.getDependencies()).gradleApi(version));
    }

    private static Predicate<Configuration> apiConfigurationOf(SourceSet sourceSet) {
        return configuration -> configuration.getName().equals(sourceSet.getApiConfigurationName());
    }

    private static Predicate<Configuration> compileOnlyConfigurationOf(SourceSet sourceSet) {
        return configuration -> configuration.getName().equals(sourceSet.getCompileOnlyConfigurationName());
    }

    private static Provider<? extends Iterable<? extends Dependency>> whenPluginSourceSet(Project project, SourceSet sourceSet, Provider<? extends Dependency> delegate) {
        return project.provider(() -> {
            if (sourceSet.equals(pluginSourceSet(project))) {
                return new Object(); // mark this case is valid
            } else {
                return null;
            }
        }).flatMap(__ -> delegate.map(Collections::singletonList)).orElse(Collections.emptyList());
    }

    private static Action<DependencySet> ifPluginSourceSet(Project project, SourceSet sourceSet, Action<? super DependencySet> delegate) {
        return dependencies -> {
            if (sourceSet.equals(pluginSourceSet(project))) {
                delegate.execute(dependencies);
            }
        };
    }

    private static Action<DependencySet> removeSelfResolvingGradleApiDependency() {
        return dependencies -> {
            dependencies.removeIf(it -> {
                if (it instanceof SelfResolvingDependencyInternal) {
                    final ComponentIdentifier targetComponentId = ((SelfResolvingDependencyInternal) it).getTargetComponentId();
                    if (targetComponentId != null) {
                        return targetComponentId.getDisplayName().equals("Gradle API");
                    }
                }
                return false;
            });
        };
    }

    private static Provider<String> gradleApiVersion(Project project) {
        return project.provider(() -> {
            final GradlePluginDevelopmentExtension developmentExtension = (GradlePluginDevelopmentExtension) project.getExtensions().getByName("gradlePlugin");
            final GradlePluginDevelopmentCompatibilityExtension result = (GradlePluginDevelopmentCompatibilityExtension) ((ExtensionAware) developmentExtension).getExtensions().findByName("compatibility");
            return result;
        }).flatMap(GradlePluginDevelopmentCompatibilityExtension::getGradleApiVersion).orElse(GradleVersion.current().getVersion());
    }

    private static SourceSet pluginSourceSet(Project project) {
        final GradlePluginDevelopmentExtension extension = (GradlePluginDevelopmentExtension) project.getExtensions().findByName("gradlePlugin");
        if (extension == null) {
            return null; // not a Gradle plugin development project
        } else {
            return extension.getPluginSourceSet(); // the API should enforce non-null values (not the case in the code), so we assume non-null
        }
    }

    private static void sourceSets(Project project, Action<? super SourceSetContainer> action) {
        action.execute((SourceSetContainer) project.getExtensions().getByName("sourceSets"));
    }
}
