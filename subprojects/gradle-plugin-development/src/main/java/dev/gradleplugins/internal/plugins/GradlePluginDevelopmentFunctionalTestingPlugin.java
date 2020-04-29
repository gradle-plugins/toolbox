package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginTestingStrategy;
import dev.gradleplugins.internal.*;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;
import org.gradle.api.tasks.testing.Test;
import org.gradle.language.base.plugins.LifecycleBasePlugin;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import javax.inject.Inject;
import java.util.Set;

import static dev.gradleplugins.internal.DefaultDependencyVersions.GRADLE_FIXTURES_VERSION;
import static dev.gradleplugins.internal.DefaultDependencyVersions.SPOCK_FRAMEWORK_VERSION;
import static dev.gradleplugins.internal.GradlePluginTestingStrategyInternal.*;
import static dev.gradleplugins.internal.plugins.SpockFrameworkTestSuiteBasePlugin.configureSpockFrameworkProjectDependency;

public abstract class GradlePluginDevelopmentFunctionalTestingPlugin implements Plugin<Project> {
    private static final String FUNCTIONAL_TEST_NAME = "functionalTest";

    @Inject
    protected abstract TaskContainer getTasks();

    @Override
    public void apply(Project project) {
        DeferredRepositoryFactory repositoryFactory = project.getObjects().newInstance(DeferredRepositoryFactory.class, project);

        project.getPluginManager().apply("groovy-base");

        project.getComponents().withType(GradlePluginSpockFrameworkTestSuiteInternal.class).configureEach(testSuite -> {
            project.afterEvaluate(proj -> {
                testSuite.getTestedSourceSet().disallowChanges();
                if (testSuite.getTestedSourceSet().isPresent()) {
                    SourceSet sourceSet = testSuite.getSourceSet();
                    SourceSet testedSourceSet = testSuite.getTestedSourceSet().get();
                    sourceSet.setCompileClasspath(sourceSet.getCompileClasspath().plus(testedSourceSet.getOutput()));
                    sourceSet.setRuntimeClasspath(sourceSet.getRuntimeClasspath().plus(sourceSet.getOutput()).plus(sourceSet.getCompileClasspath()));
                }
            });

            testSuite.getSpockVersion().convention(SPOCK_FRAMEWORK_VERSION).finalizeValueOnRead();
            configureSpockFrameworkProjectDependency(testSuite.getSpockVersion(), testSuite.getSourceSet(), project);
            repositoryFactory.spock();

            SourceSet sourceSet = testSuite.getSourceSet();

            // Configure functionalTest for GradlePluginDevelopmentExtension
            GradlePluginDevelopmentExtension gradlePlugin = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
            gradlePlugin.testSourceSets(sourceSet);

            configureTestKitProjectDependency(testSuite, project);
            configureGradleFixturesProjectDependency(testSuite, project, repositoryFactory);

            project.afterEvaluate(proj -> {
                testSuite.getTestingStrategies().disallowChanges();
                Set<GradlePluginTestingStrategy> strategies = testSuite.getTestingStrategies().get();
                if (strategies.isEmpty()) {
                    TaskProvider<Test> testTask = createTestTask(testSuite);
                    getTasks().named("check", it -> it.dependsOn(testTask));
                } else if (strategies.size() == 1) {
                    TaskProvider<Test> testTask = createTestTask(testSuite);
                    testTask.configure(testingStrategy(testSuite, (GradlePluginTestingStrategyInternal) strategies.iterator().next()));
                    getTasks().named("check", it -> it.dependsOn(testTask));
                } else {
                    for (GradlePluginTestingStrategy strategy : strategies) {
                        TaskProvider<Test> testTask = createTestTask(testSuite, ((GradlePluginTestingStrategyInternal)strategy).getName());
                        testTask.configure(testingStrategy(testSuite, (GradlePluginTestingStrategyInternal) strategy));
                        getTasks().named("check", it -> it.dependsOn(testTask));
                    }
                }
            });
        });

        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        GradlePluginSpockFrameworkTestSuiteInternal functionalTestSuite = project.getObjects().newInstance(GradlePluginSpockFrameworkTestSuiteInternal.class, FUNCTIONAL_TEST_NAME, sourceSets.maybeCreate(FUNCTIONAL_TEST_NAME));
        functionalTestSuite.getTestedSourceSet().convention(project.provider(() -> sourceSets.getByName("main")));
        functionalTestSuite.getTestedGradlePlugin().set((GradlePluginDevelopmentExtensionInternal) ((ExtensionAware)project.getExtensions().getByType(GradlePluginDevelopmentExtension.class)).getExtensions().getByName("extra"));
        functionalTestSuite.getTestedGradlePlugin().disallowChanges();
        project.getComponents().add(functionalTestSuite);
    }

    private Action<Test> testingStrategy(GradlePluginSpockFrameworkTestSuiteInternal testSuite, GradlePluginTestingStrategyInternal strategy) {
        return task -> {
            String version;
            switch (strategy.getName()) {
                case MINIMUM_GRADLE:
                    version = testSuite.getTestedGradlePlugin().get().getMinimumGradleVersion().get();
                    break;
                case LATEST_NIGHTLY:
                    version = new ReleasedVersionDistributions().getMostRecentSnapshot().getVersion();
                    break;
                case LATEST_GLOBAL_AVAILABLE:
                    version = new ReleasedVersionDistributions().getMostRecentRelease().getVersion();
                    break;
                default:
                    throw new RuntimeException("Unknown testing strategy");
            }
            task.systemProperty("dev.gradleplugins.defaultGradleVersion", version);
        };
    }

    private TaskProvider<Test> createTestTask(GradlePluginSpockFrameworkTestSuiteInternal testSuite) {
        return createTestTask(testSuite, "");
    }

    private TaskProvider<Test> createTestTask(GradlePluginSpockFrameworkTestSuiteInternal testSuite, String variant) {
        SourceSet sourceSet = testSuite.getSourceSet();
        return getTasks().register(sourceSet.getName() + StringUtils.capitalize(variant), Test.class, it -> {
            it.setDescription("Runs the functional tests");
            it.setGroup(LifecycleBasePlugin.VERIFICATION_GROUP);

            it.setTestClassesDirs(sourceSet.getOutput().getClassesDirs());
            it.setClasspath(sourceSet.getRuntimeClasspath());
        });
    }

    private static void configureTestKitProjectDependency(GradlePluginSpockFrameworkTestSuiteInternal testSuite, Project project) {
        project.getDependencies().add(testSuite.getSourceSet().getCompileOnlyConfigurationName(), project.getDependencies().gradleTestKit());
    }

    private static void configureGradleFixturesProjectDependency(GradlePluginSpockFrameworkTestSuiteInternal testSuite, Project project, DeferredRepositoryFactory repositoryFactory) {
        ModuleDependency dep = (ModuleDependency)project.getDependencies().add(testSuite.getSourceSet().getImplementationConfigurationName(), "dev.gradleplugins:gradle-fixtures:" + GRADLE_FIXTURES_VERSION);
        dep.capabilities(h -> h.requireCapability("dev.gradleplugins:gradle-fixtures-spock-support"));

        repositoryFactory.gradleFixtures();
    }
}
