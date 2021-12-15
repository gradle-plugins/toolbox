package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentDependencyExtension;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.Provider;
import org.gradle.util.GradleVersion;

@RequiredArgsConstructor
public class GradlePluginDevelopmentDependencyExtensionInternal implements GradlePluginDevelopmentDependencyExtension {
    @Getter(AccessLevel.PROTECTED) private final DependencyHandler dependencies;
    private final Project project; // for the provider as notation shim
    private final GradlePluginDevelopmentDependencyExtension extension;

    @Override
    public Dependency gradleApi(String version) {
        return extension.gradleApi(version);
    }

    @Override
    public Dependency gradleTestKit(String version) {
        return extension.gradleTestKit(version);
    }

    @Override
    public Dependency gradleFixtures() {
        return extension.gradleFixtures();
    }

    @Override
    public Dependency gradleRunnerKit() {
        return extension.gradleRunnerKit();
    }

    public Dependency groovy(String version) {
        return getDependencies().create("org.codehaus.groovy:groovy-all:" + version);
    }

    public Dependency spockFramework(String version) {
        return getDependencies().create("org.spockframework:spock-core:" + version);
    }

    // Used by SpockFrameworkTestSuiteBasePlugin
    public Dependency spockFramework() {
        return getDependencies().create("org.spockframework:spock-core");
    }

    // Used by SpockFrameworkTestSuiteBasePlugin
    public Dependency spockFrameworkPlatform(String version) {
        return getDependencies().platform(getDependencies().create("org.spockframework:spock-bom:" + version));
    }

    // Shim for supporting older Gradle versions
    public void add(Project project, String configuration, Provider<Object> notation) {
        if (isGradleVersionGreaterOrEqualsTo6Dot5()) {
            getDependencies().add(configuration, notation);
        } else {
            project.afterEvaluate(proj -> getDependencies().add(configuration, notation.get()));
        }
    }

    public void add(String configuration, Provider<Object> notation) {
        if (isGradleVersionGreaterOrEqualsTo6Dot5()) {
            getDependencies().add(configuration, notation);
        } else {
            project.afterEvaluate(proj -> getDependencies().add(configuration, notation.get()));
        }
    }

    public void add(String configuration, Object notation) {
        if (isGradleVersionGreaterOrEqualsTo6Dot5() || !(notation instanceof Provider)) {
            getDependencies().add(configuration, notation);
        } else {
            project.afterEvaluate(proj -> getDependencies().add(configuration, ((Provider<Object>)notation).get()));
        }
    }

    private static boolean isGradleVersionGreaterOrEqualsTo6Dot5() {
        return GradleVersion.current().compareTo(GradleVersion.version("6.5")) >= 0;
    }

    public static GradlePluginDevelopmentDependencyExtensionInternal of(DependencyHandler dependencies) {
        return (GradlePluginDevelopmentDependencyExtensionInternal) ExtensionAware.class.cast(dependencies).getExtensions().getByType(GradlePluginDevelopmentDependencyExtension.class);
    }

    public void applyTo(DependencyHandler dependencies) {
        ExtensionAware.class.cast(dependencies).getExtensions().add(GradlePluginDevelopmentDependencyExtension.class, "gradlePluginDevelopment", this);
    }
}
