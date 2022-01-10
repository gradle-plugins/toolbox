package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import dev.gradleplugins.internal.FinalizableComponent;
import dev.gradleplugins.internal.jvm.JvmCompatibilityProperties;
import dev.gradleplugins.internal.jvm.JvmCompatibilityPropertyFactory;
import org.gradle.api.Transformer;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;

import static dev.gradleplugins.GradlePluginDevelopmentDependencyExtension.GRADLE_API_LOCAL_VERSION;
import static dev.gradleplugins.GradleRuntimeCompatibility.minimumJavaVersionFor;
import static org.gradle.util.VersionNumber.parse;

@SuppressWarnings("UnstableApiUsage")
abstract /*final*/ class DefaultGradlePluginDevelopmentCompatibilityExtension implements GradlePluginDevelopmentCompatibilityExtension, HasPublicType, FinalizableComponent {
    private final JvmCompatibilityProperties compatibilities;
    private boolean finalized = false;

    @Inject
    public DefaultGradlePluginDevelopmentCompatibilityExtension(JavaPluginExtension java) {
        this.compatibilities = JvmCompatibilityPropertyFactory.of(java);
        getGradleApiVersion().convention(getMinimumGradleVersion().map(toLocalIfGradleSnapshotVersion()));
    }

    private static Transformer<String, String> toLocalIfGradleSnapshotVersion() {
        return it -> {
            if (GradleVersion.version(it).isSnapshot()) {
                return GRADLE_API_LOCAL_VERSION;
            }
            return it;
        };
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(GradlePluginDevelopmentCompatibilityExtension.class);
    }

    @Override
    public void finalizeComponent() {
        if (!finalized) {
            finalized = true;
            if (getMinimumGradleVersion().isPresent()) {
                compatibilities.set(minimumJavaVersionFor(parse(getMinimumGradleVersion().get())));
            } else {
                getMinimumGradleVersion().set(GradleVersion.current().getVersion());
            }
            getMinimumGradleVersion().disallowChanges();
            compatibilities.finalizeValues();
        }
    }

    @Override
    public boolean isFinalized() {
        return finalized;
    }
}
