package dev.gradleplugins.internal;

import dev.gradleplugins.CompositeGradlePluginTestingStrategy;
import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.GradlePluginTestingStrategy;
import dev.gradleplugins.GradleVersionCoverageTestingStrategy;
import dev.gradleplugins.internal.rules.RegisterTestSuiteFactoryServiceRule;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.util.GUtil;

import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dev.gradleplugins.internal.RegisterTestingStrategyPropertyExtensionRule.testingStrategyProperty;
import static java.util.Collections.emptyList;

public final class CreateTestTasksFromTestingStrategiesRule implements Action<GradlePluginDevelopmentTestSuite> {
    private final TaskContainer tasks;
    private final ObjectFactory objects;

    public CreateTestTasksFromTestingStrategiesRule(TaskContainer tasks, ObjectFactory objects) {
        this.tasks = tasks;
        this.objects = objects;
    }

    @Override
    public void execute(GradlePluginDevelopmentTestSuite testSuite) {
        testSuite.getTestingStrategies().disallowChanges();
        Set<GradlePluginTestingStrategy> strategies = testSuite.getTestingStrategies().get();
        if (strategies.isEmpty()) {
            TaskProvider<Test> testTask = createTestTask(testSuite);
            testTask.configure(new RegisterTestingStrategyPropertyExtensionRule(objects));
        } else if (strategies.size() == 1) {
            TaskProvider<Test> testTask = createTestTask(testSuite);
            testTask.configure(new RegisterTestingStrategyPropertyExtensionRule(objects));
            testTask.configure(configureTestingStrategy(testSuite, strategies.iterator().next()));
        } else {
            for (GradlePluginTestingStrategy strategy : strategies) {
                TaskProvider<Test> testTask = createTestTask(testSuite, strategy.getName());
                testTask.configure(new RegisterTestingStrategyPropertyExtensionRule(objects));
                testTask.configure(configureTestingStrategy(testSuite, strategy));
            }
        }
    }

    private Action<Test> configureTestingStrategy(GradlePluginDevelopmentTestSuite testSuite, GradlePluginTestingStrategy strategy) {
        return task -> {
            Stream.of(strategy)
                    .flatMap(this::unpackCompositeTestingStrategy)
                    .flatMap(this::onlyCoverageTestingStrategy)
                    .map(GradleVersionCoverageTestingStrategy::getVersion)
                    .findFirst()
                    .ifPresent(setDefaultGradleVersionSystemProperty(task));
            testingStrategyProperty(task).set(strategy);
        };
    }

    private static Consumer<String> setDefaultGradleVersionSystemProperty(Test task) {
        return version -> task.systemProperty("dev.gradleplugins.defaultGradleVersion", version);
    }

    private Stream<GradleVersionCoverageTestingStrategy> onlyCoverageTestingStrategy(GradlePluginTestingStrategy strategy) {
        if (strategy instanceof GradleVersionCoverageTestingStrategy) {
            return Stream.of((GradleVersionCoverageTestingStrategy) strategy);
        } else {
            return Stream.empty();
        }
    }

    private Stream<GradlePluginTestingStrategy> unpackCompositeTestingStrategy(GradlePluginTestingStrategy strategy) {
        if (strategy instanceof CompositeGradlePluginTestingStrategy) {
            return StreamSupport.stream(((CompositeGradlePluginTestingStrategy) strategy).spliterator(), false);
        } else {
            return Stream.of(strategy);
        }
    }

    private TaskProvider<Test> createTestTask(GradlePluginDevelopmentTestSuite testSuite) {
        return createTestTask(testSuite, "");
    }

    private TaskProvider<Test> createTestTask(GradlePluginDevelopmentTestSuite testSuite, String variant) {
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

            it.setTestClassesDirs(objects.fileCollection().from(testSuite.getSourceSet().map(t -> (Object) t.getOutput().getClassesDirs()).orElse(emptyList())));
            it.setClasspath(objects.fileCollection().from(testSuite.getSourceSet().map(t -> (Object) t.getRuntimeClasspath()).orElse(emptyList())));
        });

        // Register test task to TaskView
        ((RegisterTestSuiteFactoryServiceRule.DefaultGradlePluginDevelopmentTestSuiteFactory.GradlePluginDevelopmentTestSuiteInternal) testSuite).getTestTasks().add(result);

        return result;
    }
}
