package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Project;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import javax.annotation.Nullable;

import static dev.gradleplugins.GradleRuntimeCompatibility.minimumJavaVersionFor;
import static dev.gradleplugins.internal.rules.WireMinimumGradleVersionWithJvmCompatibilityRule.InjectJvmCompatibilityPropertyIntoJavaExtensionRule.SOURCE_COMPATIBILITY_PROPERTY;
import static dev.gradleplugins.internal.rules.WireMinimumGradleVersionWithJvmCompatibilityRule.InjectJvmCompatibilityPropertyIntoJavaExtensionRule.TARGET_COMPATIBILITY_PROPERTY;

public final class WireMinimumGradleVersionWithJvmCompatibilityRule implements Action<Project> {
    @Override
    public void execute(Project project) {
        new InjectJvmCompatibilityPropertyIntoJavaExtensionRule(TARGET_COMPATIBILITY_PROPERTY).execute(project);
        new InjectJvmCompatibilityPropertyIntoJavaExtensionRule(SOURCE_COMPATIBILITY_PROPERTY).execute(project);
    }

    /**
     * When applying this rule, users should wrap the JVM compatibility properties using the Provider API and only resolve them after project evaluation.
     */
    public static final class InjectJvmCompatibilityPropertyIntoJavaExtensionRule implements Action<Project> {
        private final JvmCompatibilitySpec jvmCompatibilitySpec;

        public InjectJvmCompatibilityPropertyIntoJavaExtensionRule(JvmCompatibilitySpec jvmCompatibilitySpec) {
            this.jvmCompatibilitySpec = jvmCompatibilitySpec;
        }

        @Override
        public void execute(Project project) {
            java(project, extension -> {
                final JvmCompatibilityProperty jvmCompatibilityProperty = jvmCompatibilitySpec.onExtension(extension);

                project.afterEvaluate(__ -> {
                    if (!jvmCompatibilityProperty.wasOverridden()) {
                        final String version = minimumGradleVersion(project);
                        if (version != null) { // a minimum version was set
                            jvmCompatibilityProperty.set(minimumJavaVersionFor(version));
                        } else {
                            jvmCompatibilityProperty.setToInitialValue();
                        }
                    }
                });
            });
        }

        @Nullable
        private static String minimumGradleVersion(Project project) {
            final GradlePluginDevelopmentExtension developmentExtension = (GradlePluginDevelopmentExtension) project.getExtensions().findByName("gradlePlugin");
            if (developmentExtension == null) {
                return null;
            }

            final GradlePluginDevelopmentCompatibilityExtension compatibilityExtension = (GradlePluginDevelopmentCompatibilityExtension) ((ExtensionAware) developmentExtension).getExtensions().findByName("compatibility");
            if (compatibilityExtension == null) {
                return null;
            }

            return compatibilityExtension.getMinimumGradleVersion().getOrNull();
        }

        private static void java(Project project, Action<? super JavaPluginExtension> action) {
            action.execute((JavaPluginExtension) project.getExtensions().getByName("java"));
        }

        public interface JvmCompatibilitySpec {
            String getName();

            JvmCompatibilityProperty onExtension(JavaPluginExtension extension);
        }

        private interface JvmCompatibilityProperty {
            JavaVersion get();
            void set(JavaVersion value);
            boolean wasOverridden();
            void setToInitialValue();
        }

        private static final JavaVersion OVERRIDE_DETECTION_VALUE = JavaVersion.VERSION_1_1;

        public static final JvmCompatibilitySpec SOURCE_COMPATIBILITY_PROPERTY = new JvmCompatibilitySpec() {
            @Override
            public String getName() {
                return "sourceCompatibility";
            }

            @Override
            public InjectJvmCompatibilityPropertyIntoJavaExtensionRule.JvmCompatibilityProperty onExtension(JavaPluginExtension extension) {
                final JavaVersion initialValue = extension.getSourceCompatibility();
                assert !initialValue.equals(OVERRIDE_DETECTION_VALUE);
                extension.setSourceCompatibility(OVERRIDE_DETECTION_VALUE);
                return new InjectJvmCompatibilityPropertyIntoJavaExtensionRule.JvmCompatibilityProperty() {
                    @Override
                    public JavaVersion get() {
                        if (wasOverridden()) {
                            return extension.getSourceCompatibility();
                        }
                        return initialValue;
                    }

                    @Override
                    public void set(JavaVersion value) {
                        extension.setSourceCompatibility(value);
                    }

                    @Override
                    public boolean wasOverridden() {
                        return !extension.getSourceCompatibility().equals(OVERRIDE_DETECTION_VALUE);
                    }

                    @Override
                    public void setToInitialValue() {
                       set(initialValue);
                    }
                };
            }
        };

        public static final JvmCompatibilitySpec TARGET_COMPATIBILITY_PROPERTY = new JvmCompatibilitySpec() {
            @Override
            public String getName() {
                return "targetCompatibility";
            }

            @Override
            public InjectJvmCompatibilityPropertyIntoJavaExtensionRule.JvmCompatibilityProperty onExtension(JavaPluginExtension extension) {
                final JavaVersion initialValue = extension.getTargetCompatibility();
                assert !initialValue.equals(OVERRIDE_DETECTION_VALUE) : "please configure targetCompatibility before sourceCompatibility";
                extension.setTargetCompatibility(OVERRIDE_DETECTION_VALUE);
                return new InjectJvmCompatibilityPropertyIntoJavaExtensionRule.JvmCompatibilityProperty() {
                    @Override
                    public JavaVersion get() {
                        if (wasOverridden()) {
                            return extension.getTargetCompatibility();
                        }
                        return initialValue;
                    }

                    @Override
                    public void set(JavaVersion value) {
                        extension.setTargetCompatibility(value);
                    }

                    @Override
                    public boolean wasOverridden() {
                        return !extension.getTargetCompatibility().equals(OVERRIDE_DETECTION_VALUE);
                    }

                    @Override
                    public void setToInitialValue() {
                        set(initialValue);
                    }
                };
            }
        };
    }
}
