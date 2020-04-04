package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.internal.DeferredRepositoryFactory;
import dev.gradleplugins.internal.GroovyGradlePluginSpockTestSuite;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.plugin.devel.GradlePluginDevelopmentExtension;

import static dev.gradleplugins.internal.DefaultDependencyVersions.GRADLE_FIXTURES_VERSION;

public class GradlePluginDevelopmentFunctionalTestingPlugin implements Plugin<Project> {
    private static final String FUNCTIONAL_TEST_NAME = "functionalTest";

    @Override
    public void apply(Project project) {
        DeferredRepositoryFactory repositoryFactory = project.getObjects().newInstance(DeferredRepositoryFactory.class, project);

        project.getPluginManager().apply("groovy-base");
        project.getPluginManager().apply(SpockFrameworkTestSuiteBasePlugin.class);

        project.getComponents().withType(GroovyGradlePluginSpockTestSuite.class, testSuite -> {
            SourceSet sourceSet = testSuite.getSourceSet();

            // Configure functionalTest for GradlePluginDevelopmentExtension
            GradlePluginDevelopmentExtension gradlePlugin = project.getExtensions().getByType(GradlePluginDevelopmentExtension.class);
            gradlePlugin.testSourceSets(sourceSet);

            configureTestKitProjectDependency(testSuite, sourceSet, project);
            configureGradleFixturesProjectDependency(testSuite, sourceSet, project, repositoryFactory);
        });

        SourceSetContainer sourceSets = project.getExtensions().getByType(SourceSetContainer.class);
        GroovyGradlePluginSpockTestSuite functionalTestSuite = project.getObjects().newInstance(GroovyGradlePluginSpockTestSuite.class, FUNCTIONAL_TEST_NAME, sourceSets.maybeCreate(FUNCTIONAL_TEST_NAME));
        functionalTestSuite.getTestedSourceSet().convention(project.provider(() -> sourceSets.getByName("main")));
        project.getComponents().add(functionalTestSuite);
    }

    private static void configureTestKitProjectDependency(GroovyGradlePluginSpockTestSuite testSuite, SourceSet sourceSet, Project project) {
        project.getConfigurations().matching(it -> it.getName().equals(sourceSet.getCompileOnlyConfigurationName())).configureEach( it -> {
            project.getDependencies().add(it.getName(), project.getDependencies().gradleTestKit());
        });
    }

    private static void configureGradleFixturesProjectDependency(GroovyGradlePluginSpockTestSuite testSuite, SourceSet sourceSet, Project project, DeferredRepositoryFactory repositoryFactory) {
        ModuleDependency dep = (ModuleDependency)project.getDependencies().add(sourceSet.getImplementationConfigurationName(), "dev.gradleplugins:gradle-fixtures:" + GRADLE_FIXTURES_VERSION);
        dep.capabilities(h -> h.requireCapability("dev.gradleplugins:gradle-fixtures-spock-support"));

        repositoryFactory.gradleFixtures();
    }
}
