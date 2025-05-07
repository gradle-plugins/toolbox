package dev.gradleplugins.internal.util;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.artifacts.dsl.ComponentMetadataHandler;
import org.gradle.api.artifacts.dsl.RepositoryHandler;
import org.gradle.api.initialization.Settings;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

// Adapter for a dependency resolution management-ish adapter for Settings/Project
public abstract class MinimumDependencyResolutionManagement {

    public abstract RepositoryHandler getRepositories();
    public abstract void repositories(Action<? super RepositoryHandler> action);
    public abstract void component(Action<? super ComponentMetadataHandler> action);


    public static void dependencyResolutionManagement(Settings settings, Action<? super MinimumDependencyResolutionManagement> action) {
        try {
            Method Settings__getDependencyResolutionManagement = settings.getClass().getDeclaredMethod("getDependencyResolutionManagement");
            Object dependencyResolutionManagement = Settings__getDependencyResolutionManagement.invoke(settings);

            action.execute(new MinimumDependencyResolutionManagement() {
                @Override
                public RepositoryHandler getRepositories() {
                    try {
                        Method DependencyResolutionManagement__getRepositories = dependencyResolutionManagement.getClass().getDeclaredMethod("getRepositories");
                        RepositoryHandler repositories = (RepositoryHandler) DependencyResolutionManagement__getRepositories.invoke(dependencyResolutionManagement);

                        return repositories;
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public void repositories(Action<? super RepositoryHandler> action) {
                    action.execute(getRepositories());
                }

                @Override
                public void component(Action<? super ComponentMetadataHandler> action) {
                    try {
                        Method DependencyResolutionManagement__getComponents = dependencyResolutionManagement.getClass().getDeclaredMethod("getComponents");
                        ComponentMetadataHandler components = (ComponentMetadataHandler) DependencyResolutionManagement__getComponents.invoke(dependencyResolutionManagement);

                        action.execute(components);
                    } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            // ignore, lower Gradle
        }
    }

    public static void dependencyResolutionManagement(Project project, Action<? super MinimumDependencyResolutionManagement> action) {
        action.execute(new MinimumDependencyResolutionManagement() {
            @Override
            public RepositoryHandler getRepositories() {
                return project.getRepositories();
            }

            @Override
            public void repositories(Action<? super RepositoryHandler> action) {
                action.execute(project.getRepositories());
            }

            @Override
            public void component(Action<? super ComponentMetadataHandler> action) {
                project.getDependencies().components(action);
            }
        });
    }
}
