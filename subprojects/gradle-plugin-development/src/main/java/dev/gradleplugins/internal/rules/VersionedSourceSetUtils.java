package dev.gradleplugins.internal.rules;

import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.specs.Spec;
import org.gradle.api.tasks.SourceSet;

final class VersionedSourceSetUtils {
    private VersionedSourceSetUtils() {}


    static Spec<SourceSet> isVersionedSourceSet() {
        return it -> it.getName().startsWith("v");
    }

    // TODO: Move somewhere else
    public static String capability(Project project, String name) {
        return project.getGroup() + ":" + project.getName() + "-" + name + ":" + project.getVersion();
    }

    // TODO: Move somewhere else
    public static Transformer<Dependency, String> asDependency(Project project) {
        return capabilityName -> {
            final ProjectDependency dependency = (ProjectDependency) project.getDependencies().create(project);
            dependency.capabilities(capabilities -> {
                capabilities.requireCapability(capability(project, capabilityName));
            });
            return dependency;
        };
    }

    public static String gradleVersionClassifier(String gradleVersion) {
        assert gradleVersion.startsWith("v");
        return "gradle-" + gradleVersion;
    }
}
