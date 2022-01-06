package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import dev.gradleplugins.internal.FinalizableComponent;
import dev.gradleplugins.internal.jvm.JvmCompatibilityProperties;
import dev.gradleplugins.internal.jvm.JvmCompatibilityPropertyFactory;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;

import static dev.gradleplugins.GradleRuntimeCompatibility.minimumJavaVersionFor;
import static org.gradle.util.VersionNumber.parse;

abstract /*final*/ class DefaultGradlePluginDevelopmentCompatibilityExtension implements GradlePluginDevelopmentCompatibilityExtension, HasPublicType, FinalizableComponent {
    private final JvmCompatibilityProperties compatibilities;

    @Inject
    public DefaultGradlePluginDevelopmentCompatibilityExtension(JavaPluginExtension java) {
        this.compatibilities = JvmCompatibilityPropertyFactory.of(java);
    }

    @Override
    public TypeOf<?> getPublicType() {
        return TypeOf.typeOf(GradlePluginDevelopmentCompatibilityExtension.class);
    }

    @Override
    public void finalizeComponent() {
        if (getMinimumGradleVersion().isPresent()) {
            compatibilities.set(minimumJavaVersionFor(parse(getMinimumGradleVersion().get())));
        } else {
            getMinimumGradleVersion().set(GradleVersion.current().getVersion());
        }
        getMinimumGradleVersion().disallowChanges();
        compatibilities.finalizeValues();
    }
}
