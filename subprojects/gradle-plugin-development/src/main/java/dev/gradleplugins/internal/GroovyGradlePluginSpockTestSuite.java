package dev.gradleplugins.internal;

import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;

import javax.inject.Inject;

public abstract class GroovyGradlePluginSpockTestSuite extends GroovySpockFrameworkTestSuite {
    @Inject
    public GroovyGradlePluginSpockTestSuite(String name, SourceSet sourceSet, TaskContainer tasks) {
        super(name, sourceSet, tasks);
    }
}
