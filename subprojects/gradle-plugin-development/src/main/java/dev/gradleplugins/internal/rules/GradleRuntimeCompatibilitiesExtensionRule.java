package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradleRuntimeCompatibilitiesExtension;
import dev.gradleplugins.GradleRuntimeCompatibility;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;

import static org.gradle.util.GradleVersion.version;

/*private*/ abstract /*final*/ class GradleRuntimeCompatibilitiesExtensionRule implements Plugin<Project> {
    @Inject
    public GradleRuntimeCompatibilitiesExtensionRule() {}

    @Override
    public void apply(Project project) {
        project.getExtensions().create("gradleRuntimeCompatibilities", DefaultGradleRuntimeCompatibilitiesExtension.class);
    }

    /*private*/ static abstract /*final*/ class DefaultGradleRuntimeCompatibilitiesExtension implements GradleRuntimeCompatibilitiesExtension {
        private final ProviderFactory providers;

        @Inject
        public DefaultGradleRuntimeCompatibilitiesExtension(ProviderFactory providers) {
            this.providers = providers;
        }

        @Override
        public Provider<String> groovyVersionOf(Object gradleVersion) {
            return toGradleVersion(gradleVersion)
                    .map(it -> GradleRuntimeCompatibility.groovyVersionOf(it.getVersion()));
        }

        @Override
        public Provider<JavaVersion> minimumJavaVersionFor(Object gradleVersion) {
            return toGradleVersion(gradleVersion)
                    .map(it -> GradleRuntimeCompatibility.minimumJavaVersionFor(it.getVersion()));
        }

        @Override
        public Provider<String> kotlinVersionOf(Object gradleVersion) {
            return toGradleVersion(gradleVersion)
                    .map(it -> GradleRuntimeCompatibility.kotlinVersionOf(it.getVersion()).orElse(null));
        }

        @Override
        public Provider<GradleVersion> lastPatchedVersionOf(Object gradleVersion) {
            return toGradleVersion(gradleVersion)
                    .map(it -> version(GradleRuntimeCompatibility.lastPatchedVersionOf(it.getVersion())));
        }

        @Override
        public Provider<GradleVersion> lastMinorReleaseOf(Object gradleVersion) {
            return toGradleVersion(gradleVersion)
                    .map(it -> version(GradleRuntimeCompatibility.lastMinorReleaseOf(it.getVersion())));
        }

        private Provider<GradleVersion> toGradleVersion(Object gradleVersion) {
            if (gradleVersion instanceof Provider) {
                return ((Provider<?>) gradleVersion).map(this::unpack);
            }
            return providers.provider(() -> gradleVersion).map(this::unpack);
        }

        private GradleVersion unpack(Object gradleVersion) {
            if (gradleVersion instanceof GradleVersion) {
                return (GradleVersion) gradleVersion;
            } else {
                return version(gradleVersion.toString());
            }
        }
    }
}
