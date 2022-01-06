package dev.gradleplugins.internal;

import dev.gradleplugins.GradlePluginTestingStrategy;
import dev.gradleplugins.GradleVersionCoverageTestingStrategy;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.util.GUtil;

import java.util.Set;
import java.util.concurrent.Callable;

public final class CreateTestTasksFromTestingStrategiesRule implements Action<GradlePluginDevelopmentTestSuiteInternal> {
    private final TaskContainer tasks;
    private final ObjectFactory objects;

    public CreateTestTasksFromTestingStrategiesRule(TaskContainer tasks, ObjectFactory objects) {
        this.tasks = tasks;
        this.objects = objects;
    }

    @Override
    public void execute(GradlePluginDevelopmentTestSuiteInternal testSuite) {
        testSuite.getTestingStrategies().disallowChanges();
        Set<GradlePluginTestingStrategy> strategies = testSuite.getTestingStrategies().get();
        if (strategies.isEmpty()) {
            TaskProvider<Test> testTask = createTestTask(testSuite);
            testTask.configure(applyTestActions(testSuite));
            tasks.named("check", it -> it.dependsOn(testTask));
        } else if (strategies.size() == 1) {
            TaskProvider<Test> testTask = createTestTask(testSuite);
            testTask.configure(applyTestActions(testSuite));
            testTask.configure(configureTestingStrategy(testSuite, (GradlePluginTestingStrategyInternal) strategies.iterator().next()));
            tasks.named("check", it -> it.dependsOn(testTask));
        } else {
            for (GradlePluginTestingStrategy strategy : strategies) {
                TaskProvider<Test> testTask = createTestTask(testSuite, ((GradlePluginTestingStrategyInternal)strategy).getName());
                testTask.configure(applyTestActions(testSuite));
                testTask.configure(configureTestingStrategy(testSuite, (GradlePluginTestingStrategyInternal) strategy));
                tasks.named("check", it -> it.dependsOn(testTask));
            }
        }
    }

    private Action<Test> applyTestActions(GradlePluginDevelopmentTestSuiteInternal testSuite) {
        return task -> {
            for (Action<? super Test> action : testSuite.getTestTaskActions()) {
                action.execute(task);
            }
        };
    }

    private Action<Test> configureTestingStrategy(GradlePluginDevelopmentTestSuiteInternal testSuite, GradlePluginTestingStrategyInternal strategy) {
        return task -> {
            if (!(strategy instanceof GradleVersionCoverageTestingStrategy)) {
                throw new RuntimeException("Unknown testing strategy");
            }
            String version = ((GradleVersionCoverageTestingStrategy) strategy).getVersion();
            task.systemProperty("dev.gradleplugins.defaultGradleVersion", version);
            ((Property<GradlePluginTestingStrategy>) task.getExtensions().getByName("testingStrategy")).set(strategy);
        };
    }

    private TaskProvider<Test> createTestTask(GradlePluginDevelopmentTestSuiteInternal testSuite) {
        return createTestTask(testSuite, "");
    }

    private TaskProvider<Test> createTestTask(GradlePluginDevelopmentTestSuiteInternal testSuite, String variant) {
        SourceSet sourceSet = testSuite.getSourceSet();
        String taskName = testSuite.getName() + StringUtils.capitalize(variant);

        TaskProvider<Test> result = null;
        if (tasks.getNames().contains(taskName)) {
            result = tasks.named(taskName, Test.class);
        } else {
            result = tasks.register(taskName, Test.class);
        }

        result.configure(it -> {
            it.setDescription("Runs the " + GUtil.toWords(testSuite.getName()) + "s.");
            it.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);

            it.setTestClassesDirs(objects.fileCollection().from((Callable<Object>) () -> sourceSet.getOutput().getClassesDirs()));
            it.setClasspath(objects.fileCollection().from((Callable<Object>) () -> sourceSet.getRuntimeClasspath()));
        });
        return result;
    }
}
