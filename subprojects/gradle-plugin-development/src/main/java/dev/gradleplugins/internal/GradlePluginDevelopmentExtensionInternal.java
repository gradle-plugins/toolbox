package dev.gradleplugins.internal;

import dev.gradleplugins.GroovyGradlePluginDevelopmentExtension;
import dev.gradleplugins.JavaGradlePluginDevelopmentExtension;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.attributes.Bundling;
import org.gradle.api.attributes.Category;
import org.gradle.api.attributes.DocsType;
import org.gradle.api.attributes.Usage;
import org.gradle.api.component.AdhocComponentWithVariants;
import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.component.SoftwareComponentContainer;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.BasePlugin;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.plugins.internal.JavaConfigurationVariantMapping;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.javadoc.Groovydoc;
import org.gradle.jvm.tasks.Jar;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import javax.annotation.Nullable;
import javax.inject.Inject;

public abstract class GradlePluginDevelopmentExtensionInternal implements GroovyGradlePluginDevelopmentExtension, JavaGradlePluginDevelopmentExtension {
    private final JavaPluginExtension java;

    @Inject
    public GradlePluginDevelopmentExtensionInternal(JavaPluginExtension java) {
        this.java = java;
    }

    @Inject
    protected abstract TaskContainer getTasks();

    @Inject
    protected abstract ConfigurationContainer getConfigurations();

    @Inject
    protected abstract ObjectFactory getObjects();

    @Inject
    protected abstract SoftwareComponentContainer getComponents();

    @Override
    public void withSourcesJar() {
        java.withSourcesJar();
    }

    @Override
    public void withJavadocJar() {
        java.withJavadocJar();
    }

    @Override
    public void withGroovydocJar() {
        Configuration variant = getConfigurations().maybeCreate("groovydocElements");
        variant.setVisible(false);
        variant.setDescription("groovydoc elements for main.");
        variant.setCanBeResolved(false);
        variant.setCanBeConsumed(true);
        variant.getAttributes().attribute(Usage.USAGE_ATTRIBUTE, getObjects().named(Usage.class, Usage.JAVA_RUNTIME));
        variant.getAttributes().attribute(Category.CATEGORY_ATTRIBUTE, getObjects().named(Category.class, Category.DOCUMENTATION));
        variant.getAttributes().attribute(Bundling.BUNDLING_ATTRIBUTE, getObjects().named(Bundling.class, Bundling.EXTERNAL));
        variant.getAttributes().attribute(DocsType.DOCS_TYPE_ATTRIBUTE, getObjects().named(DocsType.class, "groovydoc"));

        String jarTaskName = "groovydocJar";
        if (!getTasks().getNames().contains(jarTaskName)) {
            TaskProvider<Jar> jarTask = getTasks().register(jarTaskName, Jar.class, task -> {
                TaskProvider<Groovydoc> groovydocTask = getTasks().named("groovydoc", Groovydoc.class);
                task.dependsOn(groovydocTask);
                task.setDescription("Assembles a jar archive containing the main groovydoc.");
                task.setGroup(BasePlugin.BUILD_GROUP);
                task.getArchiveClassifier().set("groovydoc");
                task.from(groovydocTask.map(Groovydoc::getDestinationDir));
            });
            if (getTasks().getNames().contains(LifecycleBasePlugin.ASSEMBLE_TASK_NAME)) {
                getTasks().named(LifecycleBasePlugin.ASSEMBLE_TASK_NAME).configure(task -> task.dependsOn(jarTask));
            }
        }

        TaskProvider<Task> jar = getTasks().named(jarTaskName);
        variant.getOutgoing().artifact(new LazyPublishArtifact(jar));
        AdhocComponentWithVariants component = findJavaComponent(getComponents());
        if (component != null) {
            component.addVariantsFromConfiguration(variant, new JavaConfigurationVariantMapping("runtime", true));
        }
    }

    @Nullable
    private static AdhocComponentWithVariants findJavaComponent(SoftwareComponentContainer components) {
        SoftwareComponent component = components.findByName("java");
        if (component instanceof AdhocComponentWithVariants) {
            return (AdhocComponentWithVariants) component;
        }
        return null;
    }
}
