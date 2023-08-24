package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentDependencies;
import dev.gradleplugins.GradlePluginDevelopmentDependencyBucket;
import dev.gradleplugins.GradlePluginDevelopmentDependencyModifiers;
import dev.gradleplugins.internal.DefaultDependencyBucketFactory;
import dev.gradleplugins.internal.DependencyBucketFactory;
import dev.gradleplugins.internal.DependencyFactory;
import dev.gradleplugins.internal.EnforcedPlatformDependencyModifier;
import dev.gradleplugins.internal.PlatformDependencyModifier;
import dev.gradleplugins.internal.runtime.dsl.GroovyHelper;
import org.codehaus.groovy.runtime.MethodClosure;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.attributes.Usage;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.compile.CompileOptions;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import javax.inject.Inject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static dev.gradleplugins.internal.DefaultDependencyBucket.pluginSourceSet;
import static org.gradle.api.attributes.Bundling.BUNDLING_ATTRIBUTE;
import static org.gradle.api.attributes.Bundling.EXTERNAL;
import static org.gradle.api.attributes.Category.CATEGORY_ATTRIBUTE;
import static org.gradle.api.attributes.Category.LIBRARY;
import static org.gradle.api.attributes.LibraryElements.JAR;
import static org.gradle.api.attributes.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE;
import static org.gradle.api.attributes.Usage.JAVA_RUNTIME;
import static org.gradle.api.attributes.Usage.USAGE_ATTRIBUTE;
import static org.gradle.api.attributes.java.TargetJvmVersion.TARGET_JVM_VERSION_ATTRIBUTE;

public final class RegisterGradlePluginDevelopmentDependenciesExtensionRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        assert project.getPluginManager().hasPlugin("java-gradle-plugin");

        gradlePlugin(project, extension -> {
            final DefaultGradlePluginDevelopmentDependencies dependenciesExtension = project.getObjects().newInstance(DefaultGradlePluginDevelopmentDependencies.class, project, new DefaultDependencyBucketFactory(project, pluginSourceSet(project)));

            // adhoc decoration of the dependencies
            dependenciesExtension.forEach(dependencyBucket -> {
                GroovyHelper.instance().addNewInstanceMethod(dependenciesExtension, dependencyBucket.getName(), new MethodClosure(dependencyBucket, "add"));
            });
            GroovyHelper.instance().addNewInstanceMethod(dependenciesExtension, "platform", new MethodClosure(dependenciesExtension.getPlatform(), "modify"));
            GroovyHelper.instance().addNewInstanceMethod(dependenciesExtension, "enforcedPlatform", new MethodClosure(dependenciesExtension.getEnforcedPlatform(), "modify"));

            ((ExtensionAware) extension).getExtensions().add(GradlePluginDevelopmentDependencies.class, "dependencies", dependenciesExtension);

            // Shim missing configurations
            project.afterEvaluate(__ -> {
                final SourceSet sourceSet = extension.getPluginSourceSet();;

                Configuration api = project.getConfigurations().findByName(sourceSet.getApiConfigurationName());
                if (api == null) {
                    api = project.getConfigurations().create(sourceSet.getApiConfigurationName());
                    api.setDescription("API dependencies for " + sourceSet + ".");
                    api.setCanBeResolved(false);
                    api.setCanBeConsumed(false);
                    project.getConfigurations().getByName(sourceSet.getImplementationConfigurationName()).extendsFrom(api);
                }

                Configuration compileOnlyApi = project.getConfigurations().findByName(compileOnlyApiConfigurationName(sourceSet));
                if (compileOnlyApi == null) {
                    compileOnlyApi = project.getConfigurations().create(compileOnlyApiConfigurationName(sourceSet));
                    compileOnlyApi.setDescription("Compile only dependencies for " + sourceSet + ".");
                    compileOnlyApi.setCanBeResolved(false);
                    compileOnlyApi.setCanBeConsumed(false);
                    project.getConfigurations().getByName(sourceSet.getCompileOnlyConfigurationName()).extendsFrom(compileOnlyApi);
                }

                Configuration apiElements = project.getConfigurations().findByName(sourceSet.getApiElementsConfigurationName());
                if (apiElements == null) {
                    apiElements = project.getConfigurations().create(sourceSet.getApiElementsConfigurationName());
                    apiElements.setDescription("API elements for " + sourceSet + ".");
                    apiElements.setCanBeResolved(false);
                    apiElements.setCanBeConsumed(true);
                    apiElements.attributes(it -> {
                        it.attribute(USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, Usage.JAVA_API));
                        it.attribute(CATEGORY_ATTRIBUTE, project.getObjects().named(Category.class, LIBRARY));
                        it.attribute(TARGET_JVM_VERSION_ATTRIBUTE, project.getTasks().named(sourceSet.getCompileJavaTaskName(), JavaCompile.class).flatMap(toMajorVersion(project)).get());
                        it.attribute(BUNDLING_ATTRIBUTE, project.getObjects().named(Bundling.class, EXTERNAL));
                        it.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, JAR));
                    });
                }

                Configuration runtimeElements = project.getConfigurations().findByName(sourceSet.getRuntimeElementsConfigurationName());
                if (runtimeElements == null) {
                    runtimeElements = project.getConfigurations().create(sourceSet.getRuntimeElementsConfigurationName());
                    runtimeElements.setDescription("Runtime elements for " + sourceSet + ".");
                    runtimeElements.setCanBeResolved(false);
                    runtimeElements.setCanBeConsumed(true);
                    runtimeElements.attributes(it -> {
                        it.attribute(USAGE_ATTRIBUTE, project.getObjects().named(Usage.class, JAVA_RUNTIME));
                        it.attribute(CATEGORY_ATTRIBUTE, project.getObjects().named(Category.class, LIBRARY));
                        it.attribute(TARGET_JVM_VERSION_ATTRIBUTE, project.getTasks().named(sourceSet.getCompileJavaTaskName(), JavaCompile.class).flatMap(toMajorVersion(project)).get());
                        it.attribute(BUNDLING_ATTRIBUTE, project.getObjects().named(Bundling.class, EXTERNAL));
                        it.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, project.getObjects().named(LibraryElements.class, JAR));
                    });
                }

                if (!extension.getPluginSourceSet().getName().equals("main")) {
                    apiElements.extendsFrom(api);
                    apiElements.extendsFrom(compileOnlyApi);

                    runtimeElements.extendsFrom(project.getConfigurations().getByName(extension.getPluginSourceSet().getImplementationConfigurationName()));
                    runtimeElements.extendsFrom(project.getConfigurations().getByName(extension.getPluginSourceSet().getRuntimeOnlyConfigurationName()));
                }
            });
        });
    }

    private static Transformer<Provider<Integer>, JavaCompile> toMajorVersion(Project project) {
        return task -> getReleaseOption(project, task.getOptions())
                .orElse(getReleaseFlag(project, task.getOptions().getCompilerArgs()))
                .orElse(project.provider(() -> Integer.parseInt(JavaVersion.toVersion(task.getTargetCompatibility()).getMajorVersion())));
    }

    private static Provider<Integer> getReleaseOption(Project project, CompileOptions options) {
        try {
            final Method getRelease = options.getClass().getDeclaredMethod("getRelease");

            @SuppressWarnings("unchecked")
            final Provider<Integer> result = (Provider<Integer>) getRelease.invoke(options);
            return result;
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            return project.provider(() -> null);
        }
    }

    private static Provider<Integer> getReleaseFlag(Project project, List<String> compilerArgs) {
        return project.provider(() -> {
            int flagIndex = compilerArgs.indexOf("--release");
            if (flagIndex != -1 && flagIndex + 1 < compilerArgs.size()) {
                return Integer.parseInt(String.valueOf(compilerArgs.get(flagIndex + 1)));
            }
            return null;
        });
    }

    private static String compileOnlyApiConfigurationName(SourceSet sourceSet) {
        if (sourceSet.getName().equals("main")) {
            return "compileOnlyApi";
        }
        return sourceSet.getName() + "CompileOnlyApi";
    }

    private static void gradlePlugin(Project project, Action<? super GradlePluginDevelopmentExtension> action) {
        action.execute((GradlePluginDevelopmentExtension) project.getExtensions().getByName("gradlePlugin"));
    }

    protected static abstract /*final*/ class DefaultGradlePluginDevelopmentDependencies implements GradlePluginDevelopmentDependencies, Iterable<GradlePluginDevelopmentDependencyBucket> {
        private final Map<String, GradlePluginDevelopmentDependencyBucket> dependencyBuckets = new LinkedHashMap<>();
        private final DependencyFactory dependencyFactory;
        private final GradlePluginDevelopmentDependencyModifiers.DependencyModifier platformDependencyModifier;
        private final GradlePluginDevelopmentDependencyModifiers.DependencyModifier enforcedPlatformDependencyModifier;
        private final Project project;

        @Inject
        public DefaultGradlePluginDevelopmentDependencies(Project project, DependencyBucketFactory dependencyBucketFactory) {
            this.dependencyFactory = DependencyFactory.forProject(project);
            this.platformDependencyModifier = new PlatformDependencyModifier(project);
            this.enforcedPlatformDependencyModifier = new EnforcedPlatformDependencyModifier(project);
            this.project = project;
            add(dependencyBucketFactory.create("api"));
            add(dependencyBucketFactory.create("implementation"));
            add(dependencyBucketFactory.create("compileOnlyApi"));
            add(dependencyBucketFactory.create("compileOnly"));
            add(dependencyBucketFactory.create("runtimeOnly"));
            add(dependencyBucketFactory.create("annotationProcessor"));
        }

        private void add(GradlePluginDevelopmentDependencyBucket dependencyBucket) {
            dependencyBuckets.put(dependencyBucket.getName(), dependencyBucket);
        }

        @Override
        public GradlePluginDevelopmentDependencyBucket getApi() {
            return dependencyBuckets.get("api");
        }

        @Override
        public GradlePluginDevelopmentDependencyBucket getImplementation() {
            return dependencyBuckets.get("implementation");
        }

        @Override
        public GradlePluginDevelopmentDependencyBucket getCompileOnlyApi() {
            return dependencyBuckets.get("compileOnlyApi");
        }

        @Override
        public GradlePluginDevelopmentDependencyBucket getCompileOnly() {
            return dependencyBuckets.get("compileOnly");
        }

        @Override
        public GradlePluginDevelopmentDependencyBucket getRuntimeOnly() {
            return dependencyBuckets.get("runtimeOnly");
        }

        @Override
        public GradlePluginDevelopmentDependencyBucket getAnnotationProcessor() {
            return dependencyBuckets.get("annotationProcessor");
        }

        @Override
        public GradlePluginDevelopmentDependencyModifiers.DependencyModifier getPlatform() {
            return platformDependencyModifier;
        }

        @Override
        public GradlePluginDevelopmentDependencyModifiers.DependencyModifier getEnforcedPlatform() {
            return enforcedPlatformDependencyModifier;
        }

        @Override
        public Dependency gradleApi(String version) {
            if ("local".equals(version)) {
                return dependencyFactory.localGradleApi();
            }
            return dependencyFactory.gradleApi(version);
        }

        @Override
        public ProjectDependency project(String projectPath) {
            return dependencyFactory.create(project.project(projectPath));
        }

        @Override
        public ProjectDependency project() {
            return dependencyFactory.create(project);
        }

        @Override
        public ExternalModuleDependency gradlePlugin(String pluginNotation) {
            return dependencyFactory.gradlePlugin(pluginNotation);
        }

        @Override
        public Iterator<GradlePluginDevelopmentDependencyBucket> iterator() {
            return dependencyBuckets.values().iterator();
        }
    }
}
