package dev.gradleplugins.internal;

import org.gradle.api.component.SoftwareComponent;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;

import javax.inject.Inject;

public abstract class GroovySpockFrameworkTestSuite implements SoftwareComponent {
    private String name;
    private final SourceSet sourceSet;
    private final TaskProvider<Test> testTask;

    @Inject
    public GroovySpockFrameworkTestSuite(String name, SourceSet sourceSet, TaskContainer tasks) {
        this.name = name;
        this.sourceSet = sourceSet;
        this.testTask = tasks.register(sourceSet.getName(), Test.class, it -> {
            it.setDescription("Runs the functional tests");
            it.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);

            it.setTestClassesDirs(sourceSet.getOutput().getClassesDirs());
            it.setClasspath(sourceSet.getRuntimeClasspath());
        });
    }

    @Override
    public String getName() {
        return name;
    }

    public SourceSet getSourceSet() {
        return sourceSet;
    }

    public TaskProvider<Test> getTestTask() {
        return testTask;
    }

    public abstract Property<SourceSet> getTestedSourceSet();

    public abstract Property<String> getSpockVersion();
}
