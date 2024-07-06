package dev.gradleplugins.internal.rules;

import dev.gradleplugins.GradleRuntimeCompatibility;
import dev.gradleplugins.internal.GradleCompatibilities;
import dev.gradleplugins.internal.JvmCompatibilities;
import org.gradle.api.Action;
import org.gradle.api.JavaVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

/*private*/ abstract /*final*/ class GradleJvmCompatibilitiesRule implements Plugin<Project> {
    @Inject
    public GradleJvmCompatibilitiesRule() {}

    @Override
    public void apply(Project project) {
        project.getPluginManager().withPlugin("java-base", __ -> {
            project.getPluginManager().apply("gradlepluginsdev.rules.jvm-compatibilities");
            project.getPluginManager().apply("gradlepluginsdev.rules.gradle-compatibilities");

            project.getExtensions().getByType(GradleCompatibilities.ForSourceSetExtension.class).configureEach(new Action<GradleCompatibilities.ForSourceSetExtension.SourceSetGradleCompatibilities>() {
                @Override
                public void execute(GradleCompatibilities.ForSourceSetExtension.SourceSetGradleCompatibilities gradle) {
                    project.getExtensions().getByType(JvmCompatibilities.ForSourceSetExtension.class).forSourceSet(gradle.getSourceSet()).configure(jvm -> {
                        jvm.getSourceCompatibility().set(sourceCompatibilityOf(project)
                                .orElse(sourceCompatibilityOf(gradle))
                                .orElse(JavaVersion.current()));
                    });
                }

                private /*static*/ Provider<JavaVersion> sourceCompatibilityOf(Project project) {
                    JvmCompatibilities.ForProjectExtension jvmCompatibilities = project.getExtensions().getByType(JvmCompatibilities.ForProjectExtension.class);
                    return jvmCompatibilities.getSourceCompatibility();
                }

                private /*static*/ Provider<JavaVersion> sourceCompatibilityOf(GradleCompatibilities gradle) {
                    return gradle.getMinimumGradleVersion().map(GradleRuntimeCompatibility::minimumJavaVersionFor);
                }
            });
        });
    }
}
