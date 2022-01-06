package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginTestingStrategy;
import dev.gradleplugins.GradleVersionCoverageTestingStrategy;
import dev.gradleplugins.internal.GradlePluginDevelopmentTestSuiteInternal;
import dev.gradleplugins.internal.GradlePluginTestingStrategyInternal;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.util.GUtil;

import javax.inject.Inject;
import java.util.Set;

abstract class GradlePluginDevelopmentTestingBasePlugin implements Plugin<Project> {
    @Inject
    public GradlePluginDevelopmentTestingBasePlugin() {}

    @Inject
    protected abstract TaskContainer getTasks();

    @Override
    public void apply(Project project) {
        project.getPluginManager().withPlugin("java-gradle-plugin", new RegisterTestingExtensionOnGradleDevelExtensionRule(project));
        project.getComponents().withType(GradlePluginDevelopmentTestSuiteInternal.class).configureEach(testSuite -> {
            testSuite.getTestTasks().configureEach(task -> {
                val testingStrategy = project.getObjects().property(GradlePluginTestingStrategy.class);
                task.getExtensions().add(new TypeOf<Property<GradlePluginTestingStrategy>>() {}, "testingStrategy", testingStrategy);
            });
            project.afterEvaluate(proj -> {
                testSuite.getTestedSourceSet().disallowChanges();
                if (testSuite.getTestedSourceSet().isPresent()) {
                    SourceSet sourceSet = testSuite.getSourceSet();
                    SourceSet testedSourceSet = testSuite.getTestedSourceSet().get();
                    sourceSet.setCompileClasspath(sourceSet.getCompileClasspath().plus(testedSourceSet.getOutput()));
                    sourceSet.setRuntimeClasspath(sourceSet.getRuntimeClasspath().plus(sourceSet.getOutput()).plus(sourceSet.getCompileClasspath()));
                }
            });

            project.afterEvaluate(proj -> {
                testSuite.getTestingStrategies().disallowChanges();
                Set<GradlePluginTestingStrategy> strategies = testSuite.getTestingStrategies().get();
                if (strategies.isEmpty()) {
                    TaskProvider<Test> testTask = createTestTask(testSuite);
                    testTask.configure(applyTestActions(testSuite));
                    getTasks().named("check", it -> it.dependsOn(testTask));
                } else if (strategies.size() == 1) {
                    TaskProvider<Test> testTask = createTestTask(testSuite);
                    testTask.configure(applyTestActions(testSuite));
                    testTask.configure(testingStrategy(testSuite, (GradlePluginTestingStrategyInternal) strategies.iterator().next()));
                    getTasks().named("check", it -> it.dependsOn(testTask));
                } else {
                    for (GradlePluginTestingStrategy strategy : strategies) {
                        TaskProvider<Test> testTask = createTestTask(testSuite, ((GradlePluginTestingStrategyInternal)strategy).getName());
                        testTask.configure(applyTestActions(testSuite));
                        testTask.configure(testingStrategy(testSuite, (GradlePluginTestingStrategyInternal) strategy));
                        getTasks().named("check", it -> it.dependsOn(testTask));
                    }
                }
            });
        });
    }

    private Action<Test> applyTestActions(GradlePluginDevelopmentTestSuiteInternal testSuite) {
        return task -> {
            for (Action<? super Test> action : testSuite.getTestTaskActions()) {
                action.execute(task);
            }
        };
    }

    private Action<Test> testingStrategy(GradlePluginDevelopmentTestSuiteInternal testSuite, GradlePluginTestingStrategyInternal strategy) {
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
        String taskName = sourceSet.getName() + StringUtils.capitalize(variant);

        TaskProvider<Test> result = null;
        if (getTasks().getNames().contains(taskName)) {
            result = getTasks().named(taskName, Test.class);
        } else {
            result = getTasks().register(taskName, Test.class);
        }

        result.configure(it -> {
            it.setDescription("Runs the " + GUtil.toWords(sourceSet.getName()) + "s.");
            it.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);

            it.setTestClassesDirs(sourceSet.getOutput().getClassesDirs());
            it.setClasspath(sourceSet.getRuntimeClasspath());
        });
        return result;
    }
}
