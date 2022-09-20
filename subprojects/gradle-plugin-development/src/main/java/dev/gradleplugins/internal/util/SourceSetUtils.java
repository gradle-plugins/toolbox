package dev.gradleplugins.internal.util;

import org.gradle.api.Action;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.util.GradleVersion;

import static dev.gradleplugins.internal.util.GradleUtils.currentGradleVersion;

/**
 * Additional helper methods for {@link SourceSet}.
 */
public final class SourceSetUtils {
    private SourceSetUtils() {}

    public static boolean isMain(SourceSet self) {
        return self.getName().equals("main");
    }

    public static String compileOnlyApiConfigurationName(SourceSet self) {
        if (currentGradleVersion().compareTo(GradleVersion.version("6.7")) >= 0) {
            return self.getCompileOnlyConfigurationName() + "Api";
        }
        return self.getCompileOnlyConfigurationName();
    }

    public static void sourceSets(Project project, Action<? super SourceSetContainer> action) {
        project.getExtensions().configure("sourceSets", action);
    }

    public static Provider<SourceSetContainer> sourceSets(Project project) {
        return project.provider(() -> (SourceSetContainer) project.getExtensions().findByName("sourceSets"));
    }
}
