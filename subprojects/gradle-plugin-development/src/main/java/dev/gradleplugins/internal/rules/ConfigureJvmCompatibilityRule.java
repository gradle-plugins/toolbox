package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import dev.gradleplugins.internal.RuleGroup;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.Provider;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import java.util.Optional;
import java.util.concurrent.Callable;

import static dev.gradleplugins.GradleRuntimeCompatibility.minimumJavaVersionFor;
import static dev.gradleplugins.internal.rules.JvmCompatibilityExtension.jvm;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.java;
import static org.gradle.util.VersionNumber.parse;

@RuleGroup(CompatibilityGroup.class)
public final class ConfigureJvmCompatibilityRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        // Ordering is important here
        jvm(gradlePlugin(project)).getTargetCompatibility()
                .value(compatibilityOf(project, targetCompatibility(java(project))))
                .finalizeValueOnRead();
        jvm(gradlePlugin(project)).getSourceCompatibility()
                .value(compatibilityOf(project, sourceCompatibility(java(project))))
                .finalizeValueOnRead();
    }

    private static Provider<JavaVersion> compatibilityOf(Project project, JvmCompatibilityProperty delegate) {
        final Provider<JavaVersion> minimumJavaVersion = project.provider(() -> compatibility(gradlePlugin(project)).orElse(null))
                .flatMap(GradlePluginDevelopmentCompatibilityExtension::getMinimumGradleVersion)
                .map(it -> minimumJavaVersionFor(parse(it)));
        return project.provider(toValue(delegate)).orElse(minimumJavaVersion).orElse(defaultValue(delegate));
    }

    private interface JvmCompatibilityProperty {
        void set(JavaVersion value);
        JavaVersion get();
    }

    private static JvmCompatibilityProperty sourceCompatibility(JavaPluginExtension extension) {
        return new JvmCompatibilityProperty() {
            @Override
            public void set(JavaVersion value) {
                extension.setSourceCompatibility(value);
            }

            @Override
            public JavaVersion get() {
                return extension.getSourceCompatibility();
            }
        };
    }

    private static JvmCompatibilityProperty targetCompatibility(JavaPluginExtension extension) {
        return new JvmCompatibilityProperty() {
            @Override
            public void set(JavaVersion value) {
                extension.setTargetCompatibility(value);
            }

            @Override
            public JavaVersion get() {
                return extension.getTargetCompatibility();
            }
        };
    }

    private static Callable<JavaVersion> toValue(JvmCompatibilityProperty property) {
        return () -> {
            final JavaVersion value = property.get();
            if (value.equals(JavaVersion.VERSION_1_1)) {
                return null; // assume Java 1.1 as unset, continue the orElse chain
            } else {
                return value;
            }
        };
    }

    private static JavaVersion defaultValue(JvmCompatibilityProperty property) {
        try {
            return property.get();
        } finally {
            property.set(JavaVersion.VERSION_1_1);
        }
    }

    private static Optional<GradlePluginDevelopmentCompatibilityExtension> compatibility(GradlePluginDevelopmentExtension extension) {
        return Optional.ofNullable((GradlePluginDevelopmentCompatibilityExtension) ((ExtensionAware) extension).getExtensions().findByName("compatibility"));
    }
}
