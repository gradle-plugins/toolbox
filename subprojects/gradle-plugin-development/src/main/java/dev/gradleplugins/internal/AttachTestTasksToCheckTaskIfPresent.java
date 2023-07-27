package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import org.gradle.api.Action;
import org.gradle.api.plugins.PluginManager;
import org.gradle.api.tasks.TaskContainer;

public final class AttachTestTasksToCheckTaskIfPresent implements Action<GradlePluginDevelopmentTestSuite> {
    private final PluginManager pluginManager;
    private final TaskContainer tasks;

    public AttachTestTasksToCheckTaskIfPresent(PluginManager pluginManager, TaskContainer tasks) {
        this.pluginManager = pluginManager;
        this.tasks = tasks;
    }

    @Override
    public void execute(GradlePluginDevelopmentTestSuite testSuite) {
        if (pluginManager.hasPlugin("java-base")) {
            tasks.named("check", task -> task.dependsOn(testSuite.getTestTasks().getElements()));
        }
    }
}
