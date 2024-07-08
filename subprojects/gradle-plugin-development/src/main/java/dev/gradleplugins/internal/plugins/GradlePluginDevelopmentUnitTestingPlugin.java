package dev.gradleplugins.internal.plugins;

import dev.gradleplugins.GradlePluginDevelopmentTestSuite;
import dev.gradleplugins.internal.DependencyBucketFactory;
import dev.gradleplugins.internal.DependencyFactory;
import dev.gradleplugins.internal.util.LocalOrRemoteVersionTransformer;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.plugins.AppliedPlugin;

import static dev.gradleplugins.GradlePluginDevelopmentCompatibilityExtension.compatibility;
import static dev.gradleplugins.internal.util.GradlePluginDevelopmentUtils.gradlePlugin;

public abstract class GradlePluginDevelopmentUnitTestingPlugin implements Plugin<Project> {
    private static final String TEST_NAME = "test";
    private static final GradlePluginDevelopmentTestSuiteRegistrationAction TEST_RULE = new GradlePluginDevelopmentTestSuiteRegistrationAction(TEST_NAME);

    public static GradlePluginDevelopmentTestSuite test(Project project) {
        return (GradlePluginDevelopmentTestSuite) project.getExtensions().getByName("test");
    }

    @Override
    public void apply(Project project) {
        // See https://github.com/gradle-plugins/toolbox/issues/65 for more information
        project.getPluginManager().apply("java"); // Should be java-base
        project.getPluginManager().apply("dev.gradleplugins.gradle-plugin-testing-base");
        TEST_RULE.execute(project);

        project.getPluginManager().withPlugin("java-gradle-plugin", ignored -> {
            project.getPluginManager().withPlugin("dev.gradleplugins.gradle-plugin-base", useGradleApiImplementationDependency(project));
        });
    }

    private static Action<AppliedPlugin> useGradleApiImplementationDependency(Project project) {
        return ignored -> {
            // Automatically add Gradle API as a dependency. We assume unit tests are accomplished via ProjectBuilder
            final DependencyFactory factory = new DependencyFactory(project.getDependencies());
            new DependencyBucketFactory(project, test(project).getSourceSet()).create("implementation").add(project.provider(() -> compatibility(gradlePlugin(project))).flatMap(it -> it.getGradleApiVersion().orElse("local").map(localOrRemoteGradleApi(factory))));
        };
    }

    private static Transformer<Dependency, String> localOrRemoteGradleApi(DependencyFactory factory) {
        return new LocalOrRemoteVersionTransformer<>(factory::localGradleApi, factory::gradleApi);
    }
}
